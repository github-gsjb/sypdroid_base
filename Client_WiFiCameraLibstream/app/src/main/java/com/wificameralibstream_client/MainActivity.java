package com.wificameralibstream_client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private SurfaceView mSurfaceView; //用来加载相机的mSurfaceView
    private TextView ipadress ; //显示推流的IP地址
    private String PORT = "8086"; //推流的端口
    private Button btn_paizhao; //拍照按钮
    private Button btn_luxiang;//录像按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制竖屏

        setContentView(R.layout.activity_main);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        btn_paizhao = (Button) findViewById(R.id.btn_paizhao);
        btn_luxiang = (Button) findViewById(R.id.btn_luxiang);

        // Sets the port of the RTSP server to 8086 设置推流的端口
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(RtspServer.KEY_PORT, PORT);
        editor.commit();

        ipadress = (TextView) findViewById(R.id.ipadress);
        ipadress.setText("rtsp://" + getLocalIpAddress() +":"+ PORT); //显示推流的完整地址

        // Configures the SessionBuilder 使用SessionBuilder开始推流
        SessionBuilder.getInstance()
                .setSurfaceView(mSurfaceView)
//                .setPreviewOrientation(90) //竖屏显示使用90 , 设备横屏注释
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setVideoEncoder(SessionBuilder.VIDEO_H264);

        //拍照
        btn_paizhao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"点击拍照按钮");
            }
        });

        //录像
        btn_luxiang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"点击录像按钮");
            }
        });

        // Starts the RTSP server 启动RTSP服务器
        this.startService(new Intent(this,RtspServer.class));

        //测试提交


    }

    /*获取本地的IP地址*/
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        String ipAddressFormatted = String.format(Locale.ENGLISH, "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        return ipAddressFormatted;
    }
}
