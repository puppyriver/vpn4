package com.asb.pms;

import com.asb.pms.model.PM_DATA;
import com.asb.pms.api.pm.PMQuery;
import com.asb.pms.api.pm.PMQueryResult;
import com.asb.pms.model.PM_PARAMS;

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
