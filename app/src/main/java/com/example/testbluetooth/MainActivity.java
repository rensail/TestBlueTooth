package com.example.testbluetooth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.testbluetooth.bt.BtClientActivity;
import com.example.testbluetooth.bt.BtServerActivity;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_CODE = 0;
    private BluetoothAdapter bluetoothAdapter;
    private Button bediscovered_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bediscovered_button = findViewById(R.id.bediscovered_button);

        if(BluetoothAdapter.getDefaultAdapter().getScanMode()==BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            bediscovered_button.setClickable(false);
        }

        //动态权限申请
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_CODE);
            }
        }


        //判断终端是否支持经典蓝牙BT
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            APP.toast("终端不支持！",0);
            finish();
            return;
        }
        else{
            //如果蓝牙未开启，直接开启蓝牙
            if(!bluetoothAdapter.isEnabled()){
                bluetoothAdapter.enable();
            }
            APP.toast("蓝牙已开启!",0);
        }

        //判断终端是否支持低功耗蓝牙BLE
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            APP.toast("终端不支持低功耗蓝牙BLE",0);
            finish();
            return;
        }


    }

    //动态权限申请结果返回
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    APP.toast("已授权", 0);
                } else {
                    APP.toast("已拒绝授权", 0);
                }
                break;

            default:
                break;
        }
    }



    public  void openBtClient(View view){
        startActivity(new Intent(MainActivity.this, BtClientActivity.class));
    }

    public  void openBtServer(View view){
        startActivity(new Intent(MainActivity.this, BtServerActivity.class));
    }

    /**
     * 将设备设置成可被其他设备蓝牙搜索到
     * @param view
     */
    public void  beDiscovered(View view){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent intent = null;
        if(bluetoothAdapter.getScanMode()!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(intent);
        }
    }
}