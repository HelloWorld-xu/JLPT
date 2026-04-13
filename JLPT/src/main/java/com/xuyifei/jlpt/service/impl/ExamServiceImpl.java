package com.xuyifei.jlpt.service.impl;

import com.xuyifei.jlpt.dto.CreateExamDTO;
import com.xuyifei.jlpt.dto.QuestionAnswerDTO;
import com.xuyifei.jlpt.entity.Exam;
import com.xuyifei.jlpt.entity.ExamQuestionResult;
import com.xuyifei.jlpt.entity.QuestionConfig;
import com.xuyifei.jlpt.mapper.ExamMapper;
import com.xuyifei.jlpt.mapper.ExamQuestionResultMapper;
import com.xuyifei.jlpt.mapper.QuestionConfigMapper;
import com.xuyifei.jlpt.service.ExamService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考试服务实现类
 * 负责考试流程控制、答题数据更新及配置逻辑匹配
 */
@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private final ExamMapper examMapper;

    @Autowired
    private ExamQuestionResultMapper resultMapper;

    @Autowired
    private QuestionConfigMapper configMapper;

    /**
     * 构造函数注入
     */
    public ExamServiceImpl(
            ExamMapper examMapper,
            ExamQuestionResultMapper resultMapper) {
        this.examMapper = examMapper;
        this.resultMapper = resultMapper;
    }

    /**
     * 提交或更新答案 (Upsert 逻辑)
     * 使用 @Transactional 保证数据一致性：如果中间报错，所有数据库操作回滚
     * @param examId 考试 ID
     * @param answers 前端传来的答案列表
     */
    @Transactional
    @Override
    public void upsertAnswers(Long examId, List<QuestionAnswerDTO> answers) {

        if (answers == null || answers.isEmpty()) {
            throw new RuntimeException("答案不能为空");
        }

        // 1. 获取所有题目分值配置，并转为 Map 以便通过 questionCode 快速查找
        List<QuestionConfig> configs = configMapper.selectAll();
        Map<String, QuestionConfig> configMap =
                configs.stream().collect(Collectors.toMap(
                        QuestionConfig::getQuestionCode,
                        c -> c
                ));

        List<ExamQuestionResult> insertList = new ArrayList<>();

        // 2. 遍历前端提交的每一个答案项
        for (QuestionAnswerDTO dto : answers) {

            QuestionConfig config =
                    configMap.get(dto.getQuestionCode());

            if (config == null) continue;

            // 3. 检查数据库中是否已存在该题目的答题记录
            ExamQuestionResult existing =
                    resultMapper.selectByExamIdAndQuestionCode(
                            examId,
                            dto.getQuestionCode()
                    );

            if (existing == null) {

                // ===== 情况 A：新增记录 =====
                ExamQuestionResult r = buildResult(examId, dto, config);
                insertList.add(r);

            } else {

                // ===== 情况 B：更新记录 =====
                existing.setCorrectCount(dto.getCorrectCount());
                existing.setScorePerQuestion(config.getScorePerQuestion());
                existing.setSectionType(config.getSectionType());
                // 重新计算该项原始分：正确数 * 每题分值
                existing.setRawScore(
                        dto.getCorrectCount() * config.getScorePerQuestion()
                );

                resultMapper.update(existing);
            }
        }

        // 4. 批量插入新记录，优化性能
        if (!insertList.isEmpty()) {
            resultMapper.batchInsert(insertList);
        }
    }

    /**
     * 创建并初始化一场新考试
     * @param dto 包含等级、年、月
     * @return 数据库生成的考试 ID
     */
    @Override
    public Long createExam(CreateExamDTO dto) {

        Exam exam = new Exam();
        exam.setLevel(dto.getLevel());
        exam.setYear(dto.getYear());
        exam.setMonth(dto.getMonth());

        // 初始化所有分数相关字段为 0
        exam.setLanguageRawScore(0);
        exam.setReadingRawScore(0);
        exam.setListeningRawScore(0);
        exam.setLanguageScaledScore(0);
        exam.setReadingScaledScore(0);
        exam.setListeningScaledScore(0);
        exam.setTotalScaledScore(0);
        exam.setPass(false);

        examMapper.insert(exam); // 执行插入，MyBatis 会回填 ID

        return exam.getId();
    }

    /**
     * 删除考试及其关联的所有答题明细
     * @param examId 考试 ID
     */
    @Override
    public void deleteExam(Long examId) {

        // 先删除从表（明细），防止外键约束或留存脏数据
        resultMapper.deleteByExamId(examId);

        // 再删除主表记录
        examMapper.deleteById(examId);
    }

    /**
     * 私有辅助方法：构建 ExamQuestionResult 对象并计算原始分
     */
    private ExamQuestionResult buildResult(
            Long examId,
            QuestionAnswerDTO dto,
            QuestionConfig config) {

        ExamQuestionResult r = new ExamQuestionResult();

        r.setExamId(examId);
        r.setQuestionCode(dto.getQuestionCode());
        r.setCorrectCount(dto.getCorrectCount());
        r.setScorePerQuestion(config.getScorePerQuestion());
        r.setSectionType(config.getSectionType());
        // 计算公式：正确题数 * 单题分值
        r.setRawScore(
                dto.getCorrectCount() * config.getScorePerQuestion()
        );

        return r;
    }

    /**
     * 查询单场考试详情
     */
    public Exam getById(Long id) {
        return examMapper.selectById(id);
    }

    /**
     * 获取所有考试历史
     */
    public List<Exam> getAll() {
        return examMapper.selectAll();
    }
}

