package com.infox.vpn4.pms2.model;


import javax.persistence.Entity;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/1
 * Time: 21:40
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
public class PM_JOB extends DBObject{
    private Long pmParamsId;
    private String cronExp;
    private String script;

    public Long getPmParamsId() {
        return pmParamsId;
    }

    public void setPmParamsId(Long pmParamsId) {
        this.pmParamsId = pmParamsId;
    }

    public String getCronExp() {
        return cronExp;
    }

    public void setCronExp(String cronExp) {
        this.cronExp = cronExp;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
