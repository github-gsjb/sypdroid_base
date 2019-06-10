package com.android.apparatus;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@SuppressLint("NewApi")
public class StatusViewManager {

	private final int UPDATE_MIN_INTERVAL = 10000; // 网络状态最小刷新间隔

	private Context mContext;
	private BroadcastReceiver mReceiver;
	private Handler mHandler;
	private IntentFilter mFilter;
	private TelephonyManager mTelephonyManager;
	private MobileSignalStrengthListener mMobileSignalStrengthListener;
	private StatusView mStatusView;

	/**
	 * 信号强度状态更新线程
	 */
	private Runnable mSignalStrengthChangeRunnable = new Runnable() {
		@Override
		public void run() {
			updateNetWorkStatus();
		}
	};

	public StatusViewManager(Context context, StatusView view) {
		mStatusView = view;
		initData(context);
	}

	/**
	 * 注册广播监听
	 */
	public void registerStatusBarReceiver() {
		mContext.registerReceiver(mReceiver, mFilter);
		mMobileSignalStrengthListener = new MobileSignalStrengthListener();
		mTelephonyManager.listen(mMobileSignalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	/**
	 * 取消广播监听
	 */
	public void unregisterStatusBarReceiver() {
		mContext.unregisterReceiver(mReceiver);
		mTelephonyManager.listen(mMobileSignalStrengthListener, PhoneStateListener.LISTEN_NONE);
	}

	private void initData(Context context) {
		mContext = context;
		mFilter = new IntentFilter();
		mFilter.addAction(Intent.ACTION_TIME_TICK);
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
		mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		mFilter.addAction(Constant.Action.WIFI_STATUS);

		mHandler = new Handler();
		mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				switch (action) {
				// 时间广播
				case Intent.ACTION_TIME_TICK:
					updateTaskStatus(Constant.TASK_STATUS_CONTINUE);
					break;
				// 网络连接状态(网络切换,网络开关)
				case ConnectivityManager.CONNECTIVITY_ACTION:
					updateNetWorkStatus();
					break;
				// WiFi信号强度变化
				case WifiManager.RSSI_CHANGED_ACTION:
					mHandler.removeCallbacks(mSignalStrengthChangeRunnable);
					mHandler.postDelayed(mSignalStrengthChangeRunnable, UPDATE_MIN_INTERVAL);
					updateNetWorkStatus();
					break;
				// GPS连接状态(Gps开关)
				case LocationManager.MODE_CHANGED_ACTION:
				case LocationManager.PROVIDERS_CHANGED_ACTION:
					updateGpsStatus();
					break;
				// 电量变化
				case Intent.ACTION_BATTERY_CHANGED:
					int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
					int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
					int percentage = (level * 100) / scale;
					updateBatteryStatus(percentage);
					break;

				default:
					break;
				}
			}
		};

		// 初始化各个状态, 电量不需要刻意初始化
		updateTaskStatus(Constant.TASK_STATUS_OK);
		updateNetWorkStatus();
		updateGpsStatus();
		updateSpaceStatus();
		updateCameraMode();
		updateTalkMode();
	}

	private void updateCameraMode() {

		mStatusView.refreshCameraMode();
	}

	private void updateTalkMode() {

		mStatusView.refreshTalkMode();
	}

	/**
	 * 刷新电量
	 *
	 * @param percentage
	 */
	private void updateBatteryStatus(int percentage) {

		// 低电量20,15,10,5的时候,发送警告广播
		if (percentage == 20 || percentage == 15 || percentage == 10 || percentage == 5) {
			StatusUtils.sendBroadcast(mContext, Constant.Action.BATTERY_STATUS, Constant.EXTRA.BATTERY_STATUS_EXTRA,
					percentage);
		}

		mStatusView.refreshBatteryView(percentage);

	}

	/**
	 * 刷新时间任务状态
	 * 
	 * @param status
	 */
	private void updateTaskStatus(int status) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		Calendar calendar = Calendar.getInstance();
		String time = format.format(calendar.getTime());
		mStatusView.refreshTimeView(time, status);
	}

	/**
	 * 刷新Gps状态
	 */
	private void updateGpsStatus() {

		int status;

		if (StatusUtils.isGPSOn(mContext)) {
			status = Constant.GPS_STATUS_OK;
		} else {
			status = Constant.GPS_STATUS_CLOSED;
		}

		// 发送无GPS警告广播
		if (status == Constant.GPS_STATUS_CLOSED) {
			StatusUtils.sendBroadcast(mContext, Constant.Action.GPS_STATUS, Constant.EXTRA.GPS_STATUS_EXTRA,
					Constant.GPS_STATUS_CLOSED);
		}

		String location = getLocation();
		mStatusView.refreshGpsView(status, location);
	}

	/*
	 * 获取经纬度
	 */
	private String getLocation() {
		double latitude = 0.0;
		double longitude = 0.0;
		LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

		List<String> list = locationManager.getAllProviders();
		boolean bfind = false;
		for (String c : list) {
			System.out.println("LocationManager provider:" + c);
			if (c.equals(LocationManager.NETWORK_PROVIDER)) {
				bfind = true;
				break;
			}
		}
		if (bfind) {
			LocationListener locationListener = new LocationListener() {
				// Provider被enable时触发此函数，比如GPS被打开
				@Override
				public void onProviderEnabled(String provider) {

				}

				// Provider被disable时触发此函数，比如GPS被关闭
				@Override
				public void onProviderDisabled(String provider) {

				}

				// 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
				@Override
				public void onLocationChanged(Location location) {
					if (location != null) {
						Log.e("Map", "Location changed : Lat: " + location.getLatitude() + " Lng: "
								+ location.getLongitude());
					}
				}

				@Override
				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
					// TODO Auto-generated method stub

				}
			};
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
			Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location != null) {
				latitude = location.getLatitude(); // 经度
				longitude = location.getLongitude(); // 纬度
			}

		}
		return latitude + "/" + longitude;
	}

	/**
	 * 刷新网络信号状态
	 */
	private void updateNetWorkStatus() {
		mStatusView.refreshNetView(getMobileLevel());

	}

	/**
	 * 刷新空间大小
	 */
	private void updateSpaceStatus() {
		String space = getAvailableSpace();
		mStatusView.refreshSpacelView(space);
	}

	/**
	 * 获取剩余空间
	 */
	@SuppressLint("NewApi")
	private String getAvailableSpace() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		long size = blockSize * availableBlocks;
		String available = Formatter.formatFileSize(mContext, size);
		return available;
	}

	private class MobileSignalStrengthListener extends PhoneStateListener {

		private SignalStrength mSignalStrength;

		public SignalStrength getSignalStrength() {
			return mSignalStrength;
		}

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			mSignalStrength = signalStrength;
			mHandler.removeCallbacks(mSignalStrengthChangeRunnable);
			// 最小更新时间为500ms
			mHandler.postDelayed(mSignalStrengthChangeRunnable, UPDATE_MIN_INTERVAL);
		}

	}

	/**
	 * 获取蜂窝连接的强度状态
	 *
	 * @return
	 */
	private int getMobileLevel() {

		int level = Constant.NET_STATUS_OK;

		if (mMobileSignalStrengthListener == null || mMobileSignalStrengthListener.getSignalStrength() == null) {
			return level;
		}

		String signalStrength = mMobileSignalStrengthListener.getSignalStrength().toString();
		String[] parts = signalStrength.split(" ");

		switch (mTelephonyManager.getNetworkType()) {
		// 移动联通2G
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
			level = StatusUtils.getGsmLevel(parts);
			break;
		// 电信2G
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			break;
		// 4G网络
		case TelephonyManager.NETWORK_TYPE_LTE:
			level = StatusUtils.getLteLevel(parts);
			break;
		// 移动3G网络
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			level = StatusUtils.getSdcdmaLevel(parts);
			break;
		default:
			level = Constant.NET_STATUS_OK;
			break;
		}

		return level;

	}

}
