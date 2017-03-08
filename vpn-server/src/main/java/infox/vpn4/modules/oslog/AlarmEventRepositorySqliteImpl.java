package infox.vpn4.modules.oslog;

import infox.vpn4.modules.oslog.sqlite.SqliteDBUtil;
import infox.vpn4.util.*;
import infox.vpn4.valueobject.FAlarmRecord;
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
public class AlarmEventRepositorySqliteImpl implements AlarmEventRepository {
    private Logger logger = LoggerFactory.getLogger(AlarmEventRepositorySqliteImpl.class);

    private ConcurrentHashMap<String,SqliteDataSource> sqliteDBMap = new ConcurrentHashMap();

    public AlarmEventRepositorySqliteImpl() {
        try {
            loadCache();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        template.start();
    }

    BatchConsumerTemplate<FAlarmRecord> template = new BatchConsumerTemplate<FAlarmRecord>(1000) {
        @Override
        protected void processObjects(List<FAlarmRecord> events) {
            Map<String, List<FAlarmRecord>> collect = events.stream()
                    .collect(Collectors.groupingBy(event -> DateUtil.getDayString(event.getAlarmTime())));
            collect.forEach((dayString,list)->{
                SqliteDataSource sqliteDatasource = getDS(dayString);
                synchronized (dayString) {
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDatasource);
                    insertList(jdbcTemplate, "FAlarmRecord", list);
                }

            });


        }
    };

    public  void insertList(JdbcTemplate jdbcTemplate, String tableName, List list) {
        Connection connection = null;

        try {
            connection = jdbcTemplate.getDataSource().getConnection();
            BJdbcUtil.insertObjects(connection,list,"FAlarmRecord");
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
    public void insert(FAlarmRecord event) {
        template.offerOne(event);

        String dayString = DateUtil.getDayString(event.getAlarmTime());
        JdbcTemplate cacheTemplate = new JdbcTemplate(getCacheDS(dayString));
        JdbcTemplateUtil.insert(cacheTemplate,"FAlarmRecord",event);

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
                        sqliteDatasource = new H2DataSource("jdbc:h2:mem:FAlarmRecord_" + dayString, false);
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
        JdbcTemplateUtil.createTable(jdbcTemplate,FAlarmRecord.class,"FAlarmRecord");
        jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on FAlarmRecord(alarmItemDn)");
        jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on FAlarmRecord(alarmTime)");

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

        H2DataSource h2DataSource = new H2DataSource("jdbc:h2:mem:FAlarmRecord_"+ key,false);
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
            List list = JdbcTemplateUtil.queryForList(srcTemplate, FAlarmRecord.class, "SELECT * FROM FAlarmRecord");

            if (list.size() > 0) {
                Connection connection = h2DataSource.getConnection();
                try {
                    BJdbcUtil.insertObjects(connection, list, "FAlarmRecord");
                    connection.commit();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    ds.close();
                    //   connection.close();
                }
            }

            logger.info("!!!! Init FAlarmRecord Cache : {}, size = {}", key, list.size());
        }
    }

    private SqliteDataSource getDS(String dayString) {
        SqliteDataSource sqliteDatasource = sqliteDBMap.get(dayString);
        if (sqliteDatasource == null) {
            synchronized (sqliteDBMap) {
                sqliteDatasource = sqliteDBMap.get(dayString);
                if (sqliteDatasource == null) {
                    sqliteDatasource = SqliteDBUtil.getDaySqliteDatasource(dayString, FAlarmRecord.class, jdbcTemplate -> {
                        jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on FAlarmRecord(alarmItemDn)");
                        jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on FAlarmRecord(alarmTime)");
                    });
                    sqliteDBMap.put(dayString,sqliteDatasource);
                }
            }
        }
        return sqliteDatasource;
    }
    @Override
    public List<FAlarmRecord> query(Date startTime,Date endTime) throws Exception {
        long t1 = System.currentTimeMillis();
        String start = DateUtil.getDayString(startTime);
        String end = DateUtil.getDayString(endTime);
        JdbcTemplate jdbcTemplate = null;
        List result = null;
        if (start.equals(end)) {
            H2DataSource ds = getCacheDS(start);
            jdbcTemplate = new JdbcTemplate(ds);
            List<FAlarmRecord> list = JdbcTemplateUtil.queryForList(jdbcTemplate, FAlarmRecord.class, "SELECT * FROM FAlarmRecord where alarmTime <= ? and alarmTime >= ?", endTime, startTime);
            if (list.isEmpty())
            logger.debug("Query : start {}, end {} ,result = {}",startTime,endTime,list.size());
            result = list;
        } else {
            H2DataSource ds = getCacheDS(start);
            jdbcTemplate = new JdbcTemplate(ds);
            List<FAlarmRecord> list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, FAlarmRecord.class, "SELECT * FROM FAlarmRecord where  alarmTime >= ?", startTime);

            ds = getCacheDS(end);
            jdbcTemplate = new JdbcTemplate(ds);
            List<FAlarmRecord> list2 = JdbcTemplateUtil.queryForList(jdbcTemplate, FAlarmRecord.class, "SELECT * FROM FAlarmRecord where alarmTime <= ?  ", endTime);
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
    public List<FAlarmRecord> query2(Date startTime,Date endTime) throws Exception {
        String start = DateUtil.getDayString(startTime);
        String end = DateUtil.getDayString(endTime);
        JdbcTemplate jdbcTemplate = null;
        if (start.equals(end)) {
            SqliteDataSource ds = getDS(start);
            jdbcTemplate = new JdbcTemplate(ds);
            List<FAlarmRecord> list = JdbcTemplateUtil.queryForList(jdbcTemplate, FAlarmRecord.class, "SELECT * FROM FAlarmRecord where alarmTime <= ? and alarmTime >= ?", endTime, startTime);
    //        logger.debug("Query : start {}, end {} ,result = {}",startTime,endTime,list.size());
            return list;
        } else {
            SqliteDataSource ds = getDS(start);
            jdbcTemplate = new JdbcTemplate(ds);
            List<FAlarmRecord> list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, FAlarmRecord.class, "SELECT * FROM FAlarmRecord where  alarmTime >= ?", startTime);

            ds = getDS(end);
            jdbcTemplate = new JdbcTemplate(ds);
            List<FAlarmRecord> list2 = JdbcTemplateUtil.queryForList(jdbcTemplate, FAlarmRecord.class, "SELECT * FROM FAlarmRecord where alarmTime <= ?  ", endTime);
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
        JdbcTemplateUtil.createTable(jd,FAlarmRecord.class,"FAlarmRecord");
        for(int i = 0; i < 10; i++) {
            FAlarmRecord record = new FAlarmRecord();

            JdbcTemplateUtil.insert(jd, "FAlarmRecord", record);
        }
        h2DataSource.release();

        h2DataSource = new H2DataSource("jdbc:h2:mem:abc",false);
        jd = new JdbcTemplate(h2DataSource);
        JdbcTemplateUtil.createTable(jd,FAlarmRecord.class,"FAlarmRecord");
        Map<String, Object> stringObjectMap = jd.queryForMap("SELECT COUNT(*) FROM FAlarmRecord");


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
