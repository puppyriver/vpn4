package com.asb.pms.test;

//import com.alcatelsbell.nms.alarm.Test;
import com.asb.pms.model.PM_DATA;
import com.asb.pms.util.BJdbcUtil;
import com.asb.pms.util.JdbcTemplateUtil;
import com.asb.pms.util.SqliteDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Administrator on 2016/12/1.
 */
public class TestSqlite implements Runnable{
    int i = 0;

    public TestSqlite(int i) {
        this.i = i;
    }

    @Override
    public void run() {
        SqliteDataSource ds = new SqliteDataSource("ronnie3.db");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

        try {
            List l = JdbcTemplateUtil.queryForList(jdbcTemplate,PM_DATA.class,"SELECT * FROM PM_DATA where id > ?",(long)i);
            System.out.println(Thread.currentThread().getName()+"::l = " + l.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException, InterruptedException, IllegalAccessException {

//        for (int i = 0; i < 100; i++) {
//            new Thread(new TestSqlite(i)).start();
//        }
//        synchronized (TestSqlite.class) {
//            TestSqlite.class.wait();
//        }
        SqliteDataSource ds = new SqliteDataSource("ronnie3.db");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

        JdbcTemplateUtil.createTable(jdbcTemplate, PM_DATA.class,"PM_DATA");

        Connection _conn = ds.getConnection();
        _conn.setAutoCommit(false);
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            PM_DATA data = new PM_DATA();
            data.setId((long)i);
            data.setValue((float)i);
            data.setStatPointId((long)i);
            JdbcTemplateUtil.insert(jdbcTemplate,"PM_DATA",data);
            if (i % 1000 == 0)
                System.out.println("i = " + i);
            BJdbcUtil.insertObject(_conn,data,"PM_DATA");
        }
        _conn.commit();
        long t2 = System.currentTimeMillis() - t1;
        System.out.println("t2 = " + t2/1000+"s");


    }
}
