package com.infox.vpn4.pms2;

import com.infox.vpn4.pms2.model.DBObject;
import com.infox.vpn4.pms2.util.BJdbcUtil;
import com.infox.vpn4.pms2.util.IdentityUtil;
import com.infox.vpn4.pms2.util.JdbcTemplateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/16
 * Time: 9:37
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PMServerUtil {
    private static Log logger = LogFactory.getLog(PMServerUtil.class);

    private static JdbcTemplate jdbcTemplate = Configuration.getJdbcTemplate();
    public static DBObject queryObjectById(Class cls ,long id) throws Exception {
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM " + getTableName(cls) + " WHERE ID = " + id);
        if (list != null && list.size() > 0) {
            return (DBObject) JdbcTemplateUtil.mapToObject(list.get(0),cls);
        }
        return null;
    }

    public static String getTableName(Class cls) {

        Entity entity = (Entity)cls.getAnnotation(Entity.class);

        String tableName = cls.getSimpleName();
        if (entity != null && !entity.name().isEmpty())
            tableName = entity.name();

        return tableName;
    }

    public static DBObject saveObject(DBObject obj) throws Exception {
        if (obj.getId() != null)
            return updateObject(obj);
        Connection connection = null;
        try {
            connection = Configuration.getConnection();
            String tableName = getTableName(obj.getClass());
            obj.setId(IdentityUtil.getId(connection, tableName));
            obj =  (DBObject) BJdbcUtil.insertObject(connection,obj,tableName);
            connection.commit();
            return obj;
        } catch (SQLException e) {
            logger.error(e, e);
            connection.rollback();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return null;
    }

    public static DBObject updateObject(DBObject obj) throws Exception {
        Connection connection = null;
        try {
            connection = Configuration.getConnection();
            String tableName = getTableName(obj.getClass());
            obj =  (DBObject) BJdbcUtil.updateObjectById(connection,obj,tableName);
            connection.commit();
            return obj;
        } catch (SQLException e) {
            logger.error(e, e);
            connection.rollback();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return null;
    }
}
