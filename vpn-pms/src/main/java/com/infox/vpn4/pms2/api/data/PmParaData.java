package com.infox.vpn4.pms2.api.data;

import com.infox.vpn4.pms2.model.PM_PARAMS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/15
 * Time: 14:19
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PmParaData extends PM_PARAMS implements Serializable{
    public String processor_script = null;
    public Integer processor_type = null;
    public Long processor_scriptfileid = null;
    public String processor_scriptfilename = null;
    public String parentPmName = null;

    public PmParaData(PM_PARAMS pmParams) {
        if (pmParams != null)
            BeanUtils.copyProperties(pmParams,this);
    }

    public PmParaData() {
        this(null);
    }

    public PM_PARAMS convert() {
        PM_PARAMS pm_params = new PM_PARAMS();
        BeanUtils.copyProperties(this,pm_params);
        return pm_params;
    }
}
