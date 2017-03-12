package com.infox.vpn4.pms2.util;

import com.infox.vpn4.pms2.model.DBObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2015/8/12
 * Time: 14:54
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class JdbcTemplateUtil {
    private static Log logger = LogFactory.getLog(JdbcTemplateUtil.class);
    public static Object insert(JdbcTemplate jdbcTemplate,String tableName,Object obj) {
        return jdbcTemplate.execute(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                if (obj instanceof DBObject) {
                    if (((DBObject) obj).getId() == null) {
                        ((DBObject) obj).setId(IdentityUtil.getId(connection,tableName));
                    }
                }
                Class cls = obj.getClass();
                List<Field> fields = ReflectionUtil.getAllFields(cls);

                StringBuffer sb = new StringBuffer();
                StringBuffer sb2 = new StringBuffer();
                for (Field field : fields) {
                    sb.append(field.getName()+",");
                    sb2.append("?,");
                }
                String fs = sb.toString();
                fs = fs.substring(0,fs.length()-1);

                String qs = sb2.toString();
                qs = qs.substring(0,qs.length()-1);

                String sql = "insert into " + (tableName == null ? obj.getClass().getSimpleName() : tableName)
                        + "(" + fs + ") values (" + qs + ")";
                logger.debug("sql = " + sql);
                PreparedStatement prepareStatement =
                        connection.prepareStatement(sql);
                // log(prepareStatement);
                for (int i = 0; i < fields.size(); i++) {
                    Field field = fields.get(i);
                    Class<?> type = field.getType();

                    Object value = null;
                    try {
                        value = BJdbcUtil.getFieldValue(obj,field);
                    } catch (IllegalAccessException e) {
                        logger.error(e, e);
                    }
                    if (value != null) {
                        if (type.equals(Long.class) || type.equals(long.class)) {
                            prepareStatement.setLong(i + 1, (Long) value);
                        }
                        else if (type.equals(Integer.class) || type.equals(int.class)) {
                            prepareStatement.setInt(i + 1, (Integer) value);
                        }

                        else if (type.equals(Float.class) || type.equals(float.class)) {
                            prepareStatement.setFloat(i + 1, (Float) value);
                        }
                        else  if (type.equals(Double.class) || type.equals(double.class)) {
                            prepareStatement.setDouble(i + 1, (Double) value);
                        }

                        else if (type.equals(String.class)) {
                            prepareStatement.setString(i + 1, (String) value);
                        }

                        else if (type.equals(java.util.Date.class)) {
                            java.util.Date date = (java.util.Date) value;
                            prepareStatement.setTimestamp(i + 1, date == null ? null : new Timestamp(date.getTime()));
                        } else {
                            prepareStatement.setObject(i+1,value);
                        }
                    } else {
                        prepareStatement.setObject(i+1,null);
                    }

                }
                return prepareStatement;
            }
        }, new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                if (ps.execute())
                    return obj;
                return null;

            }
        });

    }

    public static Object update(JdbcTemplate jdbcTemplate,String tableName,Object obj) {
        return jdbcTemplate.execute(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {

                Class cls = obj.getClass();
                List<Field> fields = ReflectionUtil.getAllFields(cls);
                Long id = null;

                try {
                    //     id = (Long)obj.getClass().getDeclaredField("id").get(obj);
                    id = (Long)obj.getClass().getMethod("getId").invoke(obj);
                } catch ( Exception e) {

                    throw new SQLException("obj : "+obj+" has no id property");
                }
                StringBuffer sb = new StringBuffer();

                for (Field field : fields) {
                    sb.append(field.getName()+" = ");
                    sb.append("?,");
                }
                String fs = sb.toString();
                fs = fs.substring(0,fs.length()-1);



                PreparedStatement prepareStatement =
                        connection.prepareStatement("update "+(tableName == null ? obj.getClass().getSimpleName() : tableName)
                                +" set "+fs+" where id = "+id);
                // log(prepareStatement);
                for (int i = 0; i < fields.size(); i++) {
                    Field field = fields.get(i);
                    Class<?> type = field.getType();

                    Object o = null;
                    try {
                        o = ReflectionUtil.getFieldValue(obj,field);
                    } catch (IllegalAccessException e) {
                        logger.error(e, e);
                    }
                    if (o == null)
                        prepareStatement.setObject(i+1,null);
                    else {
                        if (type.equals(Long.class) || type.equals(long.class)) {
                            prepareStatement.setLong(i + 1, (Long) o);
                        }
                        if (type.equals(Integer.class) || type.equals(int.class)) {
                            prepareStatement.setInt(i + 1, (Integer) o);
                        }
                        if (type.equals(String.class)) {
                            prepareStatement.setString(i + 1, (String) o);
                        }

                        if (type.equals(java.util.Date.class)) {
                            java.util.Date date = (java.util.Date) o;
                            prepareStatement.setDate(i + 1, date == null ? null : new Date(date.getTime()));
                        }
                    }

                }

                return prepareStatement;
            }
        }, new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                if (ps.execute())
                    return obj;
                return null;

            }
        });

    }

    public static void remove(JdbcTemplate jdbcTemplate,String tableName,Long id) {
        jdbcTemplate.execute("DELETE FROM "+tableName+" WHERE ID = "+id);
    }

    public static List queryForList(JdbcTemplate jdbcTemplate,Class cls,String sql,Object... args) throws Exception {
        return mapListToObjectList(jdbcTemplate.queryForList(sql, args), cls);
    }

    public static List queryForList(JdbcTemplate jdbcTemplate,Class cls,String sql) throws Exception {
        return mapListToObjectList(jdbcTemplate.queryForList(sql), cls);
    }

    public static Object queryForObject(JdbcTemplate jdbcTemplate,Class cls,String sql) throws Exception {
        try {
            Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap(sql);
            return mapToObject(stringObjectMap,cls);
        } catch (EmptyResultDataAccessException e) {
            logger.error(e, e);
            return null;
        }
    }

    public static Object queryForObject(JdbcTemplate jdbcTemplate,Class cls,String sql,Object... args) throws Exception {
        try {
            Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap(sql,args);
            return mapToObject(stringObjectMap,cls);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public static Object queryForObjectById(JdbcTemplate jdbcTemplate,Class cls,long id) throws Exception {
        try {
            Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap("SELECT * FROM "+ cls.getSimpleName()+" where ID = ?",id);
            return mapToObject(stringObjectMap,cls);
        } catch (EmptyResultDataAccessException e) {
            logger.error(e, e);
            return null;
        }
    }

    public static Object mapToObject(Map map,Class cls) throws Exception {
        Object obj = cls.newInstance();
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Class<?> fieldType = null;
            Field field = getFieldIgnoreCase(cls, key);
            Method setMethod = null;
            Object value = null;
            if (field == null ) {
                setMethod = getSetMethodIgnoreCase(cls,"set"+key);
                if (setMethod != null && setMethod.getParameterTypes() != null && setMethod.getParameterTypes().length == 1)
                    fieldType = setMethod.getParameterTypes()[0];
            }


            if (field != null) {
                fieldType = field.getType();
            }

            if (fieldType != null) {
                value = map.get(key);

                if (value != null) {

                    if (value instanceof Number && (fieldType == Long.class || fieldType == long.class))
                        value = ((Number) value).longValue();

                    if (value instanceof Number && (fieldType == Integer.class || fieldType == int.class))
                        value = ((Number) value).intValue();

                    if (value instanceof Number && (fieldType == Double.class || fieldType == double.class))
                        value = ((Number) value).doubleValue();

                    if (value instanceof Number && (fieldType == Float.class || fieldType == float.class))
                        value = ((Number) value).floatValue();

                    if (value instanceof String && (fieldType == String.class || fieldType == String.class))
                        value = value.toString();

                    if (value instanceof Number && (fieldType == java.util.Date.class))
                        value = new java.util.Date(((Number) value).longValue());

                }

            }
            if (value != null) {
                if (field != null)
                    ReflectionUtil.setFieldValue(obj, field, value);
                else if (setMethod != null) {
                    setMethod.invoke(obj,value);
                }


            }
        }
        return  obj;

    }

    public static List mapListToObjectList(List maps,Class cls) throws Exception {
        List list = new ArrayList();
        for (Object o : maps) {
            Map map = (Map)o;
            list.add(mapToObject(map,cls));
        }
        return list;
    }

    public  static Field getFieldIgnoreCase(Class cls,String name) {
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.getName().equalsIgnoreCase(name))
                return declaredField;
        }
        return null;
    }

    public  static Method getSetMethodIgnoreCase(Class cls,String name) {
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            if (method.getName().equalsIgnoreCase(name))
                return method;
        }
        return null;
    }


    public static void createTable(JdbcTemplate jdbcTemplate,Class cls,String tableName) throws SQLException {

        DataSource dataSource = jdbcTemplate.getDataSource();
        StringBuffer sb = new StringBuffer("create table "+tableName+" (");
        List<Field> fields = ReflectionUtil.getAllFields(cls);
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            if (dataSource instanceof DBTypeMapper)
                sb.append(field.getName()).append(" "+((DBTypeMapper)dataSource).getDBTypeName(field));
            else
                sb.append(field.getName()).append(" "+getDBTypeName(field));

            if (i < fields.size() -1)
                sb.append(", ");
        }
        sb.append(")");

        logger.info(sb.toString());
        jdbcTemplate.execute(sb.toString());
    }

    private static String getDBTypeName(Field field) {
        Class<?> type = field.getType();
        if (type.equals(Integer.class) || type.equals(int.class))
            return "int";
        else if ( type.equals(Long.class) || type.equals(long.class))
            return "int";
        else if ( type.equals(Double.class) || type.equals(double.class))
            return "float";
        else if ( type.equals(Float.class) || type.equals(float.class))
            return "float";
        else if (type.equals(String.class))
            return "varchar(255)";

        else if (type.equals(java.util.Date.class))
            return "timestamp";
        return null;
    }




    public static void main(String[] args) {


    }
}
