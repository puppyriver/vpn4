package com.asb.pms.util;

/**
 * Author: Ronnie.Chen
 * Date: 2015/7/15
 * Time: 10:49
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SqlUtil {
    public static String getPagerSQL(String originalSQL, Integer start,
                                   Integer limit) {
        String dialect = SysProperty.getString("pms.sql.dialect", "oracle");
        if (dialect.equalsIgnoreCase("mysql")) {
            return originalSQL+" limit "+start+", "+limit;
        }
        return "select * from (select rownum as r,t.* from( " + originalSQL
                + ") t where rownum<= " + (start + limit) + ") where r>"
                + start;
    }



    public static String getCountSQL(String sql){
        String s = " select count(1) as COUNT from ( "+ sql + " )";
        return s;
    }
}
