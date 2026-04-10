package com.xuyifei.jlpt.service;

import com.xuyifei.jlpt.dto.CreateExamDTO;
import com.xuyifei.jlpt.dto.QuestionAnswerDTO;
import com.xuyifei.jlpt.entity.Exam;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ExamService {


    Long createExam(CreateExamDTO dto);

    void upsertAnswers(Long examId, List<QuestionAnswerDTO> answers);

    void deleteExam(Long examId);

    Exam getById(Long id);

    List<Exam> getAll();
}
