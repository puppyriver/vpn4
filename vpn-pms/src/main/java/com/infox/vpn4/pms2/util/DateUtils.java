package com.infox.vpn4.pms2.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/12/2
 * Time: 14:48
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DateUtils {
    private Logger logger = LoggerFactory.getLogger(DateUtils.class);
    static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
    public static final String getDayString(Date date) {
          return dayFormat.format(date);
    }

    public static final List<String> getDayStrings(Date start, Date end) {
        List<String> days = new ArrayList<>();
        if (end.before(start)) return days;
        while (true) {
            String s = getDayString(start);
            String e = getDayString(end);
            days.add(s);
            if (s.equals(e)) break;

            start = new Date(start.getTime()+ 24l * 3600l * 1000l);
        }

        return days;
    }

    private static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String formatDate(Date date) {
        return defaultDateFormat.format(date);
    }

    public static Date parse(String txt,String format) throws ParseException {
        return new SimpleDateFormat(format).parse(txt);
    }
}
