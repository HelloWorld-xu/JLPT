package com.xuyifei.jlpt.entity;

import lombok.Data;

/**
 * 题目分值配置实体类
 * 用于定义不同 JLPT 等级中各板块、各题型的评分标准
 */
@Data
public class QuestionConfig {

    /**
     * 考试等级，例如 N1、N2、N3
     */
    private String level;

    /**
     * 板块类型：language、reading、listening
     */
    private String sectionType;

    /**
     * 题目代码，例如 L1、R1、LS1
     */
    private String questionCode;

    /**
     * 当前题型包含的题目数量
     */
    private Integer questionCount;

    /**
     * 每题原始分值
     */
    private Integer scorePerQuestion;

    /**
     * 当前题型在所属板块中的显示顺序
     */
    private Integer sectionOrder;
}
