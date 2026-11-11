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
import com.xuyifei.jlpt.vo.ExamStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

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

        // 答案发生变化后，旧成绩不再视为有效；重新计算后才参与统计
        exam.setCalculated(false);
        examMapper.updateById(exam);
    }

    /**
     * 根据考试 ID 获取已保存的答题明细
     */
    @Override
    public List<ExamQuestionResult> getAnswerDetails(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试记录不存在");
        }
        return resultMapper.selectByExamId(examId);
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
        exam.setCalculated(false);

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

    /**
     * 统计指定等级已完成成绩计算的考试。
     * 趋势按考试年月排序，同年月时再按创建时间和 ID 排序。
     */
    @Override
    public ExamStatisticsVO getStatistics(String level) {
        String normalizedLevel = normalizeLevel(level);

        List<Exam> exams = getAll().stream()
                .filter(exam -> normalizedLevel.equalsIgnoreCase(exam.getLevel()))
                .filter(exam -> Boolean.TRUE.equals(exam.getCalculated()))
                .sorted(Comparator
                        .comparing(Exam::getYear, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Exam::getMonth, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Exam::getCreateTime, Comparator.nullsLast(java.time.LocalDateTime::compareTo))
                        .thenComparing(Exam::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());

        ExamStatisticsVO statistics = new ExamStatisticsVO();
        statistics.setLevel(normalizedLevel);
        statistics.setExamCount(exams.size());

        int passCount = (int) exams.stream()
                .filter(exam -> Boolean.TRUE.equals(exam.getPass()))
                .count();
        statistics.setPassCount(passCount);

        if (exams.isEmpty()) {
            statistics.setHighestScore(null);
            statistics.setAverageScore(null);
            statistics.setPassRate(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP));
            statistics.setTrend(new ArrayList<>());
            statistics.setSectionExtremes(buildSectionExtremes(exams));
            return statistics;
        }

        statistics.setHighestScore(exams.stream()
                .map(Exam::getTotalScaledScore)
                .filter(score -> score != null)
                .max(Integer::compareTo)
                .orElse(null));

        double average = exams.stream()
                .map(Exam::getTotalScaledScore)
                .filter(score -> score != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        statistics.setAverageScore(BigDecimal.valueOf(average)
                .setScale(2, RoundingMode.HALF_UP));
        statistics.setPassRate(BigDecimal.valueOf(passCount * 100.0 / exams.size())
                .setScale(1, RoundingMode.HALF_UP));

        List<ExamStatisticsVO.TrendItem> trend = new ArrayList<>();
        for (Exam exam : exams) {
            ExamStatisticsVO.TrendItem item = new ExamStatisticsVO.TrendItem();
            item.setExamId(exam.getId());
            item.setYear(exam.getYear());
            item.setMonth(exam.getMonth());
            item.setLabel(exam.getYear() + "." + String.format("%02d", exam.getMonth()));
            item.setLanguageScore(exam.getLanguageScaledScore());
            item.setReadingScore(exam.getReadingScaledScore());
            item.setListeningScore(exam.getListeningScaledScore());
            item.setTotalScore(exam.getTotalScaledScore());
            item.setPass(exam.getPass());
            trend.add(item);
        }
        statistics.setTrend(trend);
        statistics.setSectionExtremes(buildSectionExtremes(exams));

        if (exams.size() >= 2) {
            Exam previous = exams.get(exams.size() - 2);
            Exam current = exams.get(exams.size() - 1);
            ExamStatisticsVO.RecentComparison comparison =
                    new ExamStatisticsVO.RecentComparison();
            comparison.setPreviousLabel(formatExamLabel(previous));
            comparison.setCurrentLabel(formatExamLabel(current));

            comparison.setPreviousLanguageScore(previous.getLanguageScaledScore());
            comparison.setCurrentLanguageScore(current.getLanguageScaledScore());
            comparison.setLanguageChange(scoreOrZero(current.getLanguageScaledScore())
                    - scoreOrZero(previous.getLanguageScaledScore()));

            comparison.setPreviousReadingScore(previous.getReadingScaledScore());
            comparison.setCurrentReadingScore(current.getReadingScaledScore());
            comparison.setReadingChange(scoreOrZero(current.getReadingScaledScore())
                    - scoreOrZero(previous.getReadingScaledScore()));

            comparison.setPreviousListeningScore(previous.getListeningScaledScore());
            comparison.setCurrentListeningScore(current.getListeningScaledScore());
            comparison.setListeningChange(scoreOrZero(current.getListeningScaledScore())
                    - scoreOrZero(previous.getListeningScaledScore()));

            comparison.setPreviousTotalScore(previous.getTotalScaledScore());
            comparison.setCurrentTotalScore(current.getTotalScaledScore());
            comparison.setTotalChange(scoreOrZero(current.getTotalScaledScore())
                    - scoreOrZero(previous.getTotalScaledScore()));
            statistics.setRecentComparison(comparison);
        }

        return statistics;
    }

    private ExamStatisticsVO.SectionExtremes buildSectionExtremes(List<Exam> exams) {
        ExamStatisticsVO.SectionExtremes extremes =
                new ExamStatisticsVO.SectionExtremes();
        extremes.setLanguage(buildScoreRange(exams, Exam::getLanguageScaledScore));
        extremes.setReading(buildScoreRange(exams, Exam::getReadingScaledScore));
        extremes.setListening(buildScoreRange(exams, Exam::getListeningScaledScore));
        return extremes;
    }

    private ExamStatisticsVO.ScoreRange buildScoreRange(
            List<Exam> exams, Function<Exam, Integer> scoreGetter) {
        List<Integer> scores = exams.stream()
                .map(scoreGetter)
                .filter(score -> score != null)
                .collect(Collectors.toList());

        ExamStatisticsVO.ScoreRange range = new ExamStatisticsVO.ScoreRange();
        range.setHighest(scores.stream().max(Integer::compareTo).orElse(null));
        range.setLowest(scores.stream().min(Integer::compareTo).orElse(null));
        return range;
    }

    private int scoreOrZero(Integer score) {
        return score == null ? 0 : score;
    }

    private String formatExamLabel(Exam exam) {
        return exam.getYear() + "." + String.format("%02d", exam.getMonth());
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
