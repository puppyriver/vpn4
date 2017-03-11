package com.infox.vpn4.pms2;

import com.infox.vpn4.pms2.model.PM_STATPOINT;
import com.infox.vpn4.pms2.util.JdbcTemplateUtil;
import com.infox.vpn4.pms2.util.SqlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/25
 * Time: 15:46
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PmStatPointManager {
    private Log logger = LogFactory.getLog(getClass());
    private static PmStatPointManager ourInstance = new PmStatPointManager();
    private JdbcTemplate jdbcTemplate = null;
    public static PmStatPointManager getInstance() {
        return ourInstance;
    }
    private HashMap<Long,PM_STATPOINT> idMap = new HashMap<>();
    private PmStatPointManager() {
        jdbcTemplate = Configuration.getJdbcTemplate();
        initPmStatPoint();
    }

    private void initPmStatPoint() {
        int start = 0;
        int batchSize = 100000;
        while (true) {
            List sps = jdbcTemplate.queryForList(SqlUtil.getPagerSQL("SELECT * FROM PM_STATPOINT", start, batchSize));
            List<PM_STATPOINT> pmStatpoints = null;
            try {
                pmStatpoints = JdbcTemplateUtil.mapListToObjectList(sps, PM_STATPOINT.class);
            } catch (Exception e) {
                logger.error(e, e);
            }
            for (PM_STATPOINT pmStatpoint : pmStatpoints) {
                idMap.put(pmStatpoint.getId(),pmStatpoint);
            }
            start += sps.size();

            if (sps.size() < batchSize) break;
        }

    }

    public static void main(String[] args) {
        PmStatPointManager.getInstance();
    }


}
