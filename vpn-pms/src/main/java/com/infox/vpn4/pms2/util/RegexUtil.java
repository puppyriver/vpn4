package com.infox.vpn4.pms2.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Pattern;

/**
 * Author: Ronnie.Chen
 * Date: 2015/12/7
 * Time: 14:47
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class RegexUtil {
    public static boolean stringMatchRegex(String str,String regex) {
        Pattern p = Pattern.compile(regex);
        return p.matcher(str).find();
    }

    public static void main(String[] args) {

        System.out.println(stringMatchRegex("adronnieisme","(.*)"));
    }
}
