package infox.vpn4.modules.oslog;

//import infox.vpn4.boot.services.JpaService;
import infox.vpn4.util.JdbcTemplateUtil;
import infox.vpn4.valueobject.FAlarmItemAsso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataSource;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/4
 * Time: 9:32
 * rongrong.chen@alcatel-sbell.com.cn
 */


public class AlarmAssoMgr implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(AlarmItemMgr.class);
//    private HashMap<Long,FAlarmItemAsso> idmap = new HashMap<>();
    private HashMap<String,FAlarmItemAsso> dnmap = new HashMap<>();

    @Autowired
    private JdbcTemplate jdbcTemplate = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        initCache();
    }

    private void initCache() {
        List<FAlarmItemAsso> list = null;
        try {
            list = JdbcTemplateUtil.queryForList(jdbcTemplate, FAlarmItemAsso.class, "SELECT * FROM FAlarmItemAsso");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (list != null)
            list.forEach(item-> {
    //            idmap.put(item.getId(),item);
                dnmap.put(item.getDn(),item);
            });
        logger.info("InitCache FAlarmItemAssoMgr size = {}",list == null ? null : list.size());
    }


    public FAlarmItemAsso updateAlarmAsso(FAlarmItemAsso item) {
        item.setDn(item.getAlarmItemAId()+"::"+item.getAlarmItemBId());
        FAlarmItemAsso itemCopy = item;
        item = getByDn(item.getDn());
        if (item == null) {

            
  //     item = (FAlarmItemAsso) JdbcTemplateUtil.insert(jdbcTemplate, "FAlarmItemAsso", itemCopy);
//            idmap.put(item.getId(),item);
            item = itemCopy;


            dnmap.put(item.getDn(),item);
        } else {
            item.setCount(item.getCount()+itemCopy.getCount());
            item.setUserObject("update");
        }
        return item;
    }

//    public FAlarmItemAsso getById(long id) {
//        return idmap.get(id);
//    }

    public FAlarmItemAsso getByDn(String dn) {
        FAlarmItemAsso asso = dnmap.get(dn) ;
        if (asso == null) {
            String[] split = dn.split("::");
            asso = dnmap.get(split[1]+"::"+split[0]);
        }
        return asso;
    }
    int update = 0;
    @Transactional
    public void flush() {

        dnmap.values().stream()
                .filter(item -> "update".equals(item.getUserObject()))
                .forEach(item-> {
                    update ++;
                    if (item.getId() != null)
                        jdbcTemplate.update("UPDATE FAlarmItemAsso set count = ? where id = ?",item.getCount(),item.getId());
                    else {
                        FAlarmItemAsso insert = (FAlarmItemAsso)JdbcTemplateUtil.insert(jdbcTemplate, "FAlarmItemAsso", item);
                        item.setId(insert.getId());
                    }
                    item.setUserObject(null);
                });
        if (update > 0)
            logger.info("AlarmAssoMgr flushed ,update size = {} ",update);
        update = 0;
        //  .collect(Collectors.toList());
    }


}
