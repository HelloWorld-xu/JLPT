import axios from "axios";

const BASE_URL = "http://localhost:8080"; // 后端地址

export const createExam = (data) => {
  return axios.post(`${BASE_URL}/exam`, data);
};

export const upsertAnswers = (examId, answers) => {
  return axios.put(`${BASE_URL}/exam/${examId}/upsert`, answers);
};

export const calculate = (examId) => {
  return axios.post(`${BASE_URL}/exam/${examId}/calculate`);
};

export const deleteExam = (examId) => {
  return axios.delete(`${BASE_URL}/exam/${examId}`);
};

export async function getResult(id) {
  const res = await fetch(`${BASE_URL}/exam/${id}/get`);

  if (!res.ok) return null;

  return res.json();
}

export async function getAllExams() {
  const res = await fetch(`${BASE_URL}/exam/history`);
  return res.json();
}

export async function getExam(id) {
  const res = await fetch(`${BASE_URL}/exam/${id}/get`);

  if (!res.ok) return null;

  return res.json();
}