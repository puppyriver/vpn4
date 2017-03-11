package com.infox.vpn4.pms2;

import com.infox.vpn4.pms2.api.data.QueryCondition;
import com.infox.vpn4.pms2.api.data.QueryResult;
import com.infox.vpn4.pms2.api.pm.PMQuery;
import com.infox.vpn4.pms2.api.pm.PMQueryResult;
import com.infox.vpn4.pms2.model.PM_DATA;
import com.infox.vpn4.pms2.model.PM_PARAMS;

import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/23
 * Time: 10:18
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface PMCache {
    public boolean addToCache(PM_DATA pm_data);
    public void clearExpiredDatas(PM_PARAMS pmParam);
    public PMQueryResult query(PMQuery queryCondition) throws Exception;
    public Date getCacheEarliestTime();
    public Date getCacheEarliestTime(Long paramId);
    public PM_DATA getLatest(long stpId);
}
