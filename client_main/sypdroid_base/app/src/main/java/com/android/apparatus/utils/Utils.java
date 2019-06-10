package com.android.apparatus.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Camera;
import android.location.LocationManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Utils {
	public static SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
	public static SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static SimpleDateFormat format2 = new SimpleDateFormat("HH:mm");

	public static String getTimeDiffStr(long time) {
		StringBuilder result = new StringBuilder();
		long diff = System.currentTimeMillis() - time;
		long MIN = 60 * 1000;
		long HOUR = 60 * MIN;
		long DAY = 24 * HOUR;
		long MONTH = 30 * DAY;
		try {
			if (diff < 0) {
				result.append(format.format(new Date(time)));
			} else if (diff < MIN) {// 1分钟之前
				result.append("刚刚");
			} else if (diff < HOUR) {// 1小时之前
				result.append(diff / MIN).append("分钟前");
			} else if (diff < DAY) {// 1天之前
				result.append(diff / HOUR).append("小时前");
			} else if (diff < MONTH) {// 1月之前
				result.append(diff / DAY).append("天前");
			} else {// 很久之前
				result.append(format.format(new Date(time)));
			}
		} catch (Exception e) {
			Log.e("TimeUtils", e.toString());
			return "";
		}
		return result.toString();
	}

	public static String getDateTime(long time) {
		return format1.format(new Date(time));
	}

	/**
	 * 同服务器时间差在5分钟内不显示时间，一小时内小时HH:mm 其他显示MM-dd HH:mm
	 * 
	 * @param time
	 * @return
	 */
	public static String getSmsTime(long time) {
		StringBuilder result = new StringBuilder();
		long diff = Math.abs(System.currentTimeMillis() - time);
		long MIN = 60 * 1000;
		long HOUR = 60 * MIN;
		long DAY = 24 * HOUR;
		try {
			if (diff < MIN * 5) {// 5分钟之前
			} else if (diff < DAY) {// 1天之前
				result.append(format2.format(new Date(time)));
			} else {// 很久之前
				result.append(format.format(new Date(time)));
			}
		} catch (Exception e) {
			Log.e("TimeUtils", e.toString());
			return "";
		}
		return result.toString();
	}

	/**
	 * @param cxt
	 * @return 屏幕宽
	 */
	public static int getScreenWidth(Activity cxt) {
		WindowManager m = cxt.getWindowManager();
		Display d = m.getDefaultDisplay();
		return d.getWidth();
	}

	public static int getScreenHeight(Activity cxt) {
		WindowManager m = cxt.getWindowManager();
		Display d = m.getDefaultDisplay();
		return d.getHeight();
	}

	/**
	 * @param cxt
	 * @return 版本号
	 */
	public static String getVersion(Context context) {
		String version_name = null;
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			version_name = info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version_name;
	}

	/**
	 * 获取屏幕高度(px)
	 */
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	/**
	 * 获取屏幕宽度(px)
	 */
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public void setMobileDataState(Context context, boolean enabled) {
		TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Method setDataEnabled = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
			if (null != setDataEnabled) {
				setDataEnabled.invoke(telephonyService, enabled);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean getMobileDataState(Context context) {
		TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Method getDataEnabled = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
			if (null != getDataEnabled) {
				return (Boolean) getDataEnabled.invoke(telephonyService);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Gets the state of GPS location.
	 * 
	 * @param context
	 * @return true if enabled.
	 */
	public static boolean getGpsState(Context context) {
		ContentResolver resolver = context.getContentResolver();
		boolean open = Settings.Secure.isLocationProviderEnabled(resolver, LocationManager.GPS_PROVIDER);
		System.out.println("getGpsState:" + open);
		return open;
	}

	/**
	 * Toggles the state of GPS.
	 * 
	 * @param context
	 */
	public static void toggleGps(Context context) {
		ContentResolver resolver = context.getContentResolver();
		boolean enabled = getGpsState(context);
		Settings.Secure.setLocationProviderEnabled(resolver, LocationManager.GPS_PROVIDER, !enabled);
	}

	/**
	 * 通过对比得到与宽高比最接近的预览尺寸（如果有相同尺寸，优先选择）
	 *
	 * @param isPortrait
	 *            是否竖屏
	 * @param surfaceWidth
	 *            需要被进行对比的原宽
	 * @param surfaceHeight
	 *            需要被进行对比的原高
	 * @param preSizeList
	 *            需要对比的预览尺寸列表
	 * @return 得到与原宽高比例最接近的尺寸
	 */
	public static Camera.Size getCloselyPreSize(boolean isPortrait, int surfaceWidth, int surfaceHeight,
                                                List<Camera.Size> preSizeList) {
		int reqTmpWidth;
		int reqTmpHeight;
		// 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
		if (isPortrait) {
			reqTmpWidth = surfaceHeight;
			reqTmpHeight = surfaceWidth;
		} else {
			reqTmpWidth = surfaceWidth;
			reqTmpHeight = surfaceHeight;
		}
		// 先查找preview中是否存在与surfaceview相同宽高的尺寸
		for (Camera.Size size : preSizeList) {
			if ((size.width == reqTmpWidth) && (size.height == reqTmpHeight)) {
				return size;
			}
		}

		// 得到与传入的宽高比最接近的size
		float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
		float curRatio, deltaRatio;
		float deltaRatioMin = Float.MAX_VALUE;
		Camera.Size retSize = null;
		for (Camera.Size size : preSizeList) {
			curRatio = ((float) size.width) / size.height;
			deltaRatio = Math.abs(reqRatio - curRatio);
			if (deltaRatio < deltaRatioMin) {
				deltaRatioMin = deltaRatio;
				retSize = size;
			}
		}

		return retSize;
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
}
