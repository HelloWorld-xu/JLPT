package com.xuyifei.jlpt.entity;

import lombok.Data;

/**
 * 题目分值配置实体类
 * 用于定义考试中各板块、各题型的评分标准（权重配置）
 */
@Data
public class QuestionConfig {

    /**
     * 板块类型
     * 对应考试的大项，如：语言知识、阅读、听力
     */
    private String sectionType;

    /**
     * 题目代码
     * 对应具体的大题编号，例如：N1_Q1（N1级别问题1）
     */
    private String questionCode;

    /**
     * 每题分值
     * 在计算原始分（Raw Score）时，该题型下每答对一道题所占的分数权重
     */
    private Integer scorePerQuestion;

}
