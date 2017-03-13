package com.asb.pms.model;

import javax.persistence.Entity;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/25
 * Time: 16:32
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
public class PM_PROCESSOR_INS extends DBObject {
    private String name;
    private String memo;
    private String type;
    private String script;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
