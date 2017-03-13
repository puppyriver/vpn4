package com.asb.pms.web.controller;

import com.asb.pms.*;
import com.asb.pms.model.*;
import com.asb.pms.util.SqlUtil;
import com.asb.pms.*;
import com.asb.pms.api.data.PmParaData;
import com.asb.pms.api.data.QueryCondition;
import com.asb.pms.api.data.QueryResult;
import com.asb.pms.common.fs.FileSystem;
import com.asb.pms.util.JdbcTemplateUtil;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/15
 * Time: 20:34
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Controller
@RequestMapping("/*")
public class PMAjaxController extends AbstractAjaxController{
    private FileSystem fileSystem = Configuration.getFileSystem();
    private JdbcTemplate jdbcTemplate = Configuration.getJdbcTemplate();

    @RequestMapping(value="insert")
    public @ResponseBody
    PM_DATA insert(@RequestBody PM_DATA pm_data) throws IOException {
        pm_data.setId(111l);
        return pm_data;
    }



    @RequestMapping(value="queryEntity")
    public @ResponseBody QueryResult queryEntity(HttpServletRequest request, HttpServletResponse response) throws IOException{
        QueryCondition queryCondition = (QueryCondition)extract(request,QueryCondition.class);
        int currentPage = queryCondition.getCurrentPage();
        int pageSize = queryCondition.getPageSize();
        String searchTxt = queryCondition.getSearchTxt();
        int start = pageSize * (currentPage - 1);
        int limit = pageSize;

        long paramId = extractInt(request,"pmParams");
        String sql = "SELECT * FROM PM_ENTITY WHERE EXISTS (SELECT * FROM PM_STATPOINT WHERE entityId = PM_ENTITY.ID and paramId = ?)";


        if (searchTxt != null && !searchTxt.trim().isEmpty()) {
            sql += " WHERE a.NAME like '%"+searchTxt+"%'";
        }



        if (limit > 0) {
            sql = SqlUtil.getPagerSQL(sql,start,limit);
        }
        List list = null;
        try {
            list = JdbcTemplateUtil.queryForList(Configuration.getJdbcTemplate(), PM_ENTITY.class, sql,paramId);
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeException(e);
        }

        QueryResult queryResult = new QueryResult(0,pageSize,currentPage,list);
        return queryResult;
    }

    @RequestMapping(value="queryPMP")
    public @ResponseBody QueryResult queryPmParas(HttpServletRequest request, HttpServletResponse response) throws IOException{
        QueryCondition queryCondition = (QueryCondition)extract(request,QueryCondition.class);
        int currentPage = queryCondition.getCurrentPage();
        int pageSize = queryCondition.getPageSize();
        String searchTxt = queryCondition.getSearchTxt();
        int start = pageSize * (currentPage - 1);
        int limit = pageSize;

        String sql = "select a.*,b.type as processor_type from pm_params a left join pm_processor b on a.PROCESSORID = b.id";
        if (searchTxt != null && !searchTxt.trim().isEmpty()) {
            sql += " WHERE a.NAME like '%"+searchTxt+"%' or a.CODE like '%"+searchTxt+"%' or a.MEMO like '%"+searchTxt+"%'";
        }

        sql = addFilterConditions(request,sql);

        int total = jdbcTemplate.queryForObject(sql.replace("a.*,b.type","count(a.id)"),Integer.class);

        if (limit > 0) {
            sql = SqlUtil.getPagerSQL(sql,start,limit);
        }
        List list = null;
        try {
            list = JdbcTemplateUtil.queryForList(Configuration.getJdbcTemplate(), PmParaData.class, sql);
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeException(e);
        }
        List<PmParaData> datas = new ArrayList<>();
        for (Object o : list) {
            datas.add((PmParaData)o);
        }
        QueryResult queryResult = new QueryResult(total,pageSize,currentPage,datas);
        return queryResult;
    }

    @RequestMapping(value="storePMP",method= RequestMethod.POST)
    public PmParaData createPmPara(PmParaData pmParaData) {
        PM_PARAMS convert = pmParaData.convert();
        PM_PARAMS pm_params = null;
        try {
            pm_params = (PM_PARAMS) PMServerUtil.saveObject(convert);
            return new PmParaData(pm_params);
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value="loadProcessor")
    public @ResponseBody PmParaData loadProcessor(HttpServletRequest request, HttpServletResponse response) {

        try {
            Long paraId = extractLong(request, "paraId");
            if (paraId != null) {
                PM_PARAMS pmParams =(PM_PARAMS) PMServerUtil.queryObjectById(PM_PARAMS.class, paraId);
                PmParaData data = new PmParaData(pmParams);

                if (data.getParentId() != null) {
                    PM_PARAMS parent = Context.getInstance().pmParasDao.get(data.getParentId());
                    data.parentPmName = parent.getName();
                }

                if (data.getProcessorId() != null) {
                    PM_PROCESSOR processor = (PM_PROCESSOR) PMServerUtil.queryObjectById(PM_PROCESSOR.class, data.getProcessorId());
                    data.processor_script = processor.getScript();
                    data.processor_type = processor.getType();

                    if (processor.getScriptFileId()  != null && processor.getScriptFileId() > -1) {
                        File read = fileSystem.read(processor.getScriptFileId());
                        if (read != null) {
                            data.processor_scriptfileid = processor.getScriptFileId();
                            data.processor_scriptfilename = read.getName();
                        }
                    }
                }
                return data;

            }
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeException(e);
        }

        return new PmParaData(null);
    }

        @RequestMapping(value="savePMPara")
    public @ResponseBody PmParaData savePMPara(HttpServletRequest request, HttpServletResponse response) {

        try {
            Long paraId = extractLong(request, "paraId");
            Integer processor_type = extractInt(request,"processor_type");
            Integer hours = extractInt(request,"keepInCacheHours");
            String processor_script = request.getParameter("processor_script");
            Long processor_scriptfileid = extractLong(request, "processor_scriptfileid");
            String name = request.getParameter("name");
            String code = request.getParameter("code");
            String memo = request.getParameter("memo");
            Long parentId = extractLong(request,"parentId");
            boolean directSave = request.getParameter("directSave").equals("true");
            PM_PARAMS pmParams = null;
            PM_PROCESSOR pm_processor = null;
            if (paraId != null && paraId > -1) {
                pmParams =(PM_PARAMS) PMServerUtil.queryObjectById(PM_PARAMS.class, paraId);
                if (pmParams.getProcessorId() != null)
                    pm_processor = (PM_PROCESSOR)PMServerUtil.queryObjectById(PM_PROCESSOR.class,pmParams.getProcessorId());
            } else {
                pmParams = new PM_PARAMS();
//                pmParams.setName("name");
//                pmParams.setCode("code");
//                pmParams.setMemo("memo");


            }

            pmParams.setName(name);
            pmParams.setCode(code);
            pmParams.setMemo(memo);
            pmParams.setKeepInCacheHours(hours);
            pmParams.setParentId(parentId);



            if (!directSave) {
                if (pm_processor == null) {
                    pm_processor = new PM_PROCESSOR();
                }

                pm_processor.setType(processor_type);
                switch (processor_type) {
                    case PMDictionary.PM_PROCESSOR_TYPE.EXPRESSION: {
                        pm_processor.setScript(processor_script);
                        break;
                    }

                    case PMDictionary.PM_PROCESSOR_TYPE.SCRIPT: {
                        pm_processor.setScriptFileId(processor_scriptfileid);
                        break;
                    }

                    case PMDictionary.PM_PROCESSOR_TYPE.JOB: {
                        pm_processor.setScriptFileId(processor_scriptfileid);
                        pm_processor.setScript(processor_script);
                        break;
                    }
                }

                pm_processor =   Context.getInstance().pmProcessorDao.insertOrUpdate(pm_processor);

                pmParams.setProcessorId(pm_processor.getId());
            } else {
                pmParams.setProcessorId(null);
            }

            pmParams =  Context.getInstance().pmParasDao.insertOrUpdate(pmParams);

            return new PmParaData(pmParams);


        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeException(e);
        }


    }



    @RequestMapping(value="loadScript")
    public @ResponseBody HashMap loadScript(HttpServletRequest request, HttpServletResponse response) {

        try {
            Long fileId = extractLong(request, "fileId");
            if (fileId != null) {
                byte[] bytes = fileSystem.readBytes(fileId);

                if (bytes != null) {
                    HashMap map = new HashMap();
                    map.put("txt",new String(bytes,"utf-8"));
                    return map;
                }
            }
        } catch (IOException e) {
            logger.error(e, e);
        }

        return null;
    }

    @RequestMapping(value="saveScript")
    public @ResponseBody HashMap saveScript(HttpServletRequest request, HttpServletResponse response) {
        try {
            String txt = request.getParameter("txt");
            Long fileId = extractLong(request, "fileId");
            fileSystem.save(fileId,txt.getBytes());
        } catch (IOException e) {
            logger.error(e, e);
            throw new RuntimeException(e);
        }

        return new HashMap();
    }



    @ExceptionHandler(RuntimeException.class)

    public @ResponseBody  Map<String,Object>  handleUnexpectedServerError(RuntimeException ex) {

        Map model = new TreeMap();
        model.put("responseTxt", ex.getMessage());
        return model;


    }


    @RequestMapping(value="queryNodes")
    public @ResponseBody QueryResult queryNodes(HttpServletRequest request, HttpServletResponse response) throws IOException{
        QueryCondition queryCondition = (QueryCondition)extract(request,QueryCondition.class);
        int currentPage = queryCondition.getCurrentPage();
        int pageSize = queryCondition.getPageSize();
        String searchTxt = queryCondition.getSearchTxt();
        int start = pageSize * (currentPage - 1);
        int limit = pageSize;

        String sql = "select a.* from pm_node a ";
        if (searchTxt != null && !searchTxt.trim().isEmpty()) {
            sql += " WHERE a.NAME like '%"+searchTxt+"%' or a.dn like '%"+searchTxt+"%'  ";
        }



        if (limit > 0) {
            sql = SqlUtil.getPagerSQL(sql,start,limit);
        }
        List list = null;
        try {
            list = JdbcTemplateUtil.queryForList(Configuration.getJdbcTemplate(), PM_NODE.class, sql);
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeException(e);
        }


        List data = new ArrayList<>();
        List l = null;
        for (int i = 0; i < list.size();i++) {
            if (i % 4 == 0) {
                l = new ArrayList<>();
                data.add(l);
            }
            l.add(list.get(i));
        }


        QueryResult queryResult = new QueryResult(0,pageSize,currentPage,data);
        return queryResult;
    }

    private String addFilterConditions(HttpServletRequest request,String sql) {
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            if (name.endsWith("[]")) {
                String[] values = request.getParameterValues(name);
                String n = name.substring(0,name.length()-2);
                sql +=" and "+ n +" in (";
                for (String value : values) {
                    sql += "'"+value+"',";
                }
                sql = sql.substring(0,sql.length()-1)+")";

            }
        }
        return sql;
    }

    @RequestMapping(value="queryEvents")
    public @ResponseBody QueryResult queryEvents(HttpServletRequest request, HttpServletResponse response) throws IOException{
        QueryCondition queryCondition = (QueryCondition)extract(request,QueryCondition.class);
        int currentPage = queryCondition.getCurrentPage();
        int pageSize = queryCondition.getPageSize();
        String searchTxt = queryCondition.getSearchTxt();
        int start = pageSize * (currentPage - 1);
        int limit = pageSize;
        Long nodeId = extractLong(request, "nodeId");
        String sql = "SELECT * FROM PM_EVENT WHERE NODEID = "+nodeId;
        sql = addFilterConditions(request,sql);

        Integer total = jdbcTemplate.queryForObject(sql.replace("*","count(*)"), Integer.class);
        if (limit > 0) {
            sql = SqlUtil.getPagerSQL(sql,start,limit);
        }
        List list = null;
        try {
            list = JdbcTemplateUtil.queryForList(Configuration.getJdbcTemplate(), PM_EVENT.class, sql);
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeException(e);
        }

                QueryResult queryResult = new QueryResult(total,pageSize,currentPage,list);
        return queryResult;
    }

    @RequestMapping(value="readEvents")
    public @ResponseBody PM_EVENT readEvents(HttpServletRequest request, HttpServletResponse response) throws IOException{
        Long id = extractLong(request, "id");
        jdbcTemplate.execute("UPDATE PM_EVENT SET STATE = 0 WHERE ID = "+id);
        try {
            PM_EVENT event = (PM_EVENT)JdbcTemplateUtil.queryForObjectById(jdbcTemplate,PM_EVENT.class,id);
            if (calculateThread == null) {
                synchronized (this) {
                    if (calculateThread == null) {
                        calculateThread = new Thread() {
                            public void run() {
                                while (true) {
                                    try {
                                        Long nodeId = nodeQueue.take();

                                        jdbcTemplate.execute("UPDATE PM_NODE t SET t.eventNum = (SELECT COUNT (*) from PM_EVENT WHERE nodeid = t.id and state = 1) where t.id = "+nodeId);
                                    } catch (InterruptedException e) {
                                        logger.error(e, e);
                                    }
                                }
                            }
                        };
                    }
                }
                calculateThread.start();
            }

            if (!nodeQueue.contains(event.getNodeId()))
                nodeQueue.offer(event.getNodeId());
            return event;
        } catch (Exception e) {
            logger.error(e, e);
        }
        return null;
    }

    private LinkedBlockingQueue<Long> nodeQueue = new LinkedBlockingQueue<>();
    private Thread calculateThread = null;

    @RequestMapping(value="queryPmData")
    public @ResponseBody QueryResult queryPmData(HttpServletRequest request, HttpServletResponse response) throws IOException{
        QueryCondition queryCondition = (QueryCondition)extract(request,QueryCondition.class);
        int currentPage = queryCondition.getCurrentPage();
        int pageSize = queryCondition.getPageSize();
        String searchTxt = queryCondition.getSearchTxt();
        int start = pageSize * (currentPage - 1);
        int limit = pageSize;
        Long paramId = extractLong(request, "paramId");
        Long entityId = extractLong(request, "entityId");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");


        JdbcTemplate cacheJdbc = Configuration.getJdbcTemplate();
      //  JdbcTemplate cacheJdbc = Context.getInstance().pmStatPointDao.getCacheJdbc();
        PM_STATPOINT statpoint = null;
        try {
            statpoint = (PM_STATPOINT) JdbcTemplateUtil.queryForObject(cacheJdbc, PM_STATPOINT.class, "SELECT * FROM PM_STATPOINT WHERE entityId = " + entityId + " and paramId = " + paramId);
        } catch (Exception e) {
            logger.error(e, e);
        }

        if (statpoint == null) {
            logger.error("Faild to find statpoint where entityid = "+entityId+" paramsid = "+paramId);
            return new QueryResult(0,0,0,null);
        }

        String sql = "SELECT * FROM PM_DATA WHERE statPointId = "+statpoint.getId()+" order by timePoint";

        if (limit > 0) {
            sql = SqlUtil.getPagerSQL(sql,start,limit);
        }
        List list = null;
        try {
            PMCache pmCache = Context.getInstance().getServer().getPmCache();
            list = JdbcTemplateUtil.queryForList(Configuration.getJdbcTemplate(), PM_DATA.class, sql);
        } catch (Exception e) {
            logger.error(e, e);
            throw new RuntimeException(e);
        }

        QueryResult queryResult = new QueryResult(0,pageSize,currentPage,list);
        return queryResult;
    }




}
