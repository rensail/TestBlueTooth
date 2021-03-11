package com.example.testbluetooth.bt;



import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.testbluetooth.APP;
import com.example.testbluetooth.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class BtServerActivity extends Activity implements BtBase.BtBaseListener {
    private BtServer btServer;
    private TextView connectstate_textview;
    private EditText message_edittext,file_edittext;
    private  final static int CHOOSE_FILE = 0;
    private  String path;
    private BtLogAdapter btLogAdapter = new BtLogAdapter();
    private RecyclerView log_recyclerview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_server);

        connectstate_textview = findViewById(R.id.connectstate_textview);

        log_recyclerview = findViewById(R.id.log_recyclerview);
        log_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        log_recyclerview.setAdapter(btLogAdapter);

        message_edittext = findViewById(R.id.message_edittext);
        file_edittext = findViewById(R.id.file_edittext);

        btServer = new BtServer(this);
    }

    /**
     * 从UI获取数据并发送
     * @param view
     */
    public void sendMessage(View view){
        if(btServer.isSocketConnected(null)){
            String message = message_edittext.getText().toString();
            if(message.isEmpty()){
                APP.toast("消息不能为空",0);
            }else{
                btServer.sendMessage(message);
                message_edittext.setText("");
            }
        }else{
               APP.toast("没有连接的蓝牙",0);
        }
    }


    /**
     * 从UI获取文件路径并发送
     */
    public void  sendFile(View view){
        if(btServer.isSocketConnected(null)){
            String filepath = file_edittext.getText().toString();
            if(filepath.isEmpty()){
                APP.toast("发送文件路径不能为空",0);
            }else{
                btServer.sendFile(filepath);
                file_edittext.setText("");
            }
        }else{
            APP.toast("没有连接的蓝牙",0);
        }
    }


    /**
     * 选择文件
     */
    public void chooseFile(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //设置文件类型
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,CHOOSE_FILE);
    }

    /**
     * Activity返回回来的结果
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data 数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            switch (requestCode) {
                case CHOOSE_FILE:
                    Uri uri = data.getData();
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        path = btServer.getPath(this,uri);
                        file_edittext.setText(path);
                    } else {
                        path = btServer.getRealPathFromUri(this,uri);
                        file_edittext.setText(path);
                    }
                    break;
            }
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
                connectstate_textview.setText(message);
                break;

            case BtBase.BtBaseListener.CONNECTED:
                BluetoothDevice device = (BluetoothDevice)object;
                message = String.format("与(%s)设备连接成功",device.getName());
                connectstate_textview.setText(message);
                break;

            case BtBase.BtBaseListener.MSG:
                Message msg = (Message)object;
                btLogAdapter.addMessage(msg);
                //自动定位到最新的位置
                btLogAdapter.notifyItemInserted(btLogAdapter.getItemCount()-1);
                log_recyclerview.scrollToPosition(btLogAdapter.getItemCount()-1);
                break;
        }
    }
}