import axios from 'axios';
import { DashboardFilterRequest, DashboardResponse, FilterOptions } from '../types/dashboard';

const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const dashboardApi = {
  getData: async (filters?: DashboardFilterRequest): Promise<DashboardResponse> => {
    const response = await api.post<DashboardResponse>('/dashboard/data', filters || {});
    return response.data;
  },

  getFilters: async (): Promise<FilterOptions> => {
    const response = await api.get<FilterOptions>('/dashboard/filters');
    return response.data;
  },
};

export default api;
