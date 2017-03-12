package com.infox.vpn4.pms2.repository;

import com.infox.vpn4.pms2.model.PM_DATA;


import java.util.Date;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface PmDataRepository {
    public void insert(PM_DATA event);
    public List<PM_DATA> query(Date startTime, Date endTime,List<String> stpKeys) throws Exception ;
    public PM_DATA getLatest(long stpId);
    //public List find(String day)
}
