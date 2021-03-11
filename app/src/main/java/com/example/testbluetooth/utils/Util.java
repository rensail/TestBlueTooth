package com.example.testbluetooth.utils;

import android.util.Log;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Sail 2021.03.09
 */
public class Util {
    //用来创建新的线程池
    public final static Executor EXECUTOR = Executors.newCachedThreadPool();

    public final static void mkdirs(String filepath){
        boolean mk= new File(filepath).mkdirs();
        Log.d("Sail","创建文件成功:"+mk);
    }
}
