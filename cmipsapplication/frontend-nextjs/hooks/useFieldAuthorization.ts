import { useMemo } from 'react';

/**
 * Response structure from API endpoints that include field-level authorization
 */
export interface FieldAuthorizedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  numberOfElements: number;
  allowedActions: string[];
  first: boolean;
  last: boolean;
  empty: boolean;
}

/**
 * Hook to manage field-level authorization from API responses
 */
export function useFieldAuthorization<T extends object>(response: FieldAuthorizedResponse<T> | null) {
  const allowedActions = useMemo(() => {
    return response?.allowedActions || [];
  }, [response?.allowedActions]);

  const canRead = useMemo(() => {
    return allowedActions.includes('read');
  }, [allowedActions]);

  const canCreate = useMemo(() => {
    return allowedActions.includes('create');
  }, [allowedActions]);

  const canUpdate = useMemo(() => {
    return allowedActions.includes('update');
  }, [allowedActions]);

  const canDelete = useMemo(() => {
    return allowedActions.includes('delete');
  }, [allowedActions]);

  const canSubmit = useMemo(() => {
    return allowedActions.includes('submit');
  }, [allowedActions]);

  const canApprove = useMemo(() => {
    return allowedActions.includes('approve');
  }, [allowedActions]);

  const canReject = useMemo(() => {
    return allowedActions.includes('reject');
  }, [allowedActions]);

  /**
   * Check if a specific action is allowed
   */
  const canPerform = (action: string): boolean => {
    return allowedActions.includes(action.toLowerCase());
  };

  /**
   * Check if a field exists in the data (wasn't filtered out by backend)
   */
  const hasField = (data: T | null | undefined, fieldName: keyof T): boolean => {
    if (!data) return false;
    return fieldName in data && data[fieldName] !== undefined;
  };

  /**
   * Get a field value safely, returning a placeholder if not available
   */
  const getFieldValue = <K extends keyof T>(
    data: T | null | undefined,
    fieldName: K,
    placeholder: string = '—'
  ): T[K] | string => {
    if (!data) return placeholder;
    if (!(fieldName in data) || data[fieldName] === undefined) {
      return placeholder;
    }
    return data[fieldName];
  };

  /**
   * Get display value for a field, with formatting
   */
  const getDisplayValue = (
    data: T | null | undefined,
    fieldName: keyof T,
    options?: {
      placeholder?: string;
      type?: 'date' | 'datetime' | 'currency' | 'number' | 'text';
      locale?: string;
    }
  ): string => {
    const { placeholder = '—', type = 'text', locale = 'en-US' } = options || {};

    if (!data || !(fieldName in data) || data[fieldName] === undefined || data[fieldName] === null) {
      return placeholder;
    }

    const value = data[fieldName];

    switch (type) {
      case 'date':
        if (typeof value === 'string' || value instanceof Date) {
          return new Date(value as string | Date).toLocaleDateString(locale);
        }
        return placeholder;

      case 'datetime':
        if (typeof value === 'string' || value instanceof Date) {
          return new Date(value as string | Date).toLocaleString(locale);
        }
        return placeholder;

      case 'currency':
        if (typeof value === 'number') {
          return new Intl.NumberFormat(locale, { style: 'currency', currency: 'USD' }).format(value);
        }
        return placeholder;

      case 'number':
        if (typeof value === 'number') {
          return new Intl.NumberFormat(locale).format(value);
        }
        return placeholder;

      default:
        return String(value);
    }
  };

  return {
    allowedActions,
    canRead,
    canCreate,
    canUpdate,
    canDelete,
    canSubmit,
    canApprove,
    canReject,
    canPerform,
    hasField,
    getFieldValue,
    getDisplayValue,
    data: response?.content || [],
    totalElements: response?.totalElements || 0,
    isEmpty: response?.empty ?? true,
  };
}

/**
 * Utility function to check if a field is present in the response object
 * Use this for conditional rendering of fields
 */
export function isFieldVisible<T extends object>(data: T | null | undefined, fieldName: keyof T): boolean {
  if (!data) return false;
  return fieldName in data && data[fieldName] !== undefined;
}

/**
 * Get safe field value with optional default
 */
export function safeFieldValue<T extends object, K extends keyof T>(
  data: T | null | undefined,
  fieldName: K,
  defaultValue: T[K] | string = '—'
): T[K] | string {
  if (!data) return defaultValue;
  if (!(fieldName in data) || data[fieldName] === undefined) {
    return defaultValue;
  }
  return data[fieldName];
}

export default useFieldAuthorization;
