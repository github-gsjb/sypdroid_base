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

	private final int UPDATE_MIN_INTERVAL = 10000; // ÍøÂç×´Ì¬×îÐ¡Ë¢ÐÂ¼ä¸ô

	private Context mContext;
	private BroadcastReceiver mReceiver;
	private Handler mHandler;
	private IntentFilter mFilter;
	private TelephonyManager mTelephonyManager;
	private MobileSignalStrengthListener mMobileSignalStrengthListener;
	private StatusView mStatusView;

	/**
	 * ÐÅºÅÇ¿¶È×´Ì¬¸üÐÂÏß³Ì
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
	 * ×¢²á¹ã²¥¼àÌý
	 */
	public void registerStatusBarReceiver() {
		mContext.registerReceiver(mReceiver, mFilter);
		mMobileSignalStrengthListener = new MobileSignalStrengthListener();
		mTelephonyManager.listen(mMobileSignalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	/**
	 * È¡Ïû¹ã²¥¼àÌý
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
				// Ê±¼ä¹ã²¥
				case Intent.ACTION_TIME_TICK:
					updateTaskStatus(Constant.TASK_STATUS_CONTINUE);
					break;
				// ÍøÂçÁ¬½Ó×´Ì¬(ÍøÂçÇÐ»»,ÍøÂç¿ª¹Ø)
				case ConnectivityManager.CONNECTIVITY_ACTION:
					updateNetWorkStatus();
					break;
				// WiFiÐÅºÅÇ¿¶È±ä»¯
				case WifiManager.RSSI_CHANGED_ACTION:
					mHandler.removeCallbacks(mSignalStrengthChangeRunnable);
					mHandler.postDelayed(mSignalStrengthChangeRunnable, UPDATE_MIN_INTERVAL);
					updateNetWorkStatus();
					break;
				// GPSÁ¬½Ó×´Ì¬(Gps¿ª¹Ø)
				case LocationManager.MODE_CHANGED_ACTION:
				case LocationManager.PROVIDERS_CHANGED_ACTION:
					updateGpsStatus();
					break;
				// µçÁ¿±ä»¯
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

		// ³õÊ¼»¯¸÷¸ö×´Ì¬, µçÁ¿²»ÐèÒª¿ÌÒâ³õÊ¼»¯
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
	 * Ë¢ÐÂµçÁ¿
	 *
	 * @param percentage
	 */
	private void updateBatteryStatus(int percentage) {

		// µÍµçÁ¿20,15,10,5µÄÊ±ºò,·¢ËÍ¾¯¸æ¹ã²¥
		if (percentage == 20 || percentage == 15 || percentage == 10 || percentage == 5) {
			StatusUtils.sendBroadcast(mContext, Constant.Action.BATTERY_STATUS, Constant.EXTRA.BATTERY_STATUS_EXTRA,
					percentage);
		}

		mStatusView.refreshBatteryView(percentage);

	}

	/**
	 * Ë¢ÐÂÊ±¼äÈÎÎñ×´Ì¬
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
	 * Ë¢ÐÂGps×´Ì¬
	 */
	private void updateGpsStatus() {

		int status;

		if (StatusUtils.isGPSOn(mContext)) {
			status = Constant.GPS_STATUS_OK;
		} else {
			status = Constant.GPS_STATUS_CLOSED;
		}

		// ·¢ËÍÎÞGPS¾¯¸æ¹ã²¥
		if (status == Constant.GPS_STATUS_CLOSED) {
			StatusUtils.sendBroadcast(mContext, Constant.Action.GPS_STATUS, Constant.EXTRA.GPS_STATUS_EXTRA,
					Constant.GPS_STATUS_CLOSED);
		}

		String location = getLocation();
		mStatusView.refreshGpsView(status, location);
	}

	/*
	 * »ñÈ¡¾­Î³¶È
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
				// Provider±»enableÊ±´¥·¢´Ëº¯Êý£¬±ÈÈçGPS±»´ò¿ª
				@Override
				public void onProviderEnabled(String provider) {

				}

				// Provider±»disableÊ±´¥·¢´Ëº¯Êý£¬±ÈÈçGPS±»¹Ø±Õ
				@Override
				public void onProviderDisabled(String provider) {

				}

				// µ±×ø±ê¸Ä±äÊ±´¥·¢´Ëº¯Êý£¬Èç¹ûProvider´«½øÏàÍ¬µÄ×ø±ê£¬Ëü¾Í²»»á±»´¥·¢
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
				latitude = location.getLatitude(); // ¾­¶È
				longitude = location.getLongitude(); // Î³¶È
			}

		}
		return latitude + "/" + longitude;
	}

	/**
	 * Ë¢ÐÂÍøÂçÐÅºÅ×´Ì¬
	 */
	private void updateNetWorkStatus() {
		mStatusView.refreshNetView(getMobileLevel());

	}

	/**
	 * Ë¢ÐÂ¿Õ¼ä´óÐ¡
	 */
	private void updateSpaceStatus() {
		String space = getAvailableSpace();
		mStatusView.refreshSpacelView(space);
	}

	/**
	 * »ñÈ¡Ê£Óà¿Õ¼ä
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
			// ×îÐ¡¸üÐÂÊ±¼äÎª500ms
			mHandler.postDelayed(mSignalStrengthChangeRunnable, UPDATE_MIN_INTERVAL);
		}

	}

	/**
	 * »ñÈ¡·äÎÑÁ¬½ÓµÄÇ¿¶È×´Ì¬
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
		// ÒÆ¶¯ÁªÍ¨2G
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
			level = StatusUtils.getGsmLevel(parts);
			break;
		// µçÐÅ2G
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			break;
		// 4GÍøÂç
		case TelephonyManager.NETWORK_TYPE_LTE:
			level = StatusUtils.getLteLevel(parts);
			break;
		// ÒÆ¶¯3GÍøÂç
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
