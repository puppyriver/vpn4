package infox.vpn4.modules.oslog;

import infox.vpn4.valueobject.FAlarmRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:46
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class AlarmEventRepositorySqliteImpl implements AlarmEventRepository {
    private Logger logger = LoggerFactory.getLogger(AlarmEventRepositorySqliteImpl.class);

    @Override
    public void insert(FAlarmRecord event) {
        
    }
}
