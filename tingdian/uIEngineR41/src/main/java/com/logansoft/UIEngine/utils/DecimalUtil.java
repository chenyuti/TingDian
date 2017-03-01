package com.logansoft.UIEngine.utils;

import java.text.DecimalFormat;

public class DecimalUtil {
    public static String roundDouble2String(String format,Double value){
        DecimalFormat decimalFormat = new DecimalFormat(format);
        String a = decimalFormat.format(value);
        return a;
    }
    
    public static String roundFloat2String(String format,Float value){
        DecimalFormat decimalFormat = new DecimalFormat(format);
        String a = decimalFormat.format(value);
        return a;
    }
}
