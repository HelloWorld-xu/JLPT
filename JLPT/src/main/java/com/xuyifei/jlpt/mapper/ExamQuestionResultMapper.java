package com.xuyifei.jlpt.mapper;

import com.xuyifei.jlpt.entity.ExamQuestionResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 * 考试题目得分明细数据库映射接口
 * 处理与具体题型得分相关的持久化逻辑，支持与 Exam 主表的关联操作
 */
@Mapper
public interface ExamQuestionResultMapper {

    /**
     * 批量插入题目得分明细
     * 用于在考试提交时，一次性保存多个板块（如文字、语法、阅读、听力）的答题结果
     * @param list 包含多条得分明细的列表
     */
    void batchInsert(@Param("list")List<ExamQuestionResult> list);

    /**
     * 根据考试 ID 删除相关的题目明细
     * 通常用于级联删除或在重新计算分数前清空旧数据
     * @param examId 考试记录 ID
     */
    void deleteByExamId(Long examId);

    /**
     * 更新单条得分明细记录
     * @param entity 包含更新信息的实体对象
     */
    void update(ExamQuestionResult entity);

    /**
     * 根据考试 ID 获取该场考试所有的题目明细
     * 用于在查看考试报告时，展示各部分的详细得分情况
     * @param examId 考试记录 ID
     * @return 题目明细实体列表
     */
    List<ExamQuestionResult> selectByExamId(Long examId);

    /**
     * 根据考试 ID 和题目代码精准定位某一条得分明细
     * 用于检查用户是否已经回答过特定题目，或更新特定题项的得分
     * @param examId 考试记录 ID
     * @param questionCode 题目代码（如 "N1_Q1"）
     * @return 匹配的得分明细对象
     */
    ExamQuestionResult selectByExamIdAndQuestionCode(
            @Param("examId") Long examId,
            @Param("questionCode") String questionCode);
}
