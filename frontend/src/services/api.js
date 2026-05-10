import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token to every request automatically
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('ember_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Auto logout on 401 (but not for auth endpoints)
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      // Don't auto-logout on auth endpoints (login/register failures are expected)
      const isAuthEndpoint = err.config?.url?.includes('/api/auth/');
      if (!isAuthEndpoint) {
        localStorage.removeItem('ember_token');
        localStorage.removeItem('ember_user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  }
);

// ─── Auth ────────────────────────────────────────────
export const authApi = {
  register: (data) => api.post('/api/auth/register', data),
  login: (data) => api.post('/api/auth/login', data),
};

// ─── Habits ──────────────────────────────────────────
export const habitApi = {
  getAll: (userId) => api.get(`/api/users/${userId}/habits`),
  create: (userId, data) => api.post(`/api/users/${userId}/habits`, data),
  update: (userId, habitId, data) => api.patch(`/api/users/${userId}/habits/${habitId}`, data),

  complete: (userId, habitId) => api.patch(`/api/users/${userId}/habits/${habitId}/complete`),
  skip: (userId, habitId) => api.patch(`/api/users/${userId}/habits/${habitId}/skip`),
   reset: (userId, habitId) => api.patch(`/api/users/${userId}/habits/${habitId}/reset`),
};

// ─── Check-ins ───────────────────────────────────────
export const checkinApi = {
  create: (userId, data) => api.post(`/api/users/${userId}/checkins`, data),
  getAll: (userId) => api.get(`/api/users/${userId}/checkins`),
  getLatest: (userId) => api.get(`/api/users/${userId}/checkins/today`),
};

// ─── Autopsy ─────────────────────────────────────────
export const autopsyApi = {
  get: (userId) => api.get(`/api/users/${userId}/autopsy`),
};

export const nudgeApi = {
  // POST /api/users/{id}/ai/nudge
  getNudge: (userId, body) =>
    api.post(`/api/users/${userId}/ai/nudge`, body),

  // GET /api/users/{id}/ai/autopsy-insight
  getAutopsyInsight: (userId) =>
    api.get(`/api/users/${userId}/ai/autopsy-insight`),
};



export default api;
