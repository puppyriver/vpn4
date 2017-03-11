package com.infox.vpn4.pms2.util;

//import com.alcatelsbell.nms.valueobject.meta.MetaConfig;
import com.infox.vpn4.pms2.Configuration;
import com.infox.vpn4.pms2.model.PM_SYSTEM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2015/12/4
 * Time: 14:47
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PmSystemUtil {
    private static Logger logger = LoggerFactory.getLogger(PmSystemUtil.class);

    public static final Date getDateValue(String category, String key, String _default) throws ParseException {
        String v = getValue(category, key, _default);
        if (v != null)
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(v);

        return null;
    }

    public static final String getValue(String category,String key,String _default) {
        List<Map<String, Object>> rows = Configuration.getJdbcTemplate().queryForList("SELECT * FROM PM_SYSTEM WHERE category = ? and variable = ? ",category, key);

        if (rows == null || rows.isEmpty()) {
            PM_SYSTEM metaConfig = new PM_SYSTEM();
            metaConfig.setCategory(category);
            metaConfig.setDn(category+"@"+key);
            metaConfig.setVariable(key);
            metaConfig.setValue(_default);
            try {
                metaConfig.setId(IdentityUtil.getId(Configuration.getConnection(),"PM_SYSTEM"));
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }


            JdbcTemplateUtil.insert(Configuration.getJdbcTemplate(),"PM_SYSTEM",metaConfig);

            return _default;
        } else {
            return (String)rows.get(0).get("VALUE");
        }


    }

    public static final String updateValue(String category,String key,String value) {
        List<Map<String, Object>> rows = Configuration.getJdbcTemplate().queryForList("SELECT * FROM PM_SYSTEM WHERE category = ? and variable = ?",category, key);

        if (rows == null || rows.isEmpty()) {
            PM_SYSTEM metaConfig = new PM_SYSTEM();

            metaConfig.setCategory(category);
            metaConfig.setValue(value);
            metaConfig.setVariable(key);
            metaConfig.setDn(category+"@"+key);
            try {
                metaConfig.setId(IdentityUtil.getId(Configuration.getConnection(),"PM_SYSTEM"));
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
            JdbcTemplateUtil.insert(Configuration.getJdbcTemplate(),"PM_SYSTEM",metaConfig);

            return value;
        } else {
            Configuration.getJdbcTemplate().execute("UPDATE PM_SYSTEM set value = '"+value+"' WHERE category = '"+category+"' and variable = '"+key+"'");
        }

        return value;


    }

    public static void main(String[] args) {


    }

}
