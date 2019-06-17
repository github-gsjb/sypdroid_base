/*
 * Copyright (C) 2011-2013 GUIGUI Simon, fyhertz@gmail.com
 *
 * This file is part of Spydroid (http://code.google.com/p/spydroid-ipcamera/)
 *
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.majorkernelpanic.spydroid.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.apparatus.utils.ToastTool;

import net.majorkernelpanic.http.TinyHttpServer;
import net.majorkernelpanic.spydroid.R;
import net.majorkernelpanic.spydroid.api.CustomHttpServer;
import net.majorkernelpanic.spydroid.api.CustomRtspServer;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.video.H264Stream;
import net.majorkernelpanic.streaming.video.VideoStream;

import java.io.IOException;
import java.lang.reflect.Method;

public class PreviewFragment extends Fragment {

    public final static String TAG = "PreviewFragment";

    private SurfaceView mSurfaceView;
    private TextView mTextView;
    private Button textshezhiBtn; //设置按钮
    private Button paizhaoiBtn; //拍照按钮
    private Button luxiangBtn; //录像按钮
    //打开热点按钮
    private Button btnOpenWifiHotspot;
    private CustomHttpServer mHttpServer;
    private RtspServer mRtspServer;
    //wifi管理器
    private WifiManager wifiManager;
    //热点名称
    private String mWifiHotspotSSID = "A0000000";
    //热点的密码
    private String mWifiHotspotPassword = "12345678";
    Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unbindService(mHttpServiceConnection);
        getActivity().unbindService(mRtspServiceConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().bindService(new Intent(getActivity(), CustomHttpServer.class), mHttpServiceConnection, Context.BIND_AUTO_CREATE);
        getActivity().bindService(new Intent(getActivity(), CustomRtspServer.class), mRtspServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.preview, container, false);
        mContext=this.getContext();
        wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mTextView = (TextView) rootView.findViewById(R.id.tooltip);
        textshezhiBtn = (Button) rootView.findViewById(R.id.textshezhi);
        textshezhiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), OptionsActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        btnOpenWifiHotspot = (Button) rootView.findViewById(R.id.preview_btnOpenWifiHotspot);
        btnOpenWifiHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开热点
                createWifiHotspot();
            }
        });

        paizhaoiBtn = (Button) rootView.findViewById(R.id.paizhao);
        paizhaoiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean boolTakePicture =  SessionBuilder.getInstance().getH264Stream().doTakePicture();//拍照
                if (boolTakePicture){
                    Toast.makeText(getActivity(),"拍照成功" , Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(),"拍照失败" , Toast.LENGTH_SHORT).show();
                }
            }
        });


        luxiangBtn = (Button) rootView.findViewById(R.id.luxiang);
        luxiangBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SessionBuilder.getInstance().getH264Stream().openCamera();
                H264Stream h264Stream = new H264Stream(0);
                try {
                    h264Stream.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //开始录像
//                boolean booldoStartRecorder =  SessionBuilder.getInstance().getH264Stream().doStartRecorder();//录像
//                if (booldoStartRecorder){
//                    Toast.makeText(getActivity(),"正在录像" , Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(getActivity(),"录像结束" , Toast.LENGTH_SHORT).show();
//                }
                //停止录像

            }
        });




        if (((SpydroidActivity) getActivity()).device == ((SpydroidActivity) getActivity()).TABLET) {

            mSurfaceView = (SurfaceView) rootView.findViewById(R.id.tablet_camera_view);
            SessionBuilder.getInstance().setSurfaceView(mSurfaceView);
        }

        return rootView;
    }

    public void update() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextView != null) {
                    if ((mRtspServer != null && mRtspServer.isStreaming()) || (mHttpServer != null && mHttpServer.isStreaming()))
                        mTextView.setVisibility(View.INVISIBLE);
                    else
                        mTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private final ServiceConnection mRtspServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRtspServer = (RtspServer) ((RtspServer.LocalBinder) service).getService();
            update();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private final ServiceConnection mHttpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mHttpServer = (CustomHttpServer) ((TinyHttpServer.LocalBinder) service).getService();
            update();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    /**
     * 创建Wifi热点
     */
    private void createWifiHotspot() {
        if (wifiManager.isWifiEnabled()) {
            //如果wifi处于打开状态，则关闭wifi,
            wifiManager.setWifiEnabled(false);
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = mWifiHotspotSSID;
        config.preSharedKey = mWifiHotspotPassword;
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
        //通过反射调用设置热点
        try {
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            boolean enable = (Boolean) method.invoke(wifiManager, config, true);
            if (enable) {
                ToastTool.showLong(mContext,"热点已开启 SSID:" + mWifiHotspotSSID + " password:"+mWifiHotspotPassword);
            } else {
                ToastTool.showLong(mContext,"热点创建失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastTool.showLong(mContext,"热点创建失败！");
        }
    }


}
