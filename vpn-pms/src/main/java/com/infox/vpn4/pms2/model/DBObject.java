package com.infox.vpn4.pms2.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/25
 * Time: 15:13
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DBObject implements Serializable {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
