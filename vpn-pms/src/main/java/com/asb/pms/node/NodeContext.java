package com.asb.pms.node;



import com.asb.pms.Configuration;
import com.asb.pms.PMServerUtil;
import com.asb.pms.api.PmClient;
import com.asb.pms.model.PM_EVENT;
import com.asb.pms.model.PM_NODE;
import com.asb.pms.util.JdbcTemplateUtil;
import com.asb.pms.util.SysProperty;
import com.asb.pms.util.SysUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/23
 * Time: 16:39
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class NodeContext {
    private Log logger = LogFactory.getLog(getClass());

    private static NodeContext inst = new NodeContext();

    public static NodeContext getInstance() {
        return inst;
    }

    private NodeContext() {
        init();
    }

    private void init() {
       Runnable r = new Runnable() {
           @Override
           public void run() {
               while (true) {
                   try {
                       PM_NODE node = new PM_NODE();
                       node.setDn(getNodeIdentifier());
                       node.setDescription("");
                       node.setHost(SysUtil.getHostName());
                       node.setWorkPath(new File("").getAbsolutePath());
                       node.setVersion("0.1");
                       node.setName(getNodeName());
                       PmClient.getInstance().hello(node);
                       Thread.sleep(60000l * 10l);
                   } catch (Throwable throwable) {
                       logger.error(throwable, throwable);
                   }
               }
           }
       };

        new Thread(r).start();
    }

    public String getNodeIdentifier() {
        return SysProperty.getString("node", SysUtil.getHostName()+"/"+new File("").getAbsolutePath());
    }

    public String getNodeName() {
        return SysProperty.getString("name", SysUtil.getHostName()+"/"+new File("").getAbsolutePath());
    }

    public static void main(String[] args) {
//        for (int i = 0; i < 10; i++) {
//            PM_NODE node = new PM_NODE();
//            node.setDn("node"+i);
//            node.setDescription("");
//            node.setHost(SysUtil.getHostName());
//            node.setWorkPath(new File("").getAbsolutePath());
//            node.setVersion("0.1");
//            node.setName("采集节点:"+i);
//            JdbcTemplate jdbcTemplate = Configuration.getJdbcTemplate();
//            JdbcTemplateUtil.insert(jdbcTemplate, PMServerUtil.getTableName(node.getClass()),node);
//        }

        for (int i = 0; i < 100; i++) {
            PM_EVENT event = new PM_EVENT();
            event.setNodeId(1l);
            event.setDescription("描述描述描述描述");
            event.setTitle("标题标题");
            event.setSeverity(1);
            event.setDn(i+"");
            JdbcTemplate jdbcTemplate = Configuration.getJdbcTemplate();
            JdbcTemplateUtil.insert(jdbcTemplate, PMServerUtil.getTableName(event.getClass()),event);

        }
    }
}
