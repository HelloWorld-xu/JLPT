package com.xuyifei.jlpt.service.impl;

import com.xuyifei.jlpt.dto.CreateExamDTO;
import com.xuyifei.jlpt.dto.QuestionAnswerDTO;
import com.xuyifei.jlpt.entity.Exam;
import com.xuyifei.jlpt.entity.ExamQuestionResult;
import com.xuyifei.jlpt.entity.QuestionConfig;
import com.xuyifei.jlpt.mapper.ExamMapper;
import com.xuyifei.jlpt.mapper.ExamQuestionResultMapper;
import com.xuyifei.jlpt.mapper.QuestionConfigMapper;
import com.xuyifei.jlpt.mapper.ScoreConversionMapper;
import com.xuyifei.jlpt.service.ExamService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private final ExamMapper examMapper;
    @Autowired
    private final ScoreConversionMapper scoreConversionMapper;

    @Autowired
    private ExamQuestionResultMapper resultMapper;


    @Autowired
    private QuestionConfigMapper configMapper;

    public ExamServiceImpl(
            ExamMapper examMapper,
            ExamQuestionResultMapper resultMapper,
            ScoreConversionMapper scoreConversionMapper) {
        this.examMapper = examMapper;
        this.resultMapper = resultMapper;
        this.scoreConversionMapper = scoreConversionMapper;
    }

    @Transactional
    @Override
    public void upsertAnswers(Long examId, List<QuestionAnswerDTO> answers) {

        if (answers == null || answers.isEmpty()) {
            throw new RuntimeException("答案不能为空");
        }

        List<QuestionConfig> configs = configMapper.selectAll();
        Map<String, QuestionConfig> configMap =
                configs.stream().collect(Collectors.toMap(
                        QuestionConfig::getQuestionCode,
                        c -> c
                ));

        List<ExamQuestionResult> insertList = new ArrayList<>();

        for (QuestionAnswerDTO dto : answers) {

            QuestionConfig config =
                    configMap.get(dto.getQuestionCode());

            if (config == null) continue;

            ExamQuestionResult existing =
                    resultMapper.selectByExamIdAndQuestionCode(
                            examId,
                            dto.getQuestionCode()
                    );

            if (existing == null) {

                // ===== 新增 =====
                ExamQuestionResult r = buildResult(examId, dto, config);
                insertList.add(r);

            } else {

                // ===== 更新 =====
                existing.setCorrectCount(dto.getCorrectCount());
                existing.setScorePerQuestion(config.getScorePerQuestion());
                existing.setSectionType(config.getSectionType());
                existing.setRawScore(
                        dto.getCorrectCount() * config.getScorePerQuestion()
                );

                resultMapper.update(existing);
            }
        }

        // 只对真正需要插入的做 batchInsert
        if (!insertList.isEmpty()) {
            resultMapper.batchInsert(insertList);
        }
    }
    @Override
    public Long createExam(CreateExamDTO dto) {

        Exam exam = new Exam();
        exam.setLevel(dto.getLevel());
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

//    @Override
//    @Transactional
//    public void submitExam(Long examId) {
//
//        // 1️⃣ 查询 exam
//        Exam exam = examMapper.selectById(examId);
//
//        if (exam == null) {
//            throw new RuntimeException("考试不存在");
//        }
//
//        // 2️⃣ 获取原始分
//        Integer rawScore = exam.getRawScore();
//
//        // 3️⃣ 查询标准分
//        Integer scaledScore = scoreConversionMapper.selectScaledScore(
//                exam.getLevel(),
//                exam.getSectionType(),
//                rawScore
//        );
//
//        if (scaledScore == null) {
//            throw new RuntimeException("未找到标准分映射");
//        }
//
//        // 4️⃣ 设置标准分
//        exam.setStandardScore(scaledScore);
//
//        // 5️⃣ 更新数据库（必须带ID）
//        examMapper.updateById(exam);
//
//        System.out.println("映射成功：" + scaledScore);
//    }

    @Override
    public void deleteExam(Long examId) {

        resultMapper.deleteByExamId(examId);
        examMapper.deleteById(examId);
    }

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
        r.setRawScore(
                dto.getCorrectCount() * config.getScorePerQuestion()
        );

        return r;
    }

    public Exam getById(Long id) {
        return examMapper.selectById(id);
    }

    public List<Exam> getAll() {
        return examMapper.selectAll();
    }
}

