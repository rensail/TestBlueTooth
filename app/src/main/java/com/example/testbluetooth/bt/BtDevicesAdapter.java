package com.example.testbluetooth.bt;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testbluetooth.R;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Sail 2021.03.08
 * RecycleView控件的适配器Adapter类
 */

public class BtDevicesAdapter extends RecyclerView.Adapter<BtDevicesAdapter.VH> {

    public List<BluetoothDevice>  bluetoothDevices = new ArrayList<>();
    private AdapterListener adapterListener;

    public BtDevicesAdapter(AdapterListener adapterListener){
        this.adapterListener = adapterListener;
        addBound();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.devices_item,parent,false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        BluetoothDevice device = bluetoothDevices.get(position);
        String name = device.getName();
        String address = device.getAddress();
        int bondState = device.getBondState();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            int type = device.getBluetoothClass().getMajorDeviceClass();
            switch (type){
                case BluetoothClass.Device.Major.PHONE:
                    holder.type_imageview.setImageResource(R.drawable.phone);
                    break;
                case BluetoothClass.Device.Major.COMPUTER:
                    holder.type_imageview.setImageResource(R.drawable.computer);
                    break;
                default:
                    holder.type_imageview.setImageResource(R.drawable.bluetooth);
                    break;
            }
            holder.name_textview.setText(name == null ? " ":name);
            holder.address_textview.setText(String.format("%s(%s)",address,bondState==10? "未配对" :"已配对"));

        }
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    class VH extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView name_textview;
        final TextView address_textview;
        final ImageView type_imageview;

        VH (View itemView){
          super(itemView);
          itemView.setOnClickListener(this);
          name_textview = itemView.findViewById(R.id.name_textview);
          address_textview = itemView.findViewById(R.id.address_textview);
          type_imageview = itemView.findViewById(R.id.type_imageview);

        }

        @Override
        public void onClick(View view) {
           int position = getAdapterPosition();
           if(position>=0&&position<=bluetoothDevices.size()){
               adapterListener.onItemClick(bluetoothDevices.get(position));
           }
        }
    }

    /**
     * 添加蓝牙设备
     * @param device 蓝牙设备对象
     */
    public void add(BluetoothDevice device){
        if(bluetoothDevices.contains(device)){
            return;
        }
        bluetoothDevices.add(device);
        notifyDataSetChanged();
    }

    /**
     * 添加已配对蓝牙设备
     */
    public void addBound(){
        Set<BluetoothDevice>  bound_devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if(bound_devices!=null){
            bluetoothDevices.addAll(bound_devices);
        }
    }

    /**
     * 重新搜索蓝牙设备
     */
    public void reSearch(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.cancelDiscovery();
        bluetoothDevices.clear();
        addBound();
        bluetoothAdapter.startDiscovery();
        notifyDataSetChanged();
    }

    /**
     * 监听回调接口
     */
    public interface AdapterListener {
        public  void  onItemClick(BluetoothDevice bluetoothDevice);
    }

}

