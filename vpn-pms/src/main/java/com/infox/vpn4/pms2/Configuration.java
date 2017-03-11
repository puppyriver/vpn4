package com.infox.vpn4.pms2;


import com.infox.vpn4.pms2.common.fs.FileSystem;
import com.infox.vpn4.pms2.util.SqliteDataSource;
import com.infox.vpn4.pms2.util.SysProperty;
//import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/22
 * Time: 13:20
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class Configuration {
    private static Log   logger = LogFactory.getLog(Configuration.class);

    private static  DataSource dataSource = null;


    public static void setDataSource(DataSource dataSource) {
        Configuration.dataSource = dataSource;
    }

    public static synchronized DataSource getDataSource(){
        // logger.error("TRACE",new Exception("TRACE"));
        if (dataSource == null) {
//            dataSource = new BasicDataSource();
//            ((BasicDataSource)dataSource).setDriverClassName(SysProperty.getString("jdbc.pms.driverClassName"));
//            ((BasicDataSource)dataSource).setUrl(SysProperty.getString("jdbc.pms.url"));
//            ((BasicDataSource)dataSource).setUsername(SysProperty.getString("jdbc.pms.username"));
//            ((BasicDataSource)dataSource).setPassword(SysProperty.getString("jdbc.pms.password"));
//            ((BasicDataSource)dataSource).setInitialSize(SysProperty.getInt("jdbc.pms.initialSize"));
//            ((BasicDataSource)dataSource).setMinIdle(SysProperty.getInt("jdbc.pms.minIdle"));
//            ((BasicDataSource)dataSource).setMaxActive(SysProperty.getInt("jdbc.pms.maxActive"));
//            ((BasicDataSource)dataSource).setValidationQuery("select count(1) from dual");
            dataSource = new SqliteDataSource("pms.db");
        }

        return dataSource;

    }

    public static Connection getConnection() {

        Connection conn = null;
        try {
           conn = getDataSource().getConnection();
        } catch (SQLException e) {
            logger.error(e, e);
        }
        return conn;
    }

    private static JdbcTemplate template = null;
    public static synchronized JdbcTemplate getJdbcTemplate() {
        if (template == null)
            template = new JdbcTemplate(getDataSource());
        return template;
    }
    private static DataSourceTransactionManager dataSourceTransactionManager = null;
    private static TransactionTemplate transactionTemplate = null;
    public static  <T> T executeInTransaction(TransactionCallback<T> action) {
        if (transactionTemplate == null) {
            synchronized (Configuration.class) {
                if (transactionTemplate == null) {
                    dataSourceTransactionManager = new DataSourceTransactionManager(getDataSource());
                    transactionTemplate = new TransactionTemplate(dataSourceTransactionManager);
                }
            }
        }
        return transactionTemplate.execute(action);
    }

    private static FileSystem fileSystem = FileSystem.getFileSystem(SysProperty.getString("fs.root","../fs"));

    public static FileSystem getFileSystem() {
        return fileSystem;
    }


}
