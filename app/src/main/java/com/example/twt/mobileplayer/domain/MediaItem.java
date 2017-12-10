package com.example.twt.mobileplayer.domain;

import java.io.Serializable;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class MediaItem implements Serializable{
    private String display_name;//视频文件在sdcard的名称
    private long duration;//视频总时长
    private long size;//视频的文件大小
    private String data;//视频的绝对地址
    private String artist;//歌曲的演唱者
    private String imgUrl;
    private String summary;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MediaItem{" +
                "imgUrl='" + imgUrl + '\'' +
                ", summary='" + summary + '\'' +
                ", display_name='" + display_name + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
