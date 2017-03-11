package com.infox.vpn4.pms2.api;

//import com.alcatelsbell.nms.common.SpringContext;
import com.infox.vpn4.pms2.PmsServer;
import com.infox.vpn4.pms2.api.pm.PMQuery;
import com.infox.vpn4.pms2.model.PM_DATA;
import com.infox.vpn4.pms2.model.PM_NODE;
import com.infox.vpn4.pms2.util.SysProperty;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
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
                client = new JsonRpcHttpClient(
                        new URL(SysProperty.getString("pmserver.jsonrpc.url","http://127.0.0.1:8080/pmsweb/ajax/PmServer.json")));
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


    public PmsServer getLocalServer() {
        return PmsServer.inst;
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
}
