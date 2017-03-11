package com.infox.vpn4.pms2;


import com.infox.vpn4.pms2.model.PM_DATA;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/3
 * Time: 10:33
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ExpressionHandler {
    private String expression = null;
    private PM_DATA pmData = null;

    public ExpressionHandler(String expression, PM_DATA pmData) {
        this.expression = expression;
        this.pmData = pmData;
    }


    public List<PM_DATA> make() throws ScriptException {
        ScriptEngineManager scriptEngineManager =
                new ScriptEngineManager();
        ScriptEngine nashorn =
                scriptEngineManager.getEngineByName("nashorn");
        SimpleBindings  simpleBindings = new SimpleBindings();
        simpleBindings.put("_pmIn",pmData);
        PM_DATA pmOut = new PM_DATA();
        simpleBindings.put("_pmOut",pmOut);
        nashorn.eval(expression,simpleBindings);

        System.out.println(pmOut.getValue());

        return Arrays.asList(pmOut);


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
