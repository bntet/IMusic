package com.example.imusic.layoutControl;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.imusic.R;


public class AudioControl extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private ImageView iv_play,iv_next,iv_pre,iv_favorite,iv_mode;      //播放与暂停   下一首   上一首   收藏     播放模式
    private int layoutViewId = 1;
    //音乐相关属性
    private MediaPlayer mediaPlayer;
    private int currentTime = 0;
    private int durationTime = 0;
    public  static boolean isPause = false ;  //歌曲状态判断

    public AudioControl(Context context){
        this(context,null);
    }
    public AudioControl(Context context,AttributeSet set){
        super(context,set);
        mContext = context;
        init();
    }
    /*
          获取主程序正在播放的MediaPlayer对象
         */
    public void setMediaPlayer(MediaPlayer mediaPlayer)
    {
        this.mediaPlayer = mediaPlayer ;
    }

    public void setCurrentTime(int currentTime){
        this.currentTime = currentTime;

        invalidate();
        requestLayout();
    }
    private LinearLayout.LayoutParams getParams()
    {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(30,0,30,0);
        return params ;
    }

    private void init() {
        LinearLayout.LayoutParams params1 = getParams() ;
        iv_play = new ImageView(mContext);
        iv_play.setLayoutParams(params1);
        iv_play.setId(layoutViewId + 3);
        iv_play.setImageResource(R.drawable.ic_pause);
        iv_play.setOnClickListener(this);

        LinearLayout.LayoutParams params2 = getParams();
        iv_pre = new ImageView(mContext);
        iv_pre.setId(layoutViewId + 4);
        iv_pre.setLayoutParams(params2);
        iv_pre.setOnClickListener(this);
        iv_pre.setImageResource(R.drawable.ic_play_last);

        LinearLayout.LayoutParams params3 = getParams();
        iv_next = new ImageView(mContext);
        iv_next.setId(layoutViewId + 5);
        iv_next.setLayoutParams(params3);
        iv_next.setOnClickListener(this);
        iv_next.setImageResource(R.drawable.ic_play_next);

        LinearLayout.LayoutParams params4 = getParams();
        iv_favorite = new ImageView(mContext);
        iv_favorite.setId(layoutViewId + 6);
        iv_favorite.setLayoutParams(params4);
        iv_favorite.setOnClickListener(this);
        iv_favorite.setImageResource(R.drawable.ic_favorite_no);

        LinearLayout.LayoutParams params5 = getParams();
        iv_mode = new ImageView(mContext);
        iv_mode.setId(layoutViewId + 7);
        iv_mode.setLayoutParams(params5);
        iv_mode.setOnClickListener(this);
        iv_mode.setImageResource(R.drawable.ic_play_mode_list);
    }

    //确定控件位置
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed ,l ,t ,r ,b);
        removeAllViews();
        /*
        设置歌曲当前播放时间和总时间
         */
        try {
            currentTime = mediaPlayer.getCurrentPosition() ;
            durationTime = mediaPlayer.getDuration() ;
        }catch (Exception e)
        {
            currentTime = 0 ;
            durationTime = 0 ;
        }

        if(currentTime == 0 || isPause){
            iv_play.setImageResource(R.drawable.ic_play);
        }else{
            iv_play.setImageResource(R.drawable.ic_pause);
        }
        addView(iv_mode);
        addView(iv_pre);
        addView(iv_play);
        addView(iv_next);
        addView(iv_favorite);
    }

    @Override
    public void onClick(View view) {

        //点击播放暂停按钮
        if (view.getId() == iv_play.getId()){
            if (mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                iv_play.setImageResource(R.drawable.ic_play);
                isPause = true;
            }else{
                if(currentTime == 0){
                    mediaPlayer.start();
                }else{
                    mediaPlayer.start();
                    mediaPlayer.seekTo(currentTime);
                }
                isPause = false;
                iv_play.setImageResource(R.drawable.ic_pause);
            }
            //发送广播
            Intent intent = new Intent("com.imusic.MUSICPLAY_BROADCAST");
            intent.putExtra("AudioControl","AudioControl");
            mContext.sendBroadcast(intent);
        }

        //点击下一首
        if(view.getId() == iv_next.getId()){
            Intent intent = new Intent();
            intent.setAction("com.imusic.MUSIC_SWITCH");
            intent.putExtra("NEXT","NEXT");
            intent.putExtra("PRE","");
            mContext.sendBroadcast(intent);
        }

        //点击上一首
        if(view.getId() == iv_pre.getId()){
            Intent intent = new Intent();
            intent.setAction("com.imusic.MUSIC_SWITCH");
            intent.putExtra("NEXT","");
            intent.putExtra("PRE","PRE");
            mContext.sendBroadcast(intent);
        }

        /*
        重绘和重新Layout
         */
        invalidate();
        requestLayout();
    }


}
