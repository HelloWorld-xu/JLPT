import axios from "axios";

/**
 * 后端基础服务地址
 * 开发环境下通常指向本地 Spring Boot 端口
 */
const BASE_URL = "http://localhost:8080"; 

/**
 * 1. 创建考试 (Create)
 * 发送等级、年份、月份信息，由后端生成 Exam 主表记录
 * @param {Object} data - { year, month, level }
 */
export const createExam = (data) => {
  return axios.post(`${BASE_URL}/exam`, data);
};

/**
 * 2. 批量提交/更新答案 (Upsert)
 * 将用户在 AnswerForm 中填写的原始分数据发送至后端
 * 后端逻辑：已存在则更新，不存在则插入
 * @param {string} examId 
 * @param {Array} answers - 题目答案列表
 */
export const upsertAnswers = (examId, answers) => {
  return axios.put(`${BASE_URL}/exam/${examId}/upsert`, answers);
};

/**
 * 3. 触发分数计算 (Calculate)
 * 通知后端执行分数转换逻辑（Raw -> Scaled）并判定是否通过
 * @param {string} examId 
 */
export const calculate = (examId) => {
  return axios.post(`${BASE_URL}/exam/${examId}/calculate`);
};

/**
 * 4. 删除考试 (Delete)
 * 根据 ID 彻底移除一场考试及其关联的所有答题数据
 * @param {string} examId 
 */
export const deleteExam = (examId) => {
  return axios.delete(`${BASE_URL}/exam/${examId}`);
};

/**
 * 5. 获取考试计算结果 (Get Result)
 * 用于在页面刷新或重新进入时，重新加载已存的得分数据
 */
export async function getResult(id) {
  const res = await fetch(`${BASE_URL}/exam/${id}/get`);

  if (!res.ok) return null;

  return res.json();
}

/**
 * 6. 获取所有历史记录 (History)
 * 从后端拉取该用户（或系统内）所有的考试概览列表
 */
export async function getAllExams() {
  const res = await fetch(`${BASE_URL}/exam/history`);
  return res.json();
}

/**
 * 7. 校验/获取考试基础信息 (Check/Get Exam)
 * 在 App.js 中用于校验输入的 examId 是否真实存在于数据库中
 */
export async function getExam(id) {
  const res = await fetch(`${BASE_URL}/exam/${id}/get`);

  if (!res.ok) return null;

  return res.json();
}