package infox.vpn4.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SmartDataSource;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/17
 * Time: 13:15
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SqliteDataSource  extends AbstractDataSource implements SmartDataSource ,DBTypeMapper {
    private Connection connection = null;
    private Log logger = LogFactory.getLog(getClass());
    public SqliteDataSource(String filePath) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:"+filePath, null, null);
         //   connection.setAutoCommit(false);
        } catch (Exception e) {
            logger.error(e,e);
        }

    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
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
    public static void main(String[] args) {
        SqliteDataSource dataSource = new SqliteDataSource("ronnie3.db");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM ABC");
    }



}

