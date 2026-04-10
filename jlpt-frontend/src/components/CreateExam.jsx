import { useState } from "react";
import { createExam } from "../api";

function CreateExam({ setExamId }) {
  const [year, setYear] = useState("");
  const [month, setMonth] = useState("");
  const [level, setLevel] = useState("N2");

  const handleCreate = async () => {
    if(!year || !month) {
      alert("请输入年份和月份")
      return;
    }
    const res = await createExam({ year, month, level });
    alert("创建成功，ID：" + res.data);
    setExamId(res.data);
  };

  return (
    <div className="card p-3 mt-3">
      <h4>创建考试</h4>

      <input
        className="form-control mb-2"
        placeholder="请输入年份"
        onChange={(e) => setYear(e.target.value)}
      />

      <select
        className="form-control mb-2"
        value={month}
        onChange={(e) => setMonth(e.target.value)}
      >
        <option value="0" selected hidden>请选择月份</option>
        <option value="7">7月</option>
        <option value="12">12月</option>
      </select>

      <select
        className="form-control mb-2"
        value={level}
        onChange={(e) => setLevel(e.target.value)}
      >
        <option value="N1">N1</option>
        <option value="N2">N2</option>
        <option value="N3">N3</option>
      </select>

      <button className="btn btn-primary" onClick={handleCreate}>
        创建
      </button>
    </div>
  );
}

export default CreateExam;
