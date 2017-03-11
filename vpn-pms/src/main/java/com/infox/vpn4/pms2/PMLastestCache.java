package com.infox.vpn4.pms2;

import com.infox.vpn4.pms2.model.PM_DATA;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/1
 * Time: 20:26
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PMLastestCache {
    private Log logger = LogFactory.getLog(getClass());

    private ConcurrentHashMap<Long,PM_DATA> dataMap = new ConcurrentHashMap();

    public void add(PM_DATA pm_data) {
        dataMap.put(pm_data.getStatPointId(),pm_data);
    }

    public PM_DATA get(long stpId) {
        return dataMap.get(stpId);
    }
    public void remove(Long statPointId) {
        dataMap.remove(statPointId);
    }

}
