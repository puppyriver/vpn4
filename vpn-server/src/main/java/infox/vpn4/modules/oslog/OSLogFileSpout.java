package infox.vpn4.modules.oslog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/3/2.
 */
public class OSLogFileSpout {


    public static final String LOG_START_REGEX_PATTEN = "\\[[0-9]{4}-[0-9]{1,2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2} INFO\\] SendTrap::sendTrap>>";


    private AlarmLogCollector collector = null;
    private BufferedReader bufferedReader = null;
    private LinkedBlockingQueue queue = new LinkedBlockingQueue();
    private StringBuffer xmlBuffer = new StringBuffer();
    public void open(File file,AlarmLogCollector collector) {
        this.collector = collector;
        bufferedReader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("<Ticle>")) {
                queue.offer(xmlBuffer.toString());
                xmlBuffer = new StringBuffer();
                xmlBuffer.append(line.substring(line.indexOf("<Ticle>")));
            }
        }
    }

    public void nextTuple() {

    }
    public static void main(String[] args) {
        String log = "[2017-02-26 00:00:01 INFO] SendTrap::sendTrap>>\tSend to OS:><?xml version='1.0' encoding='gb2312'?>";
        Pattern pattern = Pattern.compile(LOG_START_REGEX_PATTEN);
        Matcher matcher = pattern.matcher(log);
        if (matcher.find()) {
            System.out.println("matcher = " + matcher.groupCount());
            System.out.println("matcher = " + matcher.group(0));
        }
    }
}
