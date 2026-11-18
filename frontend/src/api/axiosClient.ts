import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const raw = localStorage.getItem('bharatmart_auth');
  if (raw) {
    const auth = JSON.parse(raw);
    if (auth?.accessToken) {
      config.headers.Authorization = `Bearer ${auth.accessToken}`;
    }
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      const raw = localStorage.getItem('bharatmart_auth');
      if (raw) {
        const auth = JSON.parse(raw);
        if (auth?.refreshToken) {
          try {
            const refreshRes = await axios.post(`${API_BASE_URL}/auth/refresh`, {
              refreshToken: auth.refreshToken,
            });
            const updated = refreshRes.data.data;
            localStorage.setItem('bharatmart_auth', JSON.stringify(updated));
            original.headers.Authorization = `Bearer ${updated.accessToken}`;
            return api(original);
          } catch {
            localStorage.removeItem('bharatmart_auth');
            window.location.href = '/login';
          }
        }
      }
    }
    return Promise.reject(error);
  }
);
