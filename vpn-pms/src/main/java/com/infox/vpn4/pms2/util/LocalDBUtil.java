package com.infox.vpn4.pms2.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/25
 * Time: 9:04
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class LocalDBUtil {
    private static Log logger = LogFactory.getLog(LocalDBUtil.class);

    private static SqliteDataSource dataSource = new SqliteDataSource("pms.db");
    private static JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    static {
        try {
            jdbcTemplate.execute("CREATE TABLE META_CONFIG (ID NUMBER(19),TYPE VARCHAR ,KEY VARCHAR,VALUE VARCHAR )");
        } catch (DataAccessException e) {
            logger.error(e, e);
        }
    }

    static class META_CONFIG {
        public Long id;
        public String type;
        public String key;
        public String value;
    }

    public static JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
    public static final String getValue(String type,String key,String _default) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM META_CONFIG WHERE type = ? and name = ? ",type, key);

        if (rows == null || rows.isEmpty()) {
            META_CONFIG metaConfig = new META_CONFIG();

            metaConfig.type =(type);
            metaConfig.key = (key);
            metaConfig.value = (_default);

            JdbcTemplateUtil.insert(jdbcTemplate,"META_CONFIG",metaConfig);

            return _default;
        } else {
            return (String)rows.get(0).get("VALUE");
        }


    }

    public static final String updateValue(String type,String key,String value) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM META_CONFIG WHERE type = ? and name = ?",type, key);

        if (rows == null || rows.isEmpty()) {
            META_CONFIG metaConfig = new META_CONFIG();

            metaConfig.type = (type);
            metaConfig.value = (value);
            metaConfig.key  = (key);

            JdbcTemplateUtil.insert(jdbcTemplate,"META_CONFIG",metaConfig);

            return value;
        } else {
            jdbcTemplate.execute("UPDATE META_CONFIG set value = '"+value+"' WHERE type = '"+type+"' and name = '"+key+"'");
        }

        return value;


    }


}
