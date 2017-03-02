package infox.vpn4.valueobject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/2
 * Time: 10:35
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class FAlarmItemAsso extends BObject {
    private Long alarmItemAId;
    private Long alarmItemBId;
    private String alarmItemAName;
    private String alarmItemBName;
    private Integer count;

    public Long getAlarmItemAId() {
        return alarmItemAId;
    }

    public void setAlarmItemAId(Long alarmItemAId) {
        this.alarmItemAId = alarmItemAId;
    }

    public Long getAlarmItemBId() {
        return alarmItemBId;
    }

    public void setAlarmItemBId(Long alarmItemBId) {
        this.alarmItemBId = alarmItemBId;
    }

    public String getAlarmItemAName() {
        return alarmItemAName;
    }

    public void setAlarmItemAName(String alarmItemAName) {
        this.alarmItemAName = alarmItemAName;
    }

    public String getAlarmItemBName() {
        return alarmItemBName;
    }

    public void setAlarmItemBName(String alarmItemBName) {
        this.alarmItemBName = alarmItemBName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
