package com.android.apparatus.serialport.listener;

public interface OnSerialPortDataListener {

	void onDataReceived(byte[] bytes);

	void onDataSent(byte[] bytes);
}
