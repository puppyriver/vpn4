package infox.vpn4.modules.oslog;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import infox.vpn4.util.FileUtil;
import infox.vpn4.util.SysProperty;
import infox.vpn4.util.ZipUtil;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private List<File> scanDir(File dir) {
        List fileList = new ArrayList();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory())
                fileList.addAll(scanDir(file));
            else {
                if (checkMD5(file)) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    private boolean checkMD5(File file) {
        if (file.getName().endsWith(".md5"))
            return false;
        String md5Name = file.getName()+".md5";
        File md5File = new File(file.getParent(),md5Name);
        if (!md5File.exists())
            return true;

        List<String> contents = null;
        String md5Checksum = null;
        try {
            contents = Files.readLines(md5File, Charsets.UTF_8);
            md5Checksum = FileUtil.getMD5Checksum(file.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return contents.contains(md5Checksum);
        //return true;
    }

    private void scan() {
        File dir = new File(SysProperty.getString("oslog.dir","oslog"));
        if (!dir.exists()) dir.mkdirs();
       List<File> files = scanDir(dir);
        if (files.isEmpty()) {
            alarmItemMgr().flush();
            alarmAssoMgr().flush();
        //    logger.info("AlarmItemMgr,alarmAssoMgr flushed !");
        }
        if (files.size() > 0 ) {
            logger.info("scan dir : {},length = {}",dir.getAbsolutePath(),files.size());
        }
        long tt1 = System.currentTimeMillis();
        for (File file : files) {
            logger.info("####### Processing file : {}",file.getName());
            long t1 = System.currentTimeMillis();
            try {
//                new OSLogFileSpout().load(file, osTupleCollector());
                if (file.getName().endsWith(".zip")) {

                    File temp = new File("tmp");
                    if (!temp.exists()) temp.mkdir();
                    File zipDir = new File(temp,file.getName()+".unzip");
                    zipDir.mkdir();

                    try {
                        ZipUtil.decompress(file,zipDir);
                        File[] unzipFiles = zipDir.listFiles();
                        logger.info(" unzip file list = {}"+unzipFiles.length);
                        for (File unzipFile : unzipFiles) {
                            try {
                                logger.info(" ------- Processing unzip file {}",unzipFile.getName());
                                new OSLogFileFastSpout().load(unzipFile, osTupleCollector());
                            } catch (Throwable e) {
                                logger.error(e.getMessage(), e);
                            }
                            unzipFile.delete();
                        }
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                    zipDir.delete();
                } else
                    new OSLogFileFastSpout().load(file, osTupleCollector());
            } catch (Throwable e) {
                logger.error(e.getMessage(),e);
            } finally {
                file.delete();
            }
            logger.info("####### Processing file : {} finished ,spend time : {} ms",file.getName(),System.currentTimeMillis() - t1);


        }
        long tt2 = System.currentTimeMillis();
        logger.info("Scan finish ,spend : {} ms",(tt2-tt1));
    }
}
