package com.xuyifei.jlpt.vo;

import lombok.Data;

/**
 * 考试结果视图对象 (VO)
 * 用于在考试完成后，向前端展示最终的分数报告和合格状态
 * 这里的字段通常直接对应 UI 界面上显示的各项成绩数值
 */
@Data
public class ExamResultVO {

    /**
     * 语言知识板块最终得分 (标准分)
     * 通常范围：0-60
     */
    private Integer languageScore;

    /**
     * 阅读板块最终得分 (标准分)
     * 通常范围：0-60
     */
    private Integer readingScore;

    /**
     * 听力板块最终得分 (标准分)
     * 通常范围：0-60
     */
    private Integer listeningScore;

    /**
     * 总得分
     * 以上三项分数的总和，满分 180
     */
    private Integer totalScore;

    /**
     * 合格判定状态
     * true: 合格 (Pass) / false: 不合格 (Fail)
     */
    private Boolean pass;

    /**
     * 无参构造函数
     * 虽然 @Data 会处理，但显式保留以确保 JSON 反序列化时的兼容性
     */
    public ExamResultVO() {
    }
}