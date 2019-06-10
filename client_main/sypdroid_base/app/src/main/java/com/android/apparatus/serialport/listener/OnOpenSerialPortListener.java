package com.android.apparatus.serialport.listener;

import java.io.File;

public interface OnOpenSerialPortListener {

	void onSuccess(File device);

	void onFail(File device, Status status);

	enum Status {
		NO_READ_WRITE_PERMISSION, OPEN_FAIL
	}
}
