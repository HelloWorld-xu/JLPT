package com.xuyifei.jlpt.service;


import com.xuyifei.jlpt.vo.ExamResultVO;

public interface ScoreService {

    ExamResultVO calculate(Long examId);
}
