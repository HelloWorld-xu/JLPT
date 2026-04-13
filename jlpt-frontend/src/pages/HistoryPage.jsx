import HistoryList from "../components/HistoryList";
import { useNavigate } from "react-router-dom";

/**
 * HistoryPage 组件：历史成绩查询页
 * 作为一个独立的路由页面，展示用户所有参加过的 JLPT 模拟记录
 */
function HistoryPage() {
  // 获取路由跳转钩子
  const navigate = useNavigate();

  return (
    <div className="container mt-4">
      <h2>历史成绩</h2>

      {/* 返回操作：点击跳转回主页（App.jsx） */}
      <button className="btn btn-secondary mb-3" onClick={() => navigate("/")}>
        返回首页
      </button>

      {/* 历史列表组件：
        onSelect 属性是一个回调函数。
        逻辑：当用户在列表中点击某一项时，拿到该记录的 ID，
        利用路由跳转回首页，并通过 URL 参数 (?examId=xxx) 传递该 ID。
      */}
      <HistoryList
        onSelect={(id) => {
          // 这里的策略是：通过 URL 传参实现跨页面的状态共享
          // App.jsx 中的 useEffect 会监听 URL 变化并自动加载该 ID 的详情
          navigate(`/?examId=${id}`);
        }}
      />
    </div>
  );
}

export default HistoryPage;