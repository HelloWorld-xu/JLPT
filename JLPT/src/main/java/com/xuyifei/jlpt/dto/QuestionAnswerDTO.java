package com.xuyifei.jlpt.dto;

import lombok.Data;

/**
 * 题目答案统计传输对象 (DTO)
 * 用于封装用户在考试中某个具体题型或板块的得分/答题情况
 */
@Data
public class QuestionAnswerDTO {

    /**
     * 考试板块类型
     * 例如：语言知识（文字·词汇）、语言知识（语法）·阅读、听力
     * 通常对应 JLPT 的大题分类
     */
    private String sectionType;

    /**
     * 题目代码或编号
     * 用于唯一标识某道题目或某个题群的业务编码
     */
    private String questionCode;

    /**
     * 正确数量
     * 记录该题目或该题群中，用户回答正确的题目总数
     */
    private Integer correctCount;

}
