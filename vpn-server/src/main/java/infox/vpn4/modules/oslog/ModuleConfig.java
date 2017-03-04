package infox.vpn4.modules.oslog;

import infox.vpn4.util.SysProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/4
 * Time: 11:02
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Configuration
public class ModuleConfig  implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(ModuleConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public AlarmAssoMgr alarmAssoMgr() {
        return new AlarmAssoMgr();
    }
//
    @Bean
    public AlarmItemMgr alarmItemMgr() {
        return new AlarmItemMgr();
    }

    @Bean
    public AlarmEventRepository alarmEventRepository() {
        return new AlarmEventRepositorySqliteImpl();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        start();
        logger.info("Module loaded : OSLOG");
        logger.info("alarmAssoMgr : {}, alarmItemMgr: {}",alarmAssoMgr(),alarmItemMgr());
    }

    @Bean
    public OSTupleCollector osTupleCollector() {
        return new OSTupleCollector(alarmItemMgr(),alarmAssoMgr(),alarmEventRepository());
    }


    private void start() {
        new Thread(()->{
            while (true)  {
                try {
                    Thread.sleep(10000l);
                    scan();
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }).start();
    }

    private void scan() {
        File dir = new File(SysProperty.getString("oslog.dir","oslog"));

        File[] files = dir.listFiles();
        if (files.length == 0) {
            alarmItemMgr().flush();
            alarmAssoMgr().flush();
        //    logger.info("AlarmItemMgr,alarmAssoMgr flushed !");
        }
        if (files.length > 0 ) {
            logger.info("scan dir : {},length = {}",dir.getAbsolutePath(),files.length);
        }
        for (File file : files) {
            logger.info("####### Processing file : {}",file.getName());
            long t1 = System.currentTimeMillis();
            try {
                new OSLogFileSpout().load(file, osTupleCollector());
            } catch (Throwable e) {
                logger.error(e.getMessage(),e);
            } finally {
                file.delete();
            }
            logger.info("####### Processing file : {} finished ,spend time : {} ms",file.getName(),System.currentTimeMillis() - t1);


        }
    }
}
