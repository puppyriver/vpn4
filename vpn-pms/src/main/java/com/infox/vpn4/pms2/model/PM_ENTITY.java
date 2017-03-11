package com.infox.vpn4.pms2.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Transient;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/23
 * Time: 14:04
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PM_ENTITY extends DBObject implements DN{

    private String dn;
    private String name;
    private Long  entityTypeId;
    private Long refId;
    private Long refTable;
    private String parentEntityDn;


    @Transient
    private PM_ENTITY parentEntity;



    public PM_ENTITY() {
    }

    public PM_ENTITY(String dn, String name, Long entityTypeId, Long refId, Long refTable, String parentEntityDn) {
        this.dn = dn;
        this.name = name;
        this.entityTypeId = entityTypeId;
        this.refId = refId;
        this.refTable = refTable;
        this.parentEntityDn = parentEntityDn;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(Long entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public Long getRefId() {
        return refId;
    }

    public void setRefId(Long refId) {
        this.refId = refId;
    }

    public Long getRefTable() {
        return refTable;
    }

    public void setRefTable(Long refTable) {
        this.refTable = refTable;
    }

    public String getParentEntityDn() {
        return parentEntityDn;
    }

    public void setParentEntityDn(String parentEntityDn) {
        this.parentEntityDn = parentEntityDn;
    }

    public PM_ENTITY getParentEntity() {
        return parentEntity;
    }

    public PM_ENTITY setParentEntity(PM_ENTITY parentEntity) {
        if (parentEntity != null) {
            this.parentEntity = parentEntity;
            this.parentEntityDn = parentEntity.getDn();
        }
        return this.parentEntity;

    }
}
