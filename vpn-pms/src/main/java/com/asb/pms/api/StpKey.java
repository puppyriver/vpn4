package com.asb.pms.api;

/**
 * Created by Administrator on 2016/12/6.
 */
public class StpKey {
    private String paramCode;
    private int entityType;
    private String entityDn;
    private long stpId;

    public String getParamCode() {
        return paramCode;
    }



    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public String getEntityDn() {
        return entityDn;
    }



    public long getStpId() {
        return stpId;
    }



    public StpKey(String paramCode, int entityType, String entityDn, long stpId) {
        this.paramCode = paramCode;
        this.entityType = entityType;
        this.entityDn = entityDn;
        this.stpId = stpId;
    }

    public static StpKey parse(String str){
        String[] split = str.split("##");
        if (split.length != 4) return new StpKey(null,-1,null,-1);
        return new StpKey(split[0],Integer.parseInt(split[1]),split[2],Long.parseLong(split[3]));
    }


    @Override
    public String toString() {
        return paramCode+"##"+entityType+"##"+entityDn+"##"+stpId;
    }


}
