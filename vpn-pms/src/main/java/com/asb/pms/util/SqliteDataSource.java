package com.asb.pms.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SmartDataSource;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/17
 * Time: 13:15
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SqliteDataSource  extends AbstractDataSource implements SmartDataSource ,DBTypeMapper{
    private Connection connection = null;
    private String path = null;
    private Log logger = LogFactory.getLog(getClass());
    public SqliteDataSource(File file) {
        this(file.getAbsolutePath());
    }

    @Override
    public String toString() {
        return super.toString()+"{path='" + path +'}';
    }

    public SqliteDataSource(String filePath) {
        try {
            this.path = filePath;
            Class.forName("org.sqlite.JDBC");
            connection =  new SqliteConnection(DriverManager.getConnection("jdbc:sqlite:"+filePath, null, null));
            logger.info("sqlite db open : "+this.path);
         //   connection.setAutoCommit(false);
        } catch (Exception e) {
            logger.error(e,e);
        }

    }

    public void close() {
        logger.info("sqlite db close : "+this.toString());
        if (connection != null) {
            try {
                connection.close();
                if (connection instanceof SqliteConnection)
                    ((SqliteConnection)connection).doClose();
            } catch (SQLException e) {
                logger.error(e, e);
            }
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

    public class SqliteConnection implements Connection {
        private Connection conn = null;

        public SqliteConnection(Connection conn) {
            this.conn = conn;
        }

        @Override
        public Statement createStatement() throws SQLException {
            return conn.createStatement();
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return conn.prepareStatement(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return conn.prepareCall(sql);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return conn.nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            conn.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return conn.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            conn.commit();
        }

        @Override
        public void rollback() throws SQLException {
            conn.rollback();
        }

        @Override
        public void close() throws SQLException {
          //  conn.close();
        }

      //  @Override
        public void doClose() throws SQLException {
              conn.close();
        }

        @Override
        public boolean isClosed() throws SQLException {
            return conn.isClosed();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return conn.getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            conn.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return conn.isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            conn.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return conn.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            conn.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return conn.getTransactionIsolation();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return conn.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            conn.clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return conn.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return conn.getTypeMap();
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            conn.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            conn.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return conn.getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return conn.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return conn.setSavepoint(name);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
             conn.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
              conn.releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return conn.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return conn.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return conn.prepareStatement(sql, columnNames);
        }

        @Override
        public Clob createClob() throws SQLException {
            return conn.createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {
            return conn.createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return conn.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return conn.createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return conn.isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
             conn.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            conn.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return conn.getClientInfo(name);
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return conn.getClientInfo();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return conn.createArrayOf(typeName, elements);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return conn.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
              conn.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return conn.getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException {
             conn.abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
             conn.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return conn.getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return conn.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return conn.isWrapperFor(iface);
        }
    }

 
    public static void main(String[] args) {
        SqliteDataSource dataSource = new SqliteDataSource("ronnie3.db");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM ABC");
    }



}

