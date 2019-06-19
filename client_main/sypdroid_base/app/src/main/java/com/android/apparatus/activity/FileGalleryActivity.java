package com.android.apparatus.activity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.apparatus.files.FileManager;
import com.android.apparatus.files.FileType;
import com.android.apparatus.files.SyncImageLoader;
import com.android.apparatus.utils.Utils;

import net.majorkernelpanic.spydroid.R;

import java.util.List;

//ÎÄ¼þä¯ÀÀÆ÷
public class FileGalleryActivity extends Activity implements OnItemClickListener {

    private String tag = "LocaleFileGallery";
    private GridView gv;
    private MyGVAdapter adapter;
    private List<FileType> data;
    private TextView emptyView;
    private FileManager bfm;
    private TextView localefile_bottom_tv;
    private Button localefile_bottom_btn;

    private List<FileType> choosedFiles;
    private SyncImageLoader syncImageLoader;
    private int gridSize;
    private AbsListView.LayoutParams gridItemParams;// Ö÷Òª¸ù¾Ý²»Í¬·Ö±æÂÊÉèÖÃitem¿í¸ß
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (1 == msg.what) {
                syncImageLoader = new SyncImageLoader();
                choosedFiles = bfm.getChoosedFiles();
                gridItemParams = new AbsListView.LayoutParams(gridSize, gridSize);
                adapter = new MyGVAdapter();
                gv.setAdapter(adapter);
                gv.setOnScrollListener(adapter.onScrollListener);
                gv.setOnItemClickListener(FileGalleryActivity.this);
            } else if (0 == msg.what) {
                gv.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getString(R.string.curCatagoryNoFiles));
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_gallery);
        setTitle(getIntent().getStringExtra("title"));
        bfm = FileManager.getInstance();
        gv = (GridView) findViewById(R.id.gridView);
        emptyView = (TextView) findViewById(R.id.emptyView);
        localefile_bottom_btn = (Button) findViewById(R.id.file_bottom_btn);
        localefile_bottom_tv = (TextView) findViewById(R.id.file_bottom_tv);
        // ¼ÆËãÒ»ÏÂÔÚ²»Í¬·Ö±æÂÊÏÂgridItemÓ¦¸ÃÕ¾µÄ¿í¶È£¬ÔÚadapterÀïÖØÖÃÒ»ÏÂitem¿í¸ß
        gridSize = (Utils.getScreenWidth(this) - getResources().getDimensionPixelSize(R.dimen.view_8dp) * 5) / 4;// 4ÁÐ3¸ö¼ä¸ô£¬¼ÓÉÏ×óÓÒpadding£¬¹²¼Æ5¸ö
        // Log.i(tag, "gridSize:"+gridSize);
        onFileClick();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        data = bfm.getMediaFiles(FileGalleryActivity.this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (null != data)
            handler.sendEmptyMessage(1);
        else
            handler.sendEmptyMessage(0);
    }

    // µã»÷ÎÄ¼þ£¬´¥·¢ui¸üÐÂ
    // onResume£¬´¥·¢ui¸üÐÂ
    private void onFileClick() {
        localefile_bottom_tv.setText(bfm.getFilesSizes());
        int cnt = bfm.getFilesCnt();
        localefile_bottom_btn.setText(String.format(getString(R.string.file_choosedCnt), cnt + ""));
        localefile_bottom_btn.setEnabled(cnt > 0);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (null != data)
            data.clear();
        syncImageLoader = null;
        handler = null;
        data = null;
        adapter = null;
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_bottom_btn:
                setResult(2);
                finish();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        menu.add(0, 0, 0, getString(R.string.cancel));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (0 == item.getItemId()) {
            setResult(1);
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    class MyGVAdapter extends BaseAdapter {

        AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        syncImageLoader.lock();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        loadImage();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        syncImageLoader.lock();
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        };

        public void loadImage() {
            int start = gv.getFirstVisiblePosition();
            int end = gv.getLastVisiblePosition();
            if (end >= getCount()) {
                end = getCount() - 1;
            }
            syncImageLoader.setLoadLimit(start, end);
            syncImageLoader.unlock();
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if (null != data)
                return data.size();
            return 0;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if (null == convertView) {
                convertView = LayoutInflater.from(FileGalleryActivity.this).inflate(R.layout.gallery_item, null);
            }
            ImageView img = (ImageView) convertView.findViewById(R.id.img);
            img.setImageResource(R.drawable.file_default_pic);
            View itemView = convertView.findViewById(R.id.itemView);
            // ÖØÖÃ¿í¸ß
            itemView.setLayoutParams(gridItemParams);
            FileType bxfile = data.get(position);
            img.setTag(position);
            syncImageLoader.loadDiskImage(position, bxfile.getFilePath(), imageLoadListener);
            View checkView = convertView.findViewById(R.id.checkView);
            if (choosedFiles.contains(bxfile)) {
                checkView.setVisibility(View.VISIBLE);
            } else {
                checkView.setVisibility(View.GONE);
            }
            return convertView;
        }

        SyncImageLoader.OnImageLoadListener imageLoadListener = new SyncImageLoader.OnImageLoadListener() {
            @Override
            public void onImageLoad(Integer t, Drawable drawable) {
                View view = gv.findViewWithTag(t);
                if (view != null) {
                    ImageView iv = (ImageView) view.findViewById(R.id.img);
                    iv.setImageDrawable(drawable);
                } else {
                    Log.i(tag, "View not exists");
                }
            }

            @Override
            public void onError(Integer t) {
                View view = gv.findViewWithTag(t);
                if (view != null) {
                    ImageView iv = (ImageView) view.findViewById(R.id.img);
                    iv.setImageResource(R.drawable.file_default_pic);
                } else {
                    Log.i(tag, " onError View not exists");
                }
            }

        };
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View convertView, int pos, long arg3) {
        // TODO Auto-generated method stub
        View view = gv.findViewWithTag(pos);
        if (null != view) {
            View checkView = convertView.findViewById(R.id.checkView);
            FileType bxfile = data.get(pos);
            if (choosedFiles.contains(bxfile)) {
                choosedFiles.remove(bxfile);
                checkView.setVisibility(View.GONE);
            } else {
                choosedFiles.add(bxfile);
                checkView.setVisibility(View.VISIBLE);
            }
            onFileClick();
        } else {
            Log.i(tag, " onClick View not exists");
        }
    }

}
