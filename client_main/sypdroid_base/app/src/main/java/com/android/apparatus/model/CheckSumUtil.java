package com.android.apparatus.model;

/**
 * author: geshenjibi on 2019/3/30.
 * email: geshenjibi@163.com
 */
public class CheckSumUtil {
    /**
     * 求校验和的算法
     *
     * @param b 需要求校验和的字节数组
     * @return 校验和
     */
    public static byte sumCheck(byte[] b, int len) {
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum = sum + b[i];
        }
        return (byte) (sum & 0xff);
    }
    public static byte sumCheck(byte[] b) {
        int sum = 0;
        for (int i = 0; i < b.length; i++) {
            sum = sum + b[i];
        }
        return (byte) (sum & 0xff);
    }

}
