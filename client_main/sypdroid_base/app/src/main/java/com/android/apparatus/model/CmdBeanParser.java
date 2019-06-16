package com.android.apparatus.model;

import android.support.annotation.Nullable;

import com.android.apparatus.utils.LoggerUtil;
import com.android.apparatus.utils.StringHexUtils;


/**
 * 来乐2.0的协议解析工具
 */
public class CmdBeanParser {
    static final int STATE_HEAD1 = 0x01;//HEAD1
    static final int STATE_LEHGHT2 = 0x02;//LENGHT 长度
    static final int STATE_CODE3 = 0x03;//命令 长度
    static final int STATE_DATA4 = 0x04;//数据
    static final int STATE_CHK5 = 0x05;//CRC校验 CHK
    private static CmdBean cmdBean;
    //当前的解析的状态
    private static int state = STATE_HEAD1;
    //临时的存放数据包字节[]
    private static byte[] tempDatas;
    //index 读取临时byte[]的index标记
    private static int tempDatasIndex;
    private static int dataLength;

    private static byte tempCHK;
    private static boolean hasHead = false;

    public static CmdBean init() {
        state = STATE_HEAD1;
        tempDatas = null;
        tempDatasIndex = 0;
        dataLength = 0;
        tempCHK = 0x00;
        hasHead = false;
//        cmdBean=null;
        return null;
    }


    @Nullable
    public static synchronized CmdBean parseData(byte b) {
        LoggerUtil.println("状态", "state=" + state + StringHexUtils.Byte2Hex(b));
        if (state != STATE_HEAD1) {
//            init();
//            return null;
        }
        switch (state) {
            case STATE_HEAD1:
//                if (b == 0xFA) {
                    cmdBean = new CmdBean();
                    state = STATE_LEHGHT2;
//                } else {
//                    return init();
//                }
                break;
            case STATE_LEHGHT2:
                cmdBean.setLength(b);
                tempDatas = new byte[b - 2];
                tempDatasIndex=0;
                state = STATE_CODE3;
                break;
            case STATE_CODE3:
                //已经读取到长度
                cmdBean.setCode(b);
                state = STATE_DATA4;
                break;
            case STATE_DATA4:
                try {
                    //已经读取到长度
                    tempDatas[tempDatasIndex] = b;
                    tempDatasIndex++;
                    //如果标记==dataLength 长度。证明已经读完所需要的长度。
                    if (tempDatasIndex == tempDatas.length) {
                        cmdBean.setDatas(tempDatas);
                        state = STATE_CHK5;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    state = STATE_HEAD1;
//                    return cmdBean;
                }
                break;
            case STATE_CHK5:
                init();
                return cmdBean;
            //已经读取到版本
        }
        return null;
    }
}
