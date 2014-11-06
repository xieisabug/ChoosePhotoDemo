package com.xjy.choosephotodemo;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.xjy.choosephotodemo.utils.NativeImageLoader;
import com.xjy.choosephotodemo.utils.ViewHolderUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private GridView mPhotoChooseGridView;
    private TextView mTipTextView;
    private LinearLayout mDragView;
    private SlidingUpPanelLayout mSlideUpPanel;

    //touch中表示已经点击
    boolean click = false;
    //移动的x,y和当前的x,y
    float mx, my, curX, curY;
    boolean couldScroll = true;

    //本地图片加载
    private NativeImageLoader mNativeImageLoader;

    //已选照片Adapter
    private GridWithEndButtonAdapter gridWithEndButtonAdapter;

    //所有本地图片
    private List<String> picList = new ArrayList<String>();
    //所有选择了的图片
    private SparseArray<String> choosePicList = new SparseArray<String>();

    private NoScrollTouchListener mNoScrollTouchListener = new NoScrollTouchListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPhotoData();
        initView();
    }

    //查询所有照片
    private void initPhotoData() {
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = MainActivity.this.getContentResolver();

        //只查询jpeg和png的图片
        Cursor mCursor = mContentResolver.query(mImageUri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_ADDED);

        if (mCursor == null) {
            return;
        }

        while (mCursor.moveToNext()) {
            //获取图片的路径
            String path = mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
            picList.add(path);
        }

        mCursor.close();
    }

    //初始化控件页面
    private void initView() {

        GridView photoGridView = (GridView) findViewById(R.id.photo);
        mPhotoChooseGridView = (GridView) findViewById(R.id.photo_choose_grid);
        mTipTextView = (TextView) findViewById(R.id.tip);
        mDragView = (LinearLayout) findViewById(R.id.dragView);
        mSlideUpPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        //初始化本地图片加载工具
        mNativeImageLoader = NativeImageLoader.getInstance();

        //初始化已选照片的GridView
        gridWithEndButtonAdapter = new GridWithEndButtonAdapter(choosePicList);
        photoGridView.setAdapter(gridWithEndButtonAdapter);

        //初始化选择照片的GridView
        GridWithCameraAndImageButtonAdapter gridWithCameraAndImageButtonAdapter = new GridWithCameraAndImageButtonAdapter(picList);
        mPhotoChooseGridView.setAdapter(gridWithCameraAndImageButtonAdapter);
        mPhotoChooseGridView.setOnTouchListener(mNoScrollTouchListener);
        mPhotoChooseGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //TODO 拍照
                        break;
                    case 1:
                        //TODO 选择所有相片
                        break;
                    default:
                        choosePicList.put(position - 2, picList.get(position - 2));
                        gridWithEndButtonAdapter.updateData(choosePicList);
                }
            }
        });

        //初始化上拉Panel
        mSlideUpPanel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {

            }

            @Override
            public void onPanelCollapsed(View view) {
                mPhotoChooseGridView.setOnTouchListener(mNoScrollTouchListener);
                mSlideUpPanel.setDragView(mDragView);
            }

            @Override
            public void onPanelExpanded(View view) {
                //恢复滚动，如果到了顶头就进行回缩
                mPhotoChooseGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if (mPhotoChooseGridView.getFirstVisiblePosition() == 0 &&
                                mPhotoChooseGridView.getChildAt(mPhotoChooseGridView.getFirstVisiblePosition()).getTop() == 0) {
                            mPhotoChooseGridView.setOnTouchListener(new NoScrollDownTouchListener());
                            mSlideUpPanel.setDragView(mDragView);
                            couldScroll = false;
                        }else {
                            couldScroll = true;
                            mSlideUpPanel.setDragView(mTipTextView);
                            mPhotoChooseGridView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return false;
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onPanelAnchored(View view) {

            }

            @Override
            public void onPanelHidden(View view) {

            }
        });
        mSlideUpPanel.hidePanel();
    }

    @Override
    public void onBackPressed() {
        //如果按下返回的时候面板是开启的，则优先关闭面板
        if (!mSlideUpPanel.isPanelHidden()) {
            mPhotoChooseGridView.smoothScrollToPosition(0);
            mSlideUpPanel.hidePanel();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    /**
     * 已选的照片Adapter
     */
    private class GridWithEndButtonAdapter extends BaseAdapter {

        private SparseArray<String> urls;

        public GridWithEndButtonAdapter(SparseArray<String> urls) {
            this.urls = urls;
        }

        public void updateData(SparseArray<String> urls) {
            this.urls = urls;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return urls.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return urls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.gridview_common_imageview_item, null);
            }
            final ImageView imageView = ViewHolderUtil.get(convertView, R.id.image);
            ImageView delete = ViewHolderUtil.get(convertView, R.id.delete);//删除按钮
            //如果不是添加按钮，则显示本地图片
            if (position != urls.size() && urls.size() != 0) {
                Bitmap bitmap = mNativeImageLoader.loadNativeImage(urls.valueAt(position), new NativeImageLoader.NativeImageCallBack() {
                    @Override
                    public void onImageLoader(Bitmap bitmap, String path) {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                });
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.ic_launcher);
                }
                delete.setVisibility(View.VISIBLE);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choosePicList.remove(urls.keyAt(position));
                        updateData(choosePicList);
                    }
                });
            } else {
                imageView.setImageResource(R.drawable.common_add_image_button);
                delete.setVisibility(View.INVISIBLE);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "添加", Toast.LENGTH_LONG).show();
                        mDragView.setVisibility(View.VISIBLE);
                        mSlideUpPanel.showPanel();
                    }
                });
            }

            return convertView;
        }
    }
    /**
     * 附带相机和一个按钮的Adapter
     */
    private class GridWithCameraAndImageButtonAdapter extends BaseAdapter {

        private List<String> urls;

        public GridWithCameraAndImageButtonAdapter(List<String> urls) {
            this.urls = urls;
        }

        @Override
        public int getCount() {
            return urls.size() + 2;
        }

        @Override
        public Object getItem(int position) {
            return urls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (position == 0) { // 拍照按钮
                View view = View.inflate(MainActivity.this, R.layout.surfaceview_common_camera, null);

                TextView textView = (TextView) view.findViewById(R.id.text);
                textView.setText("拍照");
                textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.slide_panel_choose_image_photo, 0, 0);
                return view;
            } else if (position == 1) { // 按文件夹查看所有照片
                View view = View.inflate(MainActivity.this, R.layout.surfaceview_common_camera, null);

                TextView textView = (TextView) view.findViewById(R.id.text);
                textView.setText("所有照片");
                textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.slide_panel_choose_image_album, 0, 0);
                return view;
            } else { // 从最近的照片开始
                if ((convertView == null && position >= 2) || (convertView != null && convertView.getClass() != LinearLayout.class)) {
                    convertView = View.inflate(MainActivity.this, R.layout.gridview_common_imageview_fit_xy_item, null);
                }
                if (Build.VERSION.SDK_INT > 10) {
                    ImageView imageView = ViewHolderUtil.get(convertView, R.id.image);
                    Glide.with(MainActivity.this).load(new File(urls.get(position-2))).placeholder(R.drawable.ic_launcher).into(imageView);
                } else {
                    final ImageView imageView = ViewHolderUtil.get(convertView, R.id.image);
                    Bitmap bitmap = mNativeImageLoader.loadNativeImage(urls.get(position - 2), new NativeImageLoader.NativeImageCallBack() {
                        @Override
                        public void onImageLoader(Bitmap bitmap, String path) {
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    });
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageResource(R.drawable.ic_launcher);
                    }
                }
            }
            return convertView;
        }

    }

    /**
     * 触摸时不会滑动
     */
    private class NoScrollTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    click = true;
                    mx = event.getX();
                    my = event.getY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    curX = event.getX();
                    curY = event.getY();
                    if (click && Math.abs(mx - curX) < 5 && Math.abs(my - curY) < 5) {
                        Log.i("miTraza", "movimiento pequeño");
                        click = true;
                        return true;
                    } else {
                        click = false;
                    }
                    mx = curX;
                    my = curY;
                    return true;
                case MotionEvent.ACTION_UP:
                    if (click) {
                        int position = mPhotoChooseGridView.pointToPosition((int) event.getX(), (int) event.getY());
                        if (position != GridView.INVALID_POSITION) {
                            mPhotoChooseGridView.performItemClick(mPhotoChooseGridView.getChildAt(position - mPhotoChooseGridView.getFirstVisiblePosition()), position, mPhotoChooseGridView.getItemIdAtPosition(position));
                        }
//                            Log.d("111", "you click: " + position);
                        click = false;
                    }
                    return true;
            }
            return false;
        }
    }

    /**
     * 向下触摸时不会滑动
     */
    private class NoScrollDownTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    click = true;
                    mx = event.getX();
                    my = event.getY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    curX = event.getX();
                    curY = event.getY();
                    if (click && (mx - curX) < 5 && (my - curY) < 5) {
                        click = true;
                        return true;
                    } else {
                        if (!couldScroll && (my - curY) < 0) {
                            mSlideUpPanel.collapsePanel();
                            mPhotoChooseGridView.smoothScrollToPosition(0);
                            return true;
                        }
                        click = false;
                    }
                    mx = curX;
                    my = curY;
                    return false;
                case MotionEvent.ACTION_UP:
                    if (click) {
                        int position = mPhotoChooseGridView.pointToPosition((int) event.getX(), (int) event.getY());
                        if (position != GridView.INVALID_POSITION) {
                            mPhotoChooseGridView.performItemClick(mPhotoChooseGridView.getChildAt(position - mPhotoChooseGridView.getFirstVisiblePosition()), position, mPhotoChooseGridView.getItemIdAtPosition(position));
                        }
//                            Log.d("111", "you click: " + position);
                        click = false;
                    }
                    return true;
            }
            return false;
        }
    }
}
