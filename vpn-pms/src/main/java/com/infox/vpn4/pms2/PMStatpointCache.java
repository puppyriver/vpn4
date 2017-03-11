package com.infox.vpn4.pms2;


import com.infox.vpn4.pms2.model.PM_STATPOINT;
import com.infox.vpn4.pms2.util.JdbcTemplateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2015/8/28
 * Time: 12:53
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PMStatpointCache {
    private Log logger = LogFactory.getLog(getClass());
    private HashMap<Long,PM_STATPOINT> gps = new HashMap<Long, PM_STATPOINT>();

    private static PMStatpointCache inst = new PMStatpointCache();
    public static PMStatpointCache getCache() {
        return inst;
    }

    private PMStatpointCache() {
        init();
    }

    public void init() {
        List<Map<String, Object>> maps = Configuration.getJdbcTemplate().queryForList("SELECT * FROM PM_STATPOINT");
        List<PM_STATPOINT> list = null;
        try {
            list = JdbcTemplateUtil.mapListToObjectList(maps, PM_STATPOINT.class);
        } catch (Exception e) {
            logger.error(e, e);
        }
        for (PM_STATPOINT PM_STATPOINT : list) {
            gps.put(PM_STATPOINT.getId(),PM_STATPOINT);
        }
    }

    public void add(PM_STATPOINT statpoint) {
        gps.put(statpoint.getId(),statpoint);
    }

    public void remove(long id) {
        gps.remove(id);
    }



    public PM_STATPOINT getPMStatPoint(long id) {
        PM_STATPOINT PM_STATPOINT = gps.get(id);
        if (PM_STATPOINT != null)
            return PM_STATPOINT;

        try {
            PM_STATPOINT = (PM_STATPOINT)JdbcTemplateUtil.mapToObject
                    (Configuration.getJdbcTemplate().queryForMap("SELECT * from PM_STATPOINT WHERE ID = ?",id),PM_STATPOINT.class);
        } catch (Exception e) {
            logger.error(e, e);
        }

        gps.put(id,PM_STATPOINT);

        return PM_STATPOINT;
    }
}
