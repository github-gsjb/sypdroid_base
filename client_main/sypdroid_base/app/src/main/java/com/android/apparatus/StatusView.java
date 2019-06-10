package com.android.apparatus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.majorkernelpanic.spydroid.R;
import com.android.apparatus.Constant;

public class StatusView extends RelativeLayout {

	private TextView tv_system_loction;
	private TextView tv_wifi_net;
	private TextView tv_system_net;
	private TextView tv_system_space;
	private TextView tv_camera_mode;
	private TextView tv_talk_mode;
	private TextView tv_system_time;
	private ImageView iv_system_location;
	private TextView tv_system_battery;
	private StatusViewManager mStatusManager;
	private Context mContext;

	public StatusView(Context context) {
		this(context, null);
	}

	public StatusView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	/**
	 * 注册广播监听
	 */
	public void registerStatusBarReceiver() {
		mStatusManager.registerStatusBarReceiver();
	}

	/**
	 * 取消广播监听
	 */
	public void unregisterStatusBarReceiver() {
		mStatusManager.unregisterStatusBarReceiver();
	}

	private void initView(Context context) {
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.status_view, this);
		tv_system_net = (TextView) findViewById(R.id.tv_system_net);
		tv_system_time = (TextView) findViewById(R.id.tv_system_time);
		iv_system_location = (ImageView) findViewById(R.id.iv_system_location);
		tv_system_battery = (TextView) findViewById(R.id.tv_system_battery);
		tv_system_loction = (TextView) findViewById(R.id.gpsdata);
		tv_system_space = (TextView) findViewById(R.id.battery);
		tv_wifi_net = (TextView) findViewById(R.id.wifiintensity);
		tv_camera_mode = (TextView) findViewById(R.id.cameramode);
		tv_talk_mode = (TextView) findViewById(R.id.talkbackmode);

		mStatusManager = new StatusViewManager(context, this);
	}

	/**
	 * 刷新电量布局
	 * 
	 * @param percentage
	 */
	void refreshBatteryView(int percentage) {

		String batteryInfo = percentage + "%";

		String currentBatteryInfo = tv_system_battery.getText().toString();

		if (batteryInfo.equals(currentBatteryInfo)) {
			return;
		}

		int currentPercentage = Integer.parseInt(currentBatteryInfo.substring(0, currentBatteryInfo.length() - 1));

		int status;

		// 高电量
		if (percentage > 50) {
			if (currentPercentage > 50) {
				status = Constant.BATTERY_STATUS_CONTINUE;
			} else {
				status = Constant.BATTERY_STATUS_OK;
			}
		} else if (percentage > 20) {
			// 半电量
			if (currentPercentage > 20) {
				status = Constant.BATTERY_STATUS_CONTINUE;
			} else {
				status = Constant.BATTERY_STATUS_WEAK;
			}
		} else {
			// 弱电量
			if (currentPercentage <= 20) {
				status = Constant.BATTERY_STATUS_CONTINUE;
			} else {
				status = Constant.BATTERY_STATUS_LOST;
			}
		}

		int textColorId;
		int backgroudDrawableId;

		// 只刷新电量,颜色不变
		if (status == Constant.BATTERY_STATUS_CONTINUE) {
			tv_system_battery.setText(batteryInfo);
			return;
		}

		switch (status) {
		// 正常
		case Constant.BATTERY_STATUS_OK:
			textColorId = R.color.statusbar_text_green;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_green;
			break;
		// 警告
		case Constant.BATTERY_STATUS_WEAK:
			textColorId = R.color.statusbar_text_yellow;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_yellow;
			break;
		// 低电量
		case Constant.BATTERY_STATUS_LOST:
			textColorId = R.color.statusbar_text_red;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
			break;
		default:
			textColorId = R.color.statusbar_text_red;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
			break;
		}

		tv_system_battery.setText(batteryInfo);
		tv_system_battery.setTextColor(mContext.getResources().getColor(textColorId));
		tv_system_battery.setBackgroundResource(backgroudDrawableId);

	}

	/**
	 * 刷新时间布局
	 */
	void refreshTimeView(String time, int status) {

		int textColorId;
		int backgroudDrawableId;

		// 只刷新时间数值,颜色不变
		if (status == Constant.TASK_STATUS_CONTINUE) {
			tv_system_time.setText(time);
			return;
		}

		switch (status) {
		// 正常
		case Constant.TASK_STATUS_OK:
			textColorId = R.color.statusbar_text_green;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_green;
			break;
		// 警告
		case Constant.TASK_STATUS_EDGE:
			textColorId = R.color.statusbar_text_yellow;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_yellow;
			break;
		// 超时
		case Constant.TASK_STATUS_OVER:
			textColorId = R.color.statusbar_text_red;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
			break;
		default:
			textColorId = R.color.statusbar_text_red;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
			break;
		}

		tv_system_time.setText(time);
		tv_system_time.setTextColor(mContext.getResources().getColor(textColorId));
		tv_system_time.setBackgroundResource(backgroudDrawableId);

	}

	/**
	 * 根据Gps信号状态，刷新Gps布局
	 */
	void refreshGpsView(int status, String loction) {

		int srcDrawableId;
		int backgroudDrawableId;

		switch (status) {
		case Constant.GPS_STATUS_OK:
			srcDrawableId = R.drawable.statusbar_gps_ok;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_green;
			break;
		case Constant.GPS_STATUS_WEAK:
			srcDrawableId = R.drawable.statusbar_gps_weak;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_yellow;
			break;
		case Constant.GPS_STATUS_LOST:
		case Constant.GPS_STATUS_CLOSED:
			srcDrawableId = R.drawable.statusbar_gps_lost;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
			break;
		default:
			srcDrawableId = R.drawable.statusbar_gps_lost;
			backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
			break;
		}

		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), srcDrawableId);
		iv_system_location.setImageBitmap(bitmap);
		iv_system_location.setBackgroundResource(backgroudDrawableId);

		tv_system_loction.setTextColor(mContext.getResources().getColor(R.color.statusbar_text_green));
		tv_system_loction.setText(loction);

	}

	/**
	 * 刷新空间
	 */
	void refreshSpacelView(String space) {
		tv_system_space.setTextColor(mContext.getResources().getColor(R.color.statusbar_text_green));
		tv_system_space.setText(space);
	}

	/**
	 * 根据网络类型和网络状态，刷新网络布局
	 */
	void refreshSignalView(String networkType, int status) {

		int textColorId;
		int backgroudDrawableId;

		// 网络状态不改变时,不做任何界面刷新处理
		switch (status) {
		// 正常绿色
		case Constant.NET_STATUS_OK:
			textColorId = R.color.statusbar_text_green;
			// backgroudDrawableId = R.drawable.statusbar_shape_bg_green;
			if (networkType.equals("WIFI")) {
				backgroudDrawableId = R.drawable.wifi_4;
			} else {
				backgroudDrawableId = R.drawable.signal_0_fully;
			}
			break;
		// 微弱黄色
		case Constant.NET_STATUS_WEAK:
			textColorId = R.color.statusbar_text_yellow;
			// backgroudDrawableId = R.drawable.statusbar_shape_bg_yellow;
			if (networkType.equals("WIFI")) {
				backgroudDrawableId = R.drawable.wifi_2;
			} else {
				backgroudDrawableId = R.drawable.signal_2_fully;
			}
			break;
		// 丢失红色
		case Constant.NET_STATUS_LOST:
		case Constant.NET_STATUS_CLOSED:
			textColorId = R.color.statusbar_text_red;
			// backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
			if (networkType.equals("WIFI")) {
				backgroudDrawableId = R.drawable.wifi_0;
			} else {
				backgroudDrawableId = R.drawable.signal_4_fully;
			}
			break;
		// 默认红色
		default:
			textColorId = R.color.statusbar_text_red;
			// backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
			if (networkType.equals("WIFI")) {
				backgroudDrawableId = R.drawable.wifi_0;
			} else {
				backgroudDrawableId = R.drawable.signal_4_fully;
			}
			break;
		}

		if (networkType.equals("WIFI")) {
			 tv_wifi_net.setText(networkType);
			tv_wifi_net.setTextColor(mContext.getResources().getColor(textColorId));
			tv_wifi_net.setBackgroundResource(backgroudDrawableId);
			tv_wifi_net.setTag(networkType + status);
		} else {
			 tv_system_net.setText(networkType);
			tv_system_net.setTextColor(mContext.getResources().getColor(textColorId));
			tv_system_net.setBackgroundResource(backgroudDrawableId);
			tv_system_net.setTag(networkType + status);
		}

	}

	void refreshNetView(int status) {

		int textColorId;
		int backgroudDrawableId;

		// 网络状态不改变时,不做任何界面刷新处理
		switch (status) {
		// 正常绿色
		case Constant.NET_STATUS_OK:
			textColorId = R.color.statusbar_text_green;
			backgroudDrawableId = R.drawable.signal_0_fully;
			break;
		// 微弱黄色
		case Constant.NET_STATUS_WEAK:
			textColorId = R.color.statusbar_text_yellow;
			backgroudDrawableId = R.drawable.signal_2_fully;
			break;
		// 丢失红色
		case Constant.NET_STATUS_LOST:
		case Constant.NET_STATUS_CLOSED:
			textColorId = R.color.statusbar_text_red;
			backgroudDrawableId = R.drawable.signal_4_fully;
			break;
		// 默认红色
		default:
			textColorId = R.color.statusbar_text_red;
			backgroudDrawableId = R.drawable.signal_4_fully;
			break;
		}

		tv_system_net.setTextColor(mContext.getResources().getColor(textColorId));
		tv_system_net.setBackgroundResource(backgroudDrawableId);

	}

	void refreshWifiView(boolean status) {

		int textColorId;
		int backgroudDrawableId;

		if (status) {
			textColorId = R.color.statusbar_text_green;
			backgroudDrawableId = R.drawable.wifi_4;
		} else {
			textColorId = R.color.statusbar_text_red;
			backgroudDrawableId = R.drawable.wifi_0;
		}

		tv_wifi_net.setTextColor(mContext.getResources().getColor(textColorId));
		tv_wifi_net.setBackgroundResource(backgroudDrawableId);

		tv_system_net.setBackgroundResource(R.drawable.signal_4_fully);

	}

	void refreshTalkMode() {
		tv_talk_mode.setBackgroundResource(R.drawable.listening);
	}

	void refreshCameraMode() {
		tv_camera_mode.setBackgroundResource(R.drawable.takepic);
	}
}
