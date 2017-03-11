package com.infox.vpn4.pms2.repository;

 
import com.infox.vpn4.pms2.SqliteDBUtil;
import com.infox.vpn4.pms2.api.StpKey;
import com.infox.vpn4.pms2.model.PM_DATA;
import com.infox.vpn4.pms2.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:46
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PmDataRepositorySqliteImpl implements PmDataRepository {
    private Logger logger = LoggerFactory.getLogger(PmDataRepositorySqliteImpl.class);

    private ConcurrentHashMap<String,SqliteDataSource> sqliteDBMap = new ConcurrentHashMap();

    public PmDataRepositorySqliteImpl() {
        try {
            loadCache();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        template.start();
    }

    BatchConsumerTemplate<PM_DATA> template = new BatchConsumerTemplate<PM_DATA>(1000) {
        @Override
        protected void processObjects(List<PM_DATA> events) {
            Map<String, List<PM_DATA>> collect = events.stream()
                    .collect(Collectors.groupingBy(event -> DateUtils.getDayString(event.getTimePoint())));
            collect.forEach((dayString,list)->{
                SqliteDataSource sqliteDatasource = getDS(dayString);
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

        String dayString = DateUtils.getDayString(event.getTimePoint());
        JdbcTemplate cacheTemplate = new JdbcTemplate(getCacheDS(dayString));
        JdbcTemplateUtil.insert(cacheTemplate,"PM_DATA",event);

    }




    private ConcurrentHashMap<String,H2DataSource> cacheMap = new ConcurrentHashMap();
    private LinkedList<String> cacheOrder = new LinkedList<>();
    public H2DataSource getCacheDS(String dayString) {
        synchronized (cacheMap) {
            if (cacheOrder.isEmpty() || !cacheOrder.getFirst().equals(dayString)) {
                cacheOrder.remove(dayString);
                cacheOrder.addFirst(dayString);
            }

            H2DataSource sqliteDatasource = cacheMap.get(dayString);
            if (sqliteDatasource == null) {
                synchronized (cacheMap) {
                    sqliteDatasource = cacheMap.get(dayString);
                    if (sqliteDatasource == null) {
                        sqliteDatasource = new H2DataSource("jdbc:h2:mem:PM_DATA_" + dayString, false);
                        JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDatasource);
                        try {
                            //   init(jdbcTemplate);
                            cacheMap.put(dayString, sqliteDatasource);

                            try {
                                initAndLoadDBToCache(sqliteDatasource, dayString);
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
        jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on PM_DATA(alarmItemDn)");
        jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on PM_DATA(alarmTime)");

    }

    private void removeLeastUsedCache() {
        try {
            if (cacheMap.size() > 5) {
                synchronized (cacheMap) {
                    String last = cacheOrder.getLast();
                    //    Integer key = cacheMap.reduceKeys(Long.MAX_VALUE, k -> Integer.parseInt(k), (k1, k2) -> k1 < k2 ? k1 : k2);
                    H2DataSource h2DataSource = cacheMap.get(last);
                    h2DataSource.release();
                    logger.info("!!!!! Release cache : {}",last);
                    cacheMap.remove(last);
                    cacheOrder.remove(last);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    public void loadCache() throws Exception {
        List<File> files = SqliteDBUtil.listDBFiles();
        if (files.isEmpty()) return;
        File file = files.get(files.size() - 1);
        String key = file.getName().substring(0, file.getName().lastIndexOf("."));

        H2DataSource h2DataSource = new H2DataSource("jdbc:h2:mem:PM_DATA_"+ key,false);
        cacheMap.put(key,h2DataSource);

        initAndLoadDBToCache(h2DataSource,key);
    }

    private void initAndLoadDBToCache(H2DataSource h2DataSource, String key) throws Exception {
        synchronized (key) {
            JdbcTemplate destTemplate = new JdbcTemplate(h2DataSource);
            logger.info("Init cache table : {}", key);
            init(destTemplate);

            File file = new File(SqliteDBUtil.getFolder(), key + ".db");
            if (!file.exists()) return;
            SqliteDataSource ds = new SqliteDataSource(file.getAbsolutePath());


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
            }

            logger.info("!!!! Init PM_DATA Cache : {}, size = {}", key, list.size());
        }
    }

    private SqliteDataSource getDS(String dayString) {
        SqliteDataSource sqliteDatasource = sqliteDBMap.get(dayString);
        if (sqliteDatasource == null) {
            synchronized (sqliteDBMap) {
                sqliteDatasource = sqliteDBMap.get(dayString);
                if (sqliteDatasource == null) {
                    sqliteDatasource = SqliteDBUtil.getDaySqliteDatasource(dayString, PM_DATA.class, jdbcTemplate -> {
                        jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on PM_DATA(alarmItemDn)");
                        jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on PM_DATA(alarmTime)");
                    });
                    sqliteDBMap.put(dayString,sqliteDatasource);
                }
            }
        }
        return sqliteDatasource;
    }
    @Override
    public List<PM_DATA> query(Date startTime,Date endTime,List<String> stpKeys) throws Exception {
        long t1 = System.currentTimeMillis();
        String start = DateUtils.getDayString(startTime);
        String end = DateUtils.getDayString(endTime);
        JdbcTemplate jdbcTemplate = null;
        List result = null;
        List<Long> stpIds = stpKeys == null ? new ArrayList<>() :
                stpKeys.stream().map(key-> StpKey.parse(key).getStpId())
                        .collect(Collectors.toList());
        if (start.equals(end)) {
            H2DataSource ds = getCacheDS(start);
            jdbcTemplate = new JdbcTemplate(ds);
            List<PM_DATA> list = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where alarmTime <= ? and alarmTime >= ? and statPointId in ?", endTime, startTime,stpIds);
            if (list.isEmpty())
            logger.debug("Query : start {}, end {} ,result = {}",startTime,endTime,list.size());
            result = list;
        } else {
            H2DataSource ds = getCacheDS(start);
            jdbcTemplate = new JdbcTemplate(ds);
            List<PM_DATA> list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where  alarmTime >= ? and statPointId in ?" , startTime,stpIds);

            ds = getCacheDS(end);
            jdbcTemplate = new JdbcTemplate(ds);
            List<PM_DATA> list2 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where alarmTime <= ?  and statPointId in ?", endTime,stpIds);
            list1.addAll(list2);
            if (list1.isEmpty())
            logger.debug("Query : start {}, end {} ,result = {}",startTime,endTime,list1.size());
            result = list1;
        }

        long t2 = System.currentTimeMillis() - t1;

        if (t2 > 5)
            logger.debug("spend : "+t2+"ms");
        if (result == null || result.isEmpty())
            return query2(startTime,endTime);
        return result;
    }

    //@Override
    public List<PM_DATA> query2(Date startTime,Date endTime) throws Exception {
        String start = DateUtils.getDayString(startTime);
        String end = DateUtils.getDayString(endTime);
        JdbcTemplate jdbcTemplate = null;
        if (start.equals(end)) {
            SqliteDataSource ds = getDS(start);
            jdbcTemplate = new JdbcTemplate(ds);
            List<PM_DATA> list = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where alarmTime <= ? and alarmTime >= ?", endTime, startTime);
    //        logger.debug("Query : start {}, end {} ,result = {}",startTime,endTime,list.size());
            return list;
        } else {
            SqliteDataSource ds = getDS(start);
            jdbcTemplate = new JdbcTemplate(ds);
            List<PM_DATA> list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where  alarmTime >= ?", startTime);

            ds = getDS(end);
            jdbcTemplate = new JdbcTemplate(ds);
            List<PM_DATA> list2 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where alarmTime <= ?  ", endTime);
            list1.addAll(list2);
  //          logger.debug("Query : start {}, end {} ,result = {}",startTime,endTime,list1.size());
            return list1;
        }

    }

    public static void main(String[] args) throws SQLException {
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
