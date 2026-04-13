package com.xuyifei.jlpt.service;

import com.xuyifei.jlpt.vo.ExamResultVO;

/**
 * 分数计算服务接口
 * 专门负责考试结束后的得分汇总、标准分转换以及合格判定逻辑
 */
public interface ScoreService {

    /**
     * 计算指定考试的最终成绩
     * 该方法会读取该场考试关联的所有答题明细，计算各板块原始分，
     * 并根据配置将其折算为标准分，最后判定是否通过考试。
     * @param examId 考试记录的唯一 ID
     * @return 包含各项得分及通过情况的视图对象 (ExamResultVO)
     */
    ExamResultVO calculate(Long examId);
}
