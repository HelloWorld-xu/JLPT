package com.xuyifei.jlpt.vo;

import lombok.Data;

@Data
public class ExamResultVO {

    private Integer languageScore;
    private Integer readingScore;
    private Integer listeningScore;
    private Integer totalScore;
    private Boolean pass;

    public ExamResultVO() {
    }

    public ExamResultVO(Integer languageScore, Integer readingScore, Integer listeningScore, Integer totalScore, Boolean pass) {
        this.languageScore = languageScore;
        this.readingScore = readingScore;
        this.listeningScore = listeningScore;
        this.totalScore = totalScore;
        this.pass = pass;
    }

    public Integer getLanguageScore() {
        return languageScore;
    }

    public void setLanguageScore(Integer languageScore) {
        this.languageScore = languageScore;
    }

    public Integer getReadingScore() {
        return readingScore;
    }

    public void setReadingScore(Integer readingScore) {
        this.readingScore = readingScore;
    }

    public Integer getListeningScore() {
        return listeningScore;
    }

    public void setListeningScore(Integer listeningScore) {
        this.listeningScore = listeningScore;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Boolean getPass() {
        return pass;
    }

    public void setPass(Boolean pass) {
        this.pass = pass;
    }
}