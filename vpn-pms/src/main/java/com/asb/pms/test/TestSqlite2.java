package com.asb.pms.test;/**
 * Created by Chenrongrong on 2017/6/21.
 */

import com.asb.pms.SqliteDBUtil;
import com.asb.pms.model.PM_DATA;
import com.asb.pms.util.BJdbcUtil;
import com.asb.pms.util.JdbcTemplateUtil;
import com.asb.pms.util.SqliteDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestSqlite2 {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws SQLException, IllegalAccessException {
        long t1 = System.currentTimeMillis();
        for (int i=0; i < 10000; i++) {
            SqliteDataSource ds = SqliteDBUtil.getDaySqliteDatasource("ronniedb", PM_DATA.class, true, null);

//            Connection connection = ds.getConnection();
//            List list = new ArrayList();
//
//            PM_DATA data = new PM_DATA();
//            data.setTimePoint(new Date());
//            data.setValue((float)i);
//
//            list.add(data);
//            BJdbcUtil.insertObjects(connection,list,"PM_DATA");
//            connection.commit();
            ds.close();
        }


        long t2 = System.currentTimeMillis();
        System.out.println(t2-t1);

    }
}
