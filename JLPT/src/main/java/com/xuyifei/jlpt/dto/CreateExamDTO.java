package com.xuyifei.jlpt.dto;

import lombok.Data;

/**
 * 创建考试时的请求参数封装对象 (Data Transfer Object)
 * 用于接收前端传入的考试基本信息，如等级、年份和月份
 */
@Data
public class CreateExamDTO {

    /**
     * 考试等级
     * 例如：N1, N2, N3, N4, N5
     */
    private String level;

    /**
     * 考试年份
     * 例如：2023, 2024
     */
    private Integer year;

    /**
     * 考试月份
     * 通常为 7月或 12月
     */
    private Integer month;

}