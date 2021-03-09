package com.example.testbluetooth.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.testbluetooth.APP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;


/**
 * Sail 2021.03.08
 */
public class BtBase {

    private BtBaseListener btBaseListener;
    private BluetoothSocket bluetoothSocket;
    private DataOutputStream dataOutputStream;
    //操作短消息标志
    private final static  int FLAG_MSG=0;
    //操作文件标志
    private final static  int FLAG_FILE=1;
    //读数据标志
    private boolean isRead;
    //发送数据标志
    private boolean isSending;
    //socket连接的UUID
    final static UUID  uuid= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * BtBase类的构造函数
     */
   public BtBase(BtBaseListener btBaseListener){
       this.btBaseListener=btBaseListener;
   }


    /**
     * 循环读取数据的方法
     */
    public void loopRead(BluetoothSocket bluetoothSocket){
        this.bluetoothSocket=bluetoothSocket;
        try {
            if(!bluetoothSocket.isConnected()){
                bluetoothSocket.connect();
            }
            notifyUI(btBaseListener.CONNECTED,bluetoothSocket.getRemoteDevice());
            DataInputStream  dataInputStream = new DataInputStream(bluetoothSocket.getInputStream());
            dataOutputStream = new DataOutputStream(bluetoothSocket.getOutputStream());
            isRead = true;
            while (isRead){
                switch (dataInputStream.readInt()){
                    //读取短消息
                    case FLAG_MSG:
                        String message=dataInputStream.readUTF();
                        notifyUI(BtBaseListener.MSG,"接收短消息"+message);
                        break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 发送短消息
     */
    public void  sendMessage(String message){
        if(checkSending()){
            return;
        }
        isSending = true;
        try {
            dataOutputStream.writeInt(FLAG_MSG);
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
            notifyUI(BtBaseListener.MSG,"发送数据"+message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isSending = false;
    }


    /**
     * 判断socket是否连接
     */
    public boolean isSocketConnected(BluetoothDevice bluetoothDevice){
        boolean isconnceted = (bluetoothSocket!=null&&bluetoothSocket.isConnected());
        if(bluetoothDevice==null){
            return isconnceted;
        }
        return isconnceted&&bluetoothSocket.getRemoteDevice().equals(bluetoothDevice);
    }

    /**
     * 关闭socket,断开连接
     */
    public void closeScoket(){
        if(bluetoothSocket!=null){
            //更改读数据标志，不再读数据
            isRead =false;
            try {
                bluetoothSocket.close();
                notifyUI(btBaseListener.DISCONNECT,null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 用于通知是否正在发送消息
     */
    private boolean checkSending(){
        if(isSending==true){
            APP.toast("正在发送数据请稍后再试！",0);
            return true;
        }
        return false;
    }

    /**
     *用于通知UI层
     */
    public void notifyUI(int state,Object object){
        APP.runOnUI(new Runnable() {
            @Override
            public void run() {
                try {
                    if(btBaseListener!=null){
                        btBaseListener.socketState(state,object);
                    }
                }catch (Throwable t){
                    t.printStackTrace();
                }
            }
        });
    }


    /**
     * 用来更新UI的监听接口
     */
    public interface BtBaseListener{
        int DISCONNECT = 0;
        int CONNECTED = 1;
        int MSG = 2;

        public void socketState(int state, Object object);
    }

    /**
     * 释放监听引用
     */
    public void releaseBtBaseListener(){
        if(btBaseListener!=null){
            btBaseListener=null;
        }
    }
}
