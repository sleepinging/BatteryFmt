package com.twt.example.barretyfmt;

import de.robv.android.xposed.XposedBridge;

public class MyLog {
    static void log(String msg){
        XposedBridge.log("BatteryFmt: "+ msg);
    }
}
