package com.infox.vpn4.pms2.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;

/**
 * Author: Ronnie.Chen
 * Date: 2016/12/2
 * Time: 14:18
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
public class PM_SYSTEM extends DBObject implements DN {
    private String variable;
    private String category;
    private String value;
    private String dn;

    @Override
    public String getDn() {
        return dn;
    }

    @Override
    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
