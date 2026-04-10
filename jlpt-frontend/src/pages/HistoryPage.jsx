import HistoryList from "../components/HistoryList";
import { useNavigate } from "react-router-dom";

function HistoryPage() {
  const navigate = useNavigate();

  return (
    <div className="container mt-4">
      <h2>历史成绩</h2>

      <button className="btn btn-secondary mb-3" onClick={() => navigate("/")}>
        返回首页
      </button>

      <HistoryList
        onSelect={(id) => {
          // 点击某条 → 跳回首页并带 examId
          navigate(`/?examId=${id}`);
        }}
      />
    </div>
  );
}

export default HistoryPage;