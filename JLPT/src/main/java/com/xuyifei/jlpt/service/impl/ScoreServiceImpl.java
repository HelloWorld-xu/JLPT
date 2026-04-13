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
@Transactional // 类级别事务：确保计算分数和更新考试记录的操作原子性
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

        // 1. 获取该场考试所有的题目得分明细
        List<ExamQuestionResult> results =
                resultMapper.selectByExamId(examId);

        // 初始化三大板块的原始分累加器
        int languageRaw = 0;    // 语言知识 (文字、词汇、语法)
        int readingRaw = 0;     //阅读
        int listeningRaw = 0;   //听力

        // 2. 遍历明细，根据 sectionType 汇总各板块的原始得分
        for (ExamQuestionResult r : results) {

            if ("LANGUAGE".equalsIgnoreCase(r.getSectionType())) {
                languageRaw += r.getRawScore();
            } else if ("READING".equalsIgnoreCase(r.getSectionType())) {
                readingRaw += r.getRawScore();
            } else if ("LISTENING".equalsIgnoreCase(r.getSectionType())) {
                listeningRaw += r.getRawScore();
            }
        }

        // 3. 从配置表中获取各板块的满分值（原始分上限）
        Integer languageFull =
                configMapper.sumFullScoreBySection("LANGUAGE");

        Integer readingFull =
                configMapper.sumFullScoreBySection("READING");

        Integer listeningFull =
                configMapper.sumFullScoreBySection("LISTENING");

        // 4. 将原始分折算为标准分 (Scaled Score，每项满分 60)
        int languageScaled =
                calculateScaled(languageRaw, languageFull);

        int readingScaled =
                calculateScaled(readingRaw, readingFull);

        int listeningScaled =
                calculateScaled(listeningRaw, listeningFull);

        // 计算总分 (满分 180)
        int total =
                languageScaled + readingScaled + listeningScaled;

        // 5. 合格判定逻辑 (以 N1/N2 等级常见标准为例)
        // 条件：各单项不低于 19 分，且总分不低于 90 分
        boolean pass =
                languageScaled >= 19
                        && readingScaled >= 19
                        && listeningScaled >= 19
                        && total >= 90;

        // 6. 更新 Exam 主表信息，持久化评分结果
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

        // 7. 封装返回给前端的结果对象
        ExamResultVO vo = new ExamResultVO();
        vo.setLanguageScore(languageScaled);
        vo.setReadingScore(readingScaled);
        vo.setListeningScore(listeningScaled);
        vo.setTotalScore(total);
        vo.setPass(pass);

        return vo;
    }

    /**
     * 内部辅助方法：标准分折算算法
     * 逻辑：(个人原始分 / 该项总原始分) * 60 分，并四舍五入
     * @param raw 个人原始得分
     * @param fullScore 该板块原始分满分
     * @return 转换后的标准分 (0-60)
     */
    private int calculateScaled(int raw, int fullScore) {

        // 使用 double 运算防止整数除法导致精度丢失
        double ratio = (double) raw / fullScore;

        // 四舍五入后转回 int
        return (int) Math.round(ratio * 60);
    }


}
