package com.example.testbluetooth.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.example.testbluetooth.utils.Util;

import java.io.IOException;

public class BtServer extends BtBase{
    private BluetoothServerSocket bluetoothServerSocket;
    private final static String TAG = BtServer.class.getSimpleName();

    /**
     * BtServer类的构造函数
     * @param btBaseListener
     */
    public BtServer(BtBaseListener btBaseListener) {
        super(btBaseListener);
        listenConnect();
    }

    /**
     * 监听是否有socket连接
     */
    public  void  listenConnect(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(TAG,uuid);
            //如果有连接，则开启子线程接收数据
            Util.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    BluetoothSocket bluetoothSocket = null;
                    try {
                        bluetoothSocket = bluetoothServerSocket.accept();
                        bluetoothServerSocket.close();
                        loopRead(bluetoothSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            closeScoket();
        }
    }

    @Override
    public void closeScoket() {
        super.closeScoket();
         try {
             bluetoothServerSocket.close();
         }catch (Exception e){
             e.printStackTrace();
         }
    }
}
