import apiClient from '../api';

export interface AccessibleCountiesResponse {
  status: string;
  accessibleCounties: string[];
  totalCounties: number;
}

export const districtCountyService = {
  async getAccessibleCounties(userRole: string): Promise<AccessibleCountiesResponse> {
    const response = await apiClient.get<AccessibleCountiesResponse>(
      `/county/accessible-counties/${encodeURIComponent(userRole)}`
    );
    return response.data;
  },
};


