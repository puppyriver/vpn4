package com.asb.pms.api;

//import com.alcatelsbell.nms.common.SpringContext;
import com.asb.pms.model.PM_DATA;
import com.asb.pms.PmsServer;
import com.asb.pms.api.pm.PMQuery;
import com.asb.pms.model.PM_NODE;
import com.asb.pms.util.SysProperty;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/24
 * Time: 11:04
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PmClient {
    private static PmClient ourInstance = new PmClient();
    private Log logger = LogFactory.getLog(getClass());

    public static PmClient getInstance() {
        return ourInstance;
    }

    JsonRpcHttpClient client = null;
    public PmClient() {
        if (client == null) {
            try {
//                client = new JsonRpcHttpClient(
//                        new URL(SysProperty.getString("pmserver.jsonrpc.url","http://127.0.0.1:8080/pmsweb/ajax/PmServer.json")));
                String url = SysProperty.getString("pmserver.jsonrpc.url", null);
                if (url != null)
                    client = new JsonRpcHttpClient(
                        new URL(url));
            } catch (MalformedURLException e) {
                logger.error(e, e);
            }
        }
    }

    public PmClient(String jsonRpcUrl) {
        if (client == null) {
            try {
                client = new JsonRpcHttpClient(
                        new URL(jsonRpcUrl));
            } catch (MalformedURLException e) {
                logger.error(e, e);
            }
        }
    }


    private PMServerAPI rmiClient = null;

    public PMServerAPI getLocalServer() {



        String rmiURl = SysProperty.getString("pmserver.rmi.url");
        if (rmiURl != null) {
            if (rmiClient == null) {
                synchronized (this) {
                    if (rmiClient == null) {
                        RmiProxyFactoryBean bean = new RmiProxyFactoryBean();
                        bean.setServiceUrl(rmiURl);
                        bean.setServiceInterface(PMServerAPI.class);
                        bean.afterPropertiesSet();
                        rmiClient = (PMServerAPI) bean.getObject();
                    }
                }
            }
            return rmiClient;
        }

        if (client == null)
            return PmsServer.inst;

        return null;
    }

    public PM_DATA sendPmData(PM_DATA pm_data) throws Throwable {
        if (getLocalServer() != null ) {
            PM_DATA pm_data1 = (getLocalServer()).sendData(pm_data);
            return pm_data1;
        }

        PM_DATA sendData = client.invoke("sendData", new Object[] {pm_data}, PM_DATA.class);

        return sendData;
    }

    public PM_NODE hello(PM_NODE pm_node) throws Throwable {
        if (getLocalServer() != null ) {
            return (getLocalServer()).hello(pm_node);
        }
        PM_NODE node = client.invoke("hello", new Object[] {pm_node}, PM_NODE.class);
        return node;
    }

    public List<String> queryStatePointKeys(List<String> paramsCodes, int entityType, List<String> entityDns) throws Throwable {

        if (getLocalServer() != null ) {
            return (getLocalServer()).queryStatePointKeys(paramsCodes, entityType, entityDns);
        }

        return client.invoke("queryStatePointKeys", new Object[] {paramsCodes,entityType,entityDns}, List.class);
    }

    public Map<String, List<PM_DATA>> queryPMDATA(PMQuery query) throws Throwable {
        if (getLocalServer() != null ) {
            Map<String, List<PM_DATA>> result = getLocalServer().queryPMDATA(query);
            return result;
        }
        return client.invoke("queryPMDATA", new Object[] {query}, Map.class);
    }

    public static void main(String[] args) throws Throwable {
        System.setProperty("pmserver.rmi.url","rmi://localhost:1099/PmsRmiService");

        PM_NODE hello = PmClient.getInstance().hello(new PM_NODE());
        System.out.println(hello);
    }
}
