package com.android.apparatus.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.apparatus.Constant;
import com.android.apparatus.serialport.Device;
import com.android.apparatus.serialport.SerialPortFinder;
import com.android.apparatus.serialport.SerialPortManager;
import com.android.apparatus.serialport.listener.OnOpenSerialPortListener;
import com.android.apparatus.serialport.listener.OnSerialPortDataListener;
import com.android.apparatus.utils.LoggerUtil;
import com.android.apparatus.utils.Utils;
import com.laile.serialport.SerialPort;
import com.laile.serialport.SerialPortDataManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * author: geshenjibi on 2019-06-16 21:24.
 * email: geshenjibi@163.com
 */
public class SerialPortService extends Service {
    private static SerialPortService mSerialPortService;

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     *
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    SerialPortDataManager mSerialPortManager;
    SerialPort mSerialPort;
    SerialPortDataManager.ReceiveListener mlistener = new SerialPortDataManager.ReceiveListener() {
        @Override
        public void onReceive(int what, byte[] bean) {

        }
    };
    String TAG = "SerialPortService";

    @Override
    public void onCreate() {
        super.onCreate();
        LoggerUtil.println("创建了SerialPortService");
        mSerialPortService = this;
        //创建串口数据管理类
        try {
            String path = "/dev/ttyS1";
            int baudrate = 115200;
            mSerialPortManager = SerialPortDataManager.getInstance(path, baudrate, 0, mlistener);
            mSerialPort = mSerialPortManager.getSerialPort();
            LoggerUtil.println("串口0000000000000初始化");
            if (mSerialPort != null) {
                //初始化成功
                LoggerUtil.println("打开串口成功");
                // Toast.makeText(context,"打开串口成功",Toast.LENGTH_SHORT).show();
            } else {
                // Toast.makeText(context,"打开串口失败",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerUtil.println("串口Laile20初始化失败");
        }

        OnOpenSerialPortListener mListener = new OnOpenSerialPortListener() {
            @Override
            public void onSuccess(File device) {

            }

            @Override
            public void onFail(File device, Status status) {

            }
        };

        SerialPortManager mSerialPortManager = new SerialPortManager();
        //发送串口数据
//		mSerialPortManager.sendBytes();
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        ArrayList<Device> devices = serialPortFinder.getDevices();
        for (int i = 0; i < devices.size(); i++) {
            final File file = devices.get(i).getFile();
            boolean openSerialPort = mSerialPortManager.setOnOpenSerialPortListener(mListener).setOnSerialPortDataListener(new OnSerialPortDataListener() {
                        @Override
                        public void onDataReceived(byte[] bytes) {
                            Log.i(TAG, "onDataReceived [ byte[] ]: " + Arrays.toString(bytes));
                            Log.i(TAG, "onDataReceived [ String ]: " + new String(bytes));
                            final byte[] finalBytes = bytes;

                            String textTemp = Utils.bytesToHexString(finalBytes);
//                            if (textTemp.contains("09")){
//                                Intent intent = new Intent(MainActivity.this, FileGalleryActivity.class);
//                                startActivity(intent);
//                                return;
//                            }
//                            if (Constant.KEYCODE_ON.contains(textTemp)) {
//                                textTemp = Constant.KEYCODE_ON;
//                            }else if (Constant.KEYCODE_OK.contains(textTemp)) {
//                                textTemp = Constant.KEYCODE_OK;
//                            }
                            final String text = textTemp;
                            LoggerUtil.println(String.format("接收\n%s" + file.getAbsolutePath(), text));

                            switch (text) {
                                //菜单键
                                case Constant.KEYCODE_MENU:
                                    break; // 可选
                                case Constant.KEYCODE_UP:
                                    break; // 可选
                                case Constant.KEYCODE_DOWN:
                                    break; // 可选
                                case Constant.KEYCODE_OK:
                                    break; // 可选
                                case Constant.KEYCODE_ON:

                                    break; // 可选
                                case Constant.KEYCODE_LEFT:

                                    break; // 可选
                                case Constant.KEYCODE_RIGHT:

                                    break; // 可选
                                case Constant.KEYCODE_BACK:

                                    break; // 可选

                                default: // 可选
                                    break;
                            }

                            if (text.equals(Constant.KEYCODE_MENU)) {
                            }

                        }

                        @Override
                        public void onDataSent(byte[] bytes) {
                            Log.i(TAG, "onDataSent [ byte[] ]: " + Arrays.toString(bytes));
                            Log.i(TAG, "onDataSent [ String ]: " + new String(bytes));
                            final byte[] finalBytes = bytes;
//                            showToast(String.format("发送\n%s", new String(finalBytes)));
                        }
                    }).openSerialPort(file, 115200);

            Log.i(TAG, "onCreate: openSerialPort = " + openSerialPort);
        }
    }

    public void sendSerialPortData(byte[] datas) {
        mSerialPortManager.write(datas);
    }

    public static SerialPortService getInstance() {
        return mSerialPortService;

    }
}
