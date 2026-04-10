import { useEffect, useState } from "react";
import { getAllExams } from "../api";

function HistoryList({ onSelect }) {
  const [list, setList] = useState([]);

  useEffect(() => {
    getAllExams().then(setList);
  }, []);

  // 平均分计算
  const avg =
    list.length === 0
      ? 0
      : (
          list.reduce((sum, item) => sum + (item.totalScaledScore || 0), 0) /
          list.length
        ).toFixed(2);

  return (
    <div className="card p-3 mt-3">
      <h4>历史成绩</h4>

      <p>历史平均分：{avg}</p>

      <table className="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>等级</th>
            <th>时间</th>
            <th>总分</th>
            <th>操作</th>
          </tr>
        </thead>

        <tbody>
          {list.map((item) => (
            <tr key={item.id}>
              <td>{item.id}</td>
              <td>{item.level}</td>
              <td>
                {item.year}-{item.month}
              </td>
              <td>{item.totalScaledScore}</td>

              <td>
                <button
                  className="btn btn-sm btn-primary"
                  onClick={() => onSelect(item.id)}
                >
                  查看
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default HistoryList;