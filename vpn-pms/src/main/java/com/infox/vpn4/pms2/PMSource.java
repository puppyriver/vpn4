package com.infox.vpn4.pms2;

import com.infox.vpn4.pms2.model.PM_DATA;

import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/18
 * Time: 18:48
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface PMSource {
    public String getName();

    public PM_DATA take() throws InterruptedException;

    public List<PM_DATA> takeList(int maxSize) throws InterruptedException;
}
