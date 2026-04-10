import { deleteExam } from "../api";

function ResultView({ result, examId, onDeleted }) {
  if (!result) return null;

  const handleDelete = async () => {
    if (!examId) {
      alert("没有 examId");
      return;
    }

    if (!window.confirm("确定删除这个考试吗？")) return;

    try {
      await deleteExam(examId);
      alert("删除成功");

      // 通知父组件（清空页面）
      if (onDeleted) {
        onDeleted();
      }
    } catch (err) {
      console.error(err);
      alert("删除失败");
    }
  };

  return (
    <div className="card p-3 mt-3">
      <h4>成绩结果</h4>

      <p>语言：{result.languageScore}</p>
      <p>阅读：{result.readingScore}</p>
      <p>听力：{result.listeningScore}</p>
      <p>总分：{result.totalScore}</p>

      <h5>
        结果：
        {result.pass ? (
          <span className="text-success">通过</span>
        ) : (
          <span className="text-danger">不通过</span>
        )}
      </h5>

      {/* 🔥 删除按钮 */}
      <button className="btn btn-danger mt-3" onClick={handleDelete}>
        删除考试
      </button>
    </div>
  );
}

export default ResultView;