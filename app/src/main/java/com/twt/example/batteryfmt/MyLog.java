package com.twt.example.batteryfmt;

import de.robv.android.xposed.XposedBridge;

public class MyLog {
    static void log(String msg){
        XposedBridge.log("BatteryFmt: "+ msg);
    }
}
