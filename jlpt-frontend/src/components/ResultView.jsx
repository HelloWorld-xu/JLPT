import { deleteExam } from "../api";

/**
 * ResultView 组件：成绩报告单
 * 功能：展示各板块标准分、总分以及最终的合格判定，并允许用户删除当前考试记录
 * @param {Object} result - 包含各项得分和通过状态的对象
 * @param {string|number} examId - 当前考试的唯一标识 ID
 * @param {Function} onDeleted - 删除成功后的父组件回调函数（用于重置页面状态）
 */
function ResultView({ result, examId, onDeleted }) {
  // 如果当前还没有计算出结果，则不渲染任何内容
  if (!result) return null;

  /**
   * 处理删除考试逻辑
   * 包含二次确认机制，防止用户误触导致答题数据丢失
   */
  const handleDelete = async () => {
    if (!examId) {
      alert("没有 examId");
      return;
    }

    // 浏览器原生确认弹窗
    if (!window.confirm("确定删除这个考试吗？")) return;

    try {
      // 调用 API 执行后端逻辑删除（通常会级联删除对应的答题明细）
      await deleteExam(examId);
      alert("删除成功");

      // 执行父组件传递的回调（在 App.jsx 中通常用于清空 examId 和 result 状态）
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

      {/* 得分明细区域 */}
      <p>语言：{result.languageScore}</p>
      <p>阅读：{result.readingScore}</p>
      <p>听力：{result.listeningScore}</p>
      <p>总分：{result.totalScore}</p>

      {/* 合格判定展示 */}
      <h5>
        结果：
        {result.pass ? (
          <span className="text-success">通过</span>
        ) : (
          <span className="text-danger">不通过</span>
        )}
      </h5>

      {/* 删除按钮 */}
      <button className="btn btn-danger mt-3" onClick={handleDelete}>
        删除考试
      </button>
    </div>
  );
}

export default ResultView;