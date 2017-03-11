package com.infox.vpn4.pms2.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/18
 * Time: 15:14
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
public class PM_DATA extends DBObject{



    private Long statPointId;

    @Temporal (TemporalType.TIMESTAMP)
    private Date updateDate;
    @Temporal (TemporalType.TIMESTAMP)
    private Date timePoint;
    private Float value;
    private String displayValue;



    private Integer status;
    private String source;

    @Transient
    private Object object;

    @Transient
    private HashMap dataMap = new HashMap() ;

    @Transient
    private List<PM_ENTITY> bindEntities = new ArrayList<>();

    @Transient
    private PM_PARAMS pmParams = null;

    @Transient
    private PM_DATA parentPmData = null;

    public PM_DATA getParentPmData() {
        return parentPmData;
    }

    public void setParentPmData(PM_DATA parentPmData) {
        this.parentPmData = parentPmData;
    }

    public void setPmParams(PM_PARAMS pmParams) {
        this.pmParams = pmParams;
    }

    public PM_PARAMS getPmParams() {
        return pmParams;
    }

    public PM_ENTITY bindEntity(PM_ENTITY entity) {
        bindEntities.add(entity);
        return entity;
    }

    public List<PM_ENTITY> getBindEntities() {
        return bindEntities;
    }
    public void setBindEntities(List<PM_ENTITY> _entities) {
        this.bindEntities = _entities;
    }

    public Long getStatPointId() {
        return statPointId;
    }

    public void setStatPointId(Long statPointId) {
        this.statPointId = statPointId;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public HashMap getDataMap() {
        if (dataMap == null) dataMap = new HashMap();
        return dataMap;
    }

    public void setDataMap(HashMap dataMap) {
        this.dataMap = dataMap;
    }

    public Date getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(Date timePoint) {
        this.timePoint = timePoint;
    }



    @Override
    public String toString() {
        return "PM_DATA{" +
                "statPointId=" + statPointId +
                ", updateDate=" + updateDate +
                ", timePoint=" + timePoint +
                ", value=" + value +
                ", displayValue='" + displayValue + '\'' +
                ", status=" + status +
                ", source='" + source + '\'' +
                ", object=" + object +
                ", dataMap=" + dataMap +
                ", bindEntities=" + bindEntities +
                '}';
    }
}
