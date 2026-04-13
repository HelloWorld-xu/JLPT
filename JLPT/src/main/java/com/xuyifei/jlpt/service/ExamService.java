package com.xuyifei.jlpt.service;

import com.xuyifei.jlpt.dto.CreateExamDTO;
import com.xuyifei.jlpt.dto.QuestionAnswerDTO;
import com.xuyifei.jlpt.entity.Exam;

import java.util.List;

/**
 * 考试业务服务接口
 * 定义了针对 JLPT 考试的核心业务操作标准
 */
public interface ExamService {


    /**
     * 初始化一场新考试
     * 根据前端传入的等级、年份等信息，在数据库中预建一条考试主记录
     * @param dto 包含考试基本信息的传输对象
     * @return 成功创建后的考试记录唯一 ID
     */
    Long createExam(CreateExamDTO dto);

    /**
     * 更新或插入答题记录 (Upsert)
     * 用于实时保存用户的答题进度。如果该题目已存在则覆盖原答案，不存在则新增。
     * @param examId 考试主记录 ID
     * @param answers 用户提交的一组或多组题目答案数据
     */
    void upsertAnswers(Long examId, List<QuestionAnswerDTO> answers);

    /**
     * 删除考试记录
     * 执行物理删除，通常需要级联删除该考试关联的所有题目得分明细
     * @param examId 待删除的考试 ID
     */
    void deleteExam(Long examId);

    /**
     * 根据 ID 获取考试详细信息
     * 用于展示单场考试的状态、基础信息及最终得分（如果已计算）
     * @param id 考试 ID
     * @return 考试实体对象
     */
    Exam getById(Long id);

    /**
     * 获取所有考试记录
     * 用于前端展示用户的“考试历史清单”
     * @return 包含所有考试记录的列表
     */
    List<Exam> getAll();
}
