package com.android.apparatus.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.apparatus.Constant;
import com.android.apparatus.menu.MainMenuAdapter;
import com.android.apparatus.menu.SubordinateMenuAdapter;
import com.android.apparatus.serialport.SerialPortReceiveDataEvent;
import com.android.apparatus.utils.EventBusUtils;
import com.android.apparatus.utils.LoggerUtil;
import com.android.apparatus.utils.StringHexUtils;
import com.android.apparatus.utils.Utils;

import net.majorkernelpanic.spydroid.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: geshenjibi on 2019-06-17 01:11.
 * email: geshenjibi@163.com
 */
public class MenuActivity extends BaseActivity {
    ListView mainListView;
    ListView moreListView;
    MainMenuAdapter mainAdapter;
    private SubordinateMenuAdapter moreAdapter;
    String TAG = "MenuActivity";
    //    主ListView选中的位置
    private int mainSelectPostion = 0;

    private boolean isGetVersion = false;

    public String[] LISTVIEWTXT;
    public String[][] MORELISTVIEWTXT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_menu);
        EventBusUtils.registerEventBus(mContext);
        //先加载listview 的数据
        initModelData();
        initLayout();
    }

    private List<Map<String, Object>> mainList;

    private void initLayout() {

        mainListView = (ListView) findViewById(R.id.main_view);
        moreListView = (ListView) findViewById(R.id.more_view);
        mainAdapter = new MainMenuAdapter(this, mainList);
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Callback method to be invoked when an item in this AdapterView has
             * been clicked.
             * <p>
             * Implementers can call getItemAtPosition(position) if they need
             * to access the data associated with the selected item.
             *
             * @param parent   The AdapterView where the click happened.
             * @param view     The view within the AdapterView that was clicked (this
             *                 will be a view provided by the adapter)
             * @param position The position of the view in the adapter.
             * @param id       The row id of the item that was clicked.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "onItemSelected77777");
                // 存储选中项位置 在adapter使用
                mainSelectPostion = position;
                // 主目录一位数组的大小和侧目录二维数组的行的数目是一致的
                // 点击传入二维数组的一行的数据
                // inintAdapter(DataModel.MORELISTVIEWTXT[position]);
                if (position == 12 && !isGetVersion) {
                    isGetVersion = true;
                    String version = Utils.getVersion(MenuActivity.this);
                    String ver = MORELISTVIEWTXT[position][0] + version;
                    String[] a = ver.split("\\n");
                    MORELISTVIEWTXT[position] = a;
                }
                inintAdapter(MORELISTVIEWTXT[position]);
                mainAdapter.setSelectItem(position);
                mainAdapter.notifyDataSetChanged();
            }
        });
//        mainAdapter.setSelectItem(0);
        mainListView.setAdapter(mainAdapter);
//        mainListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        inintAdapter(MORELISTVIEWTXT[0]);

        moreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "onItemSelected");
                // 存储选中项位置 在adapter使用

                moreAdapter.setSelectItem(position);
                moreAdapter.notifyDataSetChanged();
                WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                // String content =
                // DataModel.MORELISTVIEWTXT[mainSelectPostion][position];
                String content = MORELISTVIEWTXT[mainSelectPostion][position];
                if (content.equals(getString(R.string.submenu_date))) {

                } else if (content.equals(getString(R.string.submenu_time))) {

                } else if (content.equals(getString(R.string.submenu_video_ratio1))) {

                } else if (content.equals(getString(R.string.submenu_video_ratio2))) {

                } else if (content.equals(getString(R.string.submenu_video_time1))) {

                } else if (content.equals(getString(R.string.submenu_video_time2))) {

                } else if (content.equals(getString(R.string.submenu_video_time3))) {

                } else if (content.equals(getString(R.string.submenu_video_time4))) {

                } else if (content.equals(getString(R.string.file_image))) {
                    Intent intent = new Intent(MenuActivity.this, FileGalleryActivity.class);
                    startActivity(intent);
                } else if (content.equals(getString(R.string.file_video))) {
                    Intent intent = new Intent(MenuActivity.this, FileGalleryActivity.class);
                    startActivity(intent);
                } else if (content.equals(getString(R.string.submenu_mimetic_diagram1))) {

                } else if (content.equals(getString(R.string.submenu_mimetic_diagram2))) {

                } else if (content.equals(getString(R.string.submenu_mimetic_diagram3))) {

                } else if (content.equals(getString(R.string.submenu_mimetic_diagram4))) {

                } else if (content.equals(getString(R.string.submenu_format))) {

                } else if (content.equals(getString(R.string.submenu_restore_factory))) {

                } else if (content.equals(getString(R.string.submenu_lcd))) {

                } else if (content.equals(getString(R.string.submenu_wlan_on))) {

                } else if (content.equals(getString(R.string.submenu_wlan_off))) {

                } else if (content.equals(getString(R.string.submenu_mobile_on))) {

                } else if (content.equals(getString(R.string.submenu_mobile_off))) {

                } else if (content.equals(getString(R.string.submenu_mimetic_off))) {

                } else if (content.equals(getString(R.string.submenu_language1))) {

                } else if (content.equals(getString(R.string.submenu_language2))) {

                } else if (content.equals(getString(R.string.submenu_language3))) {

                }

            }

        });
    }

    private void inintAdapter(String[] array) {
        // TODO Auto-generated method stub
        moreAdapter = new SubordinateMenuAdapter(this, array);
        moreListView.setAdapter(moreAdapter);
        moreAdapter.notifyDataSetChanged();
    }

    private void initModelData() {
        // TODO Auto-generated method stub
        Resources res = getResources();
        LISTVIEWTXT = res.getStringArray(R.array.main_menu);
        MORELISTVIEWTXT = new String[][]{getResources().getStringArray(R.array.sub_menu_playback),
                getResources().getStringArray(R.array.sub_menu_video_ratio),
                getResources().getStringArray(R.array.sub_menu_video_time),
                getResources().getStringArray(R.array.sub_menu_language),
                getResources().getStringArray(R.array.sub_menu_wlan),
                getResources().getStringArray(R.array.sub_menu_mobile_data),
                getResources().getStringArray(R.array.sub_menu_mimetic_diagram),
                getResources().getStringArray(R.array.sub_menu_gps),
                getResources().getStringArray(R.array.sub_menu_lcd),
                getResources().getStringArray(R.array.sub_menu_date),
                getResources().getStringArray(R.array.sub_menu_restore_factory),
                getResources().getStringArray(R.array.sub_menu_format),
                getResources().getStringArray(R.array.sub_menu_version)};
        mainList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < LISTVIEWTXT.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("txt", LISTVIEWTXT[i]);
            mainList.add(map);
        }
    }

    /**
     * 计算出来的位置，y方向就在anchorView的上面和下面对齐显示，x方向就是与屏幕右边对齐显示
     * 如果anchorView的位置有变化，就可以适当自己额外加入偏移来修正
     *
     * @param anchorView  呼出window的view
     * @param contentView window的内容布局
     * @return window显示的左上角的xOff, yOff坐标
     */
    private static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];
        // 获取锚点View在屏幕上的左上角坐标位置
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();
        // 获取屏幕的高宽
        final int screenHeight = Utils.getScreenHeight(anchorView.getContext());
        final int screenWidth = Utils.getScreenWidth(anchorView.getContext());
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 计算contentView的高宽
        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();
        // 判断需要向上弹出还是向下弹出显示
        final boolean isNeedShowUp = (screenHeight - anchorLoc[1] - anchorHeight < windowHeight);
        if (isNeedShowUp) {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] - windowHeight;
        } else {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] + anchorHeight;
        }
        return windowPos;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(SerialPortReceiveDataEvent receiveDataEvent) {
        //获取的蓝牙address和对应的数据
        LoggerUtil.println("FragmentTimings", "Event获取的串口数据是" + StringHexUtils.ByteArrToHex(receiveDataEvent.getData()));
        String keyType = receiveDataEvent.getKeyTyte();
        paserReceiverData(keyType);

    }

    //解析数据，处理
    private void paserReceiverData(String datas) {
        switch (datas) {
            case Constant.KEYCODE_UP:
                if (mainListView.hasFocus()) {
                    mainSelectPostion = mainSelectPostion - 1;
                    if (mainSelectPostion <= 0) {
                        mainSelectPostion = 0;
                    }
                    if (mainSelectPostion == 12 && !isGetVersion) {
                        isGetVersion = true;
                        String version = Utils.getVersion(MenuActivity.this);
                        String ver = MORELISTVIEWTXT[mainSelectPostion][0] + version;
                        String[] a = ver.split("\\n");
                        MORELISTVIEWTXT[mainSelectPostion] = a;
                    }
                    inintAdapter(MORELISTVIEWTXT[mainSelectPostion]);
                    mainAdapter.setSelectItem(mainSelectPostion);
                    mainAdapter.notifyDataSetChanged();
                }
                break; // 可选
            //菜单键
            case Constant.KEYCODE_MENU:
                break; // 可选
            case Constant.KEYCODE_RIGHT:
                break; // 可选
            case Constant.KEYCODE_DOWN:
                if (mainListView.hasFocus()) {
                    mainSelectPostion = mainSelectPostion + 1;
                    if (mainSelectPostion >= MORELISTVIEWTXT.length - 1) {
                        mainSelectPostion = MORELISTVIEWTXT.length - 1;
                    }
                    if (mainSelectPostion == 12 && !isGetVersion) {
                        isGetVersion = true;
                        String version = Utils.getVersion(MenuActivity.this);
                        String ver = MORELISTVIEWTXT[mainSelectPostion][0] + version;
                        String[] a = ver.split("\\n");
                        MORELISTVIEWTXT[mainSelectPostion] = a;
                    }
                    inintAdapter(MORELISTVIEWTXT[mainSelectPostion]);
                    mainAdapter.setSelectItem(mainSelectPostion);
                    mainAdapter.notifyDataSetChanged();
                }
                break; // 可选
            case Constant.KEYCODE_OK:
                break; // 可选
            case Constant.KEYCODE_BACK:
                break; // 可选
            default: // 可选
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtils.unRegisterEventBus(mContext);
    }
}
