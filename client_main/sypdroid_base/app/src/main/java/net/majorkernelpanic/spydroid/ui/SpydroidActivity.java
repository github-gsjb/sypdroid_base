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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.apparatus.serialport.SerialPortManager;
import com.android.apparatus.serialport.SerialPortReceiveDataEvent;
import com.android.apparatus.service.SerialPortService;
import com.android.apparatus.utils.EventBusUtils;
import com.android.apparatus.utils.LoggerUtil;
import net.majorkernelpanic.http.TinyHttpServer;
import net.majorkernelpanic.spydroid.R;
import net.majorkernelpanic.spydroid.SpydroidApplication;
import net.majorkernelpanic.spydroid.api.CustomHttpServer;
import net.majorkernelpanic.spydroid.api.CustomRtspServer;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Spydroid basically launches an RTSP server and an HTTP server,
 * clients can then connect to them and start/stop audio/video streams on the phone.
 */
public class SpydroidActivity extends FragmentActivity {

    static final public String TAG = "SpydroidActivity";

    public final int HANDSET = 0x01;
    public final int TABLET = 0x02;

    // We assume that the device is a phone
    public int device = HANDSET;

    private ViewPager mViewPager;
    private PowerManager.WakeLock mWakeLock;
    private SectionsPagerAdapter mAdapter;
    private SurfaceView mSurfaceView;
    private SpydroidApplication mApplication;
    private CustomHttpServer mHttpServer;
    private RtspServer mRtspServer;

    private SerialPortManager mSerialPortManager;

    @SuppressLint("InvalidWakeLockTag")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title bar  即隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//remove notification bar  即全屏
        mApplication = (SpydroidApplication) getApplication();
//        EventBusUtils.registerEventBus(this);
        //启动串口管理服务
        startService(new Intent(this, SerialPortService.class));//开启串口数据的service
        //初始化设置数据
        setOptionsData();

        setContentView(R.layout.spydroid);

        device = TABLET;
        mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.tablet_pager);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        SessionBuilder.getInstance().setPreviewOrientation(0);

//		if (findViewById(R.id.handset_pager) != null) {
//
//			// Handset detected !
//			mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
////			mViewPager = (ViewPager) findViewById(R.id.handset_pager);
////			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
////			mSurfaceView = (SurfaceView)findViewById(R.id.handset_camera_view);
//			SessionBuilder.getInstance().setSurfaceView(mSurfaceView);
//			SessionBuilder.getInstance().setPreviewOrientation(90);
//
//		} else {
//
//			// Tablet detected !
//			device = TABLET;
//			mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
//			mViewPager = (ViewPager) findViewById(R.id.tablet_pager);
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//			SessionBuilder.getInstance().setPreviewOrientation(0);
//
//		}

        mViewPager.setAdapter(mAdapter);

        // Remove the ads if this is the donate version of the app. //广告不用管
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "net.majorkernelpanic.spydroid.wakelock");

        // Starts the service of the HTTP server
        this.startService(new Intent(this, CustomHttpServer.class)); //HTTP不用管

        // Starts the service of the RTSP server
        this.startService(new Intent(this, CustomRtspServer.class));

    }

    private void setOptionsData() {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("video_encoder", String.valueOf(mApplication.videoEncoder));
        editor.putString("audio_encoder", String.valueOf(mApplication.audioEncoder));
        editor.putString("video_framerate", String.valueOf(mApplication.videoQuality.framerate));
        editor.putString("video_bitrate", String.valueOf(mApplication.videoQuality.bitrate / 1000));
        editor.putString("video_resolution", String.valueOf(mApplication.videoQuality.resX + "x" + mApplication.videoQuality.resY));

        settings.getBoolean("stream_audio", true);
        settings.getBoolean("stream_video", true);

        editor.putInt("video_resX", 1920);
        editor.putInt("video_resY", 1080);
        editor.commit();

        editor.commit();

    }

    public void onStart() {
        super.onStart();

        // Lock screen
        mWakeLock.acquire();

        // Did the user disabled the notification ?
        if (mApplication.notificationEnabled) {
            Intent notificationIntent = new Intent(this, SpydroidActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            Notification notification = builder.setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setTicker(getText(R.string.notification_title))
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle(getText(R.string.notification_title))
                    .setContentText(getText(R.string.notification_content)).build();
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notification);
        } else {
            removeNotification();
        }

        bindService(new Intent(this, CustomHttpServer.class), mHttpServiceConnection, Context.BIND_AUTO_CREATE); //HTTP不用管
        bindService(new Intent(this, CustomRtspServer.class), mRtspServiceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onStop() {
        super.onStop();
        // A WakeLock should only be released when isHeld() is true !
        if (mWakeLock.isHeld()) mWakeLock.release();
        if (mHttpServer != null) mHttpServer.removeCallbackListener(mHttpCallbackListener);
        unbindService(mHttpServiceConnection);
        if (mRtspServer != null) mRtspServer.removeCallbackListener(mRtspCallbackListener);
        unbindService(mRtspServiceConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
//		mApplication.applicationForeground = true;
//		mSerialPortManager = new SerialPortManager();
//
////		发送串口数据
////		mSerialPortManager.sendBytes();
//		SerialPortFinder serialPortFinder = new SerialPortFinder();
//		ArrayList<Device> devices = serialPortFinder.getDevices();
//		for (int i = 0; i < devices.size(); i++) {
//			File file = devices.get(i).getFile();
//
//			boolean openSerialPort = mSerialPortManager.setOnOpenSerialPortListener(this)
//					.setOnSerialPortDataListener(new OnSerialPortDataListener() {
//						@Override
//						public void onDataReceived(byte[] bytes) {
//							Log.i(TAG, "onDataReceived [ byte[] ]: " + Arrays.toString(bytes));
//							Log.i(TAG, "onDataReceived [ String ]: " + new String(bytes));
//							final byte[] finalBytes = bytes;
//
//							String textTemp = Utils.bytesToHexString(finalBytes);
////                            if (textTemp.contains("09")){
////                                Intent intent = new Intent(MainActivity.this, FileGalleryActivity.class);
////                                startActivity(intent);
////                                return;
////                            }
////                            if (Constant.KEYCODE_ON.contains(textTemp)) {
////                                textTemp = Constant.KEYCODE_ON;
////                            }else if (Constant.KEYCODE_OK.contains(textTemp)) {
////                                textTemp = Constant.KEYCODE_OK;
////                            }
//
//							final String text = textTemp;
//							runOnUiThread(new Runnable() {
//								@Override
//								public void run() {
////									showToast(String.format("接收\n%s", text));
//
//									switch (text) {
//										//菜单键
//										case Constant.KEYCODE_MENU:
////											downPopwindow();
//											break; // 可选
//										case Constant.KEYCODE_UP:
////											mainSelectPostion = mainSelectPostion - 1;
////											mainAdapter.setSelectItem(mainSelectPostion);
////											mainAdapter.notifyDataSetChanged();
//											break; // 可选
//										case Constant.KEYCODE_DOWN:
////											if (mainlist.hasFocus()) {
////												mainSelectPostion = mainSelectPostion + 1;
////												if (mainSelectPostion == 12 && !isGetVersion) {
////													isGetVersion = true;
////													String version = Utils.getVersion(MainActivity.this);
////													String ver = MORELISTVIEWTXT[mainSelectPostion][0] + version;
////													String[] a = ver.split("\\n");
////													MORELISTVIEWTXT[mainSelectPostion] = a;
////												}
////												inintAdapter(MORELISTVIEWTXT[mainSelectPostion]);
////												mainAdapter.setSelectItem(mainSelectPostion);
////												mainAdapter.notifyDataSetChanged();
////											}
//
//											break; // 可选
//										case Constant.KEYCODE_OK:
////											Intent intent = new Intent(MainActivity.this, FileGalleryActivity.class);
////											startActivity(intent);
//											break; // 可选
//										case Constant.KEYCODE_ON:
//
//											break; // 可选
//										case Constant.KEYCODE_MENU:
//
//											break; // 可选
//										case Constant.KEYCODE_RIGHT:
//
//											break; // 可选
//										case Constant.KEYCODE_BACK:
//
//											break; // 可选
//
//										default: // 可选
//											break;
//									}
//
//									if (text.equals(Constant.KEYCODE_MENU)) {
////										downPopwindow();
//									}
//
//								}
//							});
//						}
//
//						@Override
//						public void onDataSent(byte[] bytes) {
//							Log.i(TAG, "onDataSent [ byte[] ]: " + Arrays.toString(bytes));
//							Log.i(TAG, "onDataSent [ String ]: " + new String(bytes));
//							final byte[] finalBytes = bytes;
//							runOnUiThread(new Runnable() {
//								@Override
//								public void run() {
////									showToast(String.format("发送\n%s", new String(finalBytes)));
//								}
//							});
//						}
//					}).openSerialPort(file, 115200);
//
//			Log.i(TAG, "onCreate: openSerialPort = " + openSerialPort);
//		}

    }

    @Override
    public void onPause() {
        super.onPause();
        mApplication.applicationForeground = false;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "SpydroidActivity destroyed");
        super.onDestroy();
        quitSpydroid();
//        EventBusUtils.unRegisterEventBus(this);
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItemCompat.setShowAsAction(menu.findItem(R.id.quit), 1);
        MenuItemCompat.setShowAsAction(menu.findItem(R.id.options), 1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.options:
                // Starts QualityListActivity where user can change the streaming quality
                intent = new Intent(this.getBaseContext(), OptionsActivity.class);
                startActivityForResult(intent, 0);
                return true;
            case R.id.quit:
                quitSpydroid();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void quitSpydroid() {
        // Removes notification
        if (mApplication.notificationEnabled) removeNotification();
        // Kills HTTP server
        this.stopService(new Intent(this, CustomHttpServer.class));
        // Kills RTSP server
        this.stopService(new Intent(this, CustomRtspServer.class));
        // Returns to home menu
        finish();
    }

    private ServiceConnection mRtspServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRtspServer = (CustomRtspServer) ((RtspServer.LocalBinder) service).getService();
            mRtspServer.addCallbackListener(mRtspCallbackListener);
            mRtspServer.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

    };

    private RtspServer.CallbackListener mRtspCallbackListener = new RtspServer.CallbackListener() {

        @Override
        public void onError(RtspServer server, Exception e, int error) {
            // We alert the user that the port is already used by another app.
            if (error == RtspServer.ERROR_BIND_FAILED) {
                new AlertDialog.Builder(SpydroidActivity.this)
                        .setTitle(R.string.port_used)
                        .setMessage(getString(R.string.bind_failed, "RTSP"))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                startActivityForResult(new Intent(SpydroidActivity.this, OptionsActivity.class), 0);
                            }
                        })
                        .show();
            }
        }

        @Override
        public void onMessage(RtspServer server, int message) {
            if (message == RtspServer.MESSAGE_STREAMING_STARTED) {
                if (mAdapter != null && mAdapter.getHandsetFragment() != null)
                    mAdapter.getHandsetFragment().update();
            } else if (message == RtspServer.MESSAGE_STREAMING_STOPPED) {
                if (mAdapter != null && mAdapter.getHandsetFragment() != null)
                    mAdapter.getHandsetFragment().update();
            }
        }

    };

    private ServiceConnection mHttpServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mHttpServer = (CustomHttpServer) ((TinyHttpServer.LocalBinder) service).getService();
            mHttpServer.addCallbackListener(mHttpCallbackListener);
            mHttpServer.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

    };

    private TinyHttpServer.CallbackListener mHttpCallbackListener = new TinyHttpServer.CallbackListener() {

        @Override
        public void onError(TinyHttpServer server, Exception e, int error) {
            // We alert the user that the port is already used by another app.
            if (error == TinyHttpServer.ERROR_HTTP_BIND_FAILED ||
                    error == TinyHttpServer.ERROR_HTTPS_BIND_FAILED) {
                String str = error == TinyHttpServer.ERROR_HTTP_BIND_FAILED ? "HTTP" : "HTTPS";
                new AlertDialog.Builder(SpydroidActivity.this)
                        .setTitle(R.string.port_used)
                        .setMessage(getString(R.string.bind_failed, str))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                startActivityForResult(new Intent(SpydroidActivity.this, OptionsActivity.class), 0);
                            }
                        })
                        .show();
            }
        }

        @Override
        public void onMessage(TinyHttpServer server, int message) {
            if (message == CustomHttpServer.MESSAGE_STREAMING_STARTED) {
                if (mAdapter != null && mAdapter.getHandsetFragment() != null)
                    mAdapter.getHandsetFragment().update();
                if (mAdapter != null && mAdapter.getPreviewFragment() != null)
                    mAdapter.getPreviewFragment().update();
            } else if (message == CustomHttpServer.MESSAGE_STREAMING_STOPPED) {
                if (mAdapter != null && mAdapter.getHandsetFragment() != null)
                    mAdapter.getHandsetFragment().update();
                if (mAdapter != null && mAdapter.getPreviewFragment() != null)
                    mAdapter.getPreviewFragment().update();
            }
        }

    };

    private void removeNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
    }

    public void log(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (device == HANDSET) {
                switch (i) {
                    case 0:
                        return new HandsetFragment();
                    case 1:
                        return new PreviewFragment();
                    case 2:
                        return new AboutFragment();
                }
            } else {
                switch (i) {
                    case 0:
                        return new TabletFragment();
//				case 1: return new AboutFragment(); //取消About
                }
            }
            return null;
        }

        @Override
        public int getCount() {
//			return device==HANDSET ? 3 : 2;
            return device == HANDSET ? 3 : 1; //返回1个pager
        }

        public HandsetFragment getHandsetFragment() {
            if (device == HANDSET) {
//				return (HandsetFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.handset_pager+":0");
                return null;
            } else {
                return (HandsetFragment) getSupportFragmentManager().findFragmentById(R.id.handset);
            }
        }

        public PreviewFragment getPreviewFragment() {
            if (device == HANDSET) {
//				return (PreviewFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.handset_pager+":1");
                return null;
            } else {
                return (PreviewFragment) getSupportFragmentManager().findFragmentById(R.id.preview);
            }
        }

        //所有设备连接后，给设备设置时间
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void EventConnectResult(SerialPortReceiveDataEvent receiveDataEvent) {
            //获取的蓝牙address和对应的数据
            LoggerUtil.println(TAG, "SerialPortReceiveDataEvent获取到串口数据");
            byte[] datas = receiveDataEvent.getData();
            //使用eventBus发送数据
//            EventBus.getDefault().post(new SerialPortReceiveDataEvent(address, bytes));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (device == HANDSET) {
                switch (position) {
                    case 0:
                        return getString(R.string.page0);
                    case 1:
                        return getString(R.string.page1);
//				case 2: return getString(R.string.page2);
                }
            } else {
                switch (position) {
//				case 0: return getString(R.string.page0);
//				case 1: return getString(R.string.page2);
                }
            }
            return null;
        }

    }

}