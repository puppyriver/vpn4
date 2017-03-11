package com.infox.vpn4.pms2.api;

import com.infox.vpn4.pms2.model.PM_DATA;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/24
 * Time: 10:29
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class JsonRpcExample {
    private Log logger = LogFactory.getLog(getClass());
    JsonRpcHttpClient client = null;
    private void sendOne() throws Throwable {
          if (client == null) {
              client = new JsonRpcHttpClient(
                      new URL("http://127.0.0.1:8080/pmsweb/PmServer.json"));
          }

        PM_DATA data = new PM_DATA();
        data.setValue(0.01f);
        data.setSource("test");
        data.setUpdateDate(new Date());
        data.setStatus(0);
        PM_DATA sendData = client.invoke("sendData", new Object[] {data}, PM_DATA.class);
    }

    public static void main(String[] args) throws Throwable {




        //PM_DATA sendData = client.invoke("sendData", data, PM_DATA.class);


        JsonRpcExample example = new JsonRpcExample();
        example.sendOne();
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 2; i++) {
            example.sendOne();

        }
        long t2 = System.currentTimeMillis() - t1;

        System.out.println("t2 = " + t2);


    }
}
