package com.xuyifei.jlpt.service.impl;

import com.xuyifei.jlpt.dto.QuestionAnswerDTO;
import com.xuyifei.jlpt.entity.Exam;
import com.xuyifei.jlpt.entity.ExamQuestionResult;
import com.xuyifei.jlpt.entity.QuestionConfig;
import com.xuyifei.jlpt.mapper.ExamMapper;
import com.xuyifei.jlpt.mapper.ExamQuestionResultMapper;
import com.xuyifei.jlpt.mapper.QuestionConfigMapper;
import com.xuyifei.jlpt.mapper.ScoreConversionMapper;
import com.xuyifei.jlpt.service.ScoreService;
import com.xuyifei.jlpt.vo.ExamResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private QuestionConfigMapper configMapper;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamQuestionResultMapper resultMapper;



//    public ExamResultDTO calculateAndSaveScore(ExamSubmitDTO dto) {
//
//        // 1️⃣ 查询所有题目配置
//        List<QuestionConfig> configs =
//                questionConfigMapper.selectAll();
//
//        Map<String, QuestionConfig> configMap =
//                configs.stream()
//                        .collect(Collectors.toMap(
//                                QuestionConfig::getQuestionCode,
//                                c -> c
//                        ));
//
//        int languageRaw = 0;
//        int readingRaw = 0;
//        int listeningRaw = 0;
//
//        List<ExamQuestionResult> resultList =
//                new ArrayList<>();
//
//        // 2️⃣ 逐题计算
//        for (QuestionAnswerDTO answer : dto.getAnswers()) {
//
//            QuestionConfig config =
//                    configMap.get(answer.getQuestionCode());
//
//            if (config == null) {
//                continue;
//            }
//
//            int rawScore =
//                    answer.getCorrectCount()
//                            * config.getScorePerQuestion();
//
//            // 按板块累计
//            String section =
//                    config.getSectionType();
//
//            if ("LANGUAGE".equalsIgnoreCase(section)) {
//                languageRaw += rawScore;
//            } else if ("READING".equalsIgnoreCase(section)) {
//                readingRaw += rawScore;
//            } else if ("LISTENING".equalsIgnoreCase(section)) {
//                listeningRaw += rawScore;
//            }
//
//            // 保存明细
//            ExamQuestionResult r =
//                    new ExamQuestionResult();
//
//            r.setQuestionCode(answer.getQuestionCode());
//            r.setCorrectCount(answer.getCorrectCount());
//            r.setScorePerQuestion(config.getScorePerQuestion());
//            r.setRawScore(rawScore);
//            r.setSectionType(section);
//
//            resultList.add(r);
//
//        }
//
//        // 3️⃣ 转换成 60 分制
//        // 3️⃣ 转换成 60 分制（比例换算）
//
//        Integer languageFull =
//                questionConfigMapper.sumFullScoreBySection("language");
//
//        Integer readingFull =
//                questionConfigMapper.sumFullScoreBySection("reading");
//
//        Integer listeningFull =
//                questionConfigMapper.sumFullScoreBySection("listening");
//
//        int languageScore =
//                calculateScaled(languageRaw, languageFull);
//
//        int readingScore =
//                calculateScaled(readingRaw, readingFull);
//
//        int listeningScore =
//                calculateScaled(listeningRaw, listeningFull);
//
//        int total =
//                languageScore
//                        + readingScore
//                        + listeningScore;
//
//        // 4️⃣ 合格判定
//        boolean pass =
//                languageScore >= 19
//                        && readingScore >= 19
//                        && listeningScore >= 19
//                        && total >= 90;
//
//        // 5️⃣ 保存 exam
//        Exam exam = new Exam();
//
//        exam.setLevel(dto.getLevel());
//        exam.setYear(dto.getYear());
//        exam.setMonth(dto.getMonth());
//
//        // 保存 raw 分
//        exam.setLanguageRawScore(languageRaw);
//        exam.setReadingRawScore(readingRaw);
//        exam.setListeningRawScore(listeningRaw);
//
//        exam.setLanguageScaledScore(languageScore);
//        exam.setReadingScaledScore(readingScore);
//        exam.setListeningScaledScore(listeningScore);
//
//        exam.setTotalScaledScore(total);
//        exam.setPass(pass);
//
//        examMapper.insert(exam);
//
//        // 6️⃣ 绑定 examId
//        for (ExamQuestionResult r : resultList) {
//            r.setExamId(exam.getId());
//        }
//
//        resultMapper.batchInsert(resultList);
//        return new ExamResultDTO(
//                languageScore,
//                readingScore,
//                listeningScore,
//                total,
//                pass
//        );
//    }

    @Override
    public ExamResultVO calculate(Long examId) {

        List<ExamQuestionResult> results =
                resultMapper.selectByExamId(examId);

        int languageRaw = 0;
        int readingRaw = 0;
        int listeningRaw = 0;

        for (ExamQuestionResult r : results) {

            if ("LANGUAGE".equalsIgnoreCase(r.getSectionType())) {
                languageRaw += r.getRawScore();
            } else if ("READING".equalsIgnoreCase(r.getSectionType())) {
                readingRaw += r.getRawScore();
            } else if ("LISTENING".equalsIgnoreCase(r.getSectionType())) {
                listeningRaw += r.getRawScore();
            }
        }

        Integer languageFull =
                configMapper.sumFullScoreBySection("LANGUAGE");

        Integer readingFull =
                configMapper.sumFullScoreBySection("READING");

        Integer listeningFull =
                configMapper.sumFullScoreBySection("LISTENING");

        int languageScaled =
                calculateScaled(languageRaw, languageFull);

        int readingScaled =
                calculateScaled(readingRaw, readingFull);

        int listeningScaled =
                calculateScaled(listeningRaw, listeningFull);

        int total =
                languageScaled + readingScaled + listeningScaled;

        boolean pass =
                languageScaled >= 19
                        && readingScaled >= 19
                        && listeningScaled >= 19
                        && total >= 90;

        // 更新 exam
        Exam exam = examMapper.selectById(examId);

        exam.setLanguageRawScore(languageRaw);
        exam.setReadingRawScore(readingRaw);
        exam.setListeningRawScore(listeningRaw);

        exam.setLanguageScaledScore(languageScaled);
        exam.setReadingScaledScore(readingScaled);
        exam.setListeningScaledScore(listeningScaled);
        exam.setTotalScaledScore(total);
        exam.setPass(pass);

        examMapper.updateById(exam);

        ExamResultVO vo = new ExamResultVO();
        vo.setLanguageScore(languageScaled);
        vo.setReadingScore(readingScaled);
        vo.setListeningScore(listeningScaled);
        vo.setTotalScore(total);
        vo.setPass(pass);

        return vo;
    }

    private int calculateScaled(int raw, int fullScore) {



        double ratio = (double) raw / fullScore;

        return (int) Math.round(ratio * 60);
    }

    /**
     * Excel算分转换（先用简化版）
     */
    private int convertTo60(int raw) {

        if (raw >= 60) {
            return 60;
        }

        return raw;
    }
}
