import { useState, useEffect } from "react";
import CreateExam from "./components/CreateExam";
import AnswerForm from "./components/AnswerForm";
import ResultView from "./components/ResultView";
import HistoryList from "./components/HistoryList";
import { getResult,calculate } from "./api";
import { useSearchParams } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { getExam } from "./api"; 

function App() {
  const [examId, setExamId] = useState("");
  const [result, setResult] = useState(null); 
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [examExists, setExamExists] = useState(false);

  
  //检查id是否存在
  useEffect(() => {
  if (!examId) return;

  async function checkExam() {
    try {
      const data = await getExam(examId);
      if (data) {
        setExamExists(true);
      } else {
        setExamExists(false);
      }

    } catch (e) {
      console.error(e);
      setExamExists(false);
    }
  }

  checkExam();
}, [examId]);
  
  // 根据 examId 自动加载成绩
  useEffect(() => {
    if (!examId) return;

    getResult(examId)
      .then((data) => {
        const formattedData = {
          ...data,
          languageScore: data.languageScaledScore,
          readingScore: data.readingScaledScore,
          listeningScore: data.listeningScaledScore,
          totalScore: data.totalScaledScore,
        };
        setResult(formattedData);
      })
      .catch((err) => {
        console.error(err);
        setResult(null);
      });
  }, [examId]);

  useEffect(() => {
  const idFromUrl = searchParams.get("examId");
  if (idFromUrl) {
    setExamId(idFromUrl);
  }
}, []);

  return (
    <div className="container mt-4">
      <h2>JLPT 成绩系统</h2>

      {/* 输入 examId */}
      <input
        type="number"
        className="form-control mb-3"
        placeholder="输入 examId"
        value={examId}
        onChange={(e) => setExamId(e.target.value)}
      />

      <CreateExam setExamId={setExamId} />

      {examExists && examId && (
        <>
          <AnswerForm examId={examId} setResult={setResult} />

          <ResultView
            result={result}
            examId={examId}
            onDeleted={() => {
              setExamId("");
              setResult(null);
            }}
          />
        </>
      )}

      {examId && !examExists && (
      <div className="text-danger mt-2">
        该考试不存在，请先创建
      </div>
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