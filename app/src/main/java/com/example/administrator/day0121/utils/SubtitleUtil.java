package com.example.administrator.day0121.utils;

import android.util.Log;

import org.apache.http.params.CoreConnectionPNames;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/1/25.
 */
public class SubtitleUtil {

    private final String REG = "\\d+\\r\\n(\\d{2}:\\d{2}:\\d{2},\\d{3}) --> (\\d{2}:\\d{2}:\\d{2},\\d{3})\\r\\n(.*?)\\r\\n\\r\\n";

    private int start;
    private int end;
    private String content;

    private List<SubtitleUtil> subtitles;

    /**
     * 字幕初始化监听器
     */
    public interface OnSubtitleInitedListener {

        public void onInited(SubtitleUtil subtitle);
    }

    private OnSubtitleInitedListener onSubtitleInitedListener;

    private SubtitleUtil() {
    }

    public SubtitleUtil(OnSubtitleInitedListener onSubtitleInitedListener) {
        this.onSubtitleInitedListener = onSubtitleInitedListener;
        this.subtitles = new ArrayList<SubtitleUtil>();
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setResource() {

    }

    public void initSubtitleResource(final String url) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    URL localURL = new URL("http://localhost:8080/OneHttpServer/");
                    URLConnection connection = localURL.openConnection();
                    HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

                    httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
                    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    InputStream inputStream = null;
                    InputStreamReader inputStreamReader = null;
                    BufferedReader reader = null;
                    StringBuffer resultBuffer = new StringBuffer();
                    String tempLine = null;

                    if (httpURLConnection.getResponseCode() >= 300) {
                        throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
                    }

                    try {
                        inputStream = httpURLConnection.getInputStream();
                        inputStreamReader = new InputStreamReader(inputStream);
                        reader = new BufferedReader(inputStreamReader);

                        while ((tempLine = reader.readLine()) != null) {
                            resultBuffer.append(tempLine);
                        }

                    } finally {

                        if (reader != null) {
                            reader.close();
                        }

                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }

                        if (inputStream != null) {
                            inputStream.close();
                        }

                    }

                    parseSubtitleStr(resultBuffer.toString());
                } catch (Exception e) {
                    Log.e("CCVideoViewDemo", "" + e.getMessage());
                }
            }
        }).start();
    }

    public String getSubtitleByTime(long time) {
        for (SubtitleUtil subtitle : subtitles) {
            if (subtitle.getStart() <= time && time <= subtitle.getEnd()) {
                return subtitle.getContent();
            }
        }
        return "";
    }

    private void parseSubtitleStr(String results) {
        Pattern pattern = Pattern.compile(REG);
        Matcher matcher = pattern.matcher(results);
        while (matcher.find()) {
            SubtitleUtil subtitle = new SubtitleUtil();
            subtitle.setStart(parseTime(matcher.group(1)));
            subtitle.setEnd(parseTime(matcher.group(2)));
            subtitle.setContent(matcher.group(3));
            subtitles.add(subtitle);
        }

        onSubtitleInitedListener.onInited(this);
    }

    private int parseTime(String timeStr) {
        int nReturn = 0;
        String[] times = timeStr.split(",");
        int nMs = Integer.parseInt(times[1]);
        String[] time = times[0].split(":");
        int nH = Integer.parseInt(time[0]);
        int nM = Integer.parseInt(time[1]);
        int nS = Integer.parseInt(time[2]);
        nReturn += nS * 1000;
        nReturn += nM * 60 * 1000;
        nReturn += nH * 60 * 60 * 1000;
        nReturn += nMs;
        return nReturn;
    }
}

