package infox.vpn4.modules.oslog;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/3/2.
 */
public class OSLogFileSpout {


    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final String LOG_START_REGEX_PATTEN = "\\[[0-9]{4}-[0-9]{1,2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2} INFO\\] SendTrap::sendTrap>>";


    private OSTupleCollector collector = null;
    private BufferedReader bufferedReader = null;
 //   private LinkedBlockingQueue queue = new LinkedBlockingQueue();
    private StringBuffer xmlBuffer = new StringBuffer();
    public void load(File file,OSTupleCollector collector) throws IOException {
        this.collector = collector;
        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"gb2312"));
        try {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("<Ticle>")) {
                    xmlBuffer = new StringBuffer();
                    xmlBuffer.append(line.substring(line.indexOf("<Ticle>")));
                } else if (line.contains("</Ticle>")) {
                    xmlBuffer.append(line.substring(0, line.indexOf("</Ticle>") + "</Ticle>".length()));
                    collector.offer(parse(xmlBuffer.toString()));

                } else
                    xmlBuffer.append(line);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        } finally {
            bufferedReader.close();
        }


    }

    private OSTuple parse(String xml) {

        String START = "<param name=\"ExtendedAttr\" value=\"";
//        int i1 = xml.indexOf(START);
//        int i2 = xml.indexOf("/>",i1);
//        if (i1 > 0 && i2 > 0) {
//            String value = xml.substring(i1+START.length(),i2);
//            value = value.substring(0,value.lastIndexOf("\""));
//            value = "<![CDATA[" + value +"]]>";
//            xml = xml.substring(0,i1+START.length())+value+"\""+xml.substring(i2);
//        }
        SAXReader saxReader = new SAXReader();
        Document doc = null;
        try {
            doc = saxReader.read(new ByteArrayInputStream(xml.getBytes()));
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        Element rootElement = doc.getRootElement();
        List<Element> params = rootElement.elements("param");
        HashMap<String, Element> elementMap = params.stream().collect(() -> new HashMap<String, Element>(),
                (map, ele) -> map.put(ele.attribute("name").getValue(), ele),
                (map1, map2) -> map1.putAll(map2));
        
                
        OSTuple osTuple = new OSTuple();

        osTuple.setVendorName(elementMap.get("ASB_EQUIP_Manufacturer").attribute("value").getValue());
        setParamValue(elementMap,osTuple,"ASB_EQUIP_Manufacturer",(tuple,v)->tuple.setVendorName(v));
        setParamValue(elementMap,osTuple,"X733SpecificProb",(tuple,v)->tuple.setAlarmName(v));
        setParamValue(elementMap,osTuple,"ASB_Tech_Domain",(tuple,v)->tuple.setDomainName(v));
        setParamValue(elementMap,osTuple,"ASB_EmsEventTime", (tuple, v) -> {
            try {
                tuple.setAlarmTime(v);
            } catch (ParseException e) {
                logger.error("Failed to parse ASB_EmsEventTime : "+v);
            }
        });
        return osTuple;

    }

    private void setParamValue(HashMap<String, Element> elementMap ,OSTuple osTuple,String paramName,
                               BiConsumer<OSTuple,String> setter) {
        Element element = elementMap.get(paramName);
        if (element != null) {
            Attribute value = element.attribute("value");
            if (value != null)
                setter.accept(osTuple,value.getValue());
        }
    }

    public void nextTuple() {

    }
    public static void main(String[] args) throws IOException {
//        OSLogFileSpout spout = new OSLogFileSpout();
//        spout.load(new File("d:\\1703\\无线-阿朗-OMP1-sendalarm.log.2017-02-26"), new OSTupleCollector());
//        //spout.load(new File("d:\\1703\\传输-华为-新二干SDH_sendalarm.log.2017-02-26"), new OSTupleCollector());
//        String log = "[2017-02-26 00:00:01 INFO] SendTrap::sendTrap>>\tSend to OS:><?xml version='1.0' encoding='gb2312'?>";
//        Pattern pattern = Pattern.compile(LOG_START_REGEX_PATTEN);
//        Matcher matcher = pattern.matcher(log);
//        if (matcher.find()) {
//            System.out.println("matcher = " + matcher.groupCount());
//            System.out.println("matcher = " + matcher.group(0));
//        }
    }
}
