package com.example.testbluetooth.bt;

import android.app.usage.ExternalStorageStats;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.example.testbluetooth.APP;
import com.example.testbluetooth.utils.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import androidx.annotation.RequiresApi;


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
    //创建文件路径
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TestBlueTooth/";
    //读数据标志
    private boolean isRead;
    //发送数据标志
    private boolean isSending;
    //socket连接的UUID
    final static UUID  uuid= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final static int TYPE_RECEIVE = 0;
    private final static int TYPE_SEND = 1;
    private BtLogAdapter btLogAdapter=new BtLogAdapter();

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
                        Message msg1 = new Message(TYPE_RECEIVE,"接收数据: "+message);
                        notifyUI(BtBaseListener.MSG,msg1);
                        break;
                    case FLAG_FILE:
                        Util.mkdirs(FILE_PATH);
                        //读取文件名
                        String filename = dataInputStream.readUTF();
                        //读取文件长度
                        long filesize = dataInputStream.readLong();
                        //开始读取文件里面的内容并存储
                        int r;
                        byte[] bytes= new byte[4*1024];
                        long current_len = 0;
                        FileOutputStream fileOutputStream = new FileOutputStream(FILE_PATH+filename);
                        Message msg2 = new Message(TYPE_RECEIVE,"正在接收文件("+filename+"),请稍等...");
                        notifyUI(BtBaseListener.MSG,msg2);
                        while ((r=dataInputStream.read(bytes))!=-1){
                            fileOutputStream.write(bytes,0,r);
                            current_len = current_len+r;
                            if(current_len>=filesize){
                                fileOutputStream.close();
                                break;
                            }
                        }
                        Message msg3 = new Message(TYPE_RECEIVE,"接收("+filename+")文件成功！");
                        notifyUI(BtBaseListener.MSG,msg3);
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
            Message msg1 = new Message(TYPE_SEND,"发送数据: "+message);
            notifyUI(BtBaseListener.MSG,msg1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        isSending = false;
    }

    /**
     * 发送文件
     * @param filepath 文件路径
     */
    public void sendFile(String filepath){
       if(checkSending()){
           return;
       }
       isSending = true;

       //开启一个新的线程
       Util.EXECUTOR.execute(new Runnable() {
           @Override
           public void run() {
               try {
                   FileInputStream fileInputStream = new FileInputStream(filepath);
                   File file = new File(filepath);
                   //写入文件标志
                   dataOutputStream.writeInt(FLAG_FILE);
                   //写入文件名
                   dataOutputStream.writeUTF(file.getName());
                   //写入文件长度
                   dataOutputStream.writeLong(file.length());

                   //开始写入文件内容
                   int r;
                   byte[] bytes = new byte[4*1024];
                   Message msg1= new Message(TYPE_SEND,"正在发送文件("+file.getName()+"),请稍等...");
                   notifyUI(BtBaseListener.MSG,msg1);
                   while ((r=fileInputStream.read(bytes))!=-1){
                       dataOutputStream.write(bytes);
                   }
                   dataOutputStream.flush();
                   Message msg2= new Message(TYPE_SEND,"发送文件("+file.getName()+")成功！");
                   notifyUI(BtBaseListener.MSG,msg2);
                   fileInputStream.close();
               } catch (Exception e) {
                   e.printStackTrace();
               }
               isSending =false;
           }
       });

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


    //TODO —————————————————————————————打开文件管理器获取文件路径——————————————————————————

    /**
     * 安卓4.4以上通过uri获取绝对路径的方法
     * @param context 上下文
     * @param uri
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getPath(Context context, Uri uri){
        if(DocumentsContract.isDocumentUri(context,uri)){
             if(isExternalStorageDocument(uri)){
                 String docID = DocumentsContract.getDocumentId(uri);
                 String[] split = docID.split(":");
                 String type = split[0];
                 if("primary".equals(type)){
                     return Environment.getExternalStorageDirectory()+"/"+ split[1];
                 }
             }
             else if(isDownloadDocument(uri)){
                 String docID = DocumentsContract.getDocumentId(uri);
                 Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docID));
                 return getDataColumn(context,contentUri,null,null);
             }
             else if(isMediaDocument(uri)){
                 String docID = DocumentsContract.getDocumentId(uri);
                 String[] split = docID.split(":");
                 String type = split[0];
                 Uri contentUri=null;
                 if("image".equals(type)){
                     contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                 }
                 else if("video".equals(type)){
                     contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                 }
                 else if("audio".equals(type)){
                     contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                 }

                 String selection = "_id=?";
                 String[] selectionArgs = new String[]{split[1]};

                 return getDataColumn(context,contentUri,selection,selectionArgs);
             }
        }
        else if("file".equalsIgnoreCase(uri.getScheme())){
            return uri.getPath();
        }
        else if("content".equalsIgnoreCase(uri.getScheme())){
            return getDataColumn(context,uri,null,null);
        }

        return " ";
    }

    /**
     * 安卓4.4及以下版本通过uri获取路径的方法
     * @param uri
     * @return
     */
    public String getRealPathFromUri(Context context,Uri uri){
        String result = " ";
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor =  context.getContentResolver().query(uri,projection,null,null,null);
        if(cursor!=null&&cursor.moveToFirst()){
            int column = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            result = cursor.getString(column);
            cursor.close();
        }
        return result;
    }


    /**
     * 获取绝对路径
     */
    public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs){
        String result =" ";
        Cursor cursor =null;
        String column_name = "_data";
        String[] projection = {"_data"};
        cursor = context.getContentResolver().query(uri,projection,selection,selectionArgs,null);
        if(cursor!=null&&cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(column_name);
            result = cursor.getString(column_index);
        }
        return result;
    }


    public boolean isExternalStorageDocument(Uri uri){
        return  "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadDocument(Uri uri){
        return  "com.android.providers.download.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri){
        return  "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
