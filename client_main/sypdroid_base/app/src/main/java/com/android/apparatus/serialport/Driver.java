package com.android.apparatus.serialport;

import java.io.File;
import java.util.ArrayList;

import android.util.Log;

public class Driver {

	private static final String TAG = Driver.class.getSimpleName();
	private String mDriverName;
	private String mDeviceRoot;

	public Driver(String name, String root) {
		mDriverName = name;
		mDeviceRoot = root;
	}

	public ArrayList<File> getDevices() {
		ArrayList<File> devices = new ArrayList<>();
		File dev = new File("/dev");

		if (!dev.exists()) {
			Log.i(TAG, "getDevices: " + dev.getAbsolutePath() + " 文件不存在");
			return devices;
		}
		if (!dev.canRead()) {
			Log.i(TAG, "getDevices: " + dev.getAbsolutePath() + " 文件不可读");
			return devices;
		}

		File[] files = dev.listFiles();

		int i;
		for (i = 0; i < files.length; i++) {
			if (files[i].getAbsolutePath().startsWith(mDeviceRoot)) {
				Log.d(TAG, "Found new device: " + files[i]);
				if (files[i].getPath().startsWith("/dev/tty") && !(files[i].getPath().equals("/dev/ttySO"))) {
					devices.add(files[i]);
				}

			}
		}
		return devices;
	}

	public String getName() {
		return mDriverName;
	}

}
