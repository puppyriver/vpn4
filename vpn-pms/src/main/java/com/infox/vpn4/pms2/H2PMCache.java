package com.infox.vpn4.pms2;


import com.infox.vpn4.pms2.api.pm.PMQuery;
import com.infox.vpn4.pms2.api.pm.PMQueryResult;
import com.infox.vpn4.pms2.model.PM_DATA;
import com.infox.vpn4.pms2.model.PM_PARAMS;
import com.infox.vpn4.pms2.util.H2DataSource;
import com.infox.vpn4.pms2.util.BJdbcUtil;
import com.infox.vpn4.pms2.util.JdbcTemplateUtil;
import com.infox.vpn4.pms2.util.SysProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/23
 * Time: 10:19
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class H2PMCache implements PMCache {

    private Log logger = LogFactory.getLog(getClass());
    private JdbcTemplate jdbcTemplate = null;
    private H2DataSource dataSource = null;
    private AtomicLong count = new AtomicLong(0);
    private Date earliestTime = new Date();

    private DataSource dbDataSource = null;

    private PMLastestCache lastestCache = new PMLastestCache();

    private HashMap<Long,Date> timeLines = new HashMap<>();

    public H2PMCache() {
        dataSource = new H2DataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);

        try {
      //      Connection connection = dataSource.getConnection();
            JdbcTemplateUtil.createTable(jdbcTemplate,PM_DATA.class,"PM_DATA");
            jdbcTemplate.execute("CREATE INDEX IDX_PMDATA_STPID on PM_DATA(statPointId)");
            jdbcTemplate.execute("CREATE INDEX IDX_PMDATA_TIMEPOINT on PM_DATA(TIMEPOINT)");

//            BJdbcUtil.createTable(connection,PM_DATA.class,"PM_DATA");
         //   connection.close();
        } catch (SQLException e) {
            logger.error(e, e);
        }

        if (SysProperty.getString("LoadCacheOnStartup","on").equalsIgnoreCase("on")) {
            initData();
        }



    }

    private void initData() {
        String sql = "SELECT * FROM PM_DATA where timepoint > ? order by id";
        try {
            JdbcTemplate jdbcTemplate = null;
            if (this.dbDataSource != null) {
                jdbcTemplate = new JdbcTemplate(this.dataSource);
            } else {
                jdbcTemplate = Configuration.getJdbcTemplate();
            }
            Date date = new Date(System.currentTimeMillis() - 3600l * 1000l * 6);
            List<PM_DATA> datas = JdbcTemplateUtil.queryForList(jdbcTemplate,PM_DATA.class,sql, date);
            logger.info("pmdata size = "+datas.size()+" to insert to cache");
            int i = 0;
            for (PM_DATA data : datas) {
                this.addToCache(data);
//                if (i++ % 100 == 0) {
//                    logger.info(i+" add to cache");
//                }
            }
            logger.info("Init Cache pmdata size = "+datas.size());
            earliestTime = date;
        } catch (Exception e) {
            logger.error(e, e);
        }


    }

    public void clearExpiredDatas(PM_PARAMS pmParam) {
        Integer hours = pmParam.getKeepInCacheHours();
        if (hours == null || hours < 0)
            hours = 24;

        long timeline =  System.currentTimeMillis() - hours  * 3600l * 1000l;
        Date date = new Date(timeline);
        jdbcTemplate.update("DELETE FROM PM_DATA WHERE  UPDATEDATE < ?", date);

        synchronized (timeLines) {
            timeLines.put(pmParam.getId(), date);
        }

        List<PM_PARAMS> allParas = Context.getInstance().pmParasDao.getAll();

        Date t = null;
        for (PM_PARAMS para : allParas) {
            Date tl = timeLines.get(para.getId());
            if (tl == null || !tl.after(earliestTime)) {
                t = null;
                break;
            }

            if (t == null || t.after(tl))
                t = tl;
        }

        if (t != null) {
            earliestTime = t;
            logger.info("EarliestTime updated to :"+earliestTime);
        }


    }

    @Override
    public PMQueryResult query(PMQuery query) throws Exception {
        Map<String, List<PM_DATA>> data = query.query(jdbcTemplate);
        return new PMQueryResult(data);
    }

    @Override
    public Date getCacheEarliestTime() {
        return earliestTime;
    }

    @Override
    public Date getCacheEarliestTime(Long paramId) {
        Date d ;
        return (d = timeLines.get(paramId)) == null? earliestTime : d ;
    }

    @Override
    public PM_DATA getLatest(long stpId) {
        PM_DATA data = lastestCache.get(stpId);
        if (data != null && data.getDataMap() != null)
            data.getDataMap().clear();
        return data;
    }

    @Override
    public boolean addToCache(PM_DATA pm_data) {
     //   jdbcTemplate.execute("UPDATE PM_DATA SET STATUS="+Constants.PM_DATA_STATUS_CACHE+" WHERE STATPOINTID = "+pm_data.getStatPointId());
        pm_data.setStatus(Constants.PM_DATA_STATUS_CURRENT);
    //    JdbcTemplateUtil.insert(jdbcTemplate,"PM_DATA",pm_data);

        lastestCache.add(pm_data);
        return true;
    }


    public static void main(String[] args) throws InterruptedException {
        H2PMCache cache = new H2PMCache();
        for (int i = 0; i < 100; i++) {
            PM_DATA pm_data = new PM_DATA();
            pm_data.setId((long)i);
            pm_data.setValue((float)i);
            cache.addToCache(pm_data);
        }
        Thread.sleep(1000000l);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public H2DataSource getDataSource() {
        return dataSource;
    }

    public DataSource getDbDataSource() {
        return dbDataSource;
    }

    public void setDbDataSource(DataSource dbDataSource) {
        this.dbDataSource = dbDataSource;
    }
}
