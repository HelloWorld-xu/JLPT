package com.xuyifei.jlpt.service.impl;

import com.xuyifei.jlpt.entity.Exam;
import com.xuyifei.jlpt.entity.ExamQuestionResult;
import com.xuyifei.jlpt.mapper.ExamMapper;
import com.xuyifei.jlpt.mapper.ExamQuestionResultMapper;
import com.xuyifei.jlpt.mapper.QuestionConfigMapper;
import com.xuyifei.jlpt.service.ScoreService;
import com.xuyifei.jlpt.vo.ExamResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分数计算服务实现类
 * N1、N2、N3 使用相同的原始分汇总及标准分折算逻辑，
 * 满分、合格线等差异由考试等级和数据库配置决定
 */
@Service
@Transactional
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private QuestionConfigMapper configMapper;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamQuestionResultMapper resultMapper;

    @Override
    public ExamResultVO calculate(Long examId) {

        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试记录不存在");
        }

        String level = exam.getLevel();
        if (level == null) {
            throw new RuntimeException("考试记录缺少等级信息");
        }
        level = level.trim().toUpperCase();

        // 只读取当前考试 ID 的答题明细
        List<ExamQuestionResult> results = resultMapper.selectByExamId(examId);

        int languageRaw = 0;
        int readingRaw = 0;
        int listeningRaw = 0;

        if (results != null) {
            for (ExamQuestionResult result : results) {
                if (result.getRawScore() == null) {
                    continue;
                }

                if ("language".equalsIgnoreCase(result.getSectionType())) {
                    languageRaw += result.getRawScore();
                } else if ("reading".equalsIgnoreCase(result.getSectionType())) {
                    readingRaw += result.getRawScore();
                } else if ("listening".equalsIgnoreCase(result.getSectionType())) {
                    listeningRaw += result.getRawScore();
                }
            }
        }

        // 满分按等级查询，防止不同等级同名题型的分值被合并
        Integer languageFull =
                configMapper.sumFullScoreBySection(level, "language");
        Integer readingFull =
                configMapper.sumFullScoreBySection(level, "reading");
        Integer listeningFull =
                configMapper.sumFullScoreBySection(level, "listening");

        if (languageFull == null || languageFull <= 0
                || readingFull == null || readingFull <= 0
                || listeningFull == null || listeningFull <= 0) {
            throw new RuntimeException("未找到 " + level + " 等级完整的分值配置");
        }

        // 各板块分别折算为 60 分，计算方式沿用原有 N2 逻辑
        int languageScaled = calculateScaled(languageRaw, languageFull);
        int readingScaled = calculateScaled(readingRaw, readingFull);
        int listeningScaled = calculateScaled(listeningRaw, listeningFull);
        int total = languageScaled + readingScaled + listeningScaled;

        // 三个等级的单项合格线均为 19 分，总分线按等级区分
        int totalPassScore;
        switch (level) {
            case "N1":
                totalPassScore = 100;
                break;
            case "N2":
                totalPassScore = 90; // 保留原有 N2 合格线
                break;
            case "N3":
                totalPassScore = 95;
                break;
            default:
                throw new RuntimeException("暂不支持的 JLPT 等级：" + level);
        }

        boolean pass = languageScaled >= 19
                && readingScaled >= 19
                && listeningScaled >= 19
                && total >= totalPassScore;

        exam.setLanguageRawScore(languageRaw);
        exam.setReadingRawScore(readingRaw);
        exam.setListeningRawScore(listeningRaw);
        exam.setLanguageScaledScore(languageScaled);
        exam.setReadingScaledScore(readingScaled);
        exam.setListeningScaledScore(listeningScaled);
        exam.setTotalScaledScore(total);
        exam.setPass(pass);
        exam.setCalculated(true);

        examMapper.updateById(exam);

        ExamResultVO vo = new ExamResultVO();
        vo.setLanguageScore(languageScaled);
        vo.setReadingScore(readingScaled);
        vo.setListeningScore(listeningScaled);
        vo.setTotalScore(total);
        vo.setPass(pass);
        return vo;
    }

    /**
     * 标准分折算：(个人原始分 / 当前等级该板块原始满分) * 60，四舍五入
     */
    private int calculateScaled(int raw, int fullScore) {
        return (int) Math.round((double) raw / fullScore * 60);
    }
}
