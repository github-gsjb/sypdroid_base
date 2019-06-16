package com.android.apparatus.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.apparatus.Constant;
import com.android.apparatus.activity.MenuActivity;
import com.android.apparatus.model.CmdBean;
import com.android.apparatus.model.CmdBeanParser;
import com.android.apparatus.serialport.Device;
import com.android.apparatus.serialport.SerialPortFinder;
import com.android.apparatus.serialport.SerialPortManager;
import com.android.apparatus.serialport.SerialPortReceiveDataEvent;
import com.android.apparatus.serialport.listener.OnOpenSerialPortListener;
import com.android.apparatus.serialport.listener.OnSerialPortDataListener;
import com.android.apparatus.utils.LoggerUtil;
import com.android.apparatus.utils.StringHexUtils;
import com.android.apparatus.utils.Utils;

import org.greenrobot.eventbus.EventBus;

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

    String TAG = "SerialPortService";
    SerialPortManager mSerialPortManager;

    @Override
    public void onCreate() {
        super.onCreate();
        LoggerUtil.println("创建了SerialPortService");
        mSerialPortService = this;
        //创建串口数据管理类
        //串口打开的监听器
        OnOpenSerialPortListener mListener = new OnOpenSerialPortListener() {
            @Override
            public void onSuccess(File device) {

            }

            @Override
            public void onFail(File device, Status status) {

            }
        };
        //串口管理类
        mSerialPortManager = new SerialPortManager();
        String devPath = "/dev/ttyS1";///dev/ttyS1
//         File devFile = new File(devPath);
        int baudRate = 115200;
        //发送串口数据
//		mSerialPortManager.sendBytes();
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        ArrayList<Device> devices = serialPortFinder.getDevices();
        for (int i = 0; i < devices.size(); i++) {
            final File devFile = devices.get(i).getFile();
//            if (file.getAbsolutePath().contains("ttyS1")){
//                devFile=file;
//            }
//        }
            boolean openSerialPort = mSerialPortManager.setOnOpenSerialPortListener(mListener).setOnSerialPortDataListener(new OnSerialPortDataListener() {
                @Override
                public void onDataReceived(byte[] bytes) {
                    if (bytes != null && bytes.length > 0) {
                        for (int i = 0; i < bytes.length; i++) {
                            CmdBean cmdBean = CmdBeanParser.parseData(bytes[i]);
                            if (cmdBean != null) {
                                String receiveData = StringHexUtils.ByteArrToHex(cmdBean.getBytes());
                                LoggerUtil.println("串口接收的dataBean", receiveData);
                                //发送EventBus
                                //使用eventBus发送数据
                                receiveData=receiveData.trim().replace(" ", "");
                                EventBus.getDefault().post(new SerialPortReceiveDataEvent(receiveData, bytes));
                                paserReceiverData(receiveData);
                            }
                        }
                    }
                }

                @Override
                public void onDataSent(byte[] bytes) {
                    Log.i(TAG, "onDataSent [ byte[] ]: " + Arrays.toString(bytes));
                    Log.i(TAG, "onDataSent [ String ]: " + new String(bytes));
                    final byte[] finalBytes = bytes;
//                            showToast(String.format("发送\n%s", new String(finalBytes)));
                }
            }).openSerialPort(devFile, baudRate);
            Log.i(TAG, "onCreate: openSerialPort = " + openSerialPort);
        }
    }

    //解析数据，处理
    private void paserReceiverData(String datas) {
        switch (datas) {
            case Constant.KEYCODE_UP:
                break; // 可选
            //菜单键
            case Constant.KEYCODE_MENU:
                Intent intent = new Intent(SerialPortService.this, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break; // 可选
            case Constant.KEYCODE_RIGHT:
                break; // 可选
            case Constant.KEYCODE_DOWN:
                break; // 可选
            case Constant.KEYCODE_OK:
                break; // 可选
            case Constant.KEYCODE_BACK:
                break; // 可选
            default: // 可选
                break;
        }
    }

    public void sendSerialPortData(byte[] datas) {
        mSerialPortManager.sendBytes(datas);
    }

    public static SerialPortService getInstance() {
        return mSerialPortService;

    }
}
