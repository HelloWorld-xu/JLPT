import { useState } from "react";
import { createExam } from "../api";

/**
 * CreateExam 组件：负责新建考试记录
 * 提供年份输入、月份选择和等级选择的功能
 * @param {Function} setExamId - 父组件传递的回调函数，用于在创建成功后同步当前的考试ID
 */
function CreateExam({ setExamId }) {
  const [year, setYear] = useState("");
  const [month, setMonth] = useState("");
  const [level, setLevel] = useState("N2");

  /**
   * 处理创建逻辑
   * 包含前端基础校验、调用 API 以及更新全局状态
   */
  const handleCreate = async () => {
    // 1. 基础校验：确保关键信息已填写
    if(!year || !month) {
      alert("请输入年份和月份")
      return;
    }
    // 2. 调用后端接口创建考试
    // 后端返回的 res.data 通常是数据库生成的自增 ID
    const res = await createExam({ year, month, level });
    alert("创建成功，ID：" + res.data);
    // 3. 将新 ID 传递给父组件，触发 App.js 的后续加载逻辑
    setExamId(res.data);
  };

  return (
    <div className="card p-3 mt-3">
      <h4>创建考试</h4>

      {/* 年份输入：手动输入年份，如 2024 */}
      <input
        className="form-control mb-2"
        placeholder="请输入年份"
        onChange={(e) => setYear(e.target.value)}
      />

      {/* 月份选择：JLPT 固定在 7 月和 12 月举行 */}
      <select
        className="form-control mb-2"
        value={month}
        onChange={(e) => setMonth(e.target.value)}
      >
        <option value="0" selected hidden>请选择月份</option>
        <option value="7">7月</option>
        <option value="12">12月</option>
      </select>

      {/* 等级选择：N1 - N3 */}
      <select
        className="form-control mb-2"
        value={level}
        onChange={(e) => setLevel(e.target.value)}
      >
        <option value="N1">N1</option>
        <option value="N2">N2</option>
        <option value="N3">N3</option>
      </select>

      {/* 提交按钮 */}
      <button className="btn btn-primary" onClick={handleCreate}>
        创建
      </button>
    </div>
  );
}

export default CreateExam;
