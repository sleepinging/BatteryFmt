package com.twt.example.barretyfmt;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.XposedHelpers;

public class TimerUpdateText {
    private Object miuiBatteryMeterView_ = null;
    private TextView mBatteryPercentView_ = null;
    private TextView mBatteryTextDigitView_ = null;
    private Handler handler_;
    Timer timer_;

    public TimerUpdateText(Object miuiBatteryMeterView){
        if(miuiBatteryMeterView==null){
            MyLog.log("miuiBatteryMeterView empty");
            return;
        }
        miuiBatteryMeterView_ = miuiBatteryMeterView;
        mBatteryPercentView_ = (TextView) XposedHelpers.getObjectField(miuiBatteryMeterView, "mBatteryPercentView");
        mBatteryTextDigitView_ = (TextView) XposedHelpers.getObjectField(miuiBatteryMeterView, "mBatteryTextDigitView");
        if(mBatteryPercentView_==null){
            MyLog.log("mBatteryPercentView_ empty");
            return;
        }
        if(mBatteryTextDigitView_ ==null){
            MyLog.log("mBatteryTextDigitView_ empty");
            return;
        }

        handler_ = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.what != 114514) {
                    return;
                }
                // 定期刷新
                try {
                    XposedHelpers.callMethod(miuiBatteryMeterView_, "updateChargeAndText");
                }catch (Exception e){
                    MyLog.log("error: "+ e.toString());
                    MyLog.log("error: "+ errInfo(e));
                }
            }
        };

        timer_ = new Timer();
        timer_.schedule(new TimerTask() {
            @Override
            public void run() {
                handler_.obtainMessage(114514, null).sendToTarget();
            }
        },2000,5000);
    }

    public void stop(){
        if(timer_!=null){
            timer_.cancel();
        }
    }

    public void updateBatteryText(){
        if(miuiBatteryMeterView_==null){
            MyLog.log("miuiBatteryMeterView_ empty");
            return;
        }
        if(mBatteryPercentView_==null){
            MyLog.log("mBatteryPercentView_ empty");
        }
        //显示电量小数点
//        XposedHelpers.setObjectField(miuiBatteryMeterView_, "mLevelString", "114");
        String levelString = (String) XposedHelpers.getObjectField(miuiBatteryMeterView_, "mLevelString");
        String fmt_string = getAccurateBatteryLevel(levelString);
//        MyLog.log(fmt_string);
        mBatteryPercentView_.setText(fmt_string);
        mBatteryTextDigitView_.setText(fmt_string);
    }

    // 从内核读取可以精确到0.01的电量，但有些内核数值是错的，所以需要和系统反馈的电量(approximate)比对，如果差距太大则认为内核数值无效，不再读取
    public static String getAccurateBatteryLevel(String default_str_value){
        String accurate_value = default_str_value;
        try {
            FileInputStream fis = new FileInputStream("/sys/class/power_supply/bms/capacity_raw");
            int iAvail = fis.available();
            byte[] bytes = new byte[iAvail>10?10:iAvail];
            int read = fis.read(bytes);
            byte[] zero = new byte[1]; zero[0] = (byte) 0; String s = new String(zero);
            String raw = new String(bytes).replace(s,"").replace("\n","");
            float raw_value = ((float)Integer.parseInt(raw))/100;
            int default_value = Integer.parseInt(default_str_value);
            if(Math.abs(default_value - raw_value)>5){
                MyLog.log(String.format("%d - %.2f > 5", default_value, raw_value));
                return default_str_value;
            }
            return String.format("%.2f",raw_value);
        }catch (Throwable ign){
            MyLog.log("error: "+ ign.toString());
        }
        return accurate_value;
    }

    public static String errInfo(Exception e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            // 将出错的栈信息输出到printWriter中
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
        return sw.toString();
    }
}
