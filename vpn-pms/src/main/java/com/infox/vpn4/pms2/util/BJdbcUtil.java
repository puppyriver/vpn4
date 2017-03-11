package com.infox.vpn4.pms2.util;

//import com.alcatelsbell.nms.common.CommonUtil;
//import com.alcatelsbell.nms.valueobject.sys.Ems;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/1/24.
 */
public class BJdbcUtil {


    public static Object getFieldValue(Object o,Field field ) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(o);
    }

    public static void setFieldValue(Object o,Field field,Object value ) throws IllegalAccessException {
        field.setAccessible(true);
        if (value != null)
            field.set(o,value);
    }
    public static List queryObjects(Connection connection,Class cls,String sql) throws Exception {
        List<Field> fields = ReflectionUtil.getAllFields(cls);

        StringBuffer sqlb = new StringBuffer();
        sqlb.append("  ");
        for (Field field : fields) {
            sqlb.append(field.getName()+",");

        }
        String select = sqlb.toString();
        select = select.substring(0,select.length()-1);

        if (sql.contains("*")) sql = sql.replaceAll("\\*",select);

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        List list = new ArrayList();

        while (resultSet.next()) {
            Object obj = cls.newInstance();
            int idx = 1;
            for (Field field : fields) {

                Object value = resultSet.getObject(idx++);
                if (value != null && value instanceof Number) {
                    if (field.getType().equals(Long.class) || field.getType().equals(long.class))
                        value = ((Number) value).longValue();

                    if (field.getType().equals(Integer.class) || field.getType().equals(int.class))
                        value = ((Number) value).intValue();

                    if (field.getType().equals(Float.class) || field.getType().equals(float.class))
                        value = ((Number) value).floatValue();

                    if (field.getType().equals(Double.class) || field.getType().equals(double.class))
                        value = ((Double) value).floatValue();

                    if (field.getType().equals(java.util.Date.class))
                        value = new java.util.Date(((Number) value).longValue());
                }



                setFieldValue(obj,field,value);


            }

            list.add(obj);

        }
        resultSet.close();
        statement.close();
        return list;

    }

    public static Object queryObjectById(Connection connection,Class cls,String tableName,long id) throws Exception {

        List<Field> fields = ReflectionUtil.getAllFields(cls);

        StringBuffer sqlb = new StringBuffer();
        sqlb.append("select ");
        for (Field field : fields) {
            sqlb.append(field.getName()+",");

        }
        String sql = sqlb.toString();
        sql = sql.substring(0,sql.length()-1);
        sql += " from "+tableName+" where id = "+id;
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        Object obj = cls.newInstance();
        while (resultSet.next()) {
            int idx = 1;
            for (Field field : fields) {
                if (field.getName().equals("updateDate")) {
                    System.out.println();
                }
                Object value = resultSet.getObject(idx++);
                if (value != null && value instanceof Number) {
                    if (field.getType().equals(Long.class) || field.getType().equals(long.class))
                        value = ((Number) value).longValue();

                    if (field.getType().equals(Integer.class) || field.getType().equals(int.class))
                        value = ((Number) value).intValue();

                    if (field.getType().equals(java.util.Date.class))
                        value = new java.util.Date(((Number) value).longValue());
                }



                setFieldValue(obj,field,value);


            }

        }
        resultSet.close();
        statement.close();
        return obj;


    }

    public static long queryId(Connection  connection, String sql) throws SQLException {

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        try {
            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                return id;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }  finally {

            resultSet.close();
            statement.close();
        }
        return -1;

    }

    public static void executeUpdate(Connection connection,String sql) throws SQLException {
        if (!toDB) {
            System.out.println("executeUpdate "+sql);
            return  ;
        }
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();

    }
    public static void execute(Connection connection,String sql) throws SQLException {
        if (!toDB) {
            System.out.println("execute "+sql);
            return  ;
        }
        Statement statement = connection.createStatement();
        statement.execute(sql);
        statement.close();

    }

    public static boolean toDB =  true;
    //"jdbc:sqlite:"+filePath
//    public static void createTable(Connection connection,Class cls,String tableName) throws SQLException {
//        StringBuffer sb = new StringBuffer("create table "+tableName+" (");
//        List<Field> fields = ReflectionUtil.getAllFields(cls);
//        for (int i = 0; i < fields.size(); i++) {
//            Field field = fields.get(i);
//            sb.append(field.getName()).append(" "+getDBTypeName(field));
//
//            if (i < fields.size() -1)
//                sb.append(", ");
//        }
//        sb.append(")");
//        PreparedStatement preparedStatement = connection.prepareStatement(sb.toString());
//        preparedStatement.execute();
//        preparedStatement.close();
//    }
//
//    private static String getDBTypeName(Field field) {
//        Class<?> type = field.getType();
//        if (type.equals(Integer.class) || type.equals(int.class))
//            return "integer";
//        else if ( type.equals(Long.class) || type.equals(long.class))
//            return "bigint";
//        else if ( type.equals(Double.class) || type.equals(double.class))
//            return "double";
//        else if ( type.equals(Float.class) || type.equals(float.class))
//            return "float";
//        else if (type.equals(String.class))
//            return "varchar";
//
//        else if (type.equals(java.util.Date.class))
//            return "timestamp";
//        return null;
//    }


    public static Object insertObject(Connection connection,Object obj,String tableName) throws SQLException, IllegalAccessException {
        if (!toDB) {
            System.out.println("insert "+obj);
            return obj;
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
        System.out.println("sql = " + sql);
        PreparedStatement prepareStatement =
                connection.prepareStatement(sql);
        // log(prepareStatement);
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            Class<?> type = field.getType();

            Object value = getFieldValue(obj,field);
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
        prepareStatement.execute();
        prepareStatement.close();
        return obj;

    }

    public static void insertObjects(Connection connection,List objs,String tableName) throws SQLException, IllegalAccessException {
        Object obj0  = objs.get(0);
        Class cls = obj0.getClass();
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

        String sql = "insert into " + (tableName == null ? obj0.getClass().getSimpleName() : tableName)
                + "(" + fs + ") values (" + qs + ")";
        System.out.println("sql = " + sql);
        connection.setAutoCommit(false);
        PreparedStatement prepareStatement =
                connection.prepareStatement(sql);

        for (int j = 0; j < objs.size(); j++) {
            Object obj = objs.get(j);
            // log(prepareStatement);
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                Class<?> type = field.getType();

                Object value = getFieldValue(obj,field);
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
            prepareStatement.addBatch();
            if(j%1000==0){//可以设置不同的大小；如50，100，500，1000等等

                prepareStatement.executeBatch();

                connection.commit();

                prepareStatement.clearBatch();

            }

        }
        prepareStatement.executeBatch();
        connection.commit();
        prepareStatement.clearBatch();
        prepareStatement.close();


    }

    public static Object updateObjectById(Connection connection,Object obj,String tableName) throws SQLException, IllegalAccessException {
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

            Object o = getFieldValue(obj,field);
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
        prepareStatement.executeUpdate();
        prepareStatement.close();
        return obj;

    }


    private static void log(PreparedStatement prepareStatement) {
        System.out.println(prepareStatement);
    }

    public static void main(String[] args) throws Exception {
//        queryObjects(null,Ems.class,"select * from ems");
//        Class.forName("org.sqlite.JDBC");
//        String filePath = "cde.db";
//   //     Connection connection = DriverManager.getConnection("jdbc:sqlite:"+filePath);
//        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
//        connection.setAutoCommit(false);
//
//        //createTable(connection,Ems.class,"S_EMS");
//    //    Object s_ems = queryObjectById(connection, Ems.class, "S_EMS", 1l);
//        Ems ems = new Ems();
//        ems.setDn("test");
//        ems.setId(1l);
//        ems.setName("test1");
//
//        insertObject(connection,ems,"S_EMS");
//
//        connection.commit();
//        Object s_ems = queryObjectById(connection, Ems.class, "S_EMS", 1l);
//        connection.close();

    }

}
