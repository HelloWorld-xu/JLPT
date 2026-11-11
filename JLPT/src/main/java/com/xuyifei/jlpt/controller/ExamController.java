package com.xuyifei.jlpt.controller;

import com.xuyifei.jlpt.dto.CreateExamDTO;
import com.xuyifei.jlpt.dto.QuestionAnswerDTO;
import com.xuyifei.jlpt.entity.Exam;\nimport com.xuyifei.jlpt.entity.ExamQuestionResult;
import com.xuyifei.jlpt.entity.QuestionConfig;
import com.xuyifei.jlpt.service.ExamService;
import com.xuyifei.jlpt.service.ScoreService;
import com.xuyifei.jlpt.vo.ExamResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试管理控制器
 */
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

    /**
     * 根据等级获取题型配置
     * GET /exam/config/N1
     */
    @GetMapping("/config/{level}")
    public List<QuestionConfig> getQuestionConfigs(@PathVariable String level) {
        return examService.getQuestionConfigs(level);
    }

    /**
     * 创建新考试
     */
    @PostMapping
    public Long create(@RequestBody CreateExamDTO dto) {
        return examService.createExam(dto);
    }

    /**
     * 更新或插入考试答案
     */
    @PutMapping("/{id}/upsert")
    public void upsertAnswers(
            @PathVariable Long id,
            @RequestBody List<QuestionAnswerDTO> answers) {
        examService.upsertAnswers(id, answers);
    }

    /**
     * 删除指定考试记录
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        examService.deleteExam(id);
    }

    /**
     * 计算考试得分
     */
    @PostMapping("/{id}/calculate")
    public ExamResultVO calculate(@PathVariable Long id) {
        return scoreService.calculate(id);
    }

    /**
     * 根据 ID 获取考试详情
     */
    @GetMapping("/{id}/get")
    public Exam getExamById(@PathVariable Long id) {
        return examService.getById(id);
    }

    /**
     * 获取所有考试历史
     */
    @GetMapping("/history")
    public List<Exam> getAllExams() {
        return examService.getAll();
    }
}
