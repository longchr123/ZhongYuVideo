package com.example.administrator.day0121.utils;

import com.example.administrator.day0121.javaBean.Banners;
import com.example.administrator.day0121.javaBean.NewsItem;
import com.example.administrator.day0121.javaBean.Record;
import com.example.administrator.day0121.javaBean.Subject;
import com.example.administrator.day0121.javaBean.SubjectItem;
import com.example.administrator.day0121.javaBean.SubjectList;
import com.example.administrator.day0121.javaBean.SubjectType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/10.
 */
public class ParseJson {
    //首页轮播图
    public static List<Banners> bannersList(String json) {
        List<Banners> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject jsonObject1 = jsonObject.getJSONObject("result");
            JSONArray jsonArray = jsonObject1.getJSONArray("banners");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                String contentURL = jsonObject2.getString("contentURL");
                String imageURL = jsonObject2.getString("imageURL");
                String title = jsonObject2.getString("title");
                Banners banners = new Banners();
                banners.setContentURL(contentURL);
                banners.setImageURL(imageURL);
                banners.setTitle(title);
                list.add(banners);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    //首页课程分类
    public static List<Subject> subjectList(String json) {
        List<Subject> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject jsonObject1 = jsonObject.getJSONObject("result");
            JSONArray jsonArray = jsonObject1.getJSONArray("courseCategory");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                String imagesURL = jsonObject2.getString("imagesURL");
                String keyword = jsonObject2.getString("keyword");
                String title = jsonObject2.getString("title");
                Subject subject = new Subject();
                subject.setImagesURL(imagesURL);
                subject.setKeyword(keyword);
                subject.setTitle(title);
                list.add(subject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    //首页最新视频
    public static List<Record> recordList(String json) {
        List<Record> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject jsonObject1 = jsonObject.getJSONObject("result");
            JSONArray jsonArray = jsonObject1.getJSONArray("studyRecord");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                String date = jsonObject2.getString("date");
                String dutation = jsonObject2.getString("dutation");
                String thumbnail = jsonObject2.getString("thumbnail");
                String videoId = jsonObject2.getString("videoId");
                String title = jsonObject2.getString("title");
                Record record = new Record();
                record.setDate(date);
                record.setDutation(dutation);
                record.setThumbnail(thumbnail);
                record.setVideoId(videoId);
                record.setTitle(title);
                list.add(record);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    //最新资讯列表
    public static List<NewsItem> parseJsonNews(String json) {
        List<NewsItem> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject jsonObject1 = jsonObject.getJSONObject("result");
            JSONArray jsonArray = jsonObject1.getJSONArray("contents");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                String title = jsonObject2.getString("title");
                String posttime = jsonObject2.getString("posttime");
                String description = jsonObject2.getString("description");
                String url = jsonObject2.getString("url");
                NewsItem newsItem = new NewsItem();
                newsItem.setTitle(title);
                newsItem.setPosttime(posttime);
                newsItem.setDescription(description);
                newsItem.setUrl(url);
                list.add(newsItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    //课程分类
    public static List<SubjectType> subjectTypeList(String json) {
        List<SubjectType> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject jsonObject1 = jsonObject.optJSONObject("result");
            JSONArray jsonArray = jsonObject1.optJSONArray("content");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                SubjectType subjectType = new SubjectType();
                SubjectList subjectList = new SubjectList();
                String content = jsonObject2.optString("title");
                String subjectId = jsonObject2.optString("subjectId");
                List<SubjectItem> itemList = new ArrayList<>();
                JSONArray jsonArray1 = jsonObject2.optJSONArray("videoInfos");
                if (jsonArray1 != null) {
                    for (int j = 0; j < jsonArray1.length(); j++) {
                        JSONObject jsonObject3 = jsonArray1.getJSONObject(j);
                        String aid = jsonObject3.optString("aid");
                        String fid = jsonObject3.optString("fid");
                        String title = jsonObject3.optString("title");
                        String picurl = jsonObject3.optString("picurl");
                        String videoid = jsonObject3.optString("videoid");
                        String duration = jsonObject3.optString("duration");
                        String videosize = jsonObject3.optString("videosize");
                        String releatedurl = jsonObject3.optString("releatedurl");
                        SubjectItem subjectItem = new SubjectItem();
                        subjectItem.setAid(aid);
                        subjectItem.setFid(fid);
                        subjectItem.setTitle(title);
                        subjectItem.setPicurl(picurl);
                        subjectItem.setVideoid(videoid);
                        subjectItem.setDuration(duration);
                        subjectItem.setVideosize(videosize);
                        subjectItem.setReleatedurl(releatedurl);
                        itemList.add(subjectItem);
                    }
                }
                subjectList.setList(itemList);
                subjectType.setContent(content);
                subjectType.setSubjectId(subjectId);
                subjectType.setSubjectList(subjectList);
                if (jsonArray1 != null) {
                    list.add(subjectType);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
