package com.infox.vpn4.pms2;

import com.infox.vpn4.pms2.common.annotation.DicItem;
import com.infox.vpn4.pms2.common.fs.FileSystem;
import com.infox.vpn4.pms2.model.*;
import com.infox.vpn4.pms2.repository.PmDataRepository;
import com.infox.vpn4.pms2.repository.PmDataRepositorySqliteImpl;
import com.infox.vpn4.pms2.util.BatchConsumerTemplate;
import com.infox.vpn4.pms2.util.IdentityUtil;
import com.infox.vpn4.pms2.util.BJdbcUtil;
import com.infox.vpn4.pms2.util.JdbcTemplateUtil;
import com.google.common.collect.HashMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.script.ScriptException;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/22
 * Time: 11:19
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PMReceiver {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private BatchConsumerTemplate dbPersister = null;
   // private DataSource dataSource = Configuration.getDataSource();

 //   private PmDataRepository repository = new PmDataRepositorySqliteImpl();

//    public PmDataRepository getRepository() {
//        return repository;
//    }

    public PMReceiver (PmDataRepository repository) {
        dbPersister = new BatchConsumerTemplate() {
            @Override
            protected void processObjects(List events) {

                PMReceiver.this.logger.debug("processObjects {}",events.size());
                events.stream().forEach(event->{
                    PM_STATPOINT statpoint =  findOrCreateStatepoint((PM_DATA)event);
                    PM_DATA pm_data = (PM_DATA) event;
                    pm_data.setStatPointId(statpoint.getId());
                    repository.insert(pm_data);
                    Context.getInstance().getServer().notifyPmDataCreate(pm_data);
                    handle(statpoint.getParamId(),pm_data);
                });
            }
        };

        dbPersister.setBatchSize(10000);
        dbPersister.start();

    }

    private PM_STATPOINT findOrCreateStatepoint(PM_DATA pmData) {
        List<PM_ENTITY> bindEntities = pmData.getBindEntities();
        PM_PARAMS pmParams = pmData.getPmParams();
        if (pmParams.getEntityTypeId() == null && bindEntities.size() > 0) {
            pmParams.setEntityTypeId(bindEntities.get(0).getEntityTypeId());
        }

        if (pmParams.getKeepInCacheHours() == null)
            pmParams.setKeepInCacheHours(24);
        PM_PARAMS dbParams = Context.getInstance().pmParasDao.insertByDn(pmParams);
        if (dbParams.getParentId() == null) {
            PM_STATPOINT stp = createSTP(bindEntities, dbParams);
            return stp;
        } else {
            PM_STATPOINT stp = createSTP(dbParams,pmData.getParentPmData());
            return stp;

        }
    }

    private void handle(long paramId,PM_DATA pmData) {
        HashMultimap<Long, PM_PARAMS> parentIdPmParaMap = Context.getInstance().parentIdPmParaMap;
        Set<PM_PARAMS> subParams = parentIdPmParaMap.get(paramId);
        if (subParams != null && subParams.size() > 0) {
            logger.debug("Find "+subParams.size()+" sub params by param : "+paramId);
            for (PM_PARAMS subParam : subParams) {
                Long processorId = subParam.getProcessorId();
                PM_PROCESSOR pm_processor = Context.getInstance().pmProcessorDao.get(processorId);
                if (pm_processor != null) {
                    Integer type = pm_processor.getType();
                    switch (type) {
                        case PMDictionary.PM_PROCESSOR_TYPE.EXPRESSION : {
                            ExpressionHandler expressionHandler = new ExpressionHandler(pm_processor.getScript(), pmData);
                            try {
                                List<PM_DATA> make = expressionHandler.make();
                                logger.info("params = "+subParam.getName()+" "+(make == null ? 0: make.size())+" pm_datas created !");
                            } catch (ScriptException e) {
                                logger.error(e.getMessage(), e);
                            }
                            break;
                        }
                        case PMDictionary.PM_PROCESSOR_TYPE.SCRIPT : {
                            String script = null;
                            try {
                                byte[] bytes = Configuration.getFileSystem().readBytes(pm_processor.getScriptFileId());
                                script = new String(bytes);
                            } catch (IOException e) {
                                logger.error(e.getMessage(), e);
                            }
                            ScriptHandler expressionHandler = new ScriptHandler(script, pmData,subParam);
                            try {
                                List<PM_DATA> make = expressionHandler.make();
                                logger.info("params = "+subParam.getName()+" "+(make == null ? 0: make.size())+" pm_datas created !");
                            } catch (ScriptException e) {
                                logger.error(e.getMessage(), e);
                            }
                            break;

                        }
                        case PMDictionary.PM_PROCESSOR_TYPE.JOB : {
                            break;
                        }
                    }
                }
            }
        }
    }

//    private String getEntityDn(PM_ENTITY entity) {
//        if (entity == null) return null;
//        PM_ENTITY tmp = entity;
//        String stpDn = "";
//        while (true) {
//            stpDn = getEntityTypeCode(tmp.getEntityTypeId())+":"+tmp.getDn()+(stpDn.isEmpty()? "":"@")+stpDn;
//            tmp = tmp.getParentEntity();
//            if (tmp == null) break;
//        }
//
//        return stpDn;
//    }

    private String getEntityDn(PM_ENTITY entity) {
        if (entity == null) return null;
        return entity.getDn();
    }

    private PM_STATPOINT createSTP(PM_PARAMS pmParams,PM_DATA parent) {
        Long parentStatPointId = parent.getStatPointId();
        PM_STATPOINT statpoint = Context.getInstance().pmStatPointDao.get(parentStatPointId);

        PM_STATPOINT stp = new PM_STATPOINT();
        stp.setDn(statpoint.getDn()+"::"+pmParams.getDn());
        stp.setEntityTypeId(statpoint.getEntityTypeId());
        stp.setEntityId(statpoint.getEntityId());
        stp.setParamId(pmParams.getId());
        stp.setUpdateDate(new Date());
        stp = Context.getInstance().pmStatPointDao.insertByDn(stp);
        return stp;
    }
    private PM_STATPOINT createSTP(List<PM_ENTITY> entities,PM_PARAMS pmParams) {

        PM_ENTITY entity = entities.get(0);

        PM_ENTITY tmp = entity;
        String stpDn = "";
        while (true) {


            tmp.setDn(getEntityDn(tmp));
            tmp.setParentEntityDn(getEntityDn(tmp.getParentEntity()));
            Context.getInstance().pmEntityDao.insertByDn(tmp);
            tmp = tmp.getParentEntity();
            if (tmp == null) break;
        }

        stpDn = entity.getDn();
        stpDn += "::"+pmParams.getDn();

        PM_STATPOINT stp = new PM_STATPOINT();
        stp.setDn(stpDn);

        stp.setEntityTypeId(entity.getEntityTypeId());
        stp.setEntityId(Context.getInstance().pmEntityDao.get(entity.getDn()).getId());
        stp.setParamId(pmParams.getId());
        stp.setUpdateDate(new Date());

        stp = Context.getInstance().pmStatPointDao.insertByDn(stp);

        return stp;
    }

    private String getEntityTypeCode(Long entityTypeId) {
        String code =  PMDictionary.getCode(PMDictionary.PM_ENTITY_TYPE.class,entityTypeId.intValue());
        if (code == null) {
            throw new RuntimeException("Undefined entitytypeid = "+entityTypeId);
        }

        return code;
    }


    public void push(PM_DATA pm_data) {
         if (pm_data.getStatus() != null && pm_data.getStatus() == Constants.PM_DATA_STATUS_PERSIST) {
             dbPersister.offerOne(pm_data);
         }
    }



}
