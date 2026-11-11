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
 * 负责解析答题记录、汇总各板块原始分、计算标准分及合格判定
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

    /**
     * 计算整场考试的最终结果
     * @param examId 考试记录 ID
     * @return 包含最终得分和判定结果的视图对象 (VO)
     */
    @Override
    public ExamResultVO calculate(Long examId) {

        // 1. 获取考试记录，后续所有配置均以该考试等级为准
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试记录不存在");
        }

        String level = exam.getLevel();

        // 2. 获取该场考试所有的题目得分明细
        List<ExamQuestionResult> results =
                resultMapper.selectByExamId(examId);

        // 初始化三大板块的原始分累加器
        int languageRaw = 0;
        int readingRaw = 0;
        int listeningRaw = 0;

        // 3. 遍历明细，根据 sectionType 汇总各板块的原始得分
        for (ExamQuestionResult r : results) {

            if ("language".equalsIgnoreCase(r.getSectionType())) {
                languageRaw += r.getRawScore();
            } else if ("reading".equalsIgnoreCase(r.getSectionType())) {
                readingRaw += r.getRawScore();
            } else if ("listening".equalsIgnoreCase(r.getSectionType())) {
                listeningRaw += r.getRawScore();
            }
        }

        // 4. 按当前考试等级获取各板块的原始分满分
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

        // 5. 将原始分折算为标准分 (每项满分 60)
        int languageScaled = calculateScaled(languageRaw, languageFull);
        int readingScaled = calculateScaled(readingRaw, readingFull);
        int listeningScaled = calculateScaled(listeningRaw, listeningFull);

        // 计算总分 (满分 180)
        int total = languageScaled + readingScaled + listeningScaled;

        // 6. 根据考试等级设置总分合格线
        int totalPassScore;
        if ("N1".equalsIgnoreCase(level)) {
            totalPassScore = 100;
        } else if ("N2".equalsIgnoreCase(level)) {
            // 保留原有 N2 的合格线
            totalPassScore = 90;
        } else if ("N3".equalsIgnoreCase(level)) {
            totalPassScore = 95;
        } else {
            throw new RuntimeException("暂不支持的 JLPT 等级：" + level);
        }

        // 合格条件：三个单项均不低于 19 分，且总分达到等级要求
        boolean pass =
                languageScaled >= 19
                        && readingScaled >= 19
                        && listeningScaled >= 19
                        && total >= totalPassScore;

        // 7. 更新 Exam 主表信息，持久化评分结果
        exam.setLanguageRawScore(languageRaw);
        exam.setReadingRawScore(readingRaw);
        exam.setListeningRawScore(listeningRaw);

        exam.setLanguageScaledScore(languageScaled);
        exam.setReadingScaledScore(readingScaled);
        exam.setListeningScaledScore(listeningScaled);
        exam.setTotalScaledScore(total);
        exam.setPass(pass);

        examMapper.updateById(exam);

        // 8. 封装返回给前端的结果对象
        ExamResultVO vo = new ExamResultVO();
        vo.setLanguageScore(languageScaled);
        vo.setReadingScore(readingScaled);
        vo.setListeningScore(listeningScaled);
        vo.setTotalScore(total);
        vo.setPass(pass);

        return vo;
    }

    /**
     * 标准分折算算法：
     * (个人原始分 / 该项原始分满分) * 60 分，并四舍五入
     */
    private int calculateScaled(int raw, int fullScore) {
        double ratio = (double) raw / fullScore;
        return (int) Math.round(ratio * 60);
    }
}
