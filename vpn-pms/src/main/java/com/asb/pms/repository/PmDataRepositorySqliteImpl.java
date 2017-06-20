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

    private OrderedConcurrentHashMap<String,SqliteDataSource> sqliteDBMap = new OrderedConcurrentHashMap();

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
        protected void processObjects(List<PM_DATA> events) {
            Map<String, List<PM_DATA>> collect = events.stream()
                    .collect(Collectors.groupingBy(event -> getPartitionKey(event.getTimePoint())));
            collect.forEach((dayString,list)->{
                SqliteDataSource sqliteDatasource = getDS(dayString,true,true);
                synchronized (dayString) {
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDatasource);
                    insertList(jdbcTemplate, "PM_DATA", list);
                }

            });


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
            logger.error(e.getMessage(), e);
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


            H2DataSource sqliteDatasource = cacheMap.get(dayString);
            if (sqliteDatasource == null && (write || readForceGet)) {
                synchronized (cacheMap) {
                    sqliteDatasource = cacheMap.get(dayString);
                    if (sqliteDatasource == null) {
                        sqliteDatasource = new H2DataSource("jdbc:h2:mem:PM_DATA_" + dayString, false);
                       // JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDatasource);
                        try {
                            //   init(jdbcTemplate);
                            cacheMap.put(dayString, sqliteDatasource);

                            try {
                                initAndLoadDBToCache(sqliteDatasource, dayString,null);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }

                            removeLeastUsedCache();
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }

            return sqliteDatasource;
        }
    }

    public void init(JdbcTemplate jdbcTemplate) throws SQLException {
        JdbcTemplateUtil.createTable(jdbcTemplate,PM_DATA.class,"PM_DATA");
        jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on PM_DATA(statPointId)");
        jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on PM_DATA(timepoint)");

    }

    private void removeLeastUsedCache() {

        logger.info("writeCacheOrder size = {}",writeCacheOrder.size());
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


            JdbcTemplate srcTemplate = new JdbcTemplate(ds);
            List list = JdbcTemplateUtil.queryForList(srcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA");

            if (list.size() > 0) {
                Connection connection = h2DataSource.getConnection();
                try {
                    BJdbcUtil.insertObjects(connection, list, "PM_DATA");
                    connection.commit();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    ds.close();
                    //   connection.close();
                }
                if (consumer != null) {
                    list.stream().forEach(consumer);
                }
            }

            logger.info("!!!! Init PM_DATA Cache : {}, size = {}", key, list.size());
        }
    }

    private SqliteDataSource getDS(String dayString,boolean createIfNotExist,boolean pool) {
        SqliteDataSource sqliteDatasource = sqliteDBMap.get(dayString);
        if (sqliteDatasource == null) {
            synchronized (sqliteDBMap) {
                sqliteDatasource = sqliteDBMap.get(dayString);
                if (sqliteDatasource == null) {
                    if (!createIfNotExist) return null;
                    sqliteDatasource = SqliteDBUtil.getDaySqliteDatasource(dayString, PM_DATA.class, jdbcTemplate -> {
                        jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on PM_DATA(statPointId)");
                        jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on PM_DATA(timePoint)");
                    });
                    if (pool) {
                        sqliteDBMap.put(dayString, sqliteDatasource);
                        if (sqliteDBMap.size() > Configuration.maxSqliteConnections) {
                            try {

                                SqliteDataSource sqliteDataSource = sqliteDBMap.peekFirst();
                                logger.info("close db :"+sqliteDataSource);
                                sqliteDataSource.close();
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }

                    }
                }
            }
        }
        return sqliteDatasource;
    }
    @Override
    public List<PM_DATA> query(Date startTime,Date endTime,List<String> stpKeys) throws Exception {
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
        List result = null;
//        List<Long> stpIds = stpKeys == null ? new ArrayList<>() :
//                stpKeys.stream().map(key-> StpKey.parse(key).getStpId())
//                        .collect(Collectors.toList());

        String inIds = "("+PMQueryUtil.toEntityIdInString(stpKeys)+")";


        if (start.equals(end)) {
            H2DataSource ds = getCacheDS(start,false,true);
            jdbcTemplate = new JdbcTemplate(ds);
            List<PM_DATA> list = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where timePoint <= ? and timePoint >= ? and statPointId in "+inIds, endTime, startTime);
            if (list.isEmpty())
            logger.debug("Query : start {}, end {} ,result = {}",startTime,endTime,list.size());
            result = list;
        } else {

            List<String> partitionKeys = getPartitionKeys(startTime, endTime);
            result = new ArrayList();
            for (int i = 0; i < partitionKeys.size(); i++) {
                String key = partitionKeys.get(i);
                DataSource ds = getCacheDS(key,false,false);

                if (ds == null) {
                    ds = getDS(key,false,false);
                }
                if (ds == null)
                    continue;
                try {
                    jdbcTemplate = new JdbcTemplate(ds);
                    List<PM_DATA> list1 = null;

                    if (i == 0) {
                        list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where  timePoint >= ? and statPointId in "+inIds, startTime );
                    } else if (i == partitionKeys.size() - 1) {
                        list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where timePoint <= ?  and statPointId in "+inIds, endTime );
                    } else {
                        list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where statPointId in "+inIds);
                    }

                    if (extract && list1.size() > 24) {
                        list1 = extract(list1,24);
                    }
                    if (list1 != null)
                        result.addAll(list1);
                } finally {
                    if (ds instanceof SqliteDataSource && !sqliteDBMap.contains(key))
                        ((SqliteDataSource) ds).close();
                }



            }
        }

        long t2 = System.currentTimeMillis() - t1;

        if (t2 > 5)
            logger.debug("spend : "+t2+"ms");
        return result;
    }

    private List<PM_DATA> extract(List<PM_DATA> list1,int sampleNumber) {
        list1.sort((o1,o2)->(int)(o1.getTimePoint().getTime() - o2.getTimePoint().getTime()));
        int rate = list1.size() / sampleNumber;
        int idx = 0;
        List result = new ArrayList();
        for (int i = 0; i < list1.size(); i++) {
            if (idx % rate == 0)
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
        int i1 = 10; int j = 3;
        float b = i1/(float)j;
        List<Integer> list = Arrays.asList(1,2,3,4,5,6);

        list.stream().map(i->i>3).peek(i->System.out.println(i));
        System.out.println();

        list.sort((o1,o2)->o2 - o1);
        System.out.println("list = " + list);

        LinkedList linkedList = new LinkedList();
        linkedList.add("1");
        linkedList.add("2");
        linkedList.add("3");
        linkedList.add("4");

        linkedList.remove("3");
        linkedList.addFirst("3");

        H2DataSource h2DataSource = new H2DataSource("jdbc:h2:mem:abc",false);
        JdbcTemplate jd = new JdbcTemplate(h2DataSource);
        JdbcTemplateUtil.createTable(jd,PM_DATA.class,"PM_DATA");
        for(int i = 0; i < 10; i++) {
            PM_DATA record = new PM_DATA();

            JdbcTemplateUtil.insert(jd, "PM_DATA", record);
        }
        h2DataSource.release();

        h2DataSource = new H2DataSource("jdbc:h2:mem:abc",false);
        jd = new JdbcTemplate(h2DataSource);
        JdbcTemplateUtil.createTable(jd,PM_DATA.class,"PM_DATA");
        Map<String, Object> stringObjectMap = jd.queryForMap("SELECT COUNT(*) FROM PM_DATA");


        ConcurrentHashMap<String,String> cacheMap = new ConcurrentHashMap<>();
        cacheMap.put("11","");
        cacheMap.put("39","");
        cacheMap.put("3","");
        cacheMap.put("12","");
        cacheMap.put("31","");
        cacheMap.put("44","");
        Integer result = cacheMap.reduceKeys(Long.MAX_VALUE, k -> Integer.parseInt(k), (k1, k2) -> k1 < k2 ? k1 : k2);

        System.out.println("result = " + result);
    }
}
