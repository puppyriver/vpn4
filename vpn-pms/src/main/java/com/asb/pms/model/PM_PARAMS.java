package com.asb.pms.model;

import com.asb.pms.PMDictionary;

import javax.persistence.Entity;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/25
 * Time: 16:29
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
public class PM_PARAMS extends DBObject implements DN{
    private String name;
    private String code;
    private String memo;
    private String unit;
    private Long entityTypeId;
    private Long processorId;
    private Integer type;
    private Integer keepInCacheHours;
    private Long parentId;
    private Integer state = PMDictionary.PM_PARAMS_STATE.ON;

    public PM_PARAMS() {
    }

    public PM_PARAMS(String name, String code, String unit, String memo) {
        this.name = name;
        this.code = code;
        this.unit = unit;
        this.memo = memo;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getKeepInCacheHours() {
        return keepInCacheHours;
    }

    public void setKeepInCacheHours(Integer keepInCacheHours) {
        this.keepInCacheHours = keepInCacheHours;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(Long entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public Long getProcessorId() {
        return processorId;
    }

    public void setProcessorId(Long processorId) {
        this.processorId = processorId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public boolean equals (Object o) {
        return o instanceof PM_PARAMS ? ((PM_PARAMS) o).getId() == getId() : false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String getDn() {
        return code;
    }

    @Override
    public void setDn(String dn) {
        this.code = dn;
    }
}
