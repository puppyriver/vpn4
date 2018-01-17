package com.asb.pms;

import com.asb.pms.model.PM_DATA;
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
     //   logger.info("laster caache size = "+dataMap.size());
        PM_DATA data =  dataMap.get(stpId);
      //  logger.info("get lastest : "+data);
        return data;
    }
    public void remove(Long statPointId) {
        dataMap.remove(statPointId);
    }

}
