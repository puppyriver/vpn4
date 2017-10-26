package com.asb.pms.util;

/**
 * Created by Administrator on 2017/9/14.
 */
public class MathUtil {
    public MathUtil() {
    }

    public static boolean isNumber(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return -1;
        }
    }

    public static double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception ex) {
            return -1;
        }
    }

    public static long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception ex) {
            return -1;
        }
    }
    public static float formatFloat(float db) {
        if (!(db > 0)) return 0.0f;
        if (db == Float.NaN)
            return 0;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        try {
            if (db < 0.01 && db != 0)
                return 0.01f;
            else
                return Float.parseFloat(df.format(db));
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0f;
        }
    }
    public static double formatDouble(double db) {
        if (!(db > 0)) return 0.0d;
        if (db == Double.NaN)
            return 0;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        try {
            if (db < 0.01 && db != 0)
                return 0.01;
            else
                return Double.parseDouble(df.format(db));
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0;
        }
    }

    public static void main(String[] args) {
        System.out.println(formatDouble(3.23923234234));
        MathUtil mathutil = new MathUtil();
    }
}
