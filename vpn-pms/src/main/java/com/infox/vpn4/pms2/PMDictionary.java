package com.infox.vpn4.pms2;


import com.infox.vpn4.pms2.common.annotation.DicGroup;
import com.infox.vpn4.pms2.common.annotation.DicItem;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/3
 * Time: 10:13
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PMDictionary {

    @DicGroup
    public static abstract class PM_PROCESSOR_TYPE {
        @DicItem(desc = "表达式",code = "exp")
        public static final int EXPRESSION = 0;

        @DicItem(desc = "脚本",code = "script")
        public static final int SCRIPT = 1;

        @DicItem(desc = "任务",code = "Job")
        public static final int JOB = 2;


    }

    public static abstract class PM_ENTITY_TYPE {
        @DicItem(desc = "EMS",code = "EMS")
        public static final int EMS = 0;
        @DicItem(desc = "网元",code = "NE")
        public static final int MANAGEDELEMENT = 1;
        @DicItem(desc = "板卡",code = "CARD")
        public static final int CARD = 2;
        @DicItem(desc = "端口",code = "PTP")
        public static final int PORT = 3;
        @DicItem(desc = "CTP",code = "CTP")
        public static final int CTP = 4;

        @DicItem(desc = "客户",code = "CUSTOMER")
        public static final int CUSTOMER = 10001;

        @DicItem(desc = "业务",code = "BUSINESS")
        public static final int BUSINESS = 10002;

        @DicItem(desc = "省",code = "PROVINCE")
        public static final int PROVINCE = 20001;

        @DicItem(desc = "地市",code = "DISTRICT")
        public static final int DISTRICT = 20002;

        @DicItem(desc = "县级市",code = "CITY")
        public static final int CITY = 20003;
    }

    @DicGroup
    public static abstract class PM_PARAMS_STATE {
        @DicItem(desc = "禁用",code = "invalid")
        public static final int OFF = 0;

        @DicItem(desc = "启用",code = "valid")
        public static final int ON = 1;




    }

    private static HashMap<Class,HashMap<Object,String>> codeMap = new HashMap<>();
    public static String  getCode(Class cls,Object value) {

        HashMap<Object, String> map = codeMap.get(cls);
        if (map == null) {
            synchronized (codeMap) {
                map = codeMap.get(cls);
                if (map == null) {
                    map = new HashMap<>();
                    Field[] fields = cls.getDeclaredFields();

                    for (Field field : fields) {
                        DicItem annotation = field.getAnnotation(DicItem.class);
                        if (annotation != null) {
                            String code = annotation.code();
                            try {
                                map.put(field.get(cls),code);
                            } catch (IllegalAccessException e) {
                                //logger.error(e, e);
                            }
                        }
                    }
                    codeMap.put(cls,map);
                }
            }
        }
        return map.get(value);
    }
}
