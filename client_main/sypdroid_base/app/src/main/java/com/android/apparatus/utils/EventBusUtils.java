package com.android.apparatus.utils;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

/**
 * author: geshenjibi on 2019-06-16 21:19.
 * email: geshenjibi@163.com
 * 用于注册和注销EventBus
 */
public class EventBusUtils {
    public static void registerEventBus(Context context) {
        if (!EventBus.getDefault().isRegistered(context)) {
            EventBus.getDefault().register(context);
            LoggerUtil.println("EventBus注册");
        }
    }

    /*
     *
     *
     * */
    public static void unRegisterEventBus(Context context) {
        EventBus.getDefault().unregister(context);
    }
}
