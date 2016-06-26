package com.example.administrator.day0121.javaBean;

import java.util.List;

/**
 * Created by Administrator on 2016/3/27.
 */
public class SubjectType {
    private String content;
    private String subjectId;
    private SubjectList subjectList;

    public SubjectType() {
    }

    public SubjectList getSubjectList() {
        return subjectList;
    }

    public void setSubjectList(SubjectList subjectList) {
        this.subjectList = subjectList;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

}
