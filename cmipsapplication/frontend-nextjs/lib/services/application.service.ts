import apiClient from '../api';

export interface ApplicationForm {
  appFirstName?: string;
  appLastName?: string;
  appMiddleName?: string;
  appSuffix?: string;
  residencyRequirement?: string;
  residenceAddressType?: string;
  sameAsResidence?: boolean;
  mailingAddressType?: string;
  phoneNumber?: string;
  emailAddress?: string;
  spokenLanguage?: string;
  writtenLanguage?: string;
  additionalInfo?: string;
  [key: string]: any;
}

export interface CaseForm {
  ihssReferralData?: string;
  interpreterAvailable?: boolean;
  assignedWorker?: string;
  clientIndexNumber?: string;
  [key: string]: any;
}

export interface ApplicationData extends ApplicationForm, CaseForm {
  ssn?: string;
  firstName?: string;
  lastName?: string;
  status?: string;
  createdAt?: string;
}

export interface ApplicationSaveResponse {
  success: boolean;
  id?: number;
  message?: string;
  error?: string;
}

export const applicationService = {
  /**
   * Save application data
   */
  async saveApplication(applicationData: ApplicationData): Promise<ApplicationSaveResponse> {
    const response = await apiClient.post<ApplicationSaveResponse>('/application/save', applicationData);
    return response.data;
  },

  /**
   * Get application by ID
   */
  async getApplicationById(applicationId: number): Promise<ApplicationData> {
    const response = await apiClient.get<ApplicationData>(`/application/${applicationId}`);
    return response.data;
  },

  /**
   * Update application
   */
  async updateApplication(applicationId: number, applicationData: Partial<ApplicationData>): Promise<ApplicationData> {
    const response = await apiClient.put<ApplicationData>(`/application/${applicationId}`, applicationData);
    return response.data;
  },

  /**
   * Get application statistics
   */
  async getApplicationStats(): Promise<any> {
    const response = await apiClient.get('/application/stats');
    return response.data;
  },
};

