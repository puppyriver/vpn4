package infox.vpn4.valueobject;


import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:42
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Entity
public class FAlarmRecord {
    private Long id;
    private String alarmItemDn;

    @Temporal(TemporalType.DATE)
    private Date alarmTime;

    private Integer timeWindowInSecond;
    //private String timeString;
    private Integer occurCount;
    private Integer severity;


    public FAlarmRecord() {
        
    }

    public FAlarmRecord(String alarmItemDn, Date alarmTime, Integer timeWindowInSecond,Integer severity, Integer occurCount) {
        this.alarmItemDn = alarmItemDn;
        this.alarmTime = alarmTime;
        this.timeWindowInSecond = timeWindowInSecond;
        this.occurCount = occurCount;
        this.severity = severity;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlarmItemDn() {
        return alarmItemDn;
    }

    public void setAlarmItemDn(String alarmItemDn) {
        this.alarmItemDn = alarmItemDn;
    }

    public Date getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    public Integer getTimeWindowInSecond() {
        return timeWindowInSecond;
    }

    public void setTimeWindowInSecond(Integer timeWindowInSecond) {
        this.timeWindowInSecond = timeWindowInSecond;
    }

    public Integer getOccurCount() {
        return occurCount;
    }

    public void setOccurCount(Integer occurCount) {
        this.occurCount = occurCount;
    }
}
