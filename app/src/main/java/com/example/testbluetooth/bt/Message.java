package com.example.testbluetooth.bt;

public class Message {
    int type;
    String msg;

    public Message(int type,String msg){
        this.msg = msg;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
