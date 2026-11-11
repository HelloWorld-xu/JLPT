import { useState, useEffect } from "react";
import CreateExam from "./components/CreateExam";
import AnswerForm from "./components/AnswerForm";
import ResultView from "./components/ResultView";
import { getResult, getExam } from "./api";
import { useSearchParams, useNavigate } from "react-router-dom";

function App() {
  const [examId, setExamId] = useState("");
  const [exam, setExam] = useState(null);
  const [result, setResult] = useState(null);
  const [examExists, setExamExists] = useState(false);
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  // examId 改变时重新校验考试记录，并取得该考试所属等级
  useEffect(() => {
    let cancelled = false;

    async function checkExam() {
      if (!examId) {
        setExam(null);
        setExamExists(false);
        return;
      }

      setExam(null);
      setExamExists(false);

      try {
        const data = await getExam(examId);
        if (cancelled) return;
        setExam(data);
        setExamExists(Boolean(data));
      } catch (err) {
        if (!cancelled) {
          console.error(err);
          setExam(null);
          setExamExists(false);
        }
      }
    }

    checkExam();
    return () => {
      cancelled = true;
    };
  }, [examId]);

  // 切换考试 ID 后加载该考试已经计算的成绩
  useEffect(() => {
    let cancelled = false;

    async function loadResult() {
      if (!examId) {
        setResult(null);
        return;
      }

      try {
        const data = await getResult(examId);
        if (cancelled) return;

        if (!data) {
          setResult(null);
          return;
        }

        setResult({
          ...data,
          languageScore: data.languageScaledScore,
          readingScore: data.readingScaledScore,
          listeningScore: data.listeningScaledScore,
          totalScore: data.totalScaledScore,
        });
      } catch (err) {
        if (!cancelled) {
          console.error(err);
          setResult(null);
        }
      }
    }

    loadResult();
    return () => {
      cancelled = true;
    };
  }, [examId]);

  // 支持通过 ?examId=1 的链接直接打开指定考试
  useEffect(() => {
    const idFromUrl = searchParams.get("examId");
    if (idFromUrl) setExamId(idFromUrl);
  }, [searchParams]);

  return (
    <div className="container mt-4">
      <h2>JLPT 成绩系统</h2>

      <input
        type="number"
        min="1"
        className="form-control mb-3"
        placeholder="输入 examId"
        value={examId}
        onChange={(e) => setExamId(e.target.value)}
      />

      <CreateExam setExamId={setExamId} />

      {examExists && examId && exam && (
        <>
          <AnswerForm
            key={examId}
            examId={examId}
            level={exam.level}
            setResult={setResult}
          />

          <ResultView
            result={result}
            examId={examId}
            onDeleted={() => {
              setExamId("");
              setExam(null);
              setExamExists(false);
              setResult(null);
            }}
          />
        </>
      )}

      {examId && !examExists && (
        <div className="text-danger mt-2">该考试不存在，请先创建</div>
      )}

      <button
        className="btn btn-info mb-3"
        onClick={() => navigate("/history")}
      >
        查看历史成绩
      </button>
    </div>
  );
}

export default App;
