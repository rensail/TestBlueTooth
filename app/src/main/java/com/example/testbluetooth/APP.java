package com.example.testbluetooth;


import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

/**
 * SAIL 2021.03.01
 *
 * 用来生成Toast提示和更新UI
 */

//Class APP 需要在AndroidManifest中设置android:name=".APP"属性，否则mtoast会为空报错。
public class APP extends Application {
    private final static String TAG = "APP";
    private static  Toast mtoast;
    private final  static Handler mhandler = new Handler();

    @SuppressLint("ShowToast")
    @Override
    public void onCreate() {
        super.onCreate();
        mtoast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
    }

    /**
     * 用来设置Toast的方法
     *
     * @param message 显示信息
     * @param duration 显示时间
     */
    public static void toast(String message, int duration){
        if(mtoast!=null) {
            mtoast.setText(message);
            mtoast.setDuration(duration);
            mtoast.show();
        }
    }


    /**
     * 用来跑更新UI的线程
     *
     * @param runnable 线程对象
     */
    public static void runOnUI(Runnable runnable){
        mhandler.post(runnable);
    }

}
