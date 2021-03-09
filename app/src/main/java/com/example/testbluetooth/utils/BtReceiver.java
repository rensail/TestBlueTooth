package com.example.testbluetooth.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Sail 2021.03.08
 * 监听蓝牙搜索的广播
 */
public class BtReceiver extends BroadcastReceiver {
    private Listener receiver_listener;
    public BtReceiver(Context context,Listener listener ){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        context.registerReceiver(this,intentFilter);
        receiver_listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action==null){
            return;
        }
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        switch (action){
            case BluetoothDevice.ACTION_FOUND:
                receiver_listener.foundDevices(device);

        }
    }

    /**
     * 监听回调接口
     */
    public interface Listener{
        public void foundDevices(BluetoothDevice device);
    }
}


