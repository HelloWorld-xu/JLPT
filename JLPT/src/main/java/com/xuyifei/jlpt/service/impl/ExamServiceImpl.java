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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考试服务实现类
 * N1、N2、N3 共用同一套考试处理逻辑，具体题型和分值从数据库按等级读取
 */
@Service
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;

    @Autowired
    private ExamQuestionResultMapper resultMapper;

    @Autowired
    private QuestionConfigMapper configMapper;

    public ExamServiceImpl(ExamMapper examMapper,
                           ExamQuestionResultMapper resultMapper) {
        this.examMapper = examMapper;
        this.resultMapper = resultMapper;
    }

    /**
     * 根据等级获取题型配置
     */
    @Override
    public List<QuestionConfig> getQuestionConfigs(String level) {
        String normalizedLevel = normalizeLevel(level);
        List<QuestionConfig> configs = configMapper.selectByLevel(normalizedLevel);

        if (configs == null || configs.isEmpty()) {
            throw new RuntimeException("未找到 " + normalizedLevel + " 等级的题型配置");
        }

        return configs;
    }

    /**
     * 提交或更新答案
     * 根据当前考试记录的等级读取配置，不允许不同等级的同名题型相互混用
     */
    @Transactional
    @Override
    public void upsertAnswers(Long examId, List<QuestionAnswerDTO> answers) {

        if (answers == null || answers.isEmpty()) {
            throw new RuntimeException("答案不能为空");
        }

        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试记录不存在");
        }

        List<QuestionConfig> configs = getQuestionConfigs(exam.getLevel());
        Map<String, QuestionConfig> configMap = configs.stream()
                .collect(Collectors.toMap(
                        QuestionConfig::getQuestionCode,
                        config -> config,
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "题型配置重复：" + first.getQuestionCode());
                        },
                        LinkedHashMap::new
                ));

        // 同一批请求中不允许出现重复题型，避免批量插入重复记录
        Map<String, QuestionAnswerDTO> answerMap = new LinkedHashMap<>();
        for (QuestionAnswerDTO dto : answers) {
            if (dto == null || dto.getQuestionCode() == null
                    || dto.getQuestionCode().trim().isEmpty()) {
                throw new RuntimeException("答案中存在无效的题型编号");
            }

            String questionCode = dto.getQuestionCode().trim();
            if (answerMap.putIfAbsent(questionCode, dto) != null) {
                throw new RuntimeException("提交的题型重复：" + questionCode);
            }
        }

        List<ExamQuestionResult> insertList = new ArrayList<>();

        for (QuestionAnswerDTO dto : answerMap.values()) {
            QuestionConfig config = configMap.get(dto.getQuestionCode());
            if (config == null) {
                throw new RuntimeException(
                        exam.getLevel() + " 等级不存在题型：" + dto.getQuestionCode());
            }

            Integer correctCount = dto.getCorrectCount();
            if (correctCount == null || correctCount < 0
                    || correctCount > config.getQuestionCount()) {
                throw new RuntimeException(
                        dto.getQuestionCode() + " 的正确题数必须在 0 到 "
                                + config.getQuestionCount() + " 之间");
            }

            ExamQuestionResult existing =
                    resultMapper.selectByExamIdAndQuestionCode(
                            examId, dto.getQuestionCode());

            if (existing == null) {
                insertList.add(buildResult(examId, dto, config));
            } else {
                existing.setCorrectCount(correctCount);
                existing.setScorePerQuestion(config.getScorePerQuestion());
                existing.setSectionType(config.getSectionType());
                existing.setRawScore(correctCount * config.getScorePerQuestion());
                resultMapper.update(existing);
            }
        }

        if (!insertList.isEmpty()) {
            resultMapper.batchInsert(insertList);
        }
    }

    /**
     * 创建新考试
     */
    @Override
    public Long createExam(CreateExamDTO dto) {
        if (dto == null) {
            throw new RuntimeException("考试信息不能为空");
        }

        String level = normalizeLevel(dto.getLevel());
        List<QuestionConfig> configs = configMapper.selectByLevel(level);
        if (configs == null || configs.isEmpty()) {
            throw new RuntimeException("未找到 " + level + " 等级的题型配置");
        }

        Exam exam = new Exam();
        exam.setLevel(level);
        exam.setYear(dto.getYear());
        exam.setMonth(dto.getMonth());

        exam.setLanguageRawScore(0);
        exam.setReadingRawScore(0);
        exam.setListeningRawScore(0);
        exam.setLanguageScaledScore(0);
        exam.setReadingScaledScore(0);
        exam.setListeningScaledScore(0);
        exam.setTotalScaledScore(0);
        exam.setPass(false);

        examMapper.insert(exam);
        return exam.getId();
    }

    /**
     * 删除考试及其关联的所有答题明细
     */
    @Override
    @Transactional
    public void deleteExam(Long examId) {
        resultMapper.deleteByExamId(examId);
        examMapper.deleteById(examId);
    }

    /**
     * 构建答题明细并计算原始分
     */
    private ExamQuestionResult buildResult(
            Long examId,
            QuestionAnswerDTO dto,
            QuestionConfig config) {

        ExamQuestionResult result = new ExamQuestionResult();
        result.setExamId(examId);
        result.setQuestionCode(dto.getQuestionCode());
        result.setCorrectCount(dto.getCorrectCount());
        result.setScorePerQuestion(config.getScorePerQuestion());
        result.setSectionType(config.getSectionType());
        result.setRawScore(dto.getCorrectCount() * config.getScorePerQuestion());
        return result;
    }

    private String normalizeLevel(String level) {
        if (level == null) {
            throw new RuntimeException("考试等级不能为空");
        }

        String normalizedLevel = level.trim().toUpperCase();
        if (!"N1".equals(normalizedLevel)
                && !"N2".equals(normalizedLevel)
                && !"N3".equals(normalizedLevel)) {
            throw new RuntimeException("暂不支持的 JLPT 等级：" + level);
        }
        return normalizedLevel;
    }

    @Override
    public Exam getById(Long id) {
        return examMapper.selectById(id);
    }

    @Override
    public List<Exam> getAll() {
        return examMapper.selectAll();
    }
}
