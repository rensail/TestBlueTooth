package com.example.testbluetooth.bt;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.testbluetooth.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BtLogAdapter extends RecyclerView.Adapter<BtLogAdapter.VH>{
    public List<Message>  messageList = new ArrayList<>();
    private final static int TYPE_RECEIVE = 0;
    private final static int TYPE_SEND = 1;


    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_item,parent,false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Message message = messageList.get(position);
        int type = message.getType();
        String msg = message.getMsg();

        if(type==TYPE_RECEIVE){
            holder.receive_linearlayout.setVisibility(View.VISIBLE);
            holder.send_linearlayout.setVisibility(View.GONE);
            holder.receive_textview.setText(msg);
        }
        else if(type == TYPE_SEND){
            Log.d("Sail","TYPE_SEND");
            holder.send_linearlayout.setVisibility(View.VISIBLE);
            holder.receive_linearlayout.setVisibility(View.GONE);
            holder.send_textview.setText(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    class VH extends RecyclerView.ViewHolder{

        final TextView receive_textview,send_textview;
        final LinearLayout receive_linearlayout, send_linearlayout;

        public VH(View itemView) {
            super(itemView);
            receive_textview = itemView.findViewById(R.id.receive_textview);
            send_textview = itemView.findViewById(R.id.send_textview);
            receive_linearlayout = itemView.findViewById(R.id.recevie_linearlayout);
            send_linearlayout = itemView.findViewById(R.id.send_linearlayout);
        }
    }

    public void addMessage(Message message){
        if(message!=null){
            messageList.add(message);
            notifyDataSetChanged();
        }
    }
}
