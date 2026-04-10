package com.xuyifei.jlpt.mapper;

import com.xuyifei.jlpt.entity.ExamQuestionResult;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface ExamQuestionResultMapper {

    void batchInsert(@Param("list")List<ExamQuestionResult> list);

    void deleteByExamId(Long examId);

    void update(ExamQuestionResult entity);
    List<ExamQuestionResult> selectByExamId(Long examId);

    ExamQuestionResult selectByExamIdAndQuestionCode(
            @Param("examId") Long examId,
            @Param("questionCode") String questionCode);
}
