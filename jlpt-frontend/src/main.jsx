import './index.css'           // 引入全局自定义样式
import App from './App.jsx'    // 引入主应用组件（主页）
import 'bootstrap/dist/css/bootstrap.min.css' // 引入 Bootstrap 样式库，提供快速 UI 布局
import ReactDOM from "react-dom/client";      // 引入 React 18 的渲染引擎
import HistoryPage from "./pages/HistoryPage"; // 引入历史记录页面组件
import { BrowserRouter, Routes, Route } from "react-router-dom"; // 引入路由核心组件

/**
 * 项目根渲染逻辑
 * 使用 React 18 的 createRoot API 将应用挂载到 index.html 中 id 为 'root' 的元素上
 */
ReactDOM.createRoot(document.getElementById("root")).render(
  // BrowserRouter: 启用 HTML5 历史记录 API 的路由模式，使 URL 看起来像正常的路径
  <BrowserRouter>
    {/* Routes: 路由容器，负责匹配当前 URL 并渲染对应的组件 */}
    <Routes>
      
      {/* 路由配置表：
          path: 浏览器地址栏的路径
          element: 对应的 React 组件
      */}

      {/* 根路径 (首页): 展示考试创建、答题和当前分数查询 */}
      <Route path="/" element={<App />} />

      {/* 历史路径: 展示用户过去所有的考试记录列表 */}
      <Route path="/history" element={<HistoryPage />} />

    </Routes>
  </BrowserRouter>
);