package com.asb.pms;


//import com.alcatelsbell.nms.util.ObjectUtil;
import com.asb.pms.api.PMServerAPI;
import com.asb.pms.api.StpKey;
import com.asb.pms.api.pm.PMQueryUtil;
import com.asb.pms.model.*;
import com.asb.pms.repository.PmDataRepository;
import com.asb.pms.repository.PmDataRepositorySqliteImpl;
import com.asb.pms.util.*;
import com.asb.pms.api.pm.PMQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/21
 * Time: 18:43
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PmsServer implements ApplicationContextAware,InitializingBean,PMServerAPI {
    private Log logger = LogFactory.getLog(getClass());

    private PmDataRepository repository = null;

    private PMReceiver pmReceiver = null;
    private PMCache pmCache = null;

    private List<PMSource> pmSources = new ArrayList<PMSource>();

    private List<PM_DATA_Listener> pmDataListeners = new ArrayList<>();


    public static PmsServer inst = null;
    public PmsServer() {
        inst = this;
        repository = new PmDataRepositorySqliteImpl();
        pmReceiver = new PMReceiver(repository);
    }

    public static PmsServer createServer() {
        PmsServer pmsServer = new PmsServer();
        //PMReceiver pmReceiver = new PMReceiver();
        PMCache pmCache = new H2PMCache();
     //   pmsServer.setPmReceiver(pmReceiver);
        pmsServer.setPmCache(pmCache);
        pmsServer.addPmSource(null);

        return pmsServer;

    }

    public PmDataRepository getRepository() {
        return repository;
    }

    public void setRepository(PmDataRepository repository) {
        this.repository = repository;
    }

    public void start() {
        for (final PMSource pmSource : pmSources) {
            Runnable consumer = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        List<PM_DATA> pm_datas = null;
                        try {
                            pm_datas = pmSource.takeList(100);
                        } catch (InterruptedException e) {
                            logger.error(e, e);
                        }

                        for (PM_DATA pm_data : pm_datas) {
                            pmReceiver.push(pm_data);
                        }
                    }
                }
            };
            new Thread(consumer).start();
        }


//        Runnable clearExpiredData = new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        Thread.sleep(3600000l);
//                    } catch (InterruptedException e) {
//                        logger.error(e, e);
//                    }
//                    long t1 = System.currentTimeMillis();
//                    List<PM_PARAMS> paras = null;
//                    try {
//                        paras = Context.getInstance().pmParasDao.getAll();
//                    } catch (Exception e) {
//                        logger.error(e, e);
//                    }
//                    for (PM_PARAMS para : paras) {
//                        pmCache.clearExpiredDatas(para);
//                    }
//
//                    long t2 = System.currentTimeMillis() - t1;
//
//                    if (t2 > 60000) {
//                        logger.warn(" SPEND "+t2+" ms to clear all expired data.");
//                    } else {
//                        logger.info(" SPEND "+t2+" ms to clear all expired data.");
//                    }
//                    //    jdbcTemplate.execute();
//                }
//            }
////        };
//
//        new Thread(clearExpiredData).start();
    }


    public void notifyPmDataCreate(PM_DATA pm_data) {
        if (pmCache != null)
            pmCache.addToCache(pm_data);
        for (PM_DATA_Listener pmDataListener : pmDataListeners) {
            try {
                pmDataListener.pm_data_create(pm_data);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
    public PMReceiver getPmReceiver() {
        return pmReceiver;
    }

    public void setPmReceiver(PMReceiver pmReceiver) {
        this.pmReceiver = pmReceiver;
    }

    public void addPmSource(PMSource pmSource) {
        pmSources.add(pmSource);
    }

    public PMCache getPmCache() {
        return pmCache;
    }

    public void setPmCache(PMCache pmCache) {
        this.pmCache = pmCache;
    }


  //  private List<PM_DATA> buffer = new ArrayList<>();
    int idx = 0;
    @Override
    public PM_DATA sendData(PM_DATA pm_data) {
//        buffer.add(pm_data);
//        if (buffer.size() == 10) {
//        //    ObjectUtil.saveObject("pmBuffer_"+(idx++),buffer);
//            buffer.clear();
//        }
        pmReceiver.push(pm_data);
        return pm_data;
    }

    @Override
    public PM_NODE hello(PM_NODE node) {
        synchronized (this) {
            JdbcTemplate jdbcTemplate = Configuration.getJdbcTemplate();
            try {
                PM_NODE node1 = (PM_NODE) JdbcTemplateUtil.queryForObject(jdbcTemplate, PM_NODE.class, "SELECT * FROM PM_NODE WHERE DN = ? ", node.getDn());

                if (node1 == null) {
                    node = (PM_NODE)JdbcTemplateUtil.insert(jdbcTemplate,PMServerUtil.getTableName(PM_NODE.class),node);
                } else {
                    node.setId(node1.getId());
                    node.setEventNum(node1.getEventNum());
                    node = (PM_NODE)JdbcTemplateUtil.update(jdbcTemplate,PMServerUtil.getTableName(PM_NODE.class),node);
                }
            } catch (Exception e) {
                logger.error(e, e);
            }
            return node;
        }
    }


    @Override
    public  List<String> queryStatePointKeys(List<String> paramsCodes, int entityType, List<String> entityDns) {
        long t1 = System.currentTimeMillis();
        if (paramsCodes == null || entityDns == null) return new ArrayList<>();
        HashMap<Long,String> paraIdMap = new HashMap<>();
        List<Long> paramIds = paramsCodes.stream()
                .map(code -> {
                    PM_PARAMS para = Context.getInstance().pmParasDao.get(code);
                    if (para != null) paraIdMap.put(para.getId(),code);
                    return (para != null ? para.getId() : null);
                })
                .filter(id -> id != null)
                .collect(Collectors.toList());

        HashMap<Long,String> entityIdMap = new HashMap<>();

        List<Long> entityIds = entityDns.stream().map
                (entityDn -> {
                    List<PM_ENTITY> entities = null;
                    try {
                        entities = JdbcTemplateUtil.queryForList(Context.getInstance().pmEntityDao.getCacheJdbc(), PM_ENTITY.class, "SELECT * FROM PM_ENTITY WHERE ENTITYTYPEID = ? and DN = ?", entityType, entityDn);
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                    if (entities != null && entities.size() > 0) {
                        entityIdMap.put(entities.get(0).getId(),entityDn);
                        return entities.get(0).getId();
                    }
                    return null;
                }).filter(entity -> entity != null)
                .collect(Collectors.toList());


        String sql = "SELECT * FROM PM_STATPOINT WHERE entityId in ("+ PMQueryUtil.toInString(entityIds)+") and entityTypeId = ? and paramId in ("+PMQueryUtil.toInString(paramIds)+")";

        try {
            List<PM_STATPOINT> points = JdbcTemplateUtil.queryForList(Context.getInstance().pmStatPointDao.getCacheJdbc(), PM_STATPOINT.class,
                    sql, entityType);
            if(points != null && points.size() >0) {
                List<String> collect = points.stream().map(pt -> new StpKey(paraIdMap.get(pt.getParamId()), entityType, entityIdMap.get(pt.getEntityId()), pt.getId()).toString())
                        .collect(Collectors.toList());
                long t2 = System.currentTimeMillis();
                if (t2-t1 > 1000)
                    logger.info("queryStatePointKeys spend : "+(t2-t1)+"ms"+",paramsCodes="+Arrays.deepToString(paramsCodes.toArray())+",entityDns="+Arrays.deepToString(entityDns.toArray()));
                return collect;
            }

        } catch (Exception e) {
            logger.error(e, e);
        }

        return new ArrayList<>();
    }


    @Override
    public Map<String, List<PM_DATA>> queryPMDATA(PMQuery query) throws Exception {
        long t1 = System.currentTimeMillis();
        Map<String, List<PM_DATA>> data = doQueryPMDATA(query);
        if (data != null) {
            Collection<List<PM_DATA>> values = data.values();
            for (List<PM_DATA> pmDatas : values) {
                for (PM_DATA pmData : pmDatas) {
                    if (pmData.getDataMap() != null) {

                        HashMap map = new HashMap();
                        Set keys = pmData.getDataMap().keySet();
                        for (Object key : keys) {
                            if (key.toString().startsWith("query_"))
                                map.put(key,pmData.getDataMap().get(key));
                        }
                        pmData.getDataMap().clear();
                        pmData.getDataMap().putAll(map);
                    }
                    PM_STATPOINT statpoint = Context.getInstance().pmStatPointDao.get(pmData.getStatPointId());
                    PM_PARAMS pmParams = null;
                    if (statpoint != null) {
                        pmParams = Context.getInstance().pmParasDao.get(statpoint.getParamId());
                        pmData.getDataMap().put("param", pmParams);
                        pmData.getDataMap().put("statpoint", statpoint);
                    }
                }
            }
        }
        long t2 = System.currentTimeMillis();
        if (t2-t1 > 1000)
            logger.info("queryPMDATA spend : "+(t2-t1)+"ms");
        return data;
    }




    public Map<String, List<PM_DATA>> doQueryPMDATA(PMQuery query) throws Exception {
     //   HashBiMap<Long,String> stpKeys = HashBiMap.create();
        HashMap<Long,String> stpKeys = new HashMap<>();
        if (query.startTime == null && query.endTime == null) {
            //查询当前值
            Map<String, List<PM_DATA>> collect = query.stpKeys.stream()
                    .map(key ->{
                        long stpId = StpKey.parse(key).getStpId();
                        stpKeys.put(stpId,key);
                        return repository.getLatest(stpId);
                     } )
                    .filter(data -> data != null)
                    .collect(Collectors.groupingBy(data-> stpKeys.get(data.getStatPointId())));
            logger.info("query current : size = "+collect.size());
            return collect;
        }

        if (query.endTime == null) query.endTime = new Date();
        List<String> queryKeys = query.stpKeys;

        if (query.startTime == null) throw new Exception("startTime should not be null !");


        for (String stpKey : queryKeys) {
            stpKeys.put(StpKey.parse(stpKey).getStpId(),stpKey);
        }
        List<PM_DATA> query1 = repository.query(query.startTime, query.endTime,queryKeys,query.getAttributes());

        if (query1 != null && query1.size() > 0) {
            Map<String, List<PM_DATA>> collect = query1.stream().collect(Collectors.groupingBy(data -> Optional.ofNullable(stpKeys.get(data.getStatPointId())).orElse("NULL")));
            collect.remove("NULL");
            return collect;
            //   return query1;
        }

        return new HashMap<>();

    }

//    @Override
//    public QueryResult queryPmParas(QueryCondition queryCondition){
//        int currentPage = queryCondition.getCurrentPage();
//        int pageSize = queryCondition.getPageSize();
//        int start = pageSize * (currentPage - 1);
//        int limit = pageSize;
//
//        String sql = "SELECT * FROM PM_PARAMS";
//        if (limit > 0) {
//            sql = SqlUtil.getPagerSQL(sql,start,limit);
//        }
//        List list = null;
//        try {
//            list = JdbcTemplateUtil.queryForList(Configuration.getJdbcTemplate(), PM_DATA.class, sql);
//        } catch (Exception e) {
//            logger.error(e, e);
//            throw new RuntimeException(e);
//        }
//        List<PmParaData> datas = new ArrayList<>();
//        for (Object o : list) {
//            datas.add(new PmParaData((PM_PARAMS)o));
//        }
//        QueryResult queryResult = new QueryResult(pageSize,currentPage,datas);
//        return queryResult;
//    }






    private ApplicationContext applicationContext = null;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SysProperty.load();

        try {
            JdbcTemplateUtil.createTable(Configuration.getJdbcTemplate(),PM_SYSTEM.class,"PM_SYSTEM");
            JdbcTemplateUtil.createTable(Configuration.getJdbcTemplate(),PM_NODE.class,"PM_NODE");
            JdbcTemplateUtil.createTable(Configuration.getJdbcTemplate(),PM_ENTITY.class,"PM_ENTITY");
            JdbcTemplateUtil.createTable(Configuration.getJdbcTemplate(),PM_ENTITYTYPE.class,"PM_ENTITYTYPE");
            JdbcTemplateUtil.createTable(Configuration.getJdbcTemplate(),PM_EVENT.class,"PM_EVENT");
            JdbcTemplateUtil.createTable(Configuration.getJdbcTemplate(),PM_JOB.class,"PM_JOB");
            JdbcTemplateUtil.createTable(Configuration.getJdbcTemplate(),PM_DATA.class,"PM_DATA");


            JdbcTemplateUtil.createTable(Configuration.getJdbcTemplate(),PM_PARAMS.class,"PM_PARAMS");
            JdbcTemplateUtil.createTable(Configuration.getJdbcTemplate(),PM_PROCESSOR.class,"PM_PROCESSOR");
            JdbcTemplateUtil.createTable(Configuration.getJdbcTemplate(),PM_STATPOINT.class,"PM_STATPOINT");
        } catch (Exception e) {
            //logger.error(e, e);
        }

        try {
            Configuration.getJdbcTemplate().execute("CREATE INDEX IDX_PMDATA_TIMEPOINT on PM_DATA(TIMEPOINT)");
            Configuration.getJdbcTemplate().execute("CREATE INDEX IDX_PMDATA_STPID on PM_DATA(statPointId)");
        } catch (Exception e) {

        }

        Context.getInstance().setServer(this);
        Context.getInstance().initCacheDaos();


        JdbcTemplate jdbcTemplate = Configuration.getJdbcTemplate();
        Date date = jdbcTemplate.queryForObject("SELECT MIN(TIMEPOINT) FROM PM_DATA",Date.class);
        if (date == null) date = new Date();
        PmSystemUtil.updateValue(Constants.CATEGORY_PM_DATA,Constants.KEY_DB_TIMELINE,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));

        start();
        logger.info("PmServer started : version = 0.70730");
    }

    public List<PM_DATA_Listener> getPmDataListeners() {
        return pmDataListeners;
    }

    public void setPmDataListeners(List<PM_DATA_Listener> pmDataListeners) {
        this.pmDataListeners = pmDataListeners;
    }

    public static void main(String[] args) throws SQLException, IllegalAccessException {
        Connection con = Configuration.getConnection();

        JdbcTemplate jdbcTemplate = Configuration.getJdbcTemplate();
        JdbcTemplateUtil.createTable(jdbcTemplate,PM_NODE.class,"PM_NODE");
      //  JdbcTemplateUtil.createTable(jdbcTemplate,PM_EVENT.class,"PM_EVENT");


        for (int i = 0; i < 100; i++) {
            PM_PARAMS paras = new PM_PARAMS();
            paras.setName("para:"+i);
            paras.setCode("code:"+i);
            paras.setProcessorId(1l);
            paras.setType(1);
            paras.setId(IdentityUtil.getId(con,"PM_PARAMS"));
            BJdbcUtil.insertObject(con,paras,"PM_PARAMS");


        }
        con.commit();
        con.close();
    }
}
