package com.android.apparatus.serialport;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class SerialPort {

	static {
		System.loadLibrary("SerialPort");
	}

	private static final String TAG = SerialPort.class.getSimpleName();

	boolean chmod777(File file) {
		if (null == file || !file.exists()) {
			// 鏂囦欢涓嶅瓨鍦�
			return false;
		}
		try {
			// 鑾峰彇ROOT鏉冮檺
			Process su = Runtime.getRuntime().exec("/system/xbin/su");
			// 淇敼鏂囦欢灞炴�т负 [鍙 鍙啓 鍙墽琛宂
			String cmd = "chmod 777 " + file.getAbsolutePath() + "\n" + "exit\n";
			su.getOutputStream().write(cmd.getBytes());
			if (0 == su.waitFor() && file.canRead() && file.canWrite() && file.canExecute()) {
				return true;
			}
		} catch (IOException | InterruptedException e) {
			// 娌℃湁ROOT鏉冮檺
			e.printStackTrace();
		}
		return false;
	}

	// 鎵撳紑涓插彛
	protected native FileDescriptor open(String path, int baudRate, int flags);

	// 鍏抽棴涓插彛
	protected native void close();
}
