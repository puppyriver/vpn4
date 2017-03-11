package com.infox.vpn4.pms2;


import com.infox.vpn4.pms2.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Author: Ronnie.Chen
 * Date: 2016/12/2
 * Time: 11:09
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SqliteDBUtil {
    private static Logger logger = LoggerFactory.getLogger(SqliteDBUtil.class);

    public static SqliteDataSource getDaySqliteDatasource(String day, Class cls, Consumer<JdbcTemplate> initTable) {
        return getDaySqliteDatasource(day,cls,true,initTable);
    }
    public static File getFolder() {
        File dir = new File(getFolderPath());
        return dir;
    }
    public static List<File> listDBFiles() {
        File dir = new File(getFolderPath());
        File[] files = dir.listFiles();

        List<File> list = Arrays.asList(files);
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String n1 = o1.getName().substring(0,o1.getName().lastIndexOf("."));
                String n2 = o2.getName().substring(0,o2.getName().lastIndexOf("."));


                try {
                    return Integer.parseInt(n1) - Integer.parseInt(n2);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });
        return list;
    }
    private static String getFolderPath() {
        String folder = SysProperty.getString("archive.db.folder","dbs");
        folder = (folder.endsWith("/") || folder.endsWith("\\"))? folder : (folder+ File.separator);
        if (!new File(folder).exists()) new File(folder).mkdirs();
        return folder;
    }
    public static SqliteDataSource getDaySqliteDatasource(String day, Class cls, boolean createIfNotExist, Consumer<JdbcTemplate> initTable) {

        String folder = getFolderPath();
        File dbFile = new File(folder+day+".db");
        boolean init = !dbFile.exists();
        if (init && !createIfNotExist) return null;
        SqliteDataSource dataSource = new SqliteDataSource(folder+day+".db");

        JdbcTemplate sqliteJdbc = new JdbcTemplate(dataSource);
        if (init) {
            try {
                JdbcTemplateUtil.createTable(sqliteJdbc, cls, cls.getSimpleName());
                if (initTable != null)
                    initTable.accept(sqliteJdbc);
//                sqliteJdbc.execute("CREATE INDEX IDX_PMDATA_STPID on PM_DATA(statPointId)");
//                sqliteJdbc.execute("CREATE INDEX IDX_PMDATA_TIMEPOINT on PM_DATA(TIMEPOINT)");
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return dataSource;
    }

    private static HashMap<String,ReadWriteLock> locks = new HashMap<>();
    public synchronized static ReadWriteLock getDBLock(String day) {
        ReadWriteLock lock = locks.get(day);
        if (lock == null) {
            lock = new ReentrantReadWriteLock(true);
            locks.put(day,lock);
        }
        return lock;
    }

//    public static PMQueryResult query(PMQuery query) throws Exception {
//        Date startTime = query.startTime;
//        Date endTime = query.endTime;
//        if (endTime == null) endTime = new Date();
//
//        List<String> days = DateUtils.getDayStrings(startTime, endTime);
//
//        PMQueryResult result = null;
//        for (String day : days) {
//
//            ReadWriteLock lock = getDBLock(day);
//
//            lock.readLock().lock();
//
//            SqliteDataSource ds = getDaySqliteDatasource(day,false);
//            if (ds == null) continue;
//            JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
//
//            Map<String, List<PM_DATA>> rt = query.query(jdbcTemplate);
//            if (result == null) {
//                result = new PMQueryResult(rt);
//            } else {
//                result.merge(new PMQueryResult(rt));
//            }
//
//
//        }
//
//        return result;
//
//
//    }
}
