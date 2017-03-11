package com.infox.vpn4.pms2;

import com.infox.vpn4.pms2.model.PM_DATA;

/**
 * Author: Ronnie.Chen
 * Date: 2016/10/19
 * Time: 10:46
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface PM_DATA_Listener {
    public void pm_data_create(PM_DATA pm_data) throws Exception;
}
