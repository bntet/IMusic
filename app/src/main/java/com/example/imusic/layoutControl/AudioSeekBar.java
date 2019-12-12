package com.example.imusic.layoutControl;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;


public class AudioSeekBar extends LinearLayout implements SeekBar.OnSeekBarChangeListener {
    private Context mContext;
    private TextView tv_current , tv_totle;                   //当前时间view      总时间view
    private SeekBar seekbar;                                //进度条

    //音乐相关属性
    private MediaPlayer mediaPlayer;
    private int currentTime;
    private int durationTime;
    private int layoutViewId = 1;
    public static boolean SEEK_BAR_STATE = true ; //默认不是滑动状态

    public AudioSeekBar(Context context){
        this(context,null);
    }
    public AudioSeekBar(Context context, AttributeSet set){
        super(context,set);
        mContext = context;
        init();
    }

    private void init() {
        //当前时间 与 音乐总时间
        tv_current = newTextView(mContext,layoutViewId);
        tv_totle = newTextView(mContext,layoutViewId + 1);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

        //音乐进度条
        seekbar = new SeekBar(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1.0f);
        seekbar.setLayoutParams(params);
        seekbar.setMax(100);
        seekbar.setFocusable(true);
        seekbar.setId(layoutViewId + 2);
        seekbar.setMinimumHeight(100);
        seekbar.setThumbOffset(0);
        seekbar.setOnSeekBarChangeListener(this);
        seekbar.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    seekbar.setFocusable(true);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE)
                {
                    seekbar.requestFocus() ;
                }
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    seekbar.setFocusable(false);
                }
                return false;
            }
        }) ;
    }

    private TextView newTextView(Context mContext, int layoutViewId) {
        TextView textView = new TextView(mContext);
        textView.setId(layoutViewId);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        return textView;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
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
        /*
        对歌曲时间进行处理。由毫秒格式转换为HH:ss 形式。timeToStr方法来完成转换
         */
        tv_current.setText(timeToStr(currentTime));
        tv_totle.setText(timeToStr(durationTime));

        //设置百分比
        seekbar.setProgress((currentTime == 0)? 0 : currentTime * 100 / durationTime );
        addView(tv_current);
        addView(seekbar);
        addView(tv_totle);
    }
    /*
   将时间由毫秒转换为标准 分：秒 形式
    */
    private String timeToStr(int time)
    {
        String timeStr ;
        int second = time / 1000 ;
        int minute = second / 60 ;
        second = second - minute * 60 ;
        if (minute > 9)
        {
            timeStr = String.valueOf(minute) + ":" ;
        }else
        {
            timeStr = "0" + String .valueOf(minute) + ":" ;
        }
        if (second > 9)
        {
            timeStr += String.valueOf(second) ;
        }else {
            timeStr += "0" + String.valueOf(second) ;
        }

        return timeStr ;
    }
    /*
    获取主程序正在播放的MediaPlayer对象的当前播放时间
    */
    public void setCurrentTime(int currentTime)
    {
        this.currentTime = currentTime ;
       /*
       重绘和重新Layout
       */
        invalidate();
        requestLayout();
    }
    /*
          获取主程序正在播放的MediaPlayer对象
         */
    public void setMediaPlayer(MediaPlayer mediaPlayer)
    {
        this.mediaPlayer = mediaPlayer ;
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        /*
        对用户手动设定SeekBar进度值进行相应的跳转
         */
        if (b && SEEK_BAR_STATE)
        {
            int time = seekBar.getProgress() * durationTime / 100 ;
            mediaPlayer.seekTo(time);
            /*
            重绘和重新Layout
            */
            invalidate();
            requestLayout();

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        SEEK_BAR_STATE =false;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SEEK_BAR_STATE = true;
        onProgressChanged(seekBar,seekBar.getProgress(),true);
    }


}
