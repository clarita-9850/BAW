import apiClient from '../api';

export interface ReportRequest {
  userRole: string;
  reportType: string;
  startDate?: string;
  endDate?: string;
  userCounty?: string;
  countyId?: string; // Backend also accepts countyId
  districtId?: string;
  page?: number;
  pageSize?: number;
  statusFilter?: string[];
  providerFilter?: string[];
  projectFilter?: string[];
}

export interface ReportDataEnvelope {
  records: any[];
  totalCount: number;
  totalRecords: number;
}

export interface ReportResponse {
  reportId?: string;
  status: string;
  message?: string;
  generatedAt?: string;
  data: ReportDataEnvelope;
  totalPages?: number;
  currentPage?: number;
  pageSize?: number;
  visibleFields?: string[];
}

export const reportService = {
  /**
   * Generate real-time report
   */
  async generateReport(request: ReportRequest): Promise<ReportResponse> {
    // Map userCounty to countyId for backend compatibility
    const backendRequest = {
      ...request,
      countyId: request.userCounty || request.countyId,
      userCounty: undefined, // Remove userCounty to avoid confusion
    };
    // Remove undefined fields
    Object.keys(backendRequest).forEach(key => {
      if (backendRequest[key as keyof typeof backendRequest] === undefined) {
        delete backendRequest[key as keyof typeof backendRequest];
      }
    });
    const response = await apiClient.post('/pipeline/generate-report', backendRequest);
    return normalizeReportResponse(response.data);
  },

  /**
   * Extract data with field masking
   */
  async extractData(request: ReportRequest): Promise<ReportResponse> {
    const response = await apiClient.post('/pipeline/extract', request);
    return normalizeReportResponse(response.data);
  },

  /**
   * Download PDF report with enhanced format
   */
  async downloadPDFReport(request: ReportRequest): Promise<Blob> {
    const response = await apiClient.post(
      '/reports/generate-pdf',
      request,
      {
        responseType: 'blob',
      }
    );
    return response.data;
  },
};

function normalizeReportResponse(payload: any): ReportResponse {
  const rawRecords = Array.isArray(payload?.data)
    ? payload.data
    : Array.isArray(payload?.data?.records)
    ? payload.data.records
    : [];

  const totalRecords =
    payload?.totalRecords ??
    payload?.totalCount ??
    payload?.data?.totalRecords ??
    payload?.data?.totalCount ??
    rawRecords.length;

  const totalCount =
    payload?.totalCount ??
    payload?.data?.totalCount ??
    totalRecords ??
    rawRecords.length;

  return {
    reportId: payload?.reportId,
    status: payload?.status ?? 'SUCCESS',
    message: payload?.message,
    generatedAt: payload?.generatedAt,
    totalPages: payload?.totalPages,
    currentPage: payload?.currentPage,
    pageSize: payload?.pageSize,
    visibleFields: payload?.visibleFields,
    data: {
      records: rawRecords,
      totalCount,
      totalRecords,
    },
  };
}

