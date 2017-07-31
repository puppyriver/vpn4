package com.asb.pms.repository;

import com.asb.pms.model.PM_DATA;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface PmDataRepository {
    public void insert(PM_DATA event);
    public List<PM_DATA> query(Date startTime, Date endTime, List<String> stpKeys, HashMap queryAttributes) throws Exception ;
    public PM_DATA getLatest(long stpId);
    //public List find(String day)
}
