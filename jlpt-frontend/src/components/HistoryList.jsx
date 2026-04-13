import { useEffect, useState } from "react";
import { getAllExams } from "../api";

/**
 * HistoryList 组件：展示历史考试记录列表
 * 功能：从后端拉取所有考试数据，计算平均分，并提供单条记录的回溯入口
 * @param {Function} onSelect - 当用户点击“查看”时，将该记录的 ID 传回父组件
 */
function HistoryList({ onSelect }) {
  // 状态：存储从后端获取的考试记录数组
  const [list, setList] = useState([]);

  /**
   * 初始化挂载：组件加载后立即从 API 获取数据
   */
  useEffect(() => {
    getAllExams().then(setList);
  }, []);

  /**
   * 聚合计算：历史平均分
   * 使用数组的 reduce 方法累加总分，并除以总长度
   * .toFixed(2) 确保结果保留两位小数，避免出现 95.33333... 的情况
   */
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
          {/* 遍历列表，动态生成表格行 */}
          {list.map((item) => (
            <tr key={item.id}>
              <td>{item.id}</td>
              <td>{item.level}</td>
              <td>
                {item.year}-{item.month}
              </td>
              <td>{item.totalScaledScore}</td>

              <td>
                {/* 查看详情：触发回调，让父页面跳转回主页查看该 ID 的明细 */}
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