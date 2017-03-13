package com.asb.pms.model;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/29
 * Time: 10:20
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PM_EVENT extends DBObject implements DN{

    private Integer severity;
    private String dn;
    private String title;
    private String description;
    private String entityDn;
    private Long nodeId;
    private Integer state;

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Temporal (TemporalType.TIMESTAMP)
    private Date timepoint;

    public Date getTimepoint() {
        return timepoint;
    }

    public void setTimepoint(Date timepoint) {
        this.timepoint = timepoint;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEntityDn() {
        return entityDn;
    }

    public void setEntityDn(String entityDn) {
        this.entityDn = entityDn;
    }
}
