import axios from 'axios';

export const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance with default config
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor to handle 401 errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid, redirect to login
      localStorage.removeItem('jwt_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const API_ENDPOINTS = {
  auth: {
    login: `${API_BASE_URL}/auth/login`,
    ssoExchange: `${API_BASE_URL}/auth/sso/exchange`,
    health: `${API_BASE_URL}/auth/health`,
  },
  timesheets: {
    list: `${API_BASE_URL}/timesheets`,
    create: `${API_BASE_URL}/timesheets`,
    get: (id: number) => `${API_BASE_URL}/timesheets/${id}`,
    update: (id: number) => `${API_BASE_URL}/timesheets/${id}`,
    delete: (id: number) => `${API_BASE_URL}/timesheets/${id}`,
    byStatus: (status: string) => `${API_BASE_URL}/timesheets/status/${status}`,
    pending: `${API_BASE_URL}/timesheets/pending`,
  },
  policies: {
    list: `${API_BASE_URL}/policies`,
    get: (id: number) => `${API_BASE_URL}/policies/${id}`,
    create: `${API_BASE_URL}/policies`,
    update: (id: number) => `${API_BASE_URL}/policies/${id}`,
    delete: (id: number) => `${API_BASE_URL}/policies/${id}`,
    byRole: (role: string) => `${API_BASE_URL}/policies/role/${role}`,
    byResource: (resource: string) => `${API_BASE_URL}/policies/resource/${resource}`,
    test: `${API_BASE_URL}/policies/test`,
  },
  admin: {
    users: `${API_BASE_URL}/admin/users`,
    user: (id: number) => `${API_BASE_URL}/admin/users/${id}`,
    usersByRole: (role: string) => `${API_BASE_URL}/admin/users/role/${role}`,
    updateUserRole: (id: number) => `${API_BASE_URL}/admin/users/${id}/role`,
    deactivateUser: (id: number) => `${API_BASE_URL}/admin/users/${id}/deactivate`,
    activateUser: (id: number) => `${API_BASE_URL}/admin/users/${id}/activate`,
    stats: `${API_BASE_URL}/admin/stats`,
  },
};
