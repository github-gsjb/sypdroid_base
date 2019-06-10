package com.android.apparatus.serialport;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.android.apparatus.serialport.listener.OnOpenSerialPortListener;
import com.android.apparatus.serialport.listener.OnSerialPortDataListener;
import com.android.apparatus.serialport.thread.SerialPortReadThread;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SerialPortManager extends SerialPort {

	private static final String TAG = SerialPortManager.class.getSimpleName();
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	private FileDescriptor mFd;
	private OnOpenSerialPortListener mOnOpenSerialPortListener;
	private OnSerialPortDataListener mOnSerialPortDataListener;

	private HandlerThread mSendingHandlerThread;
	private Handler mSendingHandler;
	private SerialPortReadThread mSerialPortReadThread;

	public boolean openSerialPort(File device, int baudRate) {

		Log.i(TAG, "openSerialPort: " + String.format("鎵撳紑涓插彛 %s  娉㈢壒鐜? %s", device.getPath(), baudRate));

		// 鏍￠獙涓插彛鏉冮檺
		if (!device.canRead() || !device.canWrite()) {
			boolean chmod777 = chmod777(device);
			if (!chmod777) {
				Log.i(TAG, "openSerialPort: 娌℃湁璇诲啓鏉冮檺");
				if (null != mOnOpenSerialPortListener) {
					mOnOpenSerialPortListener.onFail(device, OnOpenSerialPortListener.Status.NO_READ_WRITE_PERMISSION);
				}
				return false;
			}
		}

		try {
			mFd = open(device.getAbsolutePath(), baudRate, 0);
			mFileInputStream = new FileInputStream(mFd);
			mFileOutputStream = new FileOutputStream(mFd);
			Log.i(TAG, "openSerialPort: 涓插彛宸茬粡鎵撳紑 " + mFd);
			if (null != mOnOpenSerialPortListener) {
				mOnOpenSerialPortListener.onSuccess(device);
			}
			// 寮?鍚彂閫佹秷鎭殑绾跨▼
			startSendThread();
			// 寮?鍚帴鏀舵秷鎭殑绾跨▼
			startReadThread();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			if (null != mOnOpenSerialPortListener) {
				mOnOpenSerialPortListener.onFail(device, OnOpenSerialPortListener.Status.OPEN_FAIL);
			}
		}
		return false;
	}

	/**
	 * 鍏抽棴涓插彛
	 */
	public void closeSerialPort() {

		if (null != mFd) {
			close();
			mFd = null;
		}
		// 鍋滄鍙戦?佹秷鎭殑绾跨▼
		stopSendThread();
		// 鍋滄鎺ユ敹娑堟伅鐨勭嚎绋?
		stopReadThread();

		if (null != mFileInputStream) {
			try {
				mFileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mFileInputStream = null;
		}

		if (null != mFileOutputStream) {
			try {
				mFileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mFileOutputStream = null;
		}

		mOnOpenSerialPortListener = null;

		mOnSerialPortDataListener = null;
	}

	/**
	 * 娣诲姞鎵撳紑涓插彛鐩戝惉
	 *
	 * @param listener
	 *            listener
	 * @return SerialPortManager
	 */
	public SerialPortManager setOnOpenSerialPortListener(OnOpenSerialPortListener listener) {
		mOnOpenSerialPortListener = listener;
		return this;
	}

	public SerialPortManager setOnSerialPortDataListener(OnSerialPortDataListener listener) {
		mOnSerialPortDataListener = listener;
		return this;
	}

	/**
	 * 寮?鍚彂閫佹秷鎭殑绾跨▼
	 */
	private void startSendThread() {
		// 寮?鍚彂閫佹秷鎭殑绾跨▼
		mSendingHandlerThread = new HandlerThread("mSendingHandlerThread");
		mSendingHandlerThread.start();
		// Handler
		mSendingHandler = new Handler(mSendingHandlerThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				byte[] sendBytes = (byte[]) msg.obj;

				if (null != mFileOutputStream && null != sendBytes && 0 < sendBytes.length) {
					try {
						mFileOutputStream.write(sendBytes);
						if (null != mOnSerialPortDataListener) {
							mOnSerialPortDataListener.onDataSent(sendBytes);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	/**
	 * 鍋滄鍙戦?佹秷鎭嚎绋?
	 */
	private void stopSendThread() {
		mSendingHandler = null;
		if (null != mSendingHandlerThread) {
			mSendingHandlerThread.interrupt();
			mSendingHandlerThread.quit();
			mSendingHandlerThread = null;
		}
	}

	/**
	 * 寮?鍚帴鏀舵秷鎭殑绾跨▼
	 */
	private void startReadThread() {
		mSerialPortReadThread = new SerialPortReadThread(mFileInputStream) {
			@Override
			public void onDataReceived(byte[] bytes) {
				if (null != mOnSerialPortDataListener) {

					mOnSerialPortDataListener.onDataReceived(bytes);
				}
			}
		};
		mSerialPortReadThread.start();
	}

	/**
	 * 鍋滄鎺ユ敹娑堟伅鐨勭嚎绋?
	 */
	private void stopReadThread() {
		if (null != mSerialPortReadThread) {
			mSerialPortReadThread.release();
		}
	}

	/**
	 * 鍙戦?佹暟鎹?
	 *
	 * @param sendBytes
	 *            鍙戦?佹暟鎹?
	 * @return 鍙戦?佹槸鍚︽垚鍔?
	 */
	public boolean sendBytes(byte[] sendBytes) {
		if (null != mFd && null != mFileInputStream && null != mFileOutputStream) {
			if (null != mSendingHandler) {
				Message message = Message.obtain();
				message.obj = sendBytes;
				return mSendingHandler.sendMessage(message);
			}
		}
		return false;
	}
}
