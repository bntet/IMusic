package com.example.imusic.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imusic.R;
import com.example.imusic.bean.SearchHistoryBean;
import com.example.imusic.bean.SearchResultBean;
import com.example.imusic.fragment.MusicLibFragment;
import com.example.imusic.sqlite.RecordsSqliteHelper;
import com.example.imusic.sqlite.SearchSqliteHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.media.CamcorderProfile.get;
import static com.example.imusic.fragment.MusicLibFragment.getMusicBeans;

public class SearchActivity extends BaseActivity {
    private TextView tv_back,tv_delete_history,tv_history,tv_search;
    private ImageView iv_clear;
    private EditText edt_search;
    private ListView lv_search_history,lv_search_result;

    private SimpleCursorAdapter recordsAdapter;
    private SimpleCursorAdapter searchAdapter;
    private RecordsSqliteHelper recordsSqliteHelper;
    private SearchSqliteHelper searchSqliteHelper;
    private SQLiteDatabase db_records,db_search;
    private Cursor cursor;
    private SearchResultBean searchResultBean;          //搜索结果
    private List<SearchResultBean> searchResultBeans;   //搜索结果集合
    private SearchHistoryBean searchHistoryBean;
    private List<SearchHistoryBean> searchHistoryBeans;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        initView();
        initData();
        initListener();
    }

    private void initData() {
        searchResultBeans = new ArrayList<>();
        searchHistoryBeans = new ArrayList<>();
        recordsSqliteHelper = new RecordsSqliteHelper(this);
        searchSqliteHelper = new SearchSqliteHelper(this);
        setListViewGone();
        cursor = recordsSqliteHelper.getReadableDatabase().rawQuery("select id as _id,name from table_records order by id desc",null);
        clearSearchHistoryBeans();
        setSearchHistory(cursor);
        recordsAdapter = new SimpleCursorAdapter(this,R.layout.search_history_item,cursor,new String[]{"name"},new int[]{R.id.tv_music_item}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        lv_search_history.setVisibility(View.VISIBLE);
        lv_search_history.setAdapter(recordsAdapter);
    }

    private void initListener() {
        //返回
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchActivity.this.finish();
            }
        });

        //清除文本框文本
        iv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edt_search.setText("");
            }
        });

        //清除历史记录
        tv_delete_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteData();
            }
        });

        //文本框变化监听
        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int x, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                clearSearchResultBeans();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(edt_search.getText().toString().equals("")){
                    setListViewGone();
                    tv_history.setText("搜索历史");
                    tv_delete_history.setVisibility(View.VISIBLE);
                    lv_search_history.setVisibility(View.VISIBLE);
                    cursor = recordsSqliteHelper.getReadableDatabase().rawQuery("select id as _id,name from table_records order by id desc",null);
                    clearSearchHistoryBeans();
                    setSearchHistory(cursor);
                    refreshRecordsListView(cursor);
                }else{
                    setListViewGone();
                    tv_history.setText("搜索结果");
                    tv_delete_history.setVisibility(View.GONE);
                    lv_search_result.setVisibility(View.VISIBLE);
                    String searchString = edt_search.getText().toString().trim();

                    querySearchData(searchString);
                }
            }
        });
        //软键盘回车 改为 搜索
        edt_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                    //隐藏键盘
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    insertData(edt_search.getText().toString().trim());
                }
                return false;
            }
        });

        //搜索 点击
        tv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //隐藏键盘
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                insertData(edt_search.getText().toString().trim());
            }
        });

        //搜索历史 item点击
        lv_search_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                System.out.println("history position is "+ position +" name is "+ searchHistoryBeans.get(position).getName());
                edt_search.setText(searchHistoryBeans.get(position).getName());
            }
        });

        //搜索结果 item点击
        lv_search_result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //db_search.rawQuery("select id as _id,musicName,musicArtist from table_search where musicName like '%" + queryName + "%' or musicArtist like '%" + queryName + "%'",null);

                int musicPosition = searchResultBeans.get(position).getPosition();
                Bundle bundle = new Bundle();
                bundle.putInt("position",musicPosition);
                bundle.putSerializable("musicBean",(Serializable) getMusicBeans());

                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(SearchActivity.this, MusicPlayActivity.class);
                startActivity(intent);
                System.out.println("the position is "+position);
            }
        });
    }

    private void initView() {
        edt_search = findViewById(R.id.edt_search);
        tv_back = findViewById(R.id.tv_search_back);
        tv_search = findViewById(R.id.tv_search);
        tv_delete_history = findViewById(R.id.tv_delete_history);
        tv_history = findViewById(R.id.tv_tip);
        iv_clear = findViewById(R.id.iv_clear);
        lv_search_history = findViewById(R.id.ml_search_history);
        lv_search_result = findViewById(R.id.ml_search_result);
    }

    //保存 历史数据
    private void insertData(String record){
        if(!hasData(record)){
            db_records =recordsSqliteHelper.getWritableDatabase();
            db_records.execSQL("insert into table_records(name) values('"+ record+"')");
        }
    }

    //判断是否有该值
    private boolean hasData(String hasName){
        Cursor cursor = recordsSqliteHelper.getReadableDatabase().rawQuery("select id as _id,name from table_records where name =?",new String[]{hasName});
        return cursor.moveToNext();
    }

    //查询数据
    private void querySearchData(String queryName){
        db_search = searchSqliteHelper.getReadableDatabase();
        cursor = db_search.rawQuery("select id as _id,position,musicName,musicArtist from table_search where musicName like '%" + queryName + "%' or musicArtist like '%" + queryName + "%'",null);
        if(cursor != null && cursor.moveToFirst() && cursor.getCount()>0){
            int i2 =1;
            do{
                searchResultBean = new SearchResultBean();
                searchResultBean.setId(i2-1);
                searchResultBean.setPosition(cursor.getInt(cursor.getColumnIndex("position")));
                searchResultBean.setMusicName(cursor.getString(cursor.getColumnIndex("musicName")));
                searchResultBean.setMusicArtist(cursor.getString(cursor.getColumnIndex("musicArtist")));
                searchResultBeans.add(searchResultBean);
                i2++;
            }while (cursor.moveToNext() && i2<=cursor.getCount());
        }
        refreshSearchListView(cursor);
    }

    //刷新    listView
    private void refreshRecordsListView(Cursor cursor){

        recordsAdapter.notifyDataSetChanged();
        recordsAdapter.swapCursor(cursor);
        lv_search_history.setAdapter(recordsAdapter);
    }

    private void refreshSearchListView(Cursor cursor){
        searchAdapter = new SimpleCursorAdapter(this,R.layout.search_result_item,cursor,new String[]{"musicName","musicArtist"},new int[]{R.id.tv_music_item,R.id.tv_musicArtist_item},CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        lv_search_result.setAdapter(searchAdapter);
    }
    //删除数据
    private void deleteData(){
        db_records =recordsSqliteHelper.getWritableDatabase();
        db_records.execSQL("delete from table_records");
        cursor = recordsSqliteHelper.getReadableDatabase().rawQuery("select id as _id,name from table_records",null);
        if (edt_search.getText().toString().trim().equals("")){
            refreshRecordsListView(cursor);
        }
    }
    //设置listview都为gone
    private void setListViewGone(){
        lv_search_result.setVisibility(View.GONE);
        lv_search_history.setVisibility(View.GONE);
    }

    private void setSearchHistory(Cursor cursor){
        if(cursor != null && cursor.moveToFirst() && cursor.getCount()>0){
            int i =1;
            do{
                searchHistoryBean = new SearchHistoryBean();
                searchHistoryBean.setId(i);
                searchHistoryBean.setName(cursor.getString(cursor.getColumnIndex("name")));
                searchHistoryBeans.add(searchHistoryBean);
                i++;
            }while (cursor.moveToNext() && i<=cursor.getCount());
        }
    }

    private void clearSearchResultBeans(){
        searchResultBeans.clear();
    }
    private void clearSearchHistoryBeans(){
        searchHistoryBeans.clear();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db_records != null) {
            db_records.close();
        }
        if (db_search != null){
            db_search.close();
        }
        if(cursor != null){
            cursor.close();
        }
    }
}
