package infox.vpn4.modules.oslog;

import infox.vpn4.util.JdbcTemplateUtil;
import infox.vpn4.valueobject.FAlarmItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.activation.DataSource;
import java.util.HashMap;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:17
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class AlarmItemMgr {
    private Logger logger = LoggerFactory.getLogger(AlarmItemMgr.class);
    private HashMap<Long,FAlarmItem> idmap = new HashMap<>();
    private HashMap<String,FAlarmItem> dnmap = new HashMap<>();
    private DataSource dataSource = null;
    private JdbcTemplate jdbcTemplate = null;

    public FAlarmItem add(FAlarmItem item) {
        FAlarmItem itemCopy = item;
        item = dnmap.get(item.getDn());
        if (item == null) {
            item = (FAlarmItem) JdbcTemplateUtil.insert(jdbcTemplate, "FAlarmItem", item);
            idmap.put(item.getId(),item);
            dnmap.put(item.getDn(),item);
        } else {
            item.setOccurCount(item.getOccurCount()+itemCopy.getOccurCount());
        }
        return item;
    }

    public FAlarmItem getById(long id) {
        return idmap.get(id);
    }

    public FAlarmItem getByDn(String dn) {
        return dnmap.get(dn);
    }

}
