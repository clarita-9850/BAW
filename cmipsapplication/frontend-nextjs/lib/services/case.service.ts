import apiClient from '../api';

export interface CaseCreationRequest {
  personData?: PersonData;
  personId?: number;
  countyCode: string;
  caseNotes?: string;
  disasterPreparednessCode?: string;
  caseOpenedDate?: string;
}

export interface PersonData {
  firstName: string;
  middleName?: string;
  lastName: string;
  suffix?: string;
  ssn: string;
  dateOfBirth: string;
  gender?: string;
  ethnicity?: string;
  preferredSpokenLanguage?: string;
  preferredWrittenLanguage?: string;
  primaryPhone?: string;
  secondaryPhone?: string;
  email?: string;
  residenceAddressLine1: string;
  residenceAddressLine2?: string;
  residenceCity: string;
  residenceState?: string;
  residenceZip: string;
  mailingAddressLine1?: string;
  mailingAddressLine2?: string;
  mailingCity?: string;
  mailingState?: string;
  mailingZip?: string;
  mailingSameAsResidence?: boolean;
  countyOfResidence: string;
  guardianConservatorName?: string;
  guardianConservatorAddress?: string;
  guardianConservatorPhone?: string;
  disasterPreparednessCode?: string;
}

export interface CaseCreationResponse {
  success: boolean;
  message?: string;
  caseId?: number;
  cmipsCaseNumber?: string;
  legacyCaseNumber?: string;
  personId?: number;
  caseStatus?: string;
  assignedCaseworkerId?: string;
  caseOpenedDate?: string;
  createdAt?: string;
}

export interface Case {
  caseId: number;
  personId: number;
  cmipsCaseNumber: string;
  legacyCaseNumber?: string;
  caseStatus: string;
  countyCode: string;
  assignedCaseworkerId: string;
  caseOpenedDate?: string;
  caseClosedDate?: string;
  caseNotes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export const caseService = {
  /**
   * Create a new case
   */
  async createCase(caseData: CaseCreationRequest): Promise<CaseCreationResponse> {
    const response = await apiClient.post<CaseCreationResponse>('/case/create', caseData);
    return response.data;
  },

  /**
   * Get case by ID
   */
  async getCaseById(caseId: number): Promise<Case> {
    const response = await apiClient.get<Case>(`/case/${caseId}`);
    return response.data;
  },

  /**
   * Get cases for a person
   */
  async getCasesByPerson(personId: number): Promise<Case[]> {
    const response = await apiClient.get<Case[]>(`/case/person/${personId}`);
    return response.data;
  },

  /**
   * Get cases for current caseworker
   */
  async getMyCases(): Promise<Case[]> {
    const response = await apiClient.get<Case[]>('/case/my-cases');
    return response.data;
  },

  /**
   * Get active cases for current caseworker
   */
  async getMyActiveCases(): Promise<Case[]> {
    const response = await apiClient.get<Case[]>('/case/my-cases/active');
    return response.data;
  },

  /**
   * Activate a case
   */
  async activateCase(caseId: number): Promise<Case> {
    const response = await apiClient.post<Case>(`/case/${caseId}/activate`);
    return response.data;
  },

  /**
   * Close a case
   */
  async closeCase(caseId: number): Promise<Case> {
    const response = await apiClient.post<Case>(`/case/${caseId}/close`);
    return response.data;
  },

  /**
   * Verify SSN against external validation service
   */
  async verifySsn(firstName: string, ssn: string): Promise<SsnVerificationResponse> {
    const response = await apiClient.post<SsnVerificationResponse>('/case/verify-ssn', {
      firstName,
      ssn,
    });
    return response.data;
  },
};

export interface SsnVerificationResponse {
  valid: boolean;
  status?: string;
  message?: string;
  matchedFirstName?: string;
  matchedSsn?: string;
}

