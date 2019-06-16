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
			// 文件不存在，返回失败
			return false;
		}
		try {
			// 获取root的权限
			Process su = Runtime.getRuntime().exec("/system/xbin/su");
			String cmd = "chmod 777 " + file.getAbsolutePath() + "\n" + "exit\n";
			su.getOutputStream().write(cmd.getBytes());
			if (0 == su.waitFor() && file.canRead() && file.canWrite() && file.canExecute()) {
				return true;
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	// jni ndk 打开串口
	protected native FileDescriptor open(String path, int baudRate, int flags);

	// 关闭串口
	protected native void close();
}
