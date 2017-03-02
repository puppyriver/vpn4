package infox.vpn4.valueobject;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/2
 * Time: 10:34
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class FAlarmItem extends BObject {
    private String vendorDn;
    private String domainCode;
    private String name;
    private Integer occurCount;

    public String getVendorDn() {
        return vendorDn;
    }

    public void setVendorDn(String vendorDn) {
        this.vendorDn = vendorDn;
    }

    public String getDomainCode() {
        return domainCode;
    }

    public void setDomainCode(String domainCode) {
        this.domainCode = domainCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOccurCount() {
        return occurCount;
    }

    public void setOccurCount(Integer occurCount) {
        this.occurCount = occurCount;
    }
}
