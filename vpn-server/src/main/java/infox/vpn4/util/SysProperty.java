package infox.vpn4.util;

/**
 *
 * User: Ronnie.Chen
 * Date: 11-6-6
 * Time: 下午8:48
 *
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;


public class SysProperty {
     private static Log logger =  null;
    static {
        logger = LogFactory.getLog(SysProperty.class);
        load();
         isload = true;
    }


   private static boolean isload = false;
    public static void load() {
        if (!isload) {
            load("system.properties","utf-8");
            isload = true;
        }
    }
    public static Properties getProperties() {
        return System.getProperties();
    }
    public static Properties getProperties(String startWith) {
        Properties properties = System.getProperties();
        Properties properties1 = new Properties();
        Enumeration<Object> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith(startWith+".")) {
                properties1.setProperty(key,properties.getProperty(key));
            }

        }
        return properties1;
    }
    public static void load(String fileName) {
            load(fileName,"utf-8");


    }
    public static void load(String fileName, String charset) {
        Properties pro = new Properties();

        try {
            pro.load(new InputStreamReader(new FileInputStream(fileName),charset));
            System.out.println("Load Property File :"+new File(fileName).getAbsolutePath());
        } catch (Exception ex) {
            try {
                InputStream is =  SysProperty.class.getClassLoader().getResourceAsStream(fileName);

                pro.load(new InputStreamReader(is,charset));
                System.out.println("Load Property File :"+  SysProperty.class.getClassLoader().getResource(fileName));
            } catch (Exception ex1) {
                logger.error("FAILED TO LOAD PROPERTY FILE :"+fileName);
                //   ex1.printStackTrace();
            }
        }
        Enumeration keys = pro.keys();
        logger.info(" =============== LOADING FILE "+fileName+" =====================");
        while (keys.hasMoreElements()){
            String key = (String)keys.nextElement();
            logger.info(key+" == "+pro.getProperty(key));
            System.setProperty(key,pro.getProperty(key));
        }
        logger.info(" =============== FINISH LOADING FILE "+fileName+" =====================");


    }

    public static List listKeys(String suffix) {
        List list = new ArrayList();
        Enumeration keys = System.getProperties().keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (key.toLowerCase().startsWith(suffix.toLowerCase())) {
                list.add(key);
            }
        }
        return list;
    }
    public static int getInt(String key) {
        String str = System.getProperty(key);
        try {
            return Integer.parseInt(str.trim());
        } catch (Exception ex) {
            return -1;
        }
    }
    public static int getInt(String key, int temp) {
        String str = System.getProperty(key);
        try {
            return Integer.parseInt(str);
        } catch (Exception ex) {
            return temp;
        }
    }

    public static String getString(String key) {
        return System.getProperty(key);
    }
    public static String getString(String key, String temp) {
        String t =  System.getProperty(key);

        if (t == null) {
          //  logger.error("PROPERTY:"+key+" NOT FOUND, USE DEFAULT VALUE :"+temp);
            return temp;
        }
        return t;
    }

    public static long getLong(String key) {
        String str = System.getProperty(key);
        try {
            return Long.parseLong(str);
        } catch (Exception ex) {
            return -1;
        }
    }
    public static long getLong(String key, long _dft) {
        String str = System.getProperty(key);
        try {
            return Long.parseLong(str);
        } catch (Exception ex) {
            return _dft;
        }
    }
    public static void main(String[] args){
      //  com.alcatelsbell.nms.util.SysProperty.load();

    }
}
