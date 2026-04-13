package com.xuyifei.jlpt.entity;

import lombok.Data;

/**
 * 分数转换映射实体类
 * 用于存储原始分 (Raw Score) 与标准分 (Scaled Score) 之间的对应关系。
 * 由于 JLPT 并非直接按正确率给分，而是通过 IRT 算法折算，此表即为该折算逻辑的持久化实现。
 */
@Data
public class ScoreConversion {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 考试等级
     * 例如：N1, N2, N3 等，不同等级的折算标准不同
     */
    private String level;

    /**
     * 板块类型
     * 例如：语言知识 (Language Knowledge)、阅读 (Reading)、听力 (Listening)
     */
    private String sectionType;

    /**
     * 原始分 (区间值)
     * 用户根据题目分值累加得到的初始分数
     */
    private Integer rawScore;

    /**
     * 标准分 (转换后分数)
     * 根据官方标准折算后的得分，通常每个板块满分为 60 分
     */
    private Integer scaledScore;

}