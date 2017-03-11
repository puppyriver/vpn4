package com.infox.vpn4.pms2;

import com.infox.vpn4.pms2.model.PM_DATA;
import com.infox.vpn4.pms2.util.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

/**
 * Author: Ronnie.Chen
 * Date: 2016/12/1
 * Time: 15:51
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DBRecycleJob  implements Job {
    private Logger logger = LoggerFactory.getLogger(DBRecycleJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JdbcTemplate jdbcTemplate = Configuration.getJdbcTemplate();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.add(Calendar.DATE,-3);
        Date time = calendar.getTime();

        long t1 = System.currentTimeMillis();

        int start = 0;
        int limit = 100000;
        boolean success = true;
        while (true) {
            try {
                List<PM_DATA>  datas = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class,
                        SqlUtil.getPagerSQL("SELECT * FROM PM_DATA WHERE TIMEPOINT < ? oder by ID",start,limit),time);

                if (datas == null || datas.isEmpty()) break;

                logger.info("Find "+datas.size()+" PM_DATAS to be recycled");

                Map<String, List<PM_DATA>> collect =
                        datas.stream().collect(Collectors.groupingBy(pm_data ->  DateUtils.getDayString(pm_data.getTimePoint())));


                for (String day : collect.keySet()) {
                    List<PM_DATA> pm_datas = collect.get(day);
                    SqliteDataSource dataSource = null;


                    ReadWriteLock dbLock = null;
                    try {
                        dataSource = SqliteDBUtil.getDaySqliteDatasource(day,PM_DATA.class,null);

                        Connection connection = dataSource.getConnection();
                        dbLock = SqliteDBUtil.getDBLock(day);
                        dbLock.writeLock().lock();
                        connection.setAutoCommit(false);
                        for (PM_DATA pm_data : pm_datas) {
                            pm_data.setId(IdentityUtil.getId(connection,day+"#PM_DATA"));
                            BJdbcUtil.insertObject(connection,pm_data,"PM_DATA");
                        }

                        connection.commit();
                    } catch (Throwable e) {
                        success = false;
                        logger.error(e.getMessage(), e);
                        throw e;
                    } finally {
                        if (dataSource != null)
                            dataSource.close();

                        if (dbLock != null) {
                            dbLock.writeLock().unlock();
                        }

                    }

                    if (datas.size() < limit) break;
                    start += limit;



                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                success = false;
                break;
            }

            break;
        }

        if (success) {
            jdbcTemplate.update("DELETE FROM PM_DATA where TIMEPOINT < ?  ",time);
            if (SysProperty.getString("pms.sql.dialect","oracle").equalsIgnoreCase("mysql")) {
                PmSystemUtil.updateValue(Constants.CATEGORY_PM_DATA,Constants.KEY_DB_TIMELINE,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time));
                jdbcTemplate.execute("OPTIMIZE TABLE PM_DATA");
            }
        }

        long t2  = System.currentTimeMillis() - t1;
        logger.info("DBREcycleJob finished ,spend time : "+(int)(t2/1000)+"s");
    }

    public static void main(String[] args) {

       // System.out.println("calendar = " + calendar.getTime());
    }
}
