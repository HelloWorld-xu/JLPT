import { useState } from "react";
import { upsertAnswers, calculate } from "../api";

function AnswerForm({ examId, setResult }) {
  
  //题目列表
  const QUESTION_LIST = [
    "L1","L2","L3","L4","L5","L6","L7","L8","L9",
    "R1","R2","R3","R4","R5",
    "LS1","LS2","LS3","LS4","LS5","LS6"
  ];

  //初始化
const [answers, setAnswers] = useState(
  QUESTION_LIST.map(code => ({
    questionCode: code,
    correctCount: "",
    deleted: false
  }))
);

  //更新输入
  const updateAnswer = (index, value) => {
    const newAnswers = [...answers];
    newAnswers[index].correctCount = value;
    setAnswers(newAnswers);
  };

  //删除
  const deleteRow = (index) => {
    const newAnswers = [...answers];
    newAnswers[index].deleted = true;
    setAnswers(newAnswers);
  };

  //提交
  const handleSubmit = async () => {
    const validAnswers = answers
      .filter(a => !a.deleted && a.correctCount !== "")
      .map(a => ({
        questionCode: a.questionCode,
        correctCount: Number(a.correctCount)
      }));

    try {
      await upsertAnswers(examId, validAnswers);

      alert("提交成功");
    } catch (e) {
      console.error(e);
      alert("提交失败");
    }
  };

  //计算
  const handleCalculate = async () => {
    try {
      const res = await calculate(examId);
      setResult(res.data);
    } catch (err) {
      console.error("计算失败", err);
    }
  };

  // 显示名称（JLPT结构）
  const getDisplayName = (code) => {

    // 听力
    if (code.startsWith("LS")) {
      const num = Number(code.slice(2));

      if (num <= 4) return `问题${num}`;
      if (num === 5) return "问题5（1）";
      if (num === 6) return "问题5（2）";
    }

    // 词汇
    if (code.startsWith("L")) {
      return `问题${code.slice(1)}`;
    }

    // 阅读（接在L后）
    if (code.startsWith("R")) {
      return `问题${Number(code.slice(1)) + 9}`;
    }

    return code;
  };

  // 分组
  const vocabReading = answers.filter(
    a => (a.questionCode.startsWith("L") && !a.questionCode.startsWith("LS")) || a.questionCode.startsWith("R")
  );

  const listening = answers.filter(
    a => a.questionCode.startsWith("LS")
  );

  //  渲染行
  const renderRow = (item) => {
    if (item.deleted) return null;

    const index = answers.findIndex(a => a.questionCode === item.questionCode);

    return (
      <div key={item.questionCode} className="d-flex mb-3 align-items-center justify-content-center">

        {/* 题号 */}
        <div style={{ width: "140px", fontWeight: "bold", fontSize: "18px"}}>
          {getDisplayName(item.questionCode)}
        </div>

        {/* 输入 */}
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

        {/* 删除 */}
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

      {/* 词汇·阅读 */}
      <div className="mt-3">
        <h5>词汇·阅读</h5>
        <hr />
        {vocabReading.map(renderRow)}
      </div>

      {/* 听力 */}
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