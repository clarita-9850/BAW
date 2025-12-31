import apiClient from '../api';

export interface BIReportRequest {
  userRole: string;
  reportType: string;
  targetSystem: string;
  dataFormat: string;
  chunkSize?: number;
  priority?: number;
  startDate?: string;
  endDate?: string;
  countyId?: string;
  districtId?: string;
}

export interface JobStatus {
  jobId: string;
  status: 'QUEUED' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  progress: number;
  totalRecords: number;
  processedRecords: number;
  errorMessage?: string;
  createdAt?: string;
  startedAt?: string;
  completedAt?: string;
  estimatedCompletionTime?: string;
  userRole: string;
  reportType: string;
  targetSystem?: string;
  dataFormat?: string;
  jobSource?: string;
}

export interface JobResult {
  jobId: string;
  status: string;
  resultPath: string;
  totalRecords: number;
  processedRecords: number;
  dataFormat: string;
  completedAt: string;
}

export const jobService = {
  /**
   * Create a new batch report job
   */
  async createJob(request: BIReportRequest): Promise<{ jobId: string; status: string; message: string }> {
    const response = await apiClient.post('/bi/reports/generate', request);
    return response.data;
  },

  /**
   * Get job status by ID
   */
  async getJobStatus(jobId: string): Promise<JobStatus> {
    const response = await apiClient.get(`/bi/jobs/${jobId}/status`);
    return response.data.jobStatus;
  },

  /**
   * Get job result (if completed)
   */
  async getJobResult(jobId: string): Promise<JobResult> {
    const response = await apiClient.get(`/bi/jobs/${jobId}/result`);
    return response.data.result;
  },

  /**
   * Download report file
   */
  async downloadReport(jobId: string): Promise<Blob> {
    const response = await apiClient.get(`/bi/jobs/${jobId}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  /**
   * Download job (alias for downloadReport)
   */
  async downloadJob(jobId: string): Promise<Blob> {
    return this.downloadReport(jobId);
  },

  /**
   * Cancel a job
   */
  async cancelJob(jobId: string): Promise<{ status: string; message: string }> {
    const response = await apiClient.post(`/bi/jobs/${jobId}/cancel`);
    return response.data;
  },

  /**
   * Get all jobs
   */
  async getAllJobs(): Promise<JobStatus[]> {
    const response = await apiClient.get('/bi/jobs/status/ALL');
    return response.data.jobs || [];
  },

  /**
   * Get jobs by status
   */
  async getJobsByStatus(status: string): Promise<JobStatus[]> {
    const response = await apiClient.get(`/bi/jobs/status/${status}`);
    return response.data.jobs || [];
  },
};

