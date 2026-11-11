import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getExamStatistics } from "../api";
import "./StatisticsPage.css";

const LEVELS = ["N1", "N2", "N3"];

function formatScore(value) {
  return value === null || value === undefined ? "--" : value;
}

function formatChange(value) {
  if (value === null || value === undefined) return "--";
  if (value > 0) return `+${value}`;
  return String(value);
}

function ChangeValue({ value }) {
  if (value === null || value === undefined) return <span className="text-muted">--</span>;
  const className = value > 0 ? "text-success" : value < 0 ? "text-danger" : "text-secondary";
  return <strong className={className}>{formatChange(value)}</strong>;
}

function LineChart({ title, subtitle, data, series, maxValue }) {
  const width = Math.max(760, data.length * 90 + 70);
  const height = 280;
  const left = 48;
  const right = 22;
  const top = 18;
  const bottom = 48;
  const plotWidth = width - left - right;
  const plotHeight = height - top - bottom;
  const ticks = [0, 1, 2, 3, 4].map((item) => (maxValue / 4) * item);
  const x = (index) =>
    data.length <= 1 ? left + plotWidth / 2 : left + (plotWidth * index) / (data.length - 1);
  const y = (value) => top + plotHeight - (Math.max(0, Number(value) || 0) / maxValue) * plotHeight;

  return (
    <section className="card shadow-sm border-0 stats-card h-100">
      <div className="card-body">
        <div className="d-flex flex-wrap justify-content-between align-items-start mb-2">
          <div>
            <h5 className="card-title mb-1">{title}</h5>
            <div className="small text-muted">{subtitle}</div>
          </div>
          <div className="d-flex flex-wrap gap-3 small">
            {series.map((item) => (
              <span key={item.key} className="d-inline-flex align-items-center gap-1">
                <span className="stats-legend-dot" style={{ backgroundColor: item.color }} />
                {item.label}
              </span>
            ))}
          </div>
        </div>

        <div className="stats-chart-scroll">
          <svg
            className="stats-chart"
            style={{ minWidth: `${width}px` }}
            viewBox={`0 0 ${width} ${height}`}
            role="img"
            aria-label={title}
          >
            {ticks.map((tick) => (
              <g key={tick}>
                <line
                  x1={left}
                  x2={width - right}
                  y1={y(tick)}
                  y2={y(tick)}
                  stroke="#e9ecef"
                  strokeDasharray="4 4"
                />
                <text x={left - 10} y={y(tick) + 4} textAnchor="end" fontSize="11" fill="#6c757d">
                  {Math.round(tick)}
                </text>
              </g>
            ))}
            <line x1={left} x2={left} y1={top} y2={top + plotHeight} stroke="#adb5bd" />
            <line x1={left} x2={width - right} y1={top + plotHeight} y2={top + plotHeight} stroke="#adb5bd" />

            {series.map((item) => {
              const points = data
                .map((entry, index) => `${x(index)},${y(entry[item.key])}`)
                .join(" ");
              return (
                <g key={item.key}>
                  {data.length > 1 && (
                    <polyline
                      points={points}
                      fill="none"
                      stroke={item.color}
                      strokeWidth="3"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  )}
                  {data.map((entry, index) => (
                    <g key={entry.examId || `${item.key}-${index}`}>
                      <circle
                        cx={x(index)}
                        cy={y(entry[item.key])}
                        r="5"
                        fill={item.color}
                        stroke="#fff"
                        strokeWidth="2"
                      >
                        <title>{`${entry.label}: ${item.label} ${entry[item.key]}分`}</title>
                      </circle>
                    </g>
                  ))}
                </g>
              );
            })}

            {data.map((entry, index) => (
              <text
                key={entry.examId || entry.label}
                x={x(index)}
                y={height - 16}
                textAnchor="middle"
                fontSize="11"
                fill="#6c757d"
              >
                {entry.label}
              </text>
            ))}
          </svg>
        </div>
        {data.length === 1 && (
          <div className="small text-muted text-center">目前只有一场已计算成绩的考试，继续记录后即可形成趋势。</div>
        )}
      </div>
    </section>
  );
}

function StatisticsPage() {
  const navigate = useNavigate();
  const [level, setLevel] = useState("N2");
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadStatistics() {
      setLoading(true);
      setError("");
      setStatistics(null);
      try {
        const data = await getExamStatistics(level);
        if (!cancelled) setStatistics(data);
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "获取成绩统计失败，请检查后端服务和数据库迁移。");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadStatistics();
    return () => {
      cancelled = true;
    };
  }, [level]);

  const trend = statistics?.trend || [];
  const latest = trend.length ? trend[trend.length - 1] : null;
  const previous = trend.length > 1 ? trend[trend.length - 2] : null;
  const sectionSeries = useMemo(
    () => [
      { key: "languageScore", label: "语言知识", color: "#0d6efd" },
      { key: "readingScore", label: "阅读", color: "#198754" },
      { key: "listeningScore", label: "听力", color: "#fd7e14" },
    ],
    []
  );

  const comparison = statistics?.recentComparison;
  const extremes = statistics?.sectionExtremes;

  return (
    <main className="container py-4 stats-page">
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div className="text-start">
          <h2 className="mb-1">JLPT 成绩分析</h2>
          <p className="text-muted mb-0">查看各等级的历史成绩、进步趋势与分项表现</p>
        </div>
        <button className="btn btn-outline-secondary" onClick={() => navigate("/")}>
          返回首页
        </button>
      </div>

      <div className="d-flex flex-wrap align-items-center gap-2 mb-4">
        <span className="fw-semibold me-2">考试等级</span>
        <div className="btn-group" role="group" aria-label="选择 JLPT 等级">
          {LEVELS.map((item) => (
            <button
              key={item}
              type="button"
              className={`btn ${level === item ? "btn-primary" : "btn-outline-primary"}`}
              onClick={() => setLevel(item)}
              aria-pressed={level === item}
            >
              {item}
            </button>
          ))}
        </div>
        <span className="small text-muted ms-sm-2">各等级独立统计，不混合计算</span>
      </div>

      {loading && <div className="alert alert-info">正在加载 {level} 成绩统计...</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      {!loading && !error && statistics && statistics.examCount === 0 && (
        <div className="card border-0 shadow-sm">
          <div className="card-body text-center py-5">
            <h5>暂无 {level} 已计算的考试成绩</h5>
            <p className="text-muted mb-3">
              尚未计算成绩的考试不会参与统计。完成一场考试并点击“计算成绩”后，这里就会显示分析结果。
            </p>
            <button className="btn btn-primary" onClick={() => navigate("/")}>去录入考试成绩</button>
          </div>
        </div>
      )}

      {!loading && !error && statistics && statistics.examCount > 0 && (
        <>
          <div className="row g-3 mb-4">
            <div className="col-6 col-xl-3">
              <div className="card border-0 shadow-sm stats-kpi h-100">
                <div className="card-body text-start">
                  <div className="text-muted small">考试次数</div>
                  <div className="stats-kpi-value">{statistics.examCount}</div>
                  <div className="small text-muted">已计算成绩</div>
                </div>
              </div>
            </div>
            <div className="col-6 col-xl-3">
              <div className="card border-0 shadow-sm stats-kpi h-100">
                <div className="card-body text-start">
                  <div className="text-muted small">历史最高分</div>
                  <div className="stats-kpi-value">{formatScore(statistics.highestScore)}</div>
                  <div className="small text-muted">满分 180</div>
                </div>
              </div>
            </div>
            <div className="col-6 col-xl-3">
              <div className="card border-0 shadow-sm stats-kpi h-100">
                <div className="card-body text-start">
                  <div className="text-muted small">历史平均分</div>
                  <div className="stats-kpi-value">{formatScore(statistics.averageScore)}</div>
                  <div className="small text-muted">满分 180</div>
                </div>
              </div>
            </div>
            <div className="col-6 col-xl-3">
              <div className="card border-0 shadow-sm stats-kpi h-100">
                <div className="card-body text-start">
                  <div className="text-muted small">合格率</div>
                  <div className="stats-kpi-value">{statistics.passRate}%</div>
                  <div className="small text-muted">{statistics.passCount} 场合格</div>
                </div>
              </div>
            </div>
          </div>

          <div className="row g-3 mb-4">
            <div className="col-12">
              <LineChart
                title="历次总分趋势"
                subtitle={`${level} 总分，满分 180 分`}
                data={trend}
                series={[{ key: "totalScore", label: "总分", color: "#0d6efd" }]}
                maxValue={180}
              />
            </div>
            <div className="col-12">
              <LineChart
                title="三大板块成绩趋势"
                subtitle="使用标准分比较，单项满分均为 60 分"
                data={trend}
                series={sectionSeries}
                maxValue={60}
              />
            </div>
          </div>

          <div className="row g-3">
            <div className="col-12 col-xl-6">
              <section className="card border-0 shadow-sm stats-card h-100">
                <div className="card-body">
                  <h5 className="card-title">最近两次考试成绩变化</h5>
                  {comparison ? (
                    <>
                      <p className="small text-muted">
                        {comparison.previousLabel} → {comparison.currentLabel}
                      </p>
                      <div className="table-responsive">
                        <table className="table align-middle mb-0">
                          <thead>
                            <tr>
                              <th>统计项目</th>
                              <th className="text-end">上一次</th>
                              <th className="text-end">最近一次</th>
                              <th className="text-end">变化</th>
                            </tr>
                          </thead>
                          <tbody>
                            {[
                              ["语言知识", comparison.previousLanguageScore, comparison.currentLanguageScore, comparison.languageChange],
                              ["阅读", comparison.previousReadingScore, comparison.currentReadingScore, comparison.readingChange],
                              ["听力", comparison.previousListeningScore, comparison.currentListeningScore, comparison.listeningChange],
                              ["总分", comparison.previousTotalScore, comparison.currentTotalScore, comparison.totalChange],
                            ].map(([label, before, after, change]) => (
                              <tr key={label}>
                                <td>{label}</td>
                                <td className="text-end">{formatScore(before)}</td>
                                <td className="text-end">{formatScore(after)}</td>
                                <td className="text-end"><ChangeValue value={change} /></td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </>
                  ) : (
                    <div className="text-muted py-4 text-center">
                      目前只有一场已计算成绩的考试，暂无两次考试对比。
                    </div>
                  )}
                </div>
              </section>
            </div>

            <div className="col-12 col-xl-6">
              <section className="card border-0 shadow-sm stats-card h-100">
                <div className="card-body">
                  <h5 className="card-title">各板块历史最高分与最低分</h5>
                  <p className="small text-muted">基于当前等级的全部已计算考试，标准分满分 60</p>
                  <div className="table-responsive">
                    <table className="table align-middle mb-0">
                      <thead>
                        <tr>
                          <th>板块</th>
                          <th className="text-end">历史最高分</th>
                          <th className="text-end">历史最低分</th>
                        </tr>
                      </thead>
                      <tbody>
                        {[
                          ["语言知识", extremes?.language],
                          ["阅读", extremes?.reading],
                          ["听力", extremes?.listening],
                        ].map(([label, range]) => (
                          <tr key={label}>
                            <td>{label}</td>
                            <td className="text-end text-success fw-semibold">{formatScore(range?.highest)}</td>
                            <td className="text-end">{formatScore(range?.lowest)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </section>
            </div>
          </div>

          <div className="small text-muted mt-3">
            当前统计范围：{level} · {statistics.examCount} 场已计算成绩的考试
            {latest && previous && (
              <span> · 最近一次考试：{latest.label}，上一次：{previous.label}</span>
            )}
          </div>
        </>
      )}
    </main>
  );
}

export default StatisticsPage;
