package com.example.imusic.activity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.imusic.R;
import com.example.imusic.bean.MusicBean;
import com.example.imusic.layoutControl.AudioControl;
import com.example.imusic.layoutControl.AudioSeekBar;
import com.example.imusic.service.MusicPlayService;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {
    private List<MusicBean> musicBeanList;                  //音乐列表
    private int position;                                   //音乐索引
    static int savePosition;            //用于判断点击的音乐是否为正在播放的音乐
    private MediaPlayer mediaPlayer = null;                 //播放音乐对象
    static MediaPlayer saveMediaPlayer; //用于判断点击的音乐是否为正在播放的音乐
    private Bundle bundle;                                  //获取从 活动 传递的参数
    public static int currentTime;                          //当前音乐播放时间
    private int duration;                                   //音乐总时长
    private Handler handler1;                               //更新音乐当前播放时间
    private Handler handler2;                               //旋转图片
    private Animation animation;                            //音乐图片旋转动画
    //布局控件
    private TextView tv_back;
    private TextView musicName;
    private TextView musicArtist;
    private ImageView musicImage;
    //自定义控件
    private AudioControl audioControl;
    private AudioSeekBar audioSeekBar;

    private MusicPlayService musicPlayService;              //服务
    private ServiceConnection serviceConnection;            //服务链接标识
    private Intent intent;                                  //用于绑定服务
    private GestureDetector detector ;                      //手势检测
    //广播接收
    private MusicReceiver musicReceiver;
    private MusicSwitchReceiver musicSwitchReceiver;

    //用来判断播放控制器是否被点击
    private static boolean AUDIO_STATE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        bundle = new Bundle();
        bundle = getIntent().getExtras();
        try{
            musicBeanList = (List<MusicBean>) bundle.getSerializable("musicBean");
            position = bundle.getInt("position");
            System.out.println("position is " + position + "  " + "musicBeanList " + musicBeanList.get(position).getData());
        }catch (Exception e){
            System.out.println("获取数据失败");
            e.printStackTrace();
        }

        //手势
        detector = new GestureDetector(this) ;

        init();

        //服务链接标识
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MusicPlayService.MusicBind mBind = (MusicPlayService.MusicBind) iBinder;
                musicPlayService = mBind.getService();
                musicPlayService.setIndex(position);
                musicPlayService.setMusicBeanList(musicBeanList);
                musicPlayService.setMediaPlayer(mediaPlayer);
                System.out.println("musicPlayerService 链接成功");
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                musicPlayService = null;
                System.out.println("musicPlayerService 链接失败");
            }
        };
        //绑定服务
        intent = new Intent();
        intent.setClass(this,MusicPlayService.class);
        bindService(intent,serviceConnection, Service.BIND_AUTO_CREATE);

        //广播注册
        //改广播用于音乐列表的点击
        musicReceiver = new MusicReceiver();
        IntentFilter filter = new IntentFilter("com.imusic.MUSIC_BROADCAST");
        registerReceiver(musicReceiver,filter);

        //该广播用于音乐播放界面的上，下首点击
        musicSwitchReceiver = new MusicSwitchReceiver();
        IntentFilter filter1 = new IntentFilter("com.imusic.MUSIC_SWITCH");
        registerReceiver(musicSwitchReceiver,filter1);


        //Time和handler用于界面更新 音乐播放界面的进度条当前时间更新
        handler1 = new MyHandler();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler1.sendEmptyMessage(01);
            }
        },100,1000);
        //主要用于音乐图片旋转
        if (mediaPlayer.isPlaying()){
            handler2 = new MyHandler2();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    handler2.sendEmptyMessage(02);
                }
            },0,8000);
        }

        audioControl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){//获得焦点
                    AUDIO_STATE = true ;
                }else{//失去焦点
                    AUDIO_STATE = false ;
                }
            }
        });
        audioControl.setFocusableInTouchMode(true);
    }

    private void init() {
        tv_back = findViewById(R.id.tv_back);
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayActivity.this.onBackPressed();
            }
        });
        mediaPlayer = MediaPlayer.create(this, Uri.parse(musicBeanList.get(position).getData()));

        /*
        绑定音乐播放完成监听事件
         */
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                int size = musicBeanList.size();
                position++;

                if (position == size){
                    position = 0;
                }

                currentTime = 0;
                init();

                musicPlayService.setIndex(position);
                musicPlayService.setMediaPlayer(mediaPlayer);
                musicPlayService.setMusicBeanList(musicBeanList);
            }
        });
        //获取总长度
        duration =musicBeanList.get(position).getDuration();

        //初始化layout的控件
        musicName = findViewById(R.id.tv_name);
        musicName.setText(musicBeanList.get(position).getTitle());

        musicArtist = findViewById(R.id.tv_artist);
        musicArtist.setText(musicBeanList.get(position).getArtist());

        musicImage = findViewById(R.id.iv_album);
        try{
            int imagePath =  Integer.parseInt(musicBeanList.get(position).getAlbum_id())  ;
            musicImage.setImageResource(imagePath);
        }catch (Exception e)
        {
            Bitmap bt = BitmapFactory.decodeFile(musicBeanList.get(position).getAlbum_id()) ;
            musicImage.setImageBitmap(bt);
        }

        //初始化音乐控制器,绑定mediaPlayer并重绘
        audioControl = findViewById(R.id.ll_control);
        audioControl.setMediaPlayer(mediaPlayer);
        audioControl.invalidate();
        audioControl.requestLayout();

        audioSeekBar = findViewById(R.id.ll_progress);
        audioSeekBar.setMediaPlayer(mediaPlayer);
        audioSeekBar.invalidate();
        audioSeekBar.requestLayout();
    }


    //手势接口方法
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    //滑动切歌
    @Override
    public boolean onFling(MotionEvent m1, MotionEvent m2, float x, float y) {

        //规定滑动速度为 ：40
        //        长度为：300
        if(m1 == null || m2 == null){
            return false;
        }
        //向左划 下一首
        if(m1.getX() - m2.getX() >= 300 && Math.abs(x) >= 40){
            Intent intent = new Intent();
            intent.setAction("com.imusic.MUSIC_SWITCH");
            intent.putExtra("NEXT","NEXT");
            intent.putExtra("PRE","");
            this.sendBroadcast(intent);
        }
        //向右划 上一首
        if(m2.getX() - m1.getX() >= 300 && Math.abs(x) >=40){
            Intent intent = new Intent();
            intent.setAction("com.imusic.MUSIC_SWITCH");
            intent.putExtra("NEXT","");
            intent.putExtra("PRE","PRE");
            this.sendBroadcast(intent);
        }
        return false;
    }


    //注册播musicReceiver
    public class MusicReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("music广播注册成功");
            int time = intent.getIntExtra("currentTime",0);
            System.out.println("this time is " + time);
            mediaPlayer.start();
            mediaPlayer.seekTo(time);
            audioControl.setCurrentTime(mediaPlayer.getCurrentPosition());
            audioSeekBar.setCurrentTime(mediaPlayer.getCurrentPosition());
        }
    }

    //注册广播musicSwitchReceiver
    public class MusicSwitchReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("musicSwitch 广播注册成功");
            if(!intent.getStringExtra("NEXT").isEmpty() && intent.getStringExtra("NEXT").equals("NEXT")){
                mediaPlayer.seekTo(mediaPlayer.getDuration());
                if(AudioControl.isPause){
                    //设置状态为播放
                    AudioControl.isPause = false;
                    mediaPlayer.start();
                    Intent intentPlay = new Intent("com.imusic.MUSICPLAY_BROADCAST");
                    intentPlay.putExtra("AudioControl","AudioControl");
                    sendBroadcast(intentPlay);
                }
            }else if(intent.getStringExtra("PRE").equals("PRE")){
                position--;
                if(position < 0){
                    position = musicBeanList.size()-1;
                }
                currentTime = 0;
                init();

                AudioControl.isPause = false;
                musicPlayService.setIndex(position);
                musicPlayService.setMediaPlayer(mediaPlayer);
                musicPlayService.setMusicBeanList(musicBeanList);
                Intent intentPlay = new Intent("com.imusic.MUSICPLAY_BROADCAST");
                intentPlay.putExtra("AudioControl","AudioControl");
                sendBroadcast(intentPlay);
            }
        }
    }

    //MyHandler用于更新音乐播放当前时间
    public class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 01){
                audioControl.setCurrentTime(mediaPlayer.getCurrentPosition());
                audioSeekBar.setCurrentTime(mediaPlayer.getCurrentPosition());
            }
            }
    }

    //MyHandler2 用于音乐播放界面的旋转图片
    public class MyHandler2 extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 02){
                animation = AnimationUtils.loadAnimation(MusicPlayActivity.this,R.anim.image_rotate);
                musicImage.setAnimation(animation);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        unregisterReceiver(musicReceiver);
        unregisterReceiver(musicSwitchReceiver);
    }
}
