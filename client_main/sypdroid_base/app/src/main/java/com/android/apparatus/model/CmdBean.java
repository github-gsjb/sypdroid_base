package com.android.apparatus.model;

/**
 * 命令的bean类
 */

/***
 1-2 SF 报文头(2 字节)，十六进制固定 LL 0x4C 0x4C
 3-4 Length 长度 (不含包头包尾和加密校验) 2 byte
 5 VER 版本号(1 字节)，二进制 如：Ver2.0 1 byte
 6
 RT 是否需回馈收到状态；
 发送方： 0=不需回馈，1=需要回馈
 MainBoard-->PC 方： 2=已收到
 1 byte
 7 SN 消息序号，0-255，每个消息的序号标志，循环使用。 1 byte
 8 MT 消息类型，十六进制 1 byte
 9-18 MSG 消息数据，普通消息为 10byte，升级包时为 1024byte Datas 10 byte
 19-20 CRC 和效验 CHK 2 byte
 21-22 END 报文尾(2 字节)，十六进制固定值 ZN 0x5A 0x4E

 数据实例：
 头   长度          版本号  是否需要回馈  messageNum   MessageType      MSG 消息数据
 LL + 0x00 + 0x0D + 0x02 + 0x01 +       0x00 +       0x05 +           [0x01 0x02 0x00 0x00 0x00
 CRC校验和
 0x00 0x00 0x00 0x00 0x00] +   0xAE + 0xEC + ZN
 */
public class CmdBean {
    //头字节数数组
    private byte head = (byte) 0xFA;

    public CmdBean(byte head, byte length, byte code, byte[] datas) {
        this.head = head;
        this.length = length;
        this.code = code;
        this.datas = datas;
        checkCRC();
    }

    public CmdBean() {
    }

    //计算出crc
    private void checkCRC() {
        if (datas != null && datas.length > 0) {
            byte[] tempBytes = new byte[datas.length + 2];
            tempBytes[0] = length;
            tempBytes[1] = code;
            System.arraycopy(datas, 0, tempBytes, 2, datas.length);
            crc = CheckSumUtil.sumCheck(tempBytes);
        }
    }

    public byte[] getBytes() {
        int tempLength = length + 2;
        byte[] tempBytes = new byte[tempLength];
        tempBytes[0] = head;
        tempBytes[1] = length;
        tempBytes[2] = code;
        System.arraycopy(datas, 0, tempBytes, 3, datas.length);
        checkCRC();
        tempBytes[tempBytes.length - 1] = crc;
        return tempBytes;
    }

    //长度字节数组
    //长度
    private byte length;
    //版本号
    private byte code;
    //预留位
    private byte[] datas;
    //消息序号
    private byte crc;


    public byte getHead() {
        return head;
    }

    public void setHead(byte head) {
        this.head = head;
    }

    public int getLength() {
        return length;
    }

    public void setLength(byte length) {
        this.length = length;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public byte[] getDatas() {
        return datas;
    }

    public void setDatas(byte[] datas) {
        this.datas = datas;
    }

    public byte getCrc() {
        return crc;
    }

    public void setCrc(byte crc) {
        this.crc = crc;
    }

}
