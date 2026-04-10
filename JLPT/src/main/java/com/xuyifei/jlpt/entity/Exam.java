package com.xuyifei.jlpt.entity;

import java.time.LocalDateTime;

public class Exam {

    private Long id;
    private String level;
    private Integer year;
    private Integer month;
    private String SectionType;

    private Integer StandardScore;

    public Integer getStandardScore() {
        return StandardScore;
    }

    public void setStandardScore(Integer standardScore) {
        StandardScore = standardScore;
    }

    public String getSectionType() {
        return SectionType;
    }

    public void setSectionType(String sectionType) {
        SectionType = sectionType;
    }

    // ===== 原始分 =====
    private Integer languageRawScore;
    private Integer readingRawScore;
    private Integer listeningRawScore;
    private Integer RawScore;

    public Integer getRawScore() {
        return RawScore;
    }

    public void setRawScore(Integer rawScore) {
        RawScore = rawScore;
    }

    // ===== 标准分 =====
    private Integer languageScaledScore;
    private Integer readingScaledScore;
    private Integer listeningScaledScore;

    // ===== 汇总 =====
    private Integer totalScaledScore;
    private Boolean pass;

    private LocalDateTime createTime;

    // ===== Getter / Setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getLanguageRawScore() {
        return languageRawScore;
    }

    public void setLanguageRawScore(Integer languageRawScore) {
        this.languageRawScore = languageRawScore;
    }

    public Integer getReadingRawScore() {
        return readingRawScore;
    }

    public void setReadingRawScore(Integer readingRawScore) {
        this.readingRawScore = readingRawScore;
    }

    public Integer getListeningRawScore() {
        return listeningRawScore;
    }

    public void setListeningRawScore(Integer listeningRawScore) {
        this.listeningRawScore = listeningRawScore;
    }

    public Integer getLanguageScaledScore() {
        return languageScaledScore;
    }

    public void setLanguageScaledScore(Integer languageScaledScore) {
        this.languageScaledScore = languageScaledScore;
    }

    public Integer getReadingScaledScore() {
        return readingScaledScore;
    }

    public void setReadingScaledScore(Integer readingScaledScore) {
        this.readingScaledScore = readingScaledScore;
    }

    public Integer getListeningScaledScore() {
        return listeningScaledScore;
    }

    public void setListeningScaledScore(Integer listeningScaledScore) {
        this.listeningScaledScore = listeningScaledScore;
    }

    public Integer getTotalScaledScore() {
        return totalScaledScore;
    }

    public void setTotalScaledScore(Integer totalScaledScore) {
        this.totalScaledScore = totalScaledScore;
    }

    public Boolean getPass() {
        return pass;
    }

    public void setPass(Boolean pass) {
        this.pass = pass;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}