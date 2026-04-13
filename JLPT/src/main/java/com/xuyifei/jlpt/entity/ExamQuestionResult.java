package com.xuyifei.jlpt.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 考试题目得分明细实体类
 * 用于持久化存储某场考试中，各个板块（Section）或题群的具体答题统计结果
 */
@Data
public class ExamQuestionResult {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 外键：所属考试记录的 ID
     * 关联 Exam 实体类的 id 字段
     */
    private Long examId;

    /**
     * 板块类型
     * 例如：文字词汇 (Vocabulary)、语法 (Grammar)、阅读 (Reading)、听力 (Listening)
     */
    private String sectionType;

    /**
     * 题目代码
     * 用于标识具体的大题编号（如：问题1、问题2等）
     */
    private String questionCode;

    /**
     * 该题项中答对的题目数量
     */
    private Integer correctCount;

    /**
     * 每道题的分值（权重）
     * 用于根据正确数计算原始分
     */
    private Integer scorePerQuestion;

    /**
     * 原始得分
     * 计算公式通常为：correctCount * scorePerQuestion
     */
    private Integer rawScore;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

}
