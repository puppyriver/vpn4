package com.infox.vpn4.pms2.processor;

import com.infox.vpn4.pms2.Configuration;
import com.infox.vpn4.pms2.Context;
import com.infox.vpn4.pms2.H2PMCache;
import com.infox.vpn4.pms2.model.PM_DATA;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/28
 * Time: 15:28
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ProcessorTemplate {
    protected Log logger = LogFactory.getLog(getClass());

    protected JdbcTemplate jdbcPm = Configuration.getJdbcTemplate();

    // 包含缓存的性能数据，缓存的几张主要的表
    protected JdbcTemplate jdbcCache = ((H2PMCache)Context.getInstance().getServer().getPmCache()).getJdbcTemplate();


    protected List<PM_DATA> newDatas = new ArrayList<>();

    protected PM_DATA parentData;

    protected PM_DATA childData = new PM_DATA();


    public void process() {



    }

    public static void main(String[] args) {

    }
}
