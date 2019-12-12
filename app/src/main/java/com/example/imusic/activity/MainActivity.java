package com.example.imusic.activity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imusic.R;
import com.example.imusic.adapter.MainAdapter;
import com.example.imusic.collector.ActivityCollector;
import com.example.imusic.fragment.MusicLibFragment;
import com.example.imusic.fragment.MyInfoFragment;
import com.example.imusic.fragment.NewsFragment;
import com.example.imusic.service.MusicPlayService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener{
    private TextView title;
    private ViewPager vp;
    private List<Fragment> fragmentList = new ArrayList<>();
    private MusicLibFragment musicLibFragment;    //音乐馆
    private NewsFragment newsFragment;            //动态
    private MyInfoFragment myInfoFragment;        //我
    private View vMusic,vNews,vMyInfo;
    private TextView tv_music,tv_news,tv_myInfo;
    private ImageView iv_music,iv_news,iv_myInfo;
    private MainAdapter mainAdapter;
    private Intent ServiceIntent;
    private LinearLayout ll_search;
    String[] titles = new String[]{"音乐馆","动态","我的"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        initView();
        initFragment();
        setInitStatus();
        mainAdapter = new MainAdapter(this.getSupportFragmentManager(),fragmentList);
        vp.setOffscreenPageLimit(3);
        vp.setAdapter(mainAdapter);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {      //页面选中时
                clearBtnState();
                title.setText(titles[position]);
                changeBtnColor(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //启动服务
        ServiceIntent = new Intent();
        ServiceIntent.setClass(this, MusicPlayService.class);
        startService(ServiceIntent);
    }

    //设置初始化样式
    private void setInitStatus(){
        clearBtnState();
        changeBtnColor(0);
        vp.setCurrentItem(0);
    }

    //重置底部导航样式
    private void clearBtnState(){
        iv_music.setColorFilter(Color.parseColor("#000000"));
        iv_news.setColorFilter(Color.parseColor("#000000"));
        iv_myInfo.setColorFilter(Color.parseColor("#000000"));
        tv_music.setTextColor(Color.parseColor("#666666"));
        tv_news.setTextColor(Color.parseColor("#666666"));
        tv_myInfo.setTextColor(Color.parseColor("#666666"));
    }

    //改变底部导航样式
    private void changeBtnColor(int position) {
        if(position == 0){
            iv_music.setColorFilter(Color.parseColor("#4a5396"));
            tv_music.setTextColor(Color.parseColor("#4a5396"));
        }if(position == 1){
            iv_news.setColorFilter(Color.parseColor("#4a5396"));
            tv_news.setTextColor(Color.parseColor("#4a5396"));
        }if(position == 2){
            iv_myInfo.setColorFilter(Color.parseColor("#4a5396"));
            tv_myInfo.setTextColor(Color.parseColor("#4a5396"));
        }
    }

    //初始化碎片
    private void initFragment() {
        //获取viewpager
        vp = findViewById(R.id.mainViewPager);
        musicLibFragment = new MusicLibFragment();
        newsFragment = new NewsFragment();
        myInfoFragment = new MyInfoFragment();

        fragmentList.add(musicLibFragment);
        fragmentList.add(newsFragment);
        fragmentList.add(myInfoFragment);
    }

    //初始化布局控件
    private void initView() {
        //获取顶部导航文字
        title = findViewById(R.id.tv_main_title);
        ll_search = findViewById(R.id.ll_search);
        //获取底部按钮
        vMusic = findViewById(R.id.bottom_bar_music_btn);
        vNews = findViewById(R.id.bottom_bar_news_btn);
        vMyInfo = findViewById(R.id.bottom_bar_myinfo_btn);
        tv_music = findViewById(R.id.bottom_bar_text_music);
        tv_news = findViewById(R.id.bottom_bar_text_news);
        tv_myInfo = findViewById(R.id.bottom_bar_text_myinfo);
        iv_music = findViewById(R.id.bottom_bar_image_music);
        iv_news = findViewById(R.id.bottom_bar_image_news);
        iv_myInfo = findViewById(R.id.bottom_bar_image_myinfo);

        ll_search.setOnClickListener(this);
        vMusic.setOnClickListener(this);
        vNews.setOnClickListener(this);
        vMyInfo.setOnClickListener(this);

    }

    //底部控件被点击事件   和    顶部搜索按钮
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bottom_bar_music_btn:
                vp.setCurrentItem(0,true);
                break;
            case R.id.bottom_bar_news_btn:
                vp.setCurrentItem(1,true);
                break;
            case R.id.bottom_bar_myinfo_btn:
                vp.setCurrentItem(2,true);
                break;
            case R.id.ll_search:
                Intent intent = new Intent(this,SearchActivity.class);
                startActivity(intent);
        }
    }

    //双击返回，退出程序
    protected long exitTime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis() - exitTime) > 2000){
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }else{
                ActivityCollector.finishAll();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //接受返回值的事件
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            boolean isLogin = data.getBooleanExtra("isLogin",false);
            if(isLogin){

            }
            if(myInfoFragment != null){
                myInfoFragment.setLoginParams(isLogin);
            }
        }
    }

    //权限处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(musicLibFragment != null){
            musicLibFragment.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(ServiceIntent);
    }
}
