import { useState, useEffect } from "react";
import CreateExam from "./components/CreateExam";
import AnswerForm from "./components/AnswerForm";
import ResultView from "./components/ResultView";
import { getResult,calculate } from "./api";
import { useSearchParams } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { getExam } from "./api"; 

/**
 * App 组件：JLPT 评分系统的核心控制中心
 * 负责管理 examId、控制界面组件的显示隐藏以及与后端接口的实时同步
 */
function App() {
  // 核心状态：当前正在操作的考试 ID
  const [examId, setExamId] = useState("");
  // 成绩状态：存储从后端获取的计算结果
  const [result, setResult] = useState(null); 
  // 路由状态：处理 URL 参数（如 ?examId=1）
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  // 校验状态：标记当前输入的 examId 在数据库中是否存在
  const [examExists, setExamExists] = useState(false);

  /**
   * 监听 1：校验 ExamId 是否合法
   * 当用户手动输入 ID 或创建新考试后，触发校验，防止操作不存在的数据
   */
  useEffect(() => {
  if (!examId) return;

  async function checkExam() {
    try {
      const data = await getExam(examId);
      // 如果能查到数据，则标记为存在
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
}, [examId]); // 依赖项为 examId，一旦 ID 变动就重新检查
  
  /**
   * 监听 2：根据 examId 自动加载成绩
   * 实现“输入 ID 即可看到结果”的逻辑，并进行字段格式化映射
   */
  useEffect(() => {
    if (!examId) return;

    getResult(examId)
      .then((data) => {
        // 字段适配：将后端实体类的命名（scaledScore）映射为前端组件需要的简洁命名
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

  /**
   * 监听 3：初始化页面
   * 从浏览器的 URL 参数中读取 examId，支持通过链接直接进入特定考试
   */
  useEffect(() => {
  const idFromUrl = searchParams.get("examId");
  if (idFromUrl) {
    setExamId(idFromUrl);
  }
}, []); // 仅在组件首次加载时运行一次

  return (
    <div className="container mt-4">
      <h2>JLPT 成绩系统</h2>

      {/* 1. ID 输入区：允许用户手动输入已有考试 ID */}
      <input
        type="number"
        className="form-control mb-3"
        placeholder="输入 examId"
        value={examId}
        onChange={(e) => setExamId(e.target.value)}
      />

      {/* 2. 创建区：调用组件创建新考试，成功后会通过 setExamId 更新父组件 */}
      <CreateExam setExamId={setExamId} />

      {/* 3. 业务操作区：只有当 ID 存在且校验通过时才显示答题卡和结果视图 */}
      {examExists && examId && (
        <>
          {/* 答题表单：提交后会更新 result 状态 */}
          <AnswerForm examId={examId} setResult={setResult} />

          {/* 结果展示：包含删除功能，删除后触发 onDeleted 回调清空页面 */}
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

      {/* 4. 异常提示：当输入了 ID 但数据库查无此人时显示 */}
      {examId && !examExists && (
      <div className="text-danger mt-2">
        该考试不存在，请先创建
      </div>
      )}

      {/* 5. 导航区：跳转到历史成绩列表页 */}
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