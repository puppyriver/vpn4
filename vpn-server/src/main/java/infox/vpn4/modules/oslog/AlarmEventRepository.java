package infox.vpn4.modules.oslog;

import infox.vpn4.valueobject.FAlarmRecord;

import java.util.Date;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface AlarmEventRepository {
    public void insert(FAlarmRecord event);
    public List<FAlarmRecord> query(Date startTime, Date endTime) throws Exception ;
    //public List find(String day)
}
