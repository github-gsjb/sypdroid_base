package com.android.apparatus.serialport;

public class SerialPortReceiveDataEvent {
    //按键类型
    private String keyTyte;
    //消息
    private byte[] data;

    public SerialPortReceiveDataEvent(String keyTyte, byte[] data) {
        this.keyTyte = keyTyte;
        this.data = data;
    }

    public String getKeyTyte() {
        return keyTyte;
    }

    public void setKeyTyte(String keyTyte) {
        this.keyTyte = keyTyte;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}