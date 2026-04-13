package com.xuyifei.jlpt.entity;

import java.time.LocalDateTime;
import lombok.Data;
/**
 * 考试记录实体类
 * 映射数据库中的考试表，存储单次考试的所有核心数据、各板块得分及最终判定结果
 */
@Data
public class Exam {

    // ===== 基础信息 =====
    private Long id;              // 唯一标识 ID
    private String level;         // 考试等级 (N1-N3)
    private Integer year;         // 考试年份
    private Integer month;        // 考试月份
    private String SectionType;   // 当前处理的板块类型
    private Integer StandardScore;// 基准分/标准参考分

    // ===== 原始分 (Raw Score) =====
    // 指的是用户直接答对的题目数量或简单累加分
    private Integer languageRawScore; // 语言知识板块原始分
    private Integer readingRawScore;  // 阅读板块原始分
    private Integer listeningRawScore;// 听力板块原始分
    private Integer RawScore;         // 总原始分

    // ===== 标准分 (Scaled Score) =====
    // JLPT 官方采用项目反应理论 (IRT) 计算的分数，这里对应转换后的分值 (每项 0-60)
    private Integer languageScaledScore; // 语言知识板块标准分
    private Integer readingScaledScore;  // 阅读板块标准分
    private Integer listeningScaledScore;// 听力板块标准分

    // ===== 判定与汇总 =====
    private Integer totalScaledScore;// 总标准分 (0-180)
    private Boolean pass;            // 是否合格 (合格判定结果)
    private LocalDateTime createTime;// 记录创建时间/考试时间

}