package infox.vpn4.modules.oslog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:45
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class AlarmEventRepositoryFactory {
    private Logger logger = LoggerFactory.getLogger(AlarmEventRepositoryFactory.class);

    public AlarmEventRepository createRepository() {
        return new AlarmEventRepositorySqliteImpl();
    }
}
