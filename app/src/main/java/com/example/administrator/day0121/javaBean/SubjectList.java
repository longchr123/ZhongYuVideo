package com.example.administrator.day0121.javaBean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/3/27.
 */
public class SubjectList implements Serializable {
    private List<SubjectItem> list;

    public SubjectList() {
    }

    public List<SubjectItem> getList() {
        return list;
    }

    public void setList(List<SubjectItem> list) {
        this.list = list;
    }
}
