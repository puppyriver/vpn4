package infox.vpn4.modules.oslog;

import infox.vpn4.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 10:08
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class OSTuple {
    private String vendorName;
    private String alarmName;
    private String domainName;
    private Date alarmTime;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getItemKey() {
        return vendorName+"::"+domainName+"::"+alarmName;
    }

    public static OSTuple parseKey(String key) {
        OSTuple osTuple = new OSTuple();
        String[] split = key.split("::");
        if (split.length == 3) {
            osTuple.setVendorName(split[0]);
            osTuple.setDomainName(split[1]);
            osTuple.setAlarmName(split[2]);
        }
        return osTuple;
    }

    public Date getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    public void setAlarmTime(String alarmTime) throws ParseException {
       this.alarmTime = DateUtil.parse(alarmTime,"yyyy-MM-dd HH:mm:ss");
    }
    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public boolean valid() {
        return ("1".equals(type)) && domainName != null && vendorName != null && alarmName != null && alarmTime != null;
    }
}
