import { useEffect, useState } from "react";
import { calculate, getQuestionConfigs, upsertAnswers } from "../api";

const SECTION_LABELS = {
  language: "语言知识",
  reading: "阅读",
  listening: "听力",
};

/**
 * 根据当前考试等级，从后端读取题型配置并动态生成答题表单。
 * N1、N2、N3 共用同一套录入和计算流程。
 */
function AnswerForm({ examId, level, setResult }) {
  const [configs, setConfigs] = useState([]);
  const [answers, setAnswers] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadConfigs() {
      if (!level) return;
      setLoading(true);
      setError("");
      setConfigs([]);
      setAnswers({});

      try {
        const data = await getQuestionConfigs(level);
        if (cancelled) return;

        setConfigs(data);
        // 每个题型单独保存正确题数，初始留空；提交时空值按 0 分处理
        setAnswers(
          Object.fromEntries(data.map((item) => [item.questionCode, ""]))
        );
      } catch (err) {
        if (!cancelled) {
          console.error(err);
          setError(err.message || "题型配置加载失败");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadConfigs();
    return () => {
      cancelled = true;
    };
  }, [level, examId]);

  const updateAnswer = (questionCode, value, max) => {
    if (value !== "" && (!/^\d+$/.test(value) || Number(value) > max)) {
      return;
    }
    setAnswers((previous) => ({
      ...previous,
      [questionCode]: value,
    }));
  };

  // 只提交本次明确填写的题型，避免表单初始空白把数据库中已有答案覆盖成 0。
  // 如果需要把某题清零，请明确输入 0。
  const buildPayload = () =>
    configs
      .filter((config) => answers[config.questionCode] !== "")
      .map((config) => ({
        questionCode: config.questionCode,
        correctCount: Number(answers[config.questionCode]),
      }));

  const handleSubmit = async () => {
    if (configs.length === 0) {
      alert("题型配置尚未加载");
      return;
    }

    const payload = buildPayload();
    if (payload.length === 0) {
      alert("请至少填写一个题型的正确题数；需要清零的题型请填写 0");
      return;
    }

    setSubmitting(true);
    try {
      await upsertAnswers(examId, payload);
      alert("答案保存成功");
    } catch (err) {
      console.error(err);
      alert(err.response?.data?.message || err.message || "答案保存失败");
    } finally {
      setSubmitting(false);
    }
  };

  // 计算前先保存当前表单，避免用户直接点击“计算成绩”时使用数据库中的旧答案
  const handleCalculate = async () => {
    if (configs.length === 0) {
      alert("题型配置尚未加载");
      return;
    }

    const payload = buildPayload();
    if (payload.length === 0) {
      alert("请至少填写一个题型的正确题数；需要清零的题型请填写 0");
      return;
    }

    setSubmitting(true);
    try {
      await upsertAnswers(examId, payload);
      const res = await calculate(examId);
      setResult(res.data);
    } catch (err) {
      console.error("计算失败", err);
      alert(err.response?.data?.message || err.message || "计算失败，请检查后端服务");
    } finally {
      setSubmitting(false);
    }
  };

  const groupedConfigs = ["language", "reading", "listening"].map((sectionType) => ({
    sectionType,
    items: configs
      .filter((item) => item.sectionType?.toLowerCase() === sectionType)
      .sort((a, b) => a.sectionOrder - b.sectionOrder),
  }));

  if (loading) {
    return <div className="card p-3 mt-3">正在加载 {level} 题型配置……</div>;
  }

  if (error) {
    return <div className="alert alert-danger mt-3">{error}</div>;
  }

  return (
    <div className="card p-3 mt-3">
      <h4>输入 / 更新答案（{level}）</h4>

      {groupedConfigs.map(({ sectionType, items }) =>
        items.length > 0 ? (
          <div className="mt-3" key={sectionType}>
            <h5>{SECTION_LABELS[sectionType]}</h5>
            <hr />
            {items.map((item) => (
              <div
                key={item.questionCode}
                className="d-flex mb-3 align-items-center flex-wrap"
              >
                <div style={{ width: "190px", fontWeight: "bold" }}>
                  题型 {item.sectionOrder}（{item.questionCode}）
                </div>

                <input
                  style={{ width: "220px", maxWidth: "100%" }}
                  type="number"
                  min={0}
                  max={item.questionCount}
                  step={1}
                  inputMode="numeric"
                  className="form-control me-3"
                  value={answers[item.questionCode] ?? ""}
                  placeholder={`0 - ${item.questionCount}`}
                  onChange={(e) =>
                    updateAnswer(
                      item.questionCode,
                      e.target.value,
                      item.questionCount
                    )
                  }
                />

                <small className="text-muted">
                  共 {item.questionCount} 题，每题 {item.scorePerQuestion} 分
                </small>
              </div>
            ))}
          </div>
        ) : null
      )}

      <div className="mt-3">
        <button
          className="btn btn-success me-2"
          onClick={handleSubmit}
          disabled={submitting || configs.length === 0}
        >
          {submitting ? "处理中…" : "保存"}
        </button>

        <button
          className="btn btn-warning"
          onClick={handleCalculate}
          disabled={submitting || configs.length === 0}
        >
          {submitting ? "处理中…" : "保存并计算成绩"}
        </button>
      </div>
    </div>
  );
}

export default AnswerForm;
