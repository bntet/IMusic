package com.example.imusic.bean;

public class SearchResultBean {
    private int id;         //搜索结果个数
    private int position;   //搜索结果在音乐列表的位置
    private String musicName;//音乐名字
    private String musicArtist;//音乐作者

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicArtist() {
        return musicArtist;
    }

    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist;
    }
}
