package com.xuyifei.jlpt.dto;

import lombok.Data;

@Data
public class CreateExamDTO {

    private String level;
    private Integer year;
    private Integer month;

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
}