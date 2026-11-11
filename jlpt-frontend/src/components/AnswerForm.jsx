import { useEffect, useState } from "react";
import {
  calculate,
  getExamAnswers,
  getQuestionConfigs,
  upsertAnswers,
} from "../api";

const SECTION_LABELS = {
  language: "语言知识",
  reading: "阅读",
  listening: "听力",
};

/**
 * 根据考试等级加载题型配置，并回显该场考试已经保存的小题分数。
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

    async function loadFormData() {
      if (!level || !examId) return;

      setLoading(true);
      setError("");
      setConfigs([]);
      setAnswers({});

      try {
        // 同时读取当前等级的题型配置和该场考试已保存的答题明细
        const [configData, savedAnswers] = await Promise.all([
          getQuestionConfigs(level),
          getExamAnswers(examId),
        ]);

        if (cancelled) return;

        // 先将所有题型初始化为空，再用数据库中已有的正确题数回填
        const answerMap = Object.fromEntries(
          configData.map((item) => [item.questionCode, ""])
        );

        savedAnswers.forEach((item) => {
          if (Object.prototype.hasOwnProperty.call(answerMap, item.questionCode)) {
            answerMap[item.questionCode] = String(item.correctCount ?? 0);
          }
        });

        setConfigs(configData);
        setAnswers(answerMap);
      } catch (err) {
        if (!cancelled) {
          console.error(err);
          setError(err.message || "考试题型或已保存答案加载失败");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadFormData();
    return () => {
      cancelled = true;
    };
  }, [level, examId]);

  const updateAnswer = (questionCode, value, max) => {
    if (value !== "" && (!/^\\d+$/.test(value) || Number(value) > max)) {
      return;
    }
    setAnswers((previous) => ({
      ...previous,
      [questionCode]: value,
    }));
  };

  // 只提交明确填写的题型，空白题型不会覆盖数据库中已有答案。
  // 若要将某题型清零，请明确输入 0。
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
    return <div className="card p-3 mt-3">正在加载 {level} 题型配置和已保存答案……</div>;
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
