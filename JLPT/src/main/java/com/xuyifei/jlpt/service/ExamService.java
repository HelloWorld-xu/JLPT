package com.xuyifei.jlpt.service;

import com.xuyifei.jlpt.dto.CreateExamDTO;
import com.xuyifei.jlpt.dto.QuestionAnswerDTO;
import com.xuyifei.jlpt.entity.Exam;
import com.xuyifei.jlpt.entity.ExamQuestionResult;
import com.xuyifei.jlpt.entity.QuestionConfig;

import java.util.List;

/**
 * 考试业务服务接口
 */
public interface ExamService {

    /**
     * 初始化一场新考试
     */
    Long createExam(CreateExamDTO dto);

    /**
     * 根据考试等级获取题型配置，供前端动态生成答题项目
     */
    List<QuestionConfig> getQuestionConfigs(String level);

    /**
     * 更新或插入答题记录
     */
    void upsertAnswers(Long examId, List<QuestionAnswerDTO> answers);

    /**
     * 根据考试 ID 获取已保存的答题明细
     */
    List<ExamQuestionResult> getAnswerDetails(Long examId);

    /**
     * 删除考试记录及其答题明细
     */
    void deleteExam(Long examId);

    /**
     * 根据 ID 获取考试详细信息
     */
    Exam getById(Long id);

    /**
     * 获取所有考试记录
     */
    List<Exam> getAll();
}
