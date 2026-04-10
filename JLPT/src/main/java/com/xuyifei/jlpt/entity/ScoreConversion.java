package com.xuyifei.jlpt.entity;

import lombok.Data;

@Data
public class ScoreConversion {

    private Long id;
    private String level;
    private String sectionType;
    private Integer rawScore;
    private Integer scaledScore;

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

    public String getSectionType() {
        return sectionType;
    }

    public void setSectionType(String sectionType) {
        this.sectionType = sectionType;
    }

    public Integer getRawScore() {
        return rawScore;
    }

    public void setRawScore(Integer rawScore) {
        this.rawScore = rawScore;
    }

    public Integer getScaledScore() {
        return scaledScore;
    }

    public void setScaledScore(Integer scaledScore) {
        this.scaledScore = scaledScore;
    }
}