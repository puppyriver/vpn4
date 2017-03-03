package infox.vpn4.modules.oslog;

import infox.vpn4.valueobject.FAlarmRecord;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:44
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface AlarmEventRepository {
    public void insert(FAlarmRecord event);
    //public List find(String day)
}
