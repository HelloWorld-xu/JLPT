import axios from "axios";

const BASE_URL = "http://localhost:8080";

// 创建考试
export const createExam = (data) => axios.post(`${BASE_URL}/exam`, data);

// 按等级获取题型配置，供答题表单动态生成输入项
export async function getQuestionConfigs(level) {
  const res = await fetch(`${BASE_URL}/exam/config/${encodeURIComponent(level)}`);
  if (!res.ok) {
    const message = await res.text();
    throw new Error(message || "获取题型配置失败");
  }
  return res.json();
}

// 保存/更新答案
export const upsertAnswers = (examId, answers) =>
  axios.put(`${BASE_URL}/exam/${examId}/upsert`, answers);

// 计算成绩
export const calculate = (examId) =>
  axios.post(`${BASE_URL}/exam/${examId}/calculate`);

// 删除考试
export const deleteExam = (examId) =>
  axios.delete(`${BASE_URL}/exam/${examId}`);

// 获取考试实体及已计算成绩
export async function getResult(id) {
  const res = await fetch(`${BASE_URL}/exam/${id}/get`);
  if (!res.ok) return null;
  return res.json();
}

// 获取历史考试记录
export async function getAllExams() {
  const res = await fetch(`${BASE_URL}/exam/history`);
  if (!res.ok) throw new Error("获取历史记录失败");
  return res.json();
}

// 获取考试基础信息
export async function getExam(id) {
  const res = await fetch(`${BASE_URL}/exam/${id}/get`);
  if (!res.ok) return null;
  return res.json();
}


// 获取指定考试已保存的各题型答题明细
export async function getExamAnswers(examId) {
  const res = await fetch(`${BASE_URL}/exam/${examId}/answers`);
  if (!res.ok) {
    const message = await res.text();
    throw new Error(message || "获取已保存答案失败");
  }
  return res.json();
}
