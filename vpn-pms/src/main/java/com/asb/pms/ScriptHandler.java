package com.asb.pms;


import com.asb.pms.model.PM_DATA;
import com.asb.pms.model.PM_PARAMS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/3
 * Time: 10:33
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ScriptHandler {
    private String script = null;
    private PM_DATA pmData = null;
    private PM_PARAMS pmParams = null;
    private Log logger = LogFactory.getLog(getClass());
    public ScriptHandler(String script, PM_DATA pmData,PM_PARAMS pmParams) {
        this.script = script;
        this.pmData = pmData;
        this.pmParams = pmParams;
    }


    public List<PM_DATA> make() throws ScriptException {
        ScriptEngineManager scriptEngineManager =
                new ScriptEngineManager();
        ScriptEngine nashorn =
                scriptEngineManager.getEngineByName("nashorn");
        SimpleBindings  simpleBindings = new SimpleBindings();
        simpleBindings.put("_cacheJdbcTemplate",((H2PMCache)Context.getInstance().getServer().getPmCache()).getJdbcTemplate());
        simpleBindings.put("_JdbcTemplate",Configuration.getJdbcTemplate());
        simpleBindings.put("_pmDataParent",pmData);
        List<PM_DATA> _pmDataChilds = new ArrayList<>();
        simpleBindings.put("_pmDataChilds",_pmDataChilds);
        simpleBindings.put("_pmParam",this.pmParams);
        simpleBindings.put("_logger",this.logger);


        nashorn.eval(script,simpleBindings);



        for (PM_DATA pmDataChild : _pmDataChilds) {

        //    pmDataChild.setStatPointId(pmData.getStatPointId());
            pmDataChild.setStatus(0);
            pmDataChild.setUpdateDate(new Date());
            pmDataChild.setSource("PMS");
            pmDataChild.setParentPmData(pmData);
            pmDataChild.setTimePoint(new Date());
            pmDataChild.setUpdateDate(new Date());
            pmDataChild.setPmParams(pmParams);
            Context.getInstance().getServer().sendData(pmDataChild);
        }

        return _pmDataChilds;




    }

    public static void main(String[] args) throws ScriptException {
        PM_DATA pm_data = new PM_DATA();
        pm_data.setId(1l);
        pm_data.setValue(0.7f);
        String exp = "_pmOut.value = _pmIn.value > 0.8 ? 20 : (_pmIn.value > 0.6 ? 40 : 80); _pmOut.status = 99";
        ExpressionHandler expressionHandler = new ExpressionHandler(exp, pm_data);
        expressionHandler.make();


    }
}
