package com.xuyifei.jlpt.entity;

import lombok.Data;

@Data
public class QuestionConfig {

    private String sectionType;
    private String questionCode;
    private Integer scorePerQuestion;
}
