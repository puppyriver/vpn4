package com.asb.pms.repository;

 
import com.asb.pms.Configuration;
import com.asb.pms.PMLastestCache;
import com.asb.pms.util.*;
import com.asb.pms.Constants;
import com.asb.pms.SqliteDBUtil;
import com.asb.pms.api.pm.PMQueryUtil;
import com.asb.pms.model.PM_DATA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:46
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PmDataRepositorySqliteImpl implements PmDataRepository {
    private Logger logger = LoggerFactory.getLogger(PmDataRepositorySqliteImpl.class);

//    private OrderedConcurrentHashMap<String,SqliteDataSource> sqliteDBMap = new OrderedConcurrentHashMap();

    public PmDataRepositorySqliteImpl() {
        logger.info("Configuration.writeCacheSize = "+Configuration.writeCacheSize);
        logger.info("Configuration.readCacheSize = "+Configuration.readCacheSize);
        logger.info("Configuration.maxQueryRangeInHours = "+Configuration.maxQueryRangeInHours);
        logger.info("Configuration.maxQueryNoExtractInHours = "+Configuration.maxQueryNoExtractInHours);
        logger.info("Configuration.maxSqliteConnections = "+Configuration.maxSqliteConnections);
        try {
            loadCache();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        template.start();
    }


    private String getPartitionKey(Date date) {
       return  new SimpleDateFormat("yyyyMMddHH").format(date);
        //return DateUtils.getDayString(date);
    }

    private List<String> getPartitionKeys(Date start,Date end) {
        List<String> keys = new ArrayList<>();
        for (long time = start.getTime();  time < end.getTime(); time+= 3600l * 1000l) {
            String key = getPartitionKey(new Date(time));
            if (!keys.contains(key))
                keys.add(key);
        }
        String key = getPartitionKey(end);
        if (!keys.contains(key))
            keys.add(key);

        return keys;

    }

    BatchConsumerTemplate<PM_DATA> template = new BatchConsumerTemplate<PM_DATA>(1000) {
        @Override
        protected void processObjects(List<PM_DATA> events, Queue<PM_DATA> queue) {
            if (!Thread.currentThread().getName().contains("BatchConsumerTemplate2"))
                Thread.currentThread().setName("BatchConsumerTemplate2");
            long t1 = System.currentTimeMillis();
            Map<String, List<PM_DATA>> collect = events.stream()
                    .collect(Collectors.groupingBy(event -> getPartitionKey(event.getTimePoint())));
            collect.forEach((dayString,list)-> {
                synchronized (dayString) {
                    SqliteDataSource sqliteDatasource = getDS(dayString, true, true);
                    try {
                        JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDatasource);
                        insertList(jdbcTemplate, "PM_DATA", list);

                    } catch(Exception e){
                        logger.error(e.getMessage(), e);
                    } finally{
                        sqliteDatasource.close();
                    }
                }

            });
            long t = System.currentTimeMillis() - t1;
            if (t > 5000)
                logger.info("processObject size = "+events.size()+" spend time : "+t+"ms, "+queue.size()+" left");

        }
    };

    public  void insertList(JdbcTemplate jdbcTemplate, String tableName, List list) {
        Connection connection = null;

        try {
            connection = jdbcTemplate.getDataSource().getConnection();
            BJdbcUtil.insertObjects(connection,list,"PM_DATA");
            connection.commit();
      //      logger.debug("commit : {} objects",list.size());
        } catch ( Exception e) {
            logger.error(e.getMessage()+" :: datasource:"+jdbcTemplate.getDataSource(), e);
            try {
                connection.rollback();
            } catch (SQLException e1) {
                logger.error(e1.getMessage(), e1);
            }
        }  

    }
    
    @Override
    public void insert(PM_DATA event) {
        template.offerOne(event);

        String dayString = getPartitionKey(event.getTimePoint());
        JdbcTemplate cacheTemplate = new JdbcTemplate(getCacheDS(dayString,true,true));
        JdbcTemplateUtil.insert(cacheTemplate,"PM_DATA",event);
        addToLatestCache(event);

    }



    private void moveToFirst(LinkedList<String> order ,String cacheName) {
        if (order.isEmpty() || !order.getFirst().equals(cacheName)) {
            order.remove(cacheName);
            order.addFirst(cacheName);
        }
    }

    private ConcurrentHashMap<String,H2DataSource> cacheMap = new ConcurrentHashMap();
    private LinkedList<String> writeCacheOrder = new LinkedList<>();
    private LinkedList<String> readCacheOrder = new LinkedList<>();
    public synchronized H2DataSource getCacheDS(String dayString,boolean write,boolean readForceGet) {
        synchronized (cacheMap) {
            if (write) {
                if (readCacheOrder.contains(dayString)) {
                    readCacheOrder.remove(dayString);
                }
                moveToFirst(writeCacheOrder,dayString);
            } else {

                if (writeCacheOrder.contains(dayString)) {
                    moveToFirst(writeCacheOrder,dayString);
                } else{
                    moveToFirst(readCacheOrder,dayString);
                }


            }


            H2DataSource h2Datasource = cacheMap.get(dayString);
            if (h2Datasource == null) {
                if (write || readForceGet) {
                    synchronized (cacheMap) {
                        h2Datasource = cacheMap.get(dayString);
                        if (h2Datasource == null) {
                            h2Datasource = new H2DataSource("jdbc:h2:mem:PM_DATA_" + dayString, false);
                            // JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDatasource);
                            try {
                                //   init(jdbcTemplate);
                                logger.info("Add cache : {}, write : {}", dayString, write);
                                cacheMap.put(dayString, h2Datasource);
                                logger.info("CacheMap="+cacheMap);

                                try {
                                    initAndLoadDBToCache(h2Datasource, dayString, null);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }

                                removeLeastUsedCache();
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                } else {
                     readCacheOrder.remove(dayString);
                }
            }

            return h2Datasource;
        }
    }

    public void init(JdbcTemplate jdbcTemplate) throws SQLException {
        JdbcTemplateUtil.createTable(jdbcTemplate,PM_DATA.class,"PM_DATA");
        jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on PM_DATA(statPointId)");
        jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on PM_DATA(timepoint)");

    }

    private void removeLeastUsedCache() {

        logger.info("writeCacheOrder size = {}",writeCacheOrder.stream().reduce((a1,a2)->a1+","+a2));
        try {
            if (writeCacheOrder.size() > Configuration.writeCacheSize) {
                synchronized (cacheMap) {
                    String last = writeCacheOrder.getLast();
                    //    Integer key = cacheMap.reduceKeys(Long.MAX_VALUE, k -> Integer.parseInt(k), (k1, k2) -> k1 < k2 ? k1 : k2);
                    H2DataSource h2DataSource = cacheMap.get(last);
                    logger.info("!!!!! Release write cache : {}",last);
                    h2DataSource.release();
                    cacheMap.remove(last);
                    writeCacheOrder.remove(last);
                }
            }


        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        logger.info("readCacheOrder size = {}",readCacheOrder.stream().reduce((a1,a2)->a1+","+a2));
        try {
            if (readCacheOrder.size() > Configuration.readCacheSize) {
                synchronized (cacheMap) {
                    String last = readCacheOrder.getLast();
                    //    Integer key = cacheMap.reduceKeys(Long.MAX_VALUE, k -> Integer.parseInt(k), (k1, k2) -> k1 < k2 ? k1 : k2);
                    H2DataSource h2DataSource = cacheMap.get(last);
                    logger.info("!!!!! Release read cache : {}",last);
                    h2DataSource.release();
                    cacheMap.remove(last);
                    readCacheOrder.remove(last);
                }
            }


        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    public void loadCache() throws Exception {
        List<File> files = SqliteDBUtil.listDBFiles();
        if (files.isEmpty()) return;

        List<File> list = files.subList(Configuration.writeCacheSize > files.size() ? 0 : files.size()- Configuration.writeCacheSize, files.size());
        for (File file : list) {
            String key = file.getName().substring(0, file.getName().lastIndexOf("."));

            H2DataSource h2DataSource = new H2DataSource("jdbc:h2:mem:PM_DATA_"+ key,false);
            cacheMap.put(key,h2DataSource);
            try {
                initAndLoadDBToCache(h2DataSource, key, pm_data -> addToLatestCache(pm_data));
                logger.info("load {}, totalMemroy {}",key,Runtime.getRuntime().totalMemory() / (1024l * 1024l)+"MB");
                logger.info("load {}, usedMemroy {}",key,(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024l * 1024l)+"MB");
            } catch (Exception e) {
                logger.error("Load DB File : "+file.getAbsolutePath()+" ERROR !",e);
            }
        }
        //File file = files.get(files.size() - 1);
    }

    private void initAndLoadDBToCache(H2DataSource h2DataSource, String key, Consumer<PM_DATA> consumer) throws Exception {
        synchronized (key) {
            JdbcTemplate destTemplate = new JdbcTemplate(h2DataSource);
            logger.info("Init cache table : {}", key);
            init(destTemplate);
            SqliteDataSource ds = SqliteDBUtil.getDaySqliteDatasource(key,PM_DATA.class,false,null);

//            File file = new File(SqliteDBUtil.getFolder(), key + ".db");
//            if (!file.exists()) return;
//            SqliteDataSource ds = new SqliteDataSource(file.getAbsolutePath());
            if (ds == null) return;

            try {

                JdbcTemplate srcTemplate = new JdbcTemplate(ds);
                List list = JdbcTemplateUtil.queryForList(srcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA");

                if (list.size() > 0) {
                    Connection connection = h2DataSource.getConnection();
                    try {
                        BJdbcUtil.insertObjects(connection, list, "PM_DATA");
                        connection.commit();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    if (consumer != null) {
                        list.stream().forEach(consumer);
                    }

                    logger.info("!!!! Init PM_DATA Cache : {}, size = {}", key, list.size());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }finally {
                ds.close();
                //   connection.close();
            }


        }
    }

    private SqliteDataSource getDS(String dayString,boolean createIfNotExist,boolean pool) {
        return SqliteDBUtil.getDaySqliteDatasource(dayString,PM_DATA.class,createIfNotExist,jdbcTemplate -> {
                            jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on PM_DATA(statPointId)");
                            jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on PM_DATA(timePoint)");
                        });
    }
//    private SqliteDataSource getDS(String dayString,boolean createIfNotExist,boolean pool) {
//        SqliteDataSource sqliteDatasource = sqliteDBMap.get(dayString);
//        if (sqliteDatasource == null) {
//            synchronized (sqliteDBMap) {
//                sqliteDatasource = sqliteDBMap.get(dayString);
//                if (sqliteDatasource == null) {
//                    if (!createIfNotExist) {
//                        sqliteDatasource = SqliteDBUtil.getDaySqliteDatasource(dayString,PM_DATA.class,false,null);
//                    }
//                    else {
//                        sqliteDatasource = SqliteDBUtil.getDaySqliteDatasource(dayString, PM_DATA.class, jdbcTemplate -> {
//                            jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on PM_DATA(statPointId)");
//                            jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on PM_DATA(timePoint)");
//                        });
//                    }
//                    if (pool) {
//                        sqliteDBMap.put(dayString, sqliteDatasource);
//                        logger.info("put "+dayString+" , sqliteDBMap = "+sqliteDBMap.toString());
//                        if (sqliteDBMap.size() > Configuration.maxSqliteConnections) {
//                            try {
//
//                                SqliteDataSource _sqliteDataSource = sqliteDBMap.peekFirst();
//                                logger.info("close db :"+_sqliteDataSource);
//                                _sqliteDataSource.close();
//                            } catch (Exception e) {
//                                logger.error(e.getMessage(), e);
//                            }
//                        }
//
//                    }
//                }
//            }
//        }
//        return sqliteDatasource;
//    }
    @Override
    public List<PM_DATA> query(Date startTime,Date endTime,List<String> stpKeys, HashMap queryAttributes) throws Exception {
        logger.info("query :: startTime = {}, endTime = {} ,keys = {}",startTime,endTime,stpKeys == null ? null : stpKeys.stream().reduce((a1,a2)->a1+","+a2));

        if (endTime.before(startTime)) {
            throw new Exception("start time can not before end time");
        }
        if (Configuration.maxQueryRangeInHours > 0 && (endTime.getTime() - startTime.getTime() > 3600l * 1000l * Configuration.maxQueryRangeInHours)) {
            throw new Exception("query time out of range !");
        }

        boolean extract = false;
        if (Configuration.maxQueryNoExtractInHours > 0 && (endTime.getTime() - startTime.getTime() > 3600l * 1000l * Configuration.maxQueryNoExtractInHours)) {
            extract = true;
        }


        long t1 = System.currentTimeMillis();
        String start = getPartitionKey(startTime);
        String end = getPartitionKey(endTime);
        JdbcTemplate jdbcTemplate = null;
        List<PM_DATA> result = null;
//        List<Long> stpIds = stpKeys == null ? new ArrayList<>() :
//                stpKeys.stream().map(key-> StpKey.parse(key).getStpId())
//                        .collect(Collectors.toList());

        String inIds = "("+PMQueryUtil.toEntityIdInString(stpKeys)+")";


        if (start.equals(end)) {
            H2DataSource ds = getCacheDS(start,false,true);
            jdbcTemplate = new JdbcTemplate(ds);
            List<PM_DATA> list = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where timePoint <= ? and timePoint >= ? and statPointId in "+inIds, endTime, startTime);
           if (!list.isEmpty())
                logger.info("1::Query in {}: start {}, end {} ,result = {}",ds,startTime,endTime,list.size());
            result = list;
        } else {

            List<String> partitionKeys = getPartitionKeys(startTime, endTime);
            result = new ArrayList();
            for (int i = 0; i < partitionKeys.size(); i++) {
                String key = partitionKeys.get(i);
                boolean useCache = true;
                DataSource ds = getCacheDS(key,false,false);


                if (ds == null) {
                    ds = getDS(key,false,false);
                }
                if (ds == null)
                    continue;
                try {
                    jdbcTemplate = new JdbcTemplate(ds);
                    List<PM_DATA> list1 = null;

                    try {
                        if (i == 0) {
                            list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where  timePoint >= ? and statPointId in " + inIds, startTime);
                        } else if (i == partitionKeys.size() - 1) {
                            list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where timePoint <= ?  and statPointId in " + inIds, endTime);
                        } else {
                            list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where statPointId in " + inIds);
                        }
                    } catch (Throwable e) {
                        logger.error(e.getMessage(),e);
                    }
                    logger.info("2::Query in {}: start {}, end {} ,result = {}",ds,startTime,endTime,list1 == null ? null : list1.size());

                    if (list1 != null && extract && list1.size() > 24) {
                        list1 = extract(list1,24);
                    }
                    if (list1 != null)
                        result.addAll(list1);
                } finally {
                    if (ds instanceof SqliteDataSource
                            //&& !sqliteDBMap.contains(key)
                            )
                        ((SqliteDataSource) ds).close();
                }



            }
        }

        long t2 = System.currentTimeMillis() - t1;

        if (t2 > 5)
            logger.info("query spend : "+t2+"ms");

        if (queryAttributes != null && queryAttributes.containsKey("query.granularityInMin")) {
            int granularityInMin = Integer.parseInt(queryAttributes.get("query.granularityInMin").toString());
            Map<Long, List<PM_DATA>> collect = result.stream().collect(Collectors.groupingBy(p -> p.getStatPointId()));
            result.clear();
            for (List<PM_DATA> datas : collect.values()) {
                result.addAll(extractByMinutes(datas,granularityInMin));
            }
            //result = extractByMinutes(result,granularityInMin);
        } else if (result != null && result.size() > 48) {
           // result = extract(result,48);
            Map<Long, List<PM_DATA>> collect = result.stream().collect(Collectors.groupingBy(p -> p.getStatPointId()));
            result.clear();
            for (List<PM_DATA> datas : collect.values()) {
                result.addAll(extract(datas,48));
            }
        }
        return result;
    }

    private List<PM_DATA> extractByMinutes(List<PM_DATA> list,int granularityInMin) {
        list.sort((o1,o2)->(int)(o1.getTimePoint().getTime() - o2.getTimePoint().getTime()));

        List result = new ArrayList();
        long t = -1;
        List<PM_DATA> bulk = new ArrayList<>();
        for (PM_DATA pm_data : list) {
            bulk.add(pm_data);
            if (t < 0 || pm_data.getTimePoint().getTime() - t >= granularityInMin * 60 * 1000l - 10000l) {
                t = pm_data.getTimePoint().getTime();
                result.add(pm_data);


                if (bulk.size() > 0) {
                    Comparator<PM_DATA> cp = (p1, p2) -> (p1.getValue() - p2.getValue() > 0 ? 1 : 0);
                    Optional<PM_DATA> max = bulk.stream().max(cp);
                    Optional<PM_DATA> min = bulk.stream().min(cp);
                    Optional<Float> total = bulk.stream().map(pm_data1 -> pm_data1.getValue()).reduce((v1, v2) -> v1 + v2);
                    float v = total.get() / bulk.size();

                    pm_data.getDataMap().put("query_max",max.isPresent() ? max.get().getValue() : null);
                    pm_data.getDataMap().put("query_min",min.isPresent() ? min.get().getValue() : null);
                    pm_data.getDataMap().put("query_averageValue",v);

                }
                bulk.clear();

            } else {
                bulk.add(pm_data);
            }
        }
        return result;
    }



    private List<PM_DATA> extract(List<PM_DATA> list1,int sampleNumber) {
        list1.sort((o1,o2)->(int)(o1.getTimePoint().getTime() - o2.getTimePoint().getTime()));
        int rate = list1.size() / sampleNumber;
   //     int idx = 0;
        List result = new ArrayList();
        for (int i = 0; i < list1.size(); i++) {
            if (i % rate == 0)
                result.add(list1.get(i));
        }
        return result;
        //list1.stream().peek()
    }


    private PMLastestCache lastestCache = new PMLastestCache();

    @Override
    public PM_DATA getLatest(long stpId) {
        PM_DATA data = lastestCache.get(stpId);
        if (data != null && data.getDataMap() != null)
            data.getDataMap().clear();
        return data;
    }

    //@Override
    private boolean addToLatestCache(PM_DATA pm_data) {
        //   jdbcTemplate.execute("UPDATE PM_DATA SET STATUS="+Constants.PM_DATA_STATUS_CACHE+" WHERE STATPOINTID = "+pm_data.getStatPointId());
        pm_data.setStatus(Constants.PM_DATA_STATUS_CURRENT);
        //    JdbcTemplateUtil.insert(jdbcTemplate,"PM_DATA",pm_data);

        lastestCache.add(pm_data);
        return true;
    }


    public static void main(String[] args) throws SQLException {

        List list = new ArrayList();

        for (int j = 0 ; j < 1000; j++) {
            System.out.println(j);
            H2DataSource h2DataSource = new H2DataSource("jdbc:h2:mem:abc_"+j,false);
            list.add(h2DataSource);
            JdbcTemplate jd = new JdbcTemplate(h2DataSource);
            JdbcTemplateUtil.createTable(jd,PM_DATA.class,"PM_DATA");
            for(int i = 0; i < 10000; i++) {
                PM_DATA record = new PM_DATA();

                JdbcTemplateUtil.insert(jd, "PM_DATA", record);
            }
            h2DataSource.release();
            list.remove(h2DataSource);
        }



    }
}
