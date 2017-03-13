package com.asb.pms.util;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: Ronnie.Chen
 * Date: 11-5-23
 * Time:
 */
public class SysUtil {
    public static String DN_SEPERATOR = "<>";
    public static String DN_BLANK = "__";
    public static String createSourceObjectName(String emsname,String mename,String rackname,
                                                String shelfname,String cardname,String portname){
        return checkNull(emsname)+DN_SEPERATOR+checkNull(mename)+DN_SEPERATOR+
                checkNull(rackname)+DN_SEPERATOR+checkNull(shelfname)+DN_SEPERATOR+checkNull(cardname)
                +DN_SEPERATOR+checkNull(portname);

    }
    public static String checkNull(String field){
        return field == null ? DN_BLANK : field;
    }


    private static AtomicLong atomicLong = new AtomicLong(System.currentTimeMillis());

    /**
     *  please use nextLongUUId instead !
     * @return
     */
    public static long nextLongId() {
        return atomicLong.incrementAndGet();
    }


    /**
     * Generate global unique long-type uuid,
     * please make sure entity:MetaConfig is mapped for JPA
     * @return
     */








    public static String nextStringId() {
        return nextLongId()+"";
    }
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    public static String getDayString(Date date) {
        return sdf.format(date);
    }

    public static String idsToString(long[] ids) {
        if (ids == null || ids.length == 0) return "";
        StringBuffer sb = new StringBuffer(",");
        for (long id : ids) 
            sb.append(id).append(",");
        return sb.toString();
    }
    public static String idsToString(List<Long> ids) {
        if (ids == null || ids.size() == 0) return "";
        StringBuffer sb = new StringBuffer(",");
        for (long id : ids)
            sb.append(id).append(",");
        return sb.toString();
    }
    public static List<Long> stringToIds(String s) {
        StringTokenizer st = new StringTokenizer(s,",");
        List<Long> idList = new ArrayList<Long>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            long id = Long.valueOf(token);
            idList.add(id);
        }
        return idList;

    }
    
    public static synchronized String nextDN() {
        return UUID.randomUUID().toString();
    }

    private static HashMap<String,AtomicLong> dnSeqMap = new HashMap<String, AtomicLong>();
    private static Date now = new Date();
    private static String nowTime = null;
    private static String timeOfDay = getTimeOfDay();
    private static ReentrantLock dnSeqMapLock = new ReentrantLock();

    public static synchronized String nextDaySequenceDN(String object) {
        if (object == null) object = "null";
        AtomicLong seq = dnSeqMap.get(object);
        if (seq == null) {
            dnSeqMapLock.lock();
            seq = dnSeqMap.get(object);
            if (seq == null) {
                seq = new AtomicLong(0);
                dnSeqMap.put(object,seq);
            }
            dnSeqMapLock.unlock();
        }

        now.setTime(System.currentTimeMillis());
        String format = sdf.format(now);
        if (!format.equals(nowTime)) {
            dnSeqMapLock.lock();
            seq.set(0);
            nowTime = format;
            timeOfDay = getTimeOfDay();
            dnSeqMapLock.unlock();

        }
       //  return new StringBuilder().append(object).append("_").append(format).append("_").append(seq.incrementAndGet()).toString();
       return object+"_"+format+"_"+timeOfDay+"_"+seq.incrementAndGet();

    }

    private static String getTimeOfDay() {
        Calendar ca = Calendar.getInstance();
        int hour = ca.get(Calendar.HOUR_OF_DAY);
        int min = ca.get(Calendar.MINUTE);
        int sec = ca.get(Calendar.SECOND);
        return (hour+":"+min+":"+sec);
    }

    public static String propertiesToString(Map pro) {
        StringBuffer sb = new StringBuffer();
        Iterator keys = pro.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            String value = pro.get(key).toString();
            sb.append(key).append("=").append(value).append(";");
        }
        return sb.toString();

    }
    // abc=aa;cde=ff;123=sdf
    public static Properties stringToProperties(String str) {
        Properties pro = new Properties();
        try {
        String[] vs = str.split(";");
            if (vs != null) {
                 for (String s : vs) {
                     String key = s.substring(0,s.indexOf("="));
                     String value = s.substring(s.indexOf("=")+1);
                     if (key != null && value != null) {
                         pro.put(key.trim(),value.trim());
                     }
                 }
            }
        }catch (Exception e) {

        }

        return pro;
    }
        
    

    
     public synchronized static int getFreePort(){//获取空闲端口
        ServerSocket tmp;
        int i=20000;
        for(;i<=30000;i++){
            try{
                tmp=new ServerSocket(i);
                tmp.close();
                tmp=null;
                Thread.sleep(5000);
                return i;
            }
            catch(Exception e4){
            //    System.out.println("端口"+i+"已经被占用");
            }
        }
        return -1;
    }
    public static void main(String[] args) throws Exception, InterruptedException {
        System.out.println(getCurrentStackInfo());
         List l = (getMACAddress());
        System.out.println(l.size());


    }

    public static String getHostName() {
        String host = SysProperty.getString("java.rmi.server.hostname");
        if (host == null) host = SysProperty.getString("hostname");
        if (host == null) try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (host != null)
           return host;
        return "localhost";
    }

    public static String getRandmonVerifyCode(int verifyCode_len){
        char[] c = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        int maxNum = 10;
        int count = 0;//记录验证码长度

        StringBuffer verifyCodeStr = new StringBuffer();
        Random random = new Random();
        while(count < verifyCode_len){
            int i = random.nextInt(maxNum);
            if(i >= 0 && i < c.length){
                verifyCodeStr.append(String.valueOf(i));
                count++;
            }
        }
        return verifyCodeStr.toString();
    }


    public static ArrayList<String> getMACAddress()  throws Exception
    {
        ArrayList localArrayList = new ArrayList();
        String str1 = "";
        String str4 = System.getProperty("os.name");
        String str2;
        Process localProcess;
        BufferedReader localBufferedReader;
        String str3;
        if (str4.startsWith("Windows"))
        {
            try
            {
                str2 = "cmd.exe /c ipconfig /all";
                localProcess = Runtime.getRuntime().exec(str2);
                localBufferedReader = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
                while ((str3 = localBufferedReader.readLine()) != null)
                {
                    if (str3.indexOf("Physical Address") <= 0 && str3.indexOf("物理地址") <= 0)
                        continue;
                    int i = str3.indexOf(":");
                    i += 2;
                    str1 = str3.substring(i);
                    localArrayList.add(str1.trim());
                }
                localBufferedReader.close();
            }
            catch (IOException localIOException1)
            {
                localIOException1.printStackTrace();
            }
        }
        else if (str4.startsWith("SunOS"))
        {
            str2 = "/usr/sbin/ifconfig -a";
            try
            {
                localProcess = Runtime.getRuntime().exec(str2);
                localBufferedReader = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
                while ((str3 = localBufferedReader.readLine()) != null)
                {
                    if (str3.indexOf("ether") <= 0)
                        continue;
                    int j = str3.indexOf("ether") + "ether".length();
                    str1 = str3.substring(j);
                    localArrayList.add(str1.trim());
                }
                localBufferedReader.close();
            }
            catch (IOException localIOException2)
            {
                localIOException2.printStackTrace();
            }
        }
        return localArrayList;
    }


    public static String getCurrentStackInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();
        if (stackTrace.length > 2) {
            for (int i = 2; i < stackTrace.length; i++) {
                StackTraceElement stackTraceElement = stackTrace[i];
                 sb.append(stackTraceElement.toString()+"\n");
            }
        }

        return sb.toString();
    }









}
