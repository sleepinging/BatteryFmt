package com.twt.example.batteryfmt;

import android.text.TextUtils;

import java.lang.reflect.Method;

public class OsHelper {
    /**
     * 是否为澎湃系统
     *
     * @return true为澎湃系统
     */
    public static boolean isHyperOs() {
        MyLog.log("android.os.Build.BRAND:" + android.os.Build.BRAND);
        if(!"Xiaomi".equalsIgnoreCase(android.os.Build.BRAND) && !"Redmi".equalsIgnoreCase(android.os.Build.BRAND)){
            return false;
        }
        String ver = getHyperVersion();
        MyLog.log("ver:"+ver);
        return ver.startsWith("OS");
    }

    /**
     * 获取澎湃系统版本号
     *
     * @return 版本号
     */
    public static String getHyperVersion() {
        return getProp("ro.mi.os.version.name", "");
    }

    private static String getProp(String property, String defaultValue) {
        try {
            Class spClz = Class.forName("android.os.SystemProperties");
            Method method = spClz.getDeclaredMethod("get", String.class);
            String value = (String) method.invoke(spClz, property);
            if (TextUtils.isEmpty(value)) {
                return defaultValue;
            }
            return value;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defaultValue;
    }
}
