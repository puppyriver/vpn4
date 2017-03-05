package infox.vpn4.modules.oslog;

import infox.vpn4.modules.oslog.sqlite.SqliteDBUtil;
import infox.vpn4.util.*;
import infox.vpn4.valueobject.FAlarmRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    }

    @Override
    public void insert(FAlarmRecord event) {
        String dayString = DateUtil.getDayString(event.getAlarmTime());
        SqliteDataSource sqliteDatasource = getDS(dayString);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDatasource);
        JdbcTemplateUtil.insert(jdbcTemplate,"FAlarmRecord",event);


        JdbcTemplate cacheTemplate = new JdbcTemplate(getCacheDS(dayString));
        JdbcTemplateUtil.insert(cacheTemplate,"FAlarmRecord",event);

    }

    private ConcurrentHashMap<String,H2DataSource> cacheMap = new ConcurrentHashMap();
    private H2DataSource getCacheDS(String dayString) {
        H2DataSource sqliteDatasource = cacheMap.get(dayString);
        if (sqliteDatasource == null) {
            synchronized (cacheMap) {
                sqliteDatasource = cacheMap.get(dayString);
                if (sqliteDatasource == null) {
                    sqliteDatasource = new H2DataSource("jdbc:h2:mem:FAlarmRecord_"+dayString,false);
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDatasource);
                    try {
                        init(jdbcTemplate);
                        cacheMap.put(dayString,sqliteDatasource);

                        removeLeastUsedCache();
                    } catch (SQLException e) {
                        logger.error(e.getMessage(),e);
                    }
                }
            }
        }
        markUse(dayString);
        return sqliteDatasource;
    }

    private void init(JdbcTemplate jdbcTemplate) throws SQLException {
        JdbcTemplateUtil.createTable(jdbcTemplate,FAlarmRecord.class,"FAlarmRecord");
        jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on FAlarmRecord(alarmItemDn)");
        jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on FAlarmRecord(alarmTime)");

    }

    private void removeLeastUsedCache() {
        try {
            if (cacheMap.size() > 3) {
                Integer key = cacheMap.reduceKeys(Long.MAX_VALUE, k -> Integer.parseInt(k), (k1, k2) -> k1 < k2 ? k1 : k2);
                H2DataSource h2DataSource = cacheMap.get(key + "");
                h2DataSource.release();
                cacheMap.remove(key+"");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }
    private void markUse(String day) {

    }
    private void loadCache() throws Exception {
        List<File> files = SqliteDBUtil.listDBFiles();
        File file = files.get(files.size() - 1);
        SqliteDataSource ds = new SqliteDataSource(file.getAbsolutePath());
        String key = file.getName().substring(0, file.getName().lastIndexOf("."));
        H2DataSource h2DataSource = new H2DataSource("jdbc:h2:mem:FAlarmRecord_"+ key,false);
        cacheMap.put(key,h2DataSource);

        JdbcTemplate srcTemplate = new JdbcTemplate(ds);
        JdbcTemplate destTemplate = new JdbcTemplate(h2DataSource);

        List list = JdbcTemplateUtil.queryForList(srcTemplate, FAlarmRecord.class, "SELECT * FROM FAlarmRecord");
        init(destTemplate);
        BJdbcUtil.insertObjects(h2DataSource.getConnection(),list,"FAlarmRecord");
        logger.info("Init FAlarmRecord Cache : {}, size = {}",key,list.size());
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
        String start = DateUtil.getDayString(startTime);
        String end = DateUtil.getDayString(endTime);
        JdbcTemplate jdbcTemplate = null;
        List result = null;
        if (start.equals(end)) {
            H2DataSource ds = getCacheDS(start);
            jdbcTemplate = new JdbcTemplate(ds);
            List<FAlarmRecord> list = JdbcTemplateUtil.queryForList(jdbcTemplate, FAlarmRecord.class, "SELECT * FROM FAlarmRecord where alarmTime <= ? and alarmTime >= ?", endTime, startTime);
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
            logger.debug("Query : start {}, end {} ,result = {}",startTime,endTime,list1.size());
            result = list1;
        }

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
            logger.debug("Query : start {}, end {} ,result = {}",startTime,endTime,list.size());
            return list;
        } else {
            SqliteDataSource ds = getDS(start);
            jdbcTemplate = new JdbcTemplate(ds);
            List<FAlarmRecord> list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, FAlarmRecord.class, "SELECT * FROM FAlarmRecord where  alarmTime >= ?", startTime);

            ds = getDS(end);
            jdbcTemplate = new JdbcTemplate(ds);
            List<FAlarmRecord> list2 = JdbcTemplateUtil.queryForList(jdbcTemplate, FAlarmRecord.class, "SELECT * FROM FAlarmRecord where alarmTime <= ?  ", endTime);
            list1.addAll(list2);
            logger.debug("Query : start {}, end {} ,result = {}",startTime,endTime,list1.size());
            return list1;
        }

    }

    public static void main(String[] args) {
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
