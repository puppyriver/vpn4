package infox.vpn4.modules.oslog;

import infox.vpn4.boot.services.JpaService;
import infox.vpn4.util.JdbcTemplateUtil;
import infox.vpn4.valueobject.FAlarmItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:17
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class AlarmItemMgr implements InitializingBean{
    private Logger logger = LoggerFactory.getLogger(AlarmItemMgr.class);
    private HashMap<Long,FAlarmItem> idmap = new HashMap<>();
    private HashMap<String,FAlarmItem> dnmap = new HashMap<>();

    @Autowired
    private JdbcTemplate jdbcTemplate = null;

    @Autowired
    private JpaService jpaService = null;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        initCache();
    }

    private void initCache() {
        List<FAlarmItem> list = null;
        try {
            list = JdbcTemplateUtil.queryForList(jdbcTemplate, FAlarmItem.class, "SELECT * FROM FAlarmItem");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (list != null)
            list.forEach(item-> {
                idmap.put(item.getId(),item);
                dnmap.put(item.getDn(),item);
            });
        logger.info("InitCache AlarmItemMgr size = {}",list == null ? null : list.size());
    }


    public FAlarmItem add(FAlarmItem item) {
        FAlarmItem itemCopy = item;
        item = dnmap.get(item.getDn());
        if (item == null) {
            item = (FAlarmItem) JdbcTemplateUtil.insert(jdbcTemplate, "FAlarmItem", itemCopy);
            idmap.put(item.getId(),item);
            dnmap.put(item.getDn(),item);
        } else {
            item.setOccurCount(item.getOccurCount()+itemCopy.getOccurCount());
            item.setUserObject("update");
        }
        return item;
    }

    int update = 0;
    @Transactional
    public void flush() {
        idmap.values().stream()
                .filter(item -> "update".equals(item.getUserObject()))
                .forEach(item-> {
                    jdbcTemplate.update("UPDATE FAlarmItem set occurCount = ? where id = ?",item.getOccurCount(),item.getId());
                    item.setUserObject(null);
                    update ++;
                });

        if (update > 0)
            logger.info("AlarmAssoMgr flushed ,update size = {} ",update);
        update = 0;
              //  .collect(Collectors.toList());
    }

    public FAlarmItem getById(long id) {
        return idmap.get(id);
    }

    public FAlarmItem getByDn(String dn) {
        return dnmap.get(dn);
    }


}
