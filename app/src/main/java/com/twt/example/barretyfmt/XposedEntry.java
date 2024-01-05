package com.twt.example.barretyfmt;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedEntry implements IXposedHookLoadPackage {
    TimerUpdateText timerUpdateText_;
    Map<Integer, TimerUpdateText> timerUpdateTexts_;

    boolean expired(){
        LocalDateTime currentDateTime = LocalDateTime.now();  // 获取当前日期时间
        LocalDateTime targetDateTime = LocalDateTime.of(2024, 1, 5, 02, 10);  // 目标日期时间

        if(currentDateTime.isAfter((targetDateTime))){
            return true;
        }
        return false;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        MyLog.log(lpparam.packageName);
        if(!lpparam.packageName.equals("com.android.systemui")) {
            return;
        };
//        if(expired()){
//            MyLog.log("expired");
//            return;
//        }
        timerUpdateTexts_ = new HashMap<>();
        // 可能有多个
        XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.classLoader,"initMiuiView", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                int id = param.thisObject.hashCode();
                MyLog.log("initMiuiView "+id);
                if(!timerUpdateTexts_.containsKey(id)) {
                    TimerUpdateText update_timer = new TimerUpdateText(param.thisObject);
                    timerUpdateTexts_.put(id, update_timer);
                }
//                if(timerUpdateText_!=null){
//                    timerUpdateText_.stop();
//                }
//                timerUpdateText_ = new TimerUpdateText(param.thisObject);
            }
        });

        XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.classLoader, "updateChargeAndText", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                int id = param.thisObject.hashCode();
                if(!timerUpdateTexts_.containsKey(id)) {
                    return;
                }
                TimerUpdateText update_timer = timerUpdateTexts_.get(id);
                update_timer.updateBatteryText();
//                if(timerUpdateText_==null){
//                    MyLog.log("timerUpdateText_ empty");
//                    return;
//                }
//                timerUpdateText_.updateBatteryText();
            }
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                MyLog.log("before update "+param.thisObject.hashCode());
//                if(timerUpdateText_==null){
//                    MyLog.log("timerUpdateText_ empty");
//                    return;
//                }
//                timerUpdateText_.updateBatteryText();
            }
        });
    }
}
