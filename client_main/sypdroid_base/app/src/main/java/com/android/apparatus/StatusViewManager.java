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

import com.android.apparatus.Constant;
import com.android.apparatus.StatusUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@SuppressLint("NewApi")
public class StatusViewManager {

	private final int UPDATE_MIN_INTERVAL = 10000; // ����״̬��Сˢ�¼��

	private Context mContext;
	private BroadcastReceiver mReceiver;
	private Handler mHandler;
	private IntentFilter mFilter;
	private TelephonyManager mTelephonyManager;
	private MobileSignalStrengthListener mMobileSignalStrengthListener;
	private StatusView mStatusView;

	/**
	 * �ź�ǿ��״̬�����߳�
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
	 * ע��㲥����
	 */
	public void registerStatusBarReceiver() {
		mContext.registerReceiver(mReceiver, mFilter);
		mMobileSignalStrengthListener = new MobileSignalStrengthListener();
		mTelephonyManager.listen(mMobileSignalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	/**
	 * ȡ���㲥����
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
				// ʱ��㲥
				case Intent.ACTION_TIME_TICK:
					updateTaskStatus(Constant.TASK_STATUS_CONTINUE);
					break;
				// ��������״̬(�����л�,���翪��)
				case ConnectivityManager.CONNECTIVITY_ACTION:
					updateNetWorkStatus();
					break;
				// WiFi�ź�ǿ�ȱ仯
				case WifiManager.RSSI_CHANGED_ACTION:
					mHandler.removeCallbacks(mSignalStrengthChangeRunnable);
					mHandler.postDelayed(mSignalStrengthChangeRunnable, UPDATE_MIN_INTERVAL);
					break;
				// GPS����״̬(Gps����)
				case LocationManager.MODE_CHANGED_ACTION:
				case LocationManager.PROVIDERS_CHANGED_ACTION:
					updateGpsStatus();
					break;
				// �����仯
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

		// ��ʼ������״̬, ��������Ҫ�����ʼ��
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
	 * ˢ�µ���
	 *
	 * @param percentage
	 */
	private void updateBatteryStatus(int percentage) {

		// �͵���20,15,10,5��ʱ��,���;���㲥
		if (percentage == 20 || percentage == 15 || percentage == 10 || percentage == 5) {
			StatusUtils.sendBroadcast(mContext, Constant.Action.BATTERY_STATUS, Constant.EXTRA.BATTERY_STATUS_EXTRA,
					percentage);
		}

		mStatusView.refreshBatteryView(percentage);

	}

	/**
	 * ˢ��ʱ������״̬
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
	 * ˢ��Gps״̬
	 */
	private void updateGpsStatus() {

		int status;

		if (StatusUtils.isGPSOn(mContext)) {
			status = Constant.GPS_STATUS_OK;
		} else {
			status = Constant.GPS_STATUS_CLOSED;
		}

		// ������GPS����㲥
		if (status == Constant.GPS_STATUS_CLOSED) {
			StatusUtils.sendBroadcast(mContext, Constant.Action.GPS_STATUS, Constant.EXTRA.GPS_STATUS_EXTRA,
					Constant.GPS_STATUS_CLOSED);
		}

		String location = getLocation();
		mStatusView.refreshGpsView(status, location);
	}

	/*
	 * ��ȡ��γ��
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
				// Provider��enableʱ�����˺���������GPS����
				@Override
				public void onProviderEnabled(String provider) {

				}

				// Provider��disableʱ�����˺���������GPS���ر�
				@Override
				public void onProviderDisabled(String provider) {

				}

				// ������ı�ʱ�����˺��������Provider������ͬ�����꣬���Ͳ��ᱻ����
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

//			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
//			Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//			if (location != null) {
//				latitude = location.getLatitude(); // ����
//				longitude = location.getLongitude(); // γ��
//			}

		}
		return latitude + "/" + longitude;
	}

	/**
	 * ˢ�������ź�״̬
	 */
	private void updateNetWorkStatus() {

	}

	/**
	 * ˢ�¿ռ��С
	 */
	private void updateSpaceStatus() {
		String space = getAvailableSpace();
		mStatusView.refreshSpacelView(space);
	}

	/**
	 * ��ȡʣ��ռ�
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
			// ��С����ʱ��Ϊ500ms
			mHandler.postDelayed(mSignalStrengthChangeRunnable, UPDATE_MIN_INTERVAL);
		}

	}

	/**
	 * ��ȡ�������ӵ�ǿ��״̬
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
		// �ƶ���ͨ2G
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
			level = StatusUtils.getGsmLevel(parts);
			break;
		// ����2G
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			break;
		// 4G����
		case TelephonyManager.NETWORK_TYPE_LTE:
			level = StatusUtils.getLteLevel(parts);
			break;
		// �ƶ�3G����
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
