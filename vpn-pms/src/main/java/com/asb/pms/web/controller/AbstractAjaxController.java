package com.asb.pms.web.controller;

import com.asb.pms.util.ReflectionUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/16
 * Time: 14:04
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class AbstractAjaxController {
    protected static Log logger = LogFactory.getLog(AbstractAjaxController.class);

    public static Long extractLong(HttpServletRequest request,String key) {
        String value = request.getParameter(key);
        if (value != null && !value.isEmpty()) {
           return Long.parseLong(value);
        }
        return null;
    }
    public static Integer extractInt(HttpServletRequest request,String key) {
        String value = request.getParameter(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.error(e, e);
            }
        }
        return null;
    }

    public static Object extract(HttpServletRequest request, Class cls) {
        Enumeration<String> parameterNames = request.getParameterNames();
        Object obj = null;
        try {
            obj = cls.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String para = request.getParameter(key);
            Object value = null;

            if (para != null) {
                Field field = ReflectionUtils.findField(cls, key);
                if (field != null) {
                    if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
                        value = Integer.parseInt(para);
                    } else if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
                        value = Long.parseLong(para);
                    }  else if (field.getType().equals(Float.class) || field.getType().equals(float.class)) {
                        value = Float.parseFloat(para);
                    } else if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
                        value = Double.parseDouble(para);
                    } else if (field.getType().equals(Date.class)) {
                        try {
                            value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(para);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        value = para;
                    }
                    try {
                        ReflectionUtil.setFieldValue(obj,field, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return obj;
    }

//    @RequestMapping(value="queryPMP",method= RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public @ResponseBody QueryResult queryPmParas(@RequestBody QueryCondition queryCondition){
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
//            list = JdbcTemplateUtil.queryForList(Configuration.getJdbcTemplate(), PM_PARAMS.class, sql);
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

}
