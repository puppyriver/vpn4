package com.infox.vpn4.pms2.api;

import com.infox.vpn4.pms2.api.data.PmParaData;
import com.infox.vpn4.pms2.api.data.QueryCondition;
import com.infox.vpn4.pms2.api.data.QueryResult;
import com.infox.vpn4.pms2.api.pm.PMQuery;
import com.infox.vpn4.pms2.model.PM_DATA;
import com.infox.vpn4.pms2.model.PM_ENTITY;
import com.infox.vpn4.pms2.model.PM_NODE;
import com.infox.vpn4.pms2.model.PM_STATPOINT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/24
 * Time: 10:12
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface PMServerAPI {
    public PM_DATA sendData(PM_DATA pm_data);

    public PM_NODE hello(PM_NODE node);


//    public HashMap<String,Long> queryStatPointIds(List<String> paramsCodes, int entityType, List<String> entityDns);
    public List<String> queryStatePointKeys(List<String> paramsCodes, int entityType, List<String> entityDns);

    public Map<String, List<PM_DATA>> queryPMDATA(PMQuery query) throws Exception;



//    public QueryResult queryPmParas(QueryCondition queryCondition);
//
//    public PmParaData createPmPara(PmParaData pmParaData);
}
