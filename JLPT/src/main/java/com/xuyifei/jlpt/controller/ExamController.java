package com.xuyifei.jlpt.controller;


import com.xuyifei.jlpt.dto.CreateExamDTO;
import com.xuyifei.jlpt.dto.QuestionAnswerDTO;
import com.xuyifei.jlpt.entity.Exam;
import com.xuyifei.jlpt.service.ExamService;
import com.xuyifei.jlpt.service.ScoreService;
import com.xuyifei.jlpt.vo.ExamResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ExamController {

    private final ExamService examService;
    private final ScoreService scoreService;


    @PostMapping("/test")
    public String test() {
        return "OK";
    }


    @PostMapping
    public Long create(@RequestBody CreateExamDTO dto) {
        return examService.createExam(dto);
    }

    @PutMapping("/{id}/upsert")
    public void upsertAnswers(
            @PathVariable Long id,
            @RequestBody List<QuestionAnswerDTO> answers) {

        examService.upsertAnswers(id, answers);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        examService.deleteExam(id);
    }

    @PostMapping("/{id}/calculate")
    public ExamResultVO calculate(@PathVariable Long id) {
        return scoreService.calculate(id);
    }

    @GetMapping("/{id}/get")
    public Exam getExamById(@PathVariable Long id) {
        return examService.getById(id);
    }

    @GetMapping("/history")
    public List<Exam> getAllExams() {
        return examService.getAll();
    }
}
