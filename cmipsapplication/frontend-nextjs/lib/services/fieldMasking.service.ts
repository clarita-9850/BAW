import apiClient from '../api';

export interface FieldMaskingRule {
  fieldName: string;
  maskingType: 'NONE' | 'HIDDEN' | 'PARTIAL_MASK' | 'HASH_MASK' | 'ANONYMIZE' | 'AGGREGATE';
  accessLevel: 'FULL_ACCESS' | 'MASKED_ACCESS' | 'HIDDEN_ACCESS';
  maskingPattern?: string;
  reportType?: string;
  description?: string;
  enabled?: boolean;
}

export interface FieldMetadata {
  name: string;
  displayName: string;
  type: string;
  description?: string;
}

export interface FieldMaskingInterface {
  userRole: string;
  rules: FieldMaskingRule[];
  selectedFields: string[];
  availableFields: FieldMetadata[];
  source?: string;
}

export const fieldMaskingService = {
  /**
   * Get full masking interface (rules + selected fields + metadata)
   */
  async getInterface(userRole: string, reportType: string): Promise<FieldMaskingInterface> {
    const response = await apiClient.get<{ interface?: FieldMaskingInterface; rules?: FieldMaskingRule[]; selectedFields?: string[]; availableFields?: FieldMetadata[] }>(
      `/field-masking/interface/${encodeURIComponent(userRole)}`,
      { params: { reportType } }
    );

    if (response.data?.interface) {
      return {
        userRole: response.data.interface.userRole || userRole,
        rules: response.data.interface.rules || [],
        selectedFields: response.data.interface.selectedFields || [],
        availableFields: response.data.interface.availableFields || [],
        source: response.data.interface.source,
      };
    }

    return {
      userRole,
      rules: response.data?.rules || [],
      selectedFields: response.data?.selectedFields || [],
      availableFields: response.data?.availableFields || [],
      source: 'LEGACY',
    };
  },

  /**
   * Update masking rules for a role
   */
  async updateRules(
    userRole: string,
    rules: FieldMaskingRule[],
    selectedFields?: string[]
  ): Promise<{ status: string; message: string }> {
    const response = await apiClient.post('/field-masking/update-rules', {
      userRole,
      rules,
      selectedFields,
    });
    return response.data;
  },

  /**
   * Fetch all available fields that can be masked
   */
  async getAvailableFields(): Promise<FieldMetadata[]> {
    const response = await apiClient.get<{ fields: FieldMetadata[] }>('/field-masking/available-fields');
    return response.data.fields || [];
  },

  /**
   * Get available roles
   */
  async getAvailableRoles(): Promise<string[]> {
    const response = await apiClient.get<{ roles: string[] }>('/field-masking/available-roles');
    return response.data.roles || ['ADMIN', 'SUPERVISOR', 'CASE_WORKER', 'PROVIDER', 'RECIPIENT'];
  },
};

