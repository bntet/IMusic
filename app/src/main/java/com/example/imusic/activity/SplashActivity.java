package com.example.imusic.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imusic.R;
import com.example.imusic.adapter.SplashAdapter;
import com.example.imusic.collector.ActivityCollector;
import com.example.imusic.fragment.SplashFragment_1;
import com.example.imusic.fragment.SplashFragment_2;
import com.example.imusic.fragment.SplashFragment_3;
import com.example.imusic.fragment.SplashFragment_4;
import com.example.imusic.fragment.SplashFragment_5;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends BaseActivity {
    private SplashAdapter splashAdapter;
    private ViewPager vp;
    private TextView splashSkip;
    private SplashFragment_1 splashFragment_1;
    private SplashFragment_2 splashFragment_2;
    private SplashFragment_3 splashFragment_3;
    private SplashFragment_4 splashFragment_4;
    private SplashFragment_5 splashFragment_5;

    private List<Fragment> sFragmentList = new ArrayList<>();
    public SplashActivity(){

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initView();
        splashAdapter = new SplashAdapter(this.getSupportFragmentManager(),sFragmentList);
        vp.setOffscreenPageLimit(5);
        vp.setAdapter(splashAdapter);
        vp.setCurrentItem(0);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void initView() {
        splashSkip = findViewById(R.id.splash_skip);
        vp = findViewById(R.id.splash_ViewPager);
        splashFragment_1 = new SplashFragment_1();
        splashFragment_2 = new SplashFragment_2();
        splashFragment_3 = new SplashFragment_3();
        splashFragment_4 = new SplashFragment_4();
        splashFragment_5 = new SplashFragment_5();
        sFragmentList.add(splashFragment_1);
        sFragmentList.add(splashFragment_2);
        sFragmentList.add(splashFragment_3);
        sFragmentList.add(splashFragment_4);
        sFragmentList.add(splashFragment_5);

    }

    private void init() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        };
        timer.schedule(task, 3000);
    }
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
}
