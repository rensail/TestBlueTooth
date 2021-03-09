package com.example.testbluetooth.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.testbluetooth.utils.Util;

import java.io.IOException;

public class BtClient extends BtBase{
    private  BtBaseListener btclientListener;

    /**
     * BtClient类的构造函数
     * @param btBaseListener
     */
    public BtClient(BtBaseListener btBaseListener) {
        super(btBaseListener);
        btclientListener = btBaseListener;
    }


    public void connect(BluetoothDevice bluetoothDevice){
        closeScoket();
        try {
            final BluetoothSocket bluetoothSocket= bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            //开启子线程
            Util.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    loopRead(bluetoothSocket);
                }
            });
        } catch (IOException e) {
            closeScoket();
            e.printStackTrace();
        }
    }

}
