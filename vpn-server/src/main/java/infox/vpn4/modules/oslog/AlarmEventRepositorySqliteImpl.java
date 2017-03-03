package infox.vpn4.modules.oslog;

import infox.vpn4.modules.oslog.sqlite.SqliteDBUtil;
import infox.vpn4.util.DateUtil;
import infox.vpn4.util.JdbcTemplateUtil;
import infox.vpn4.util.SqliteDataSource;
import infox.vpn4.valueobject.FAlarmRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;
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
    @Override
    public void insert(FAlarmRecord event) {
        String dayString = DateUtil.getDayString(event.getAlarmTime());
        SqliteDataSource sqliteDatasource = getDS(dayString);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDatasource);
        JdbcTemplateUtil.insert(jdbcTemplate,"FAlarmRecord",event);

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
}
