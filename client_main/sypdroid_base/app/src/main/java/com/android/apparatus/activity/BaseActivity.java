package com.android.apparatus.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * author: geshenjibi on 2019-06-17 01:12.
 * email: geshenjibi@163.com
 */
public class BaseActivity extends Activity {
    Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
    }
}
