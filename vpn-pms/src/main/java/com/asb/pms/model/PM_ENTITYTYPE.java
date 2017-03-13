package com.asb.pms.model;

import javax.persistence.Entity;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/25
 * Time: 16:20
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
public class PM_ENTITYTYPE extends DBObject{
    private String name;
    private String code;
    private String extensiontable;
    private Long metaentitytype_id;
    private Long specialitytype_id;
    private String coretable;
    private String spacetable;
    private String icon;

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

    public String getExtensiontable() {
        return extensiontable;
    }

    public void setExtensiontable(String extensiontable) {
        this.extensiontable = extensiontable;
    }

    public Long getMetaentitytype_id() {
        return metaentitytype_id;
    }

    public void setMetaentitytype_id(Long metaentitytype_id) {
        this.metaentitytype_id = metaentitytype_id;
    }

    public Long getSpecialitytype_id() {
        return specialitytype_id;
    }

    public void setSpecialitytype_id(Long specialitytype_id) {
        this.specialitytype_id = specialitytype_id;
    }

    public String getCoretable() {
        return coretable;
    }

    public void setCoretable(String coretable) {
        this.coretable = coretable;
    }

    public String getSpacetable() {
        return spacetable;
    }

    public void setSpacetable(String spacetable) {
        this.spacetable = spacetable;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}

