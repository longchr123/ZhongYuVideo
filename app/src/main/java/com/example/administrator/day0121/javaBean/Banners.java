package com.example.administrator.day0121.javaBean;

/**
 * Created by Administrator on 2016/3/10.
 */
public class Banners {
    private String contentURL;
    private String imageURL;
    private String title;

    public Banners() {

    }

    public String getContentURL() {
        return contentURL;
    }

    public void setContentURL(String contentURL) {
        this.contentURL = contentURL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Banners{" +
                "contentURL='" + contentURL + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
