import { useState } from "react";
import { createExam } from "../api";

function CreateExam({ setExamId }) {
  const [year, setYear] = useState("");
  const [month, setMonth] = useState("");
  const [level, setLevel] = useState("N2");
  const [submitting, setSubmitting] = useState(false);

  const handleCreate = async () => {
    const yearNumber = Number(year);
    if (!Number.isInteger(yearNumber) || yearNumber < 2000 || yearNumber > 2100) {
      alert("请输入正确的年份");
      return;
    }
    if (!["7", "12"].includes(month)) {
      alert("请选择考试月份");
      return;
    }
    if (!["N1", "N2", "N3"].includes(level)) {
      alert("请选择有效的 JLPT 等级");
      return;
    }

    setSubmitting(true);
    try {
      const res = await createExam({
        year: yearNumber,
        month: Number(month),
        level,
      });
      alert(`创建成功，ID：${res.data}`);
      setExamId(String(res.data));
    } catch (err) {
      console.error(err);
      alert(err.response?.data?.message || err.message || "创建考试失败");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="card p-3 mt-3">
      <h4>创建考试</h4>

      <input
        type="number"
        min="2000"
        max="2100"
        className="form-control mb-2"
        placeholder="请输入年份"
        value={year}
        onChange={(e) => setYear(e.target.value)}
      />

      <select
        className="form-control mb-2"
        value={month}
        onChange={(e) => setMonth(e.target.value)}
      >
        <option value="" disabled>请选择月份</option>
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

      <button
        className="btn btn-primary"
        onClick={handleCreate}
        disabled={submitting}
      >
        {submitting ? "创建中…" : "创建"}
      </button>
    </div>
  );
}

export default CreateExam;
