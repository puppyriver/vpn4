package com.infox.vpn4.pms2;

import com.infox.vpn4.pms2.model.*;
import com.infox.vpn4.pms2.util.CachedDao;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/1
 * Time: 21:03
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class Context {
    private static Context ourInstance = new Context();
    private Log log = LogFactory.getLog(getClass());

    public static Context getInstance() {
        return ourInstance;
    }

    public CachedDao<PM_PARAMS> pmParasDao = null;
    public CachedDao<PM_STATPOINT> pmStatPointDao = null ;
    public CachedDao<PM_ENTITY> pmEntityDao = null ;
    public CachedDao<PM_PROCESSOR> pmProcessorDao = null ;
    public CachedDao<PM_NODE> pmNodeDao = null ;
    public HashMultimap<Long,PM_PARAMS> parentIdPmParaMap =  null;
//    public ConcurrentHashMap<String,PM_PARAMS> codePmParamsMap = new ConcurrentHashMap<>();

    private PmsServer server = null;

    private Context() {
    }

    private CachedDao initCachedData(Class cls) {
        try {
            return new CachedDao(Configuration.getDataSource(),cls);
        } catch (Exception e) {
            log.error("Failed to initize cache data : "+cls+" cause : "+e.getMessage());
            //log.error(e.getMessage(), e);
        }
        return null;
    }

    public void initCacheDaos() {
        pmParasDao = initCachedData(PM_PARAMS.class);
        pmStatPointDao = initCachedData(PM_STATPOINT.class);
        pmEntityDao = initCachedData(PM_ENTITY.class);
        pmProcessorDao = initCachedData(PM_PROCESSOR.class);
        pmNodeDao = initCachedData(PM_NODE.class);
        parentIdPmParaMap =   HashMultimap.create();

        List<PM_PARAMS> all = pmParasDao.getAll();
        for (PM_PARAMS pm_params : all) {
            parentIdPmParaMap.put(pm_params.getParentId(),pm_params);
            //  codePmParamsMap.put(pm_params.getCode(),pm_params);
        }
        pmParasDao.register(new Object(){
            @Subscribe
            public void handleDaoEvent(CachedDao.DaoEvent  daoEvent) {
                if (daoEvent.opType == CachedDao.DaoEvent.OP_TYPE_CREATE) {
                    parentIdPmParaMap.put(((PM_PARAMS) daoEvent.newObject).getParentId(), (PM_PARAMS) daoEvent.newObject);
                    //    codePmParamsMap.put(((PM_PARAMS) daoEvent.newObject).getCode(), ((PM_PARAMS) daoEvent.newObject));
                }
                if (daoEvent.opType == CachedDao.DaoEvent.OP_TYPE_UPDATE) {
                    parentIdPmParaMap.remove(((PM_PARAMS) daoEvent.oldObject).getParentId(),daoEvent.oldObject);
                    parentIdPmParaMap.put(((PM_PARAMS) daoEvent.newObject).getParentId(), (PM_PARAMS)daoEvent.newObject);

//                    codePmParamsMap.remove(((PM_PARAMS) daoEvent.newObject).getCode());
//                    codePmParamsMap.put(((PM_PARAMS) daoEvent.newObject).getCode(), ((PM_PARAMS) daoEvent.newObject));
                }
                if (daoEvent.opType == CachedDao.DaoEvent.OP_TYPE_DELETE) {
                    parentIdPmParaMap.remove(((PM_PARAMS) daoEvent.newObject).getParentId(), daoEvent.oldObject);
//                    codePmParamsMap.remove(((PM_PARAMS) daoEvent.newObject).getCode());
                }
            }

        });

    }

    public PmsServer getServer() {
        return server;
    }

    public void setServer(PmsServer server) {
        this.server = server;
    }
}
