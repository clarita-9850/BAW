import apiClient from '../api';

export interface AnalyticsFilters {
  districtId?: string;
  county?: string;
  statusFilter?: string;
  priorityLevel?: string;
  serviceType?: string;
  createdAfter?: string;
  createdBefore?: string;
}

export interface AnalyticsSummary {
  totalRecords: number;
  totalHours: number;
  totalAmount: number;
  pendingApprovals: number;
  avgApprovalTimeHours: number;
  uniqueProviders: number;
  uniqueRecipients: number;
  avgHours?: number;
  avgAmount?: number;
}

export interface DemographicData {
  gender: Array<{ gender: string; count: number }>;
  ethnicity: Array<{ ethnicity: string; count: number }>;
  ageGroup: Array<{ ageGroup: string; count: number }>;
}

export interface AdhocFiltersResponse {
  // Provider demographics
  providerGenders?: string[];
  providerEthnicities?: string[];
  providerAgeGroups?: string[];
  // Recipient demographics
  recipientGenders?: string[];
  recipientEthnicities?: string[];
  recipientAgeGroups?: string[];
  // Combined (backward compatibility)
  genders?: string[];
  ethnicities?: string[];
  ageGroups?: string[];
  // Standard filters
  locations?: string[];
  departments?: string[];
  statuses?: string[];
}

export interface AdhocStatsResponse {
  status: string;
  stats: {
    totalRecords: number;
    totalHours: number;
    totalAmount: number;
    avgHours: number;
    avgAmount: number;
  };
}

export interface AdhocBreakdownsResponse {
  status: string;
  gender?: Record<string, number>;
  ethnicity?: Record<string, number>;
  ageGroup?: Record<string, number>;
}

export interface AdhocCrosstabResponse {
  status: string;
  genderEthnicity?: Array<{ gender: string; ethnicity: string; count: number }>;
  genderAge?: Array<{ gender: string; ageGroup: string; count: number }>;
  ethnicityAge?: Array<{ ethnicity: string; ageGroup: string; count: number }>;
}

export interface AdhocDataResponse {
  status: string;
  columns: string[];
  rows: Array<Record<string, any>>;
  count: number;
  limit: number;
  lastUpdated?: string;
}

export const analyticsService = {
  /**
   * Get summary statistics
   */
  async getSummary(filters?: AnalyticsFilters): Promise<AnalyticsSummary> {
    const response = await apiClient.get<AnalyticsSummary>('/analytics/summary', {
      params: filters,
    });
    return response.data;
  },

  /**
   * Get demographic breakdowns
   */
  async getDemographics(filters?: AnalyticsFilters): Promise<DemographicData> {
    const response = await apiClient.get<DemographicData>('/analytics/demographics', {
      params: filters,
    });
    return response.data;
  },

  /**
   * Get trend data
   */
  async getTrends(filters?: AnalyticsFilters): Promise<any> {
    const response = await apiClient.get('/analytics/trends', {
      params: filters,
    });
    return response.data;
  },

  async getAdhocFilters(): Promise<AdhocFiltersResponse> {
    const response = await apiClient.get('/analytics/adhoc-filters');
    return response.data;
  },

  async getAdhocStats(params: Record<string, string>): Promise<AdhocStatsResponse> {
    const response = await apiClient.get('/analytics/adhoc-stats', { params });
    return response.data;
  },

  async getAdhocBreakdowns(params: Record<string, string>): Promise<AdhocBreakdownsResponse> {
    const response = await apiClient.get('/analytics/adhoc-breakdowns', { params });
    return response.data;
  },

  async getAdhocCrosstabs(params: Record<string, string>): Promise<AdhocCrosstabResponse> {
    const response = await apiClient.get('/analytics/adhoc-crosstab', { params });
    return response.data;
  },

  async getAdhocData(params: Record<string, string | number>): Promise<AdhocDataResponse> {
    const response = await apiClient.get('/analytics/adhoc-data', { params });
    return response.data;
  },
};

