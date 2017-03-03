package infox.vpn4.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Author: Ronnie.Chen
 * Date: 2016/10/11
 * Time: 11:23
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class DateUtil {
    private Logger logger = LoggerFactory.getLogger(DateUtil.class);
    final static String[] possibleDateFormats = {
                             /* RFC 1123 with 2-digit Year */"EEE, dd MMM yy HH:mm:ss z",
                             /* RFC 1123 with 4-digit Year */"EEE, dd MMM yyyy HH:mm:ss z",
                             /* RFC 1123 with no Timezone */"EEE, dd MMM yy HH:mm:ss",
                             /* Variant of RFC 1123 */"EEE, MMM dd yy HH:mm:ss",
                             /* RFC 1123 with no Seconds */"EEE, dd MMM yy HH:mm z",
                             /* Variant of RFC 1123 */"EEE dd MMM yyyy HH:mm:ss",

                             /* RFC 1123 with no Day */"dd MMM yy HH:mm:ss z",
                             /* RFC 1123 with no Day or Seconds */"dd MMM yy HH:mm z",
                             /* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:ssZ",
                             /* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:ss'Z'",
                             /* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:sszzzz",
                             /* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:ss z",
                             /* ISO 8601 */"yyyy-MM-dd'T'HH:mm:ssz",
                             /* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:ss.SSSz",
                             /* ISO 8601 slightly modified */"yyyy-MM-dd'T'HHmmss.SSSz",
                             /* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:ss",
                             /* ISO 8601 w/o seconds */"yyyy-MM-dd'T'HH:mmZ",
                             /* ISO 8601 w/o seconds */"yyyy-MM-dd'T'HH:mm'Z'",
                             /* RFC 1123 without Day Name */"dd MMM yyyy HH:mm:ss z",
                             /* RFC 1123 without Day Name and Seconds */"dd MMM yyyy HH:mm z",
                             /* Simple Date Format */"yyyy-MM-dd",
                             /* Simple Date Format */"MMM dd, yyyy",

            "EEE MMM dd HH:mm:ss yyyy","yyyy-MM-dd HH:mm:ss","yyyyMMddHHmmss"


    };

    private static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String formatDate(Date date) {
        return defaultDateFormat.format(date);
    }

    public static Date parse(String txt,String format) throws ParseException {
        return new SimpleDateFormat(format).parse(txt);
    }
    public static Date parse(String txt) throws Exception {
        if (txt == null) return null;
        TimeZone TIMEZONE = TimeZone.getTimeZone("GMT");
        SimpleDateFormat[] CUSTOM_DATE_FORMATS = new SimpleDateFormat[possibleDateFormats.length];
        CUSTOM_DATE_FORMATS = new SimpleDateFormat[possibleDateFormats.length * 2];

        for (int i = 0; i < possibleDateFormats.length; i+=2) {
            CUSTOM_DATE_FORMATS[i] = new SimpleDateFormat(possibleDateFormats[i], Locale.ENGLISH);
            CUSTOM_DATE_FORMATS[i].setTimeZone(TIMEZONE);
            CUSTOM_DATE_FORMATS[i+1] = new SimpleDateFormat(possibleDateFormats[i], Locale.getDefault());
            CUSTOM_DATE_FORMATS[i+1].setTimeZone(TIMEZONE);

        }




        int i = 0;
        while (i < CUSTOM_DATE_FORMATS.length) {
            try {
             //  synchronized (CUSTOM_DATE_FORMATS[i]) {
                if (CUSTOM_DATE_FORMATS[i] != null) {
                    synchronized (CUSTOM_DATE_FORMATS[i]) {
                        return CUSTOM_DATE_FORMATS[i].parse(txt);
                    }
                }
              //  }
            } catch (Exception e) {

            } finally {
                i++;
            }
        }

        if (txt.startsWith("20") || txt.startsWith("19")) {
            if (txt.length() > 14)
                return new SimpleDateFormat("yyyyMMddHHmmss").parse(txt.substring(0,14));
        }

        throw new Exception("unparseable date string : "+txt);

    }

    public static String format(Date date, String format){
        if(date == null)
            return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
    /**
     * format : yyyy-MM-dd HH:mm:ss
     * @param date
     * @return
     */
    public static String format(Date date){
        if(date == null)
            return "";
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static Calendar latestWeek(int weeks){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, - weeks);
        return calendar;
    }

    public static Calendar latestMonth(int months){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, - months);
        return calendar;
    }

    public static Calendar lastMonth(int months){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, - months);
        return firstDayOfMonth(calendar);
    }

    public static Calendar firstDayOfMonth(Calendar calendar){
        if(calendar == null)
            return null;
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    public static Calendar firstDayOfWeek(Calendar calendar){
        if(calendar == null)
            return null;
        int mondayPlus = 0;
        int dayOfWeek= calendar.get(Calendar.DAY_OF_WEEK) - 1;//一周一为一周的第一天
        if (dayOfWeek == 1) {
            mondayPlus = 0;
        } else {
            mondayPlus = 1 - dayOfWeek;
        }
        Calendar current = Calendar.getInstance();
        current.add(Calendar.DAY_OF_MONTH, mondayPlus);
        return current;
    }

    public static Calendar firstDay(Calendar calendar){
        if(calendar == null)
            return null;
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }


    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmsszzzzzz");
        Date parse1 = sdf.parse("20160825222189100000");

        String format = sdf.format(new Date());
        System.out.println("format = " + format);
        Date parse = DateUtil.parse("Mon Oct 10 13:56:00 2016");
        System.out.println("parse = " + parse);
    }

    private static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
    public static String getDayString(Date time) {
        return dayFormat.format(time);
    }
}
