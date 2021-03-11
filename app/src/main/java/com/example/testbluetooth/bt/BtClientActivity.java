package com.example.testbluetooth.bt;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.testbluetooth.APP;
import com.example.testbluetooth.R;
import com.example.testbluetooth.utils.BtReceiver;

public class BtClientActivity extends Activity implements BtReceiver.Listener,BtDevicesAdapter.AdapterListener, BtBase.BtBaseListener {
    private  Button search_button,research_button;
    private TextView connectstate_textview;
    private EditText message_edittext,file_edittext;
    private RecyclerView devices_recycleview,log_recyclerview;
    private  BtDevicesAdapter btDevicesAdapter = new BtDevicesAdapter(this);
    private BtLogAdapter btLogAdapter = new BtLogAdapter();
    private  BtReceiver btReceiver;
    private  BtClient btClient;
    private  final static int CHOOSE_FILE = 0;
    //文件的路径
    private  String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_client);
        search_button = findViewById(R.id.search_button);
        research_button = findViewById(R.id.research_button);
        research_button.setClickable(false);

        devices_recycleview = findViewById(R.id.devices_recyclerview);
        devices_recycleview.setLayoutManager(new LinearLayoutManager(this));
        devices_recycleview.setAdapter(btDevicesAdapter);

        connectstate_textview = findViewById(R.id.connectstate_textview);

        log_recyclerview = findViewById(R.id.log_recyclerview);
        log_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        log_recyclerview.setAdapter(btLogAdapter);


        message_edittext = findViewById(R.id.message_edittext);
        file_edittext = findViewById(R.id.file_edittext);

        btReceiver = new BtReceiver(this,this);
        btClient = new BtClient(this);
    }

    /**
     * 开始扫描蓝牙按钮事件
     */
    public void searchButton(View view){
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
        research_button.setClickable(true);
        search_button.setClickable(false);
        APP.toast("开始扫描",0);
    }

    public void researchButton(View view){
        btDevicesAdapter.reSearch();
        APP.toast("重新扫描",0);
    }

    /**
     * 停止扫描蓝牙按钮事件
     */
    public void stopButton(View view){
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        APP.toast("停止扫描",0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btReceiver);
        //释放对BtClient类的监听回调
        btClient.releaseBtBaseListener();
        //关闭socket连接
        btClient.closeScoket();
    }

    /**
     * BtReceiver类中的监听回调接口重载
     * @param device 蓝牙设备对象
     */
    @Override
    public void foundDevices(BluetoothDevice device) {
        btDevicesAdapter.add(device);
    }


    /**
     * BtDevicesAdapter类的监听回调接口重载
     */
    public void onItemClick(BluetoothDevice bluetoothDevice){
        if(btClient.isSocketConnected(null)){
            APP.toast("蓝牙已连接",0);
            return;
        }else{
            btClient.connect(bluetoothDevice);
            connectstate_textview.setText("正在连接蓝牙");
        }
    }


    /**
     * 从UI获取数据并发送
     */
    public void sendMessage(View view){
        if(btClient.isSocketConnected(null)){
            String message = message_edittext.getText().toString();
            if(message.isEmpty()){
                APP.toast("发送消息不能为空",0);
            }else {
                btClient.sendMessage(message);
                message_edittext.setText("");
            }
        }else{
            APP.toast("没有连接的蓝牙",0);
        }
    }

    /**
     * 从UI获取文件并发送
     */
    public void  sendFile(View view){
        if(btClient.isSocketConnected(null)){
            String filepath = file_edittext.getText().toString();
            if(filepath.isEmpty()){
                APP.toast("发送文件路径不能为空",0);
            }else{
                btClient.sendFile(filepath);
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
                        path = btClient.getPath(this,uri);
                        file_edittext.setText(path);
                    } else {
                        path = btClient.getRealPathFromUri(this,uri);
                        file_edittext.setText(path);
                    }
                break;
            }
        }
    }

    /**
     * BtBase类的监听回调接口重载
     * @param state  socket连接状态
     * @param object 传递的参数对象
     */
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