package com.example.administrator.day0121.javaBean;

/**
 * Created by Administrator on 2016/3/10.
 */
public class Subject {
    private String imagesURL;
    private String keyword;
    private String title;

    public Subject() {

    }

    public String getImagesURL() {
        return imagesURL;
    }

    public void setImagesURL(String imagesURL) {
        this.imagesURL = imagesURL;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "imagesURL='" + imagesURL + '\'' +
                ", keyword='" + keyword + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
