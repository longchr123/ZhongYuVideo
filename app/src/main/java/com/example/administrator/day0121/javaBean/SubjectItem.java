package com.example.administrator.day0121.javaBean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/3/27.
 */
public class SubjectItem implements Serializable {
    private String aid;
    private String fid;
    private String title;
    private String picurl;
    private String videoid;
    private String duration;
    private String videosize;
    private String releatedurl;

    public SubjectItem() {

    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicurl() {
        return picurl;
    }

    public void setPicurl(String picurl) {
        this.picurl = picurl;
    }

    public String getVideoid() {
        return videoid;
    }

    public void setVideoid(String videoid) {
        this.videoid = videoid;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getVideosize() {
        return videosize;
    }

    public void setVideosize(String videosize) {
        this.videosize = videosize;
    }

    public String getReleatedurl() {
        return releatedurl;
    }

    public void setReleatedurl(String releatedurl) {
        this.releatedurl = releatedurl;
    }
}
