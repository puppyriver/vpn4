package com.infox.vpn4.pms2.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.tools.Server;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SmartDataSource;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2015/10/26.
 */
public class H2DataSource extends AbstractDataSource implements SmartDataSource,DBTypeMapper {
    private Connection connection = null;
    private Log logger = LogFactory.getLog(getClass());
    public H2DataSource() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:mem:pms", null, null);

            startServer();
        } catch (Exception e) {
            logger.error(e,e);
        }

    }
    public H2DataSource(String url) {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(url, null, null);

            startServer();
        } catch (Exception e) {
            logger.error(e,e);
        }

    }
    public H2DataSource(String url,boolean startServer) {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(url, null, null);

            if (startServer)
                startServer();
        } catch (Exception e) {
            logger.error(e,e);
        }

    }
    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return connection;
    }

    @Override
    public boolean shouldClose(Connection con) {
        return false;
    }

    private Server server;
    private String port = SysProperty.getString("h2.tcp.port","10099");
    public void startServer() {
        try {
            logger.info("正在启动h2...");
            server = Server.createTcpServer(
                    new String[]{"-tcpPort", port}).start();
            logger.info("启动成功");
        } catch (SQLException e) {
            logger.error("启动h2出错：" + e.toString(), e);
// TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    public void stopServer() {
        if (server != null) {
            logger.info("正在关闭h2...");
            server.stop();
            logger.info("关闭成功.");
        }
    }

    @Override
    public String getDBTypeName(Field field) {
        Class<?> type = field.getType();
        if (type.equals(Integer.class) || type.equals(int.class))
            return "integer";
        else if ( type.equals(Long.class) || type.equals(long.class))
            return "bigint";
        else if ( type.equals(Double.class) || type.equals(double.class))
            return "double";
        else if ( type.equals(Float.class) || type.equals(float.class))
            return "float";
        else if (type.equals(String.class))
            return "varchar";

        else if (type.equals(java.util.Date.class))
            return "timestamp";
        return null;
    }

    public void release() {
        try {
            connection.close();
        } catch (SQLException e) {
            logger.error(e.getErrorCode(),e);
        }

    }

}
