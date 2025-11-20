import axios from 'axios';

// API Base URL
export const API_BASE_URL = 'http://localhost:8081/api';

// Create axios instance
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    } else if (error.response?.status === 403) {
      // Access denied - show user-friendly message
      console.error('Access denied:', error.response.data);
    } else if (!error.response) {
      // Network error or server not reachable
      console.error('Network error:', error.message);
    }
    return Promise.reject(error);
  }
);

// API Endpoints
export const API_ENDPOINTS = {
  timesheets: {
    list: '/timesheets',
    my: '/timesheets/my',
    create: '/timesheets',
    update: (id) => `/timesheets/${id}`,
    delete: (id) => `/timesheets/${id}`,
    approve: (id) => `/timesheets/${id}/approve`,
    reject: (id) => `/timesheets/${id}/reject`,
    submit: (id) => `/timesheets/${id}/submit`,
    pending: '/timesheets/pending',
    byStatus: (status) => `/timesheets/status/${status}`,
  },
  cases: {
    list: '/cases',
    create: '/cases',
    update: (id) => `/cases/${id}`,
    delete: (id) => `/cases/${id}`,
  },
  persons: {
    search: '/persons/search',
    create: '/persons',
    update: (id) => `/persons/${id}`,
    delete: (id) => `/persons/${id}`,
  },
  payments: {
    list: '/payments',
    create: '/payments',
    update: (id) => `/payments/${id}`,
    delete: (id) => `/payments/${id}`,
    process: (id) => `/payments/${id}/process`,
  },
};

export default apiClient;
