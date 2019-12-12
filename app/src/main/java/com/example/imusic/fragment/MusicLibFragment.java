package com.example.imusic.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.imusic.R;
import com.example.imusic.activity.MusicPlayActivity;
import com.example.imusic.bean.MusicBean;
import com.example.imusic.service.MusicPlayService;
import com.example.imusic.sqlite.SearchSqliteHelper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicLibFragment extends Fragment implements AdapterView.OnItemClickListener{
    private Cursor mCursor;
    private Cursor mCursor2;        //用于搜索
    private View view;
    //MainActivity的activity
    private Activity mActivity;
    //权限申请码requestCode
    private final static int STORGE_REQUEST = 1 ;
    //用于装载MusicBeans对象
    private static List<MusicBean> musicBeans ;
    //用于将mCursor的数据导入到List对象中，再作为Adapater参数传入
    private List<Map<String,String>> list_map;
    private ListView MusicListView ;
    private ContentResolver contentResolver;
    //指定SimpleAdapter对象
    private SimpleAdapter simpleAdapter ;
    private static SearchSqliteHelper searchSqliteHelper;
    private SQLiteDatabase db_search;
    public MusicLibFragment(){
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_music_lib, container, false);
        mActivity = getActivity();
        MusicListView = view.findViewById(R.id.lv_music);
        //首先检查自身是否已经拥有相关权限，拥有则不再重复申请
        if (ContextCompat.checkSelfPermission(mActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            //申请权限
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE} ,STORGE_REQUEST);
        }else {
            init();
        }

        return view;
    }

    //初始化
    private void init() {
        searchSqliteHelper = new SearchSqliteHelper(mActivity);
        deleteSearchResultData();
        //从数据库中获取指定列的信息
        contentResolver = mActivity.getContentResolver();
        mCursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI ,
                new String[] {MediaStore.Audio.Media._ID ,
                        MediaStore.Audio.Media.TITLE ,
                        MediaStore.Audio.Media.ALBUM ,
                        MediaStore.Audio.Media.ARTIST ,
                        MediaStore.Audio.Media.DURATION ,
                        MediaStore.Audio.Media.DISPLAY_NAME ,
                        MediaStore.Audio.Media.SIZE ,
                        MediaStore.Audio.Media.DATA ,
                        MediaStore.Audio.Media.ALBUM_ID } , null ,null ,null) ;
        list_map = new ArrayList<>();
        musicBeans = new ArrayList<>();
        for (int i = 0 ; i < mCursor.getCount() ; i++)
        {
            //用于simpleAdapter数据
            Map<String , String> map = new HashMap<>() ;
            MusicBean musicBean = new MusicBean() ;
            //列表移动
            mCursor.moveToNext() ;
            //将数据装载到List<MusicBean>中
            musicBean.set_id(mCursor.getInt(0));
            musicBean.setTitle(mCursor.getString(1));
            musicBean.setAlbum(mCursor.getString(2));
            musicBean.setDuration(mCursor.getInt(4));
            musicBean.setMusicName(mCursor.getString(5));
            musicBean.setSize(mCursor.getInt(6));
            musicBean.setData(mCursor.getString(7));

            musicBean.setPosition(i);
            map.put("position",String.valueOf(musicBean.getPosition()));


            //将音乐数据存入sqlLite中，用于搜索

            insertData(i,mCursor.getString(1),mCursor.getString(3));


            //查看是否有作者名
            String Artist = mCursor.getString(3);
            if(Artist == null){
                map.put("artist","无名氏");
                musicBean.setArtist("无名氏");
            }else{
                map.put("artist",mCursor.getString(3));
                musicBean.setArtist(mCursor.getString(3));
            }

            //查看是否有音乐名
            String MusicName = mCursor.getString(1);
            if(MusicName == null){
                map.put("name","未知");
                musicBean.setMusicName("未知");
            }else{
                map.put("name",mCursor.getString(1));
                musicBean.setMusicName(mCursor.getString(1));
            }

            //查看是否有音乐图片
            String MusicImage = getAlbumArt(mCursor.getInt(8));
            if (MusicImage == null){
                map.put("image",String.valueOf(R.drawable.default_record_album));
                musicBean.setAlbum_id(String.valueOf(R.drawable.default_record_album));
            }else{
                map.put("image",MusicImage);
                musicBean.setAlbum_id(MusicImage);
            }

            musicBeans.add(musicBean);
            list_map.add(map);
        }
        simpleAdapter = new SimpleAdapter(mActivity,list_map,R.layout.music_list_item ,
                new String[] {"image" , "name" , "artist"} ,
                new int[]{R.id.MusicImage , R.id.MusicName , R.id.MusicArtist});
        MusicListView.setAdapter(simpleAdapter);
        MusicListView.setOnItemClickListener(this);
    }

    //获取音乐封面
    private String getAlbumArt(int album_id){
        String UriAlbum = "content://media/external/audio/albums" ;
        String projecttion[] =  new String[] {"album_art"} ;
        Cursor cursor = contentResolver.query(Uri.parse(UriAlbum + File.separator +Integer.toString(album_id)) ,
                projecttion , null , null , null);
        String album = null ;
        if (cursor.getCount() > 0 && cursor.getColumnCount() > 0)
        {
            cursor.moveToNext() ;
            album = cursor.getString(0) ;
        }
        //关闭资源数据
        cursor.close();
        return album ;
    }

    public static List<MusicBean> getMusicBeans(){
        return musicBeans;
    }

    //item被点击时
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        Bundle bundle = new Bundle();
        bundle.putInt("position",position);
        bundle.putSerializable("musicBean",(Serializable) getMusicBeans());

        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setClass(mActivity, MusicPlayActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case STORGE_REQUEST :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //完成程序的初始化
                    init();
                    System.out.println("程序申请权限成功，完成初始化") ;
                }
                else {
                    System.out.println("程序没有获得相关权限，请处理");
                }
                break ;
        }

    }

    private void insertData(int position,String musicName,String musicArtist){
        if(!hasData(musicName,musicArtist)){
            db_search = searchSqliteHelper.getWritableDatabase();
            db_search.execSQL("insert into table_search(position,musicName,musicArtist) values ('"+ position+"','"+musicName+"','"+musicArtist+"')");
        }
    }
    //判断是否有该值
    private boolean hasData(String musicName,String musicArtist){
        Cursor cursor = searchSqliteHelper.getReadableDatabase().rawQuery("select id as _id,musicName from table_search where musicName =? and musicArtist =?",new String[]{musicName,musicArtist});
        return cursor.moveToNext();
    }
    public static SearchSqliteHelper getSearchSqliteHelper(){
        return searchSqliteHelper;
    }

    public void deleteSearchResultData(){
        db_search = searchSqliteHelper.getWritableDatabase();
        db_search.execSQL("delete from table_search");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(db_search != null){
            db_search.close();
        }
    }
}
