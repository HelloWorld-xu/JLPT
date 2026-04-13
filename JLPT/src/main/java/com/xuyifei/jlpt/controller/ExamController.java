package com.xuyifei.jlpt.controller;


import com.xuyifei.jlpt.dto.CreateExamDTO;
import com.xuyifei.jlpt.dto.QuestionAnswerDTO;
import com.xuyifei.jlpt.entity.Exam;
import com.xuyifei.jlpt.service.ExamService;
import com.xuyifei.jlpt.service.ScoreService;
import com.xuyifei.jlpt.vo.ExamResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;


/**
 * 考试管理控制器
 * 处理考试的创建、答题记录、评分及历史查询等请求
 */
@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor// Lombok注解：自动生成包含 final 字段的构造函数，用于依赖注入
@CrossOrigin(origins = "http://localhost:5173")// 允许来自前端开发服务器的跨域请求
public class ExamController {

    private final ExamService examService;
    private final ScoreService scoreService;

    /**
     * 测试接口
     * 用于检查后端服务是否正常响应
     * @return 字符串 "OK"
     */
    @PostMapping("/test")
    public String test() {
        return "OK";
    }

    /**
     * 创建新考试
     * @param dto 包含考试初始化信息的传输对象
     * @return 生成的考试 ID
     */
    @PostMapping
    public Long create(@RequestBody CreateExamDTO dto) {
        return examService.createExam(dto);
    }

    /**
     * 更新或插入考试答案（保存答题进度）
     * @param id 考试 ID
     * @param answers 用户提交的问题答案列表
     */
    @PutMapping("/{id}/upsert")
    public void upsertAnswers(
            @PathVariable Long id,
            @RequestBody List<QuestionAnswerDTO> answers) {

        examService.upsertAnswers(id, answers);
    }

    /**
     * 删除指定的考试记录
     * @param id 考试 ID
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        examService.deleteExam(id);
    }

    /**
     * 计算考试得分
     * 提交试卷并由 ScoreService 处理评分逻辑
     * @param id 考试 ID
     * @return 包含得分详情、正确率等信息的视图对象 (VO)
     */
    @PostMapping("/{id}/calculate")
    public ExamResultVO calculate(@PathVariable Long id) {
        return scoreService.calculate(id);
    }

    /**
     * 根据 ID 获取考试详情
     * @param id 考试 ID
     * @return 考试实体对象（包含基本信息及关联数据）
     */
    @GetMapping("/{id}/get")
    public Exam getExamById(@PathVariable Long id) {
        return examService.getById(id);
    }

    /**
     * 获取所有考试的历史记录
     * @return 考试实体列表
     */
    @GetMapping("/history")
    public List<Exam> getAllExams() {
        return examService.getAll();
    }
}
