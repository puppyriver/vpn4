package infox.vpn4.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/4
 * Time: 14:16
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class VendorUtil {
    private Logger logger = LoggerFactory.getLogger(VendorUtil.class);
    private static Map<String,String> map =  Maps.newHashMap();
    static {
        map.put("朗讯", "lucent");
        map.put("上海贝尔", "asb");
        map.put("贝尔", "asb");
        map.put("阿尔卡特朗讯", "asb");
        map.put("华为", "huawei");
        map.put("中兴", "zte");
        map.put("烽火", "fenghuo");
        map.put("中盈","zhongying");
        map.put("中兴动环","zte");
    }
    public static String getVendorDn(String name) {
         return map.get(name);
    }
}
