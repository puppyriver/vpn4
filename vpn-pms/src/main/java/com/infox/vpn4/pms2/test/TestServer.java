package com.infox.vpn4.pms2.test;

import com.infox.vpn4.pms2.*;
import com.infox.vpn4.pms2.api.StpKey;
import com.infox.vpn4.pms2.api.pm.PMQuery;
import com.infox.vpn4.pms2.model.PM_DATA;
import com.infox.vpn4.pms2.model.PM_ENTITY;
import com.infox.vpn4.pms2.model.PM_PARAMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/11
 * Time: 15:50
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class TestServer {
    private Logger logger = LoggerFactory.getLogger(TestServer.class);

    private static PM_DATA createData(double value) {
        PM_DATA pmData = new PM_DATA();

    //    pmData.getDataMap().put("HostInfo",hostInfo);
        pmData.setValue((float)(value));
        pmData.setUpdateDate(new Date());
        pmData.setTimePoint(new Date());
        // pmData.bindEntity();
        pmData.setSource("ITManager6");
        pmData.setStatus(Constants.PM_DATA_STATUS_PERSIST);


        String emsdn = "NM-ITManager6";
        PM_ENTITY entity = new PM_ENTITY("127.0.0.1","localhost",(long) PMDictionary.PM_ENTITY_TYPE.MANAGEDELEMENT,null,null,emsdn);
        entity.setParentEntity(new PM_ENTITY(emsdn,emsdn,(long) PMDictionary.PM_ENTITY_TYPE.EMS,null,null,null));
        pmData.bindEntity(entity);
        pmData.setPmParams(new PM_PARAMS("CPU利用率","cpu","%",""));



        return pmData;


    }

    public static void main(String[] args) throws Exception {
        PmsServer server = new PmsServer();
        H2PMCache pmCache = new H2PMCache();
        server.setPmCache(pmCache);
        //PMReceiver pmReceiver = new PMReceiver();
        //server.setPmReceiver(pmReceiver);
        server.afterPropertiesSet();



        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StpKey stpKey = new StpKey("cpu",1,"127.0.0.1",2);
        Map<String, List<PM_DATA>> stringListMap = server.doQueryPMDATA(new PMQuery(Arrays.asList(stpKey.toString()), simpleDateFormat.parse("2017-01-01 18:00:01"),
                simpleDateFormat.parse("2017-01-02 10:00:01")));
        System.out.println("stringListMap = " + stringListMap);
//        Date date = simpleDateFormat.parse("2017-01-01 00:00:01");
//        for (int i = 0; i < 10; i++) {
//            PM_DATA data = createData(i);
//            data.setTimePoint(new Date(date.getTime() + i * 1000));
//            server.sendData(data);
//            if (i % 1000 == 0) {
//                System.out.println("i = " + i);
//            }
//        }

    }
}
