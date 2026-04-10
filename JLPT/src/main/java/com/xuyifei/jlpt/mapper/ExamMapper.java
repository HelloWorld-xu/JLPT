package com.xuyifei.jlpt.mapper;

import com.xuyifei.jlpt.entity.Exam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExamMapper {

    int insert(Exam exam);

    Exam selectById(Long id);

    void updateById(Exam exam);

    void deleteById(Long examId);

    List<Exam> selectAll();
}
