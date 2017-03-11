package com.infox.vpn4.pms2.web.controller;

import com.infox.vpn4.pms2.model.PM_DATA;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.mvc.Controller;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/23
 * Time: 19:46
 * rongrong.chen@alcatel-sbell.com.cn
 */
//@RestController
public class RESTController   {
    private Log logger = LogFactory.getLog(getClass());

    @RequestMapping("/rest/{player}")
    public PM_DATA message(@PathVariable String player) {

        PM_DATA msg = new PM_DATA();
        msg.setId(111l);
        msg.setValue(123f);
        return msg;
    }
}
