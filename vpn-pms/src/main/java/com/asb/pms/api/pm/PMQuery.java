package com.asb.pms.api.pm;

import com.asb.pms.api.StpKey;
import com.asb.pms.model.PM_DATA;
import com.asb.pms.util.JdbcTemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Ronnie.Chen
 * Date: 2016/11/30
 * Time: 13:59
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PMQuery implements Serializable{
    private Logger logger = LoggerFactory.getLogger(PMQuery.class);
    public static final int QUERY_MODE_CACHE_ONLY = 0x001;
    public static final int QUERY_MODE_CACHE_AND_DB = 0x011;
    public static final int QUERY_MODE_ALL = 0x111;



    public Date startTime;
    public Date endTime;

    public int queryMode = QUERY_MODE_ALL;

    private HashMap attributes = new HashMap();


    public List<String> stpKeys = new ArrayList<>();


    public PMQuery copy() {
        return new PMQuery(stpKeys,startTime,endTime,queryMode);
    }

    public PMQuery() {
    }

    public PMQuery(List<String> stpKeys, Date startTime, Date endTime) {
        this.stpKeys = stpKeys;
        this.endTime = endTime;
        this.startTime = startTime;
    }

    public PMQuery(List<String> stpKeys, Date startTime,Date endTime,int queryMode) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.queryMode = queryMode;
        this.stpKeys = stpKeys;
    }

    public Map<String, List<PM_DATA>> query(JdbcTemplate jdbcTemplate) throws Exception {

        List paras = new ArrayList();
        String sql = "SELECT * FROM PM_DATA where 1=1 ";

        if (stpKeys != null && stpKeys.size() > 0) {
            sql += " and statPointId in  ("+PMQueryUtil.toEntityIdInString(stpKeys)+") ";
        }

        if (startTime != null) {
            sql += " and timePoint >  ? ";
            paras.add(startTime);
        }
        if (endTime != null) {
            sql += " and timePoint <  ? ";
            paras.add(endTime);
        }

        sql+= " order by timePoint ";

        HashMap<Long,String> stpKeyMap = new HashMap<>();
        for (String stpKey : stpKeys) {
            stpKeyMap.put(StpKey.parse(stpKey).getStpId(),stpKey);
        }


        List<PM_DATA> list = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class,sql,paras.toArray());
        logger.debug("stpkey= {}",stpKeyMap);
        logger.debug("sql = {} ,result = {}",sql,list == null ? "null" : list.size());

        return  list.stream().collect(Collectors.groupingBy(data->stpKeyMap.get(data.getStatPointId())));


    }

    public String getStpKey(long stpId) {
        HashMap<Long,String> stpKeyMap = new HashMap<>();
        for (String stpKey : stpKeys) {
            stpKeyMap.put(StpKey.parse(stpKey).getStpId(),stpKey);
        }
        return stpKeyMap.get(stpId);
    }

    public HashMap getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap attributes) {
        this.attributes = attributes;
    }
}
