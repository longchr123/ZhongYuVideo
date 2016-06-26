package com.example.administrator.day0121.javaBean;

/**
 * Created by Administrator on 2016/3/10.
 */
public class Record {
    private String date;
    private String dutation;
    private String thumbnail;
    private String videoId;
    private String title;

    public Record() {

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDutation() {
        return dutation;
    }

    public void setDutation(String dutation) {
        this.dutation = dutation;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Record{" +
                "date='" + date + '\'' +
                ", dutation='" + dutation + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", videoId='" + videoId + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
