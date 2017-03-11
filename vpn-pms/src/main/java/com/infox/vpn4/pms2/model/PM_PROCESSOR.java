package com.infox.vpn4.pms2.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Entity;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/25
 * Time: 16:31
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
public class PM_PROCESSOR extends DBObject{
    private String name;
    private String memo;
    private String objectName;
    private Integer type;
    private String script;
    private Long scriptFileId;

    public Long getScriptFileId() {
        return scriptFileId;
    }

    public void setScriptFileId(Long scriptFileId) {
        this.scriptFileId = scriptFileId;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
