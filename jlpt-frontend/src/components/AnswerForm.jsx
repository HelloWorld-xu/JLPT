import { useState } from "react";
import { upsertAnswers, calculate } from "../api";

/**
 * AnswerForm 组件：答题数据录入表单
 * 功能：提供针对不同题型的正确题数输入，支持保存进度（Upsert）和触发后端评分计算
 */
function AnswerForm({ examId, setResult }) {
  
  /**
   * 题目列表常量
   * L: Language Knowledge (词汇/语法)
   * R: Reading (阅读)
   * LS: Listening (听力)
   */
  const QUESTION_LIST = [
    "L1","L2","L3","L4","L5","L6","L7","L8","L9",
    "R1","R2","R3","R4","R5",
    "LS1","LS2","LS3","LS4","LS5","LS6"
  ];

  // 初始化状态：将题号列表映射为初始答案对象数组
  const [answers, setAnswers] = useState(
    QUESTION_LIST.map(code => ({
      questionCode: code,
      correctCount: "", // 正确题目数量
      deleted: false    // 用于前端软删除该题的输入框
    }))
  );

  /**
   * 更新输入框内容
   * @param {number} index - 在 answers 数组中的索引
   * @param {string} value - 输入的数字字符串
   */
  const updateAnswer = (index, value) => {
    const newAnswers = [...answers];
    newAnswers[index].correctCount = value;
    setAnswers(newAnswers);
  };

  /**
   * 软删除某一行（不显示该题目的输入框）
   */
  const deleteRow = (index) => {
    const newAnswers = [...answers];
    newAnswers[index].deleted = true;
    setAnswers(newAnswers);
  };

  /**
   * 提交答案（保存到数据库）
   * 过滤掉已删除或未填写的项，并转换为后端需要的 DTO 格式
   */
  const handleSubmit = async () => {
    const validAnswers = answers
      .filter(a => !a.deleted && a.correctCount !== "")
      .map(a => ({
        questionCode: a.questionCode,
        correctCount: Number(a.correctCount)
      }));

    try {
      // 调用 API 执行新增或更新操作
      await upsertAnswers(examId, validAnswers);
      alert("提交成功");
    } catch (e) {
      console.error(e);
      alert("提交失败");
    }
  };

  /**
   * 计算成绩
   * 触发后端执行 IRT 算法或权重转换，并将返回的结果通过 setResult 更新父组件状态
   */
  const handleCalculate = async () => {
    try {
      const res = await calculate(examId);
      setResult(res.data);
    } catch (err) {
      console.error("计算失败", err);
    }
  };

  /**
   * 格式化显示题号名称
   * 将数据库简码（L1, R1...）转换为人类可读的 JLPT 试卷题号（问题1, 问题10...）
   */
  const getDisplayName = (code) => {

    // 1. 处理听力 (LS)
    if (code.startsWith("LS")) {
      const num = Number(code.slice(2));

      if (num <= 4) return `问题${num}`;
      if (num === 5) return "问题5（1）";
      if (num === 6) return "问题5（2）";
    }

    // 2. 处理词汇/语法 (L)
    if (code.startsWith("L")) {
      return `问题${code.slice(1)}`;
    }

    // 3. 处理阅读 (R) - JLPT 阅读通常接在语法题之后
    if (code.startsWith("R")) {
      // 假设语法题到问题9结束，阅读从问题10开始
      return `问题${Number(code.slice(1)) + 9}`;
    }

    return code;
  };

  /**
   * 逻辑分组渲染
   * 根据题号前缀，将表单拆分为“词汇·阅读”和“听力”两个视觉区域
   */
  const vocabReading = answers.filter(
    a => (a.questionCode.startsWith("L") && !a.questionCode.startsWith("LS")) || a.questionCode.startsWith("R")
  );

  const listening = answers.filter(
    a => a.questionCode.startsWith("LS")
  );

  /**
   * 单行渲染函数
   */
  const renderRow = (item) => {
    if (item.deleted) return null;

    const index = answers.findIndex(a => a.questionCode === item.questionCode);

    return (
      <div key={item.questionCode} className="d-flex mb-3 align-items-center justify-content-center">

        {/* 题号标签区域 */}
        <div style={{ width: "140px", fontWeight: "bold", fontSize: "18px"}}>
          {getDisplayName(item.questionCode)}
        </div>

        {/* 正确题数输入框 */}
        <input
          style={{ width: "400px", height: "55px"}}
          type="number"
          min={0}
          max={12}
          className="form-control me-2"
          value={item.correctCount}
          placeholder="正确数"
          onChange={(e) => updateAnswer(index, e.target.value)}
        />

        {/* 删除本行按钮 */}
        <button
          className="btn btn-danger"
          style={{padding: "6px 12px", fontSize: "14px"}}
          onClick={() => deleteRow(index)}
        >
          删除
        </button>

      </div>
    );
  };

  return (
     <div className="card p-3 mt-3">

      <h4>输入 / 更新答案</h4>

      {/* 模块 A：词汇与阅读 */}
      <div className="mt-3">
        <h5>词汇·阅读</h5>
        <hr />
        {vocabReading.map(renderRow)}
      </div>

      {/* 模块 B：听力 */}
      <div className="mt-3">
        <h5>听力</h5>
        <hr />
        {listening.map(renderRow)}
      </div>

      {/* 操作按钮 */}
      <div className="mt-3">
        <button className="btn btn-success me-2" onClick={handleSubmit}>
          保存
        </button>

        <button className="btn btn-warning" onClick={handleCalculate}>
          计算成绩
        </button>
      </div>

    </div>
  );
}

export default AnswerForm;