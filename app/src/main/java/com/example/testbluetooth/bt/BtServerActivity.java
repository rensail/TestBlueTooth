package com.example.testbluetooth.bt;



import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.testbluetooth.APP;
import com.example.testbluetooth.R;


public class BtServerActivity extends Activity implements BtBase.BtBaseListener {
    private BtServer btServer;
    private TextView connectstate2_textview, view2_textview;
    private EditText message2_edittext,file_edittext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_server);

        connectstate2_textview = findViewById(R.id.connectstate2_textview);
        view2_textview = findViewById(R.id.view2_textview);

        message2_edittext = findViewById(R.id.message2_edittext);

        btServer = new BtServer(this);
    }

    /**
     * 从UI获取数据并发送
     * @param view
     */
    public void sendMessage(View view){
        if(btServer.isSocketConnected(null)){
            String message = message2_edittext.getText().toString();
            if(message.isEmpty()){
                APP.toast("消息不能为空",0);
            }else{
                btServer.sendMessage(message);
            }
        }else{
               APP.toast("没有连接的蓝牙",0);
        }
    }

    /**
     * 释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(btServer!=null) {
            btServer.releaseBtBaseListener();
            btServer.closeScoket();
            btServer=null;
        }
    }

    @Override
    public void socketState(int state, Object object) {
        String message = "";

        switch (state){

            case BtBase.BtBaseListener.DISCONNECT:
                message = "连接断开";
                connectstate2_textview.setText(message);
                break;

            case BtBase.BtBaseListener.CONNECTED:
                BluetoothDevice device = (BluetoothDevice)object;
                message = String.format("与(%s)设备连接成功",device.getName());
                connectstate2_textview.setText(message);
                break;

            case BtBase.BtBaseListener.MSG:
                message = String.format("\n%s",object);
                view2_textview.append(message);
                break;
        }
    }
}