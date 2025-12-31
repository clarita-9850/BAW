'use client';

import React from 'react';

interface FieldAuthorizedValueProps<T extends object> {
  data: T | null | undefined;
  field: keyof T;
  placeholder?: string;
  type?: 'text' | 'date' | 'datetime' | 'currency' | 'number' | 'badge';
  badgeColor?: string;
  className?: string;
  emptyClassName?: string;
}

/**
 * Component that displays a field value only if it exists in the data.
 * Used for field-level authorization where the backend filters out unauthorized fields.
 */
export function FieldAuthorizedValue<T extends object>({
  data,
  field,
  placeholder = '—',
  type = 'text',
  badgeColor,
  className = '',
  emptyClassName = 'text-muted',
}: FieldAuthorizedValueProps<T>) {
  // Check if field exists and has a value
  if (!data || !(field in data) || data[field] === undefined || data[field] === null) {
    return <span className={emptyClassName}>{placeholder}</span>;
  }

  const value = data[field];

  // Format based on type
  const formattedValue = (() => {
    switch (type) {
      case 'date':
        if (typeof value === 'string' || value instanceof Date) {
          const date = new Date(value as string | Date);
          return isNaN(date.getTime()) ? placeholder : date.toLocaleDateString();
        }
        return placeholder;

      case 'datetime':
        if (typeof value === 'string' || value instanceof Date) {
          const date = new Date(value as string | Date);
          return isNaN(date.getTime()) ? placeholder : date.toLocaleString();
        }
        return placeholder;

      case 'currency':
        if (typeof value === 'number') {
          return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
        }
        return placeholder;

      case 'number':
        if (typeof value === 'number') {
          return new Intl.NumberFormat('en-US').format(value);
        }
        return placeholder;

      case 'badge':
        const colorClass = badgeColor || getStatusColor(String(value));
        return (
          <span className={`badge ${colorClass}`}>
            {String(value)}
          </span>
        );

      default:
        return String(value);
    }
  })();

  if (type === 'badge') {
    return <>{formattedValue}</>;
  }

  return <span className={className}>{formattedValue}</span>;
}

/**
 * Get Bootstrap badge color class based on status value
 */
function getStatusColor(status: string): string {
  const statusLower = status.toLowerCase();

  if (['approved', 'active', 'completed', 'success'].some(s => statusLower.includes(s))) {
    return 'bg-success';
  }
  if (['pending', 'submitted', 'waiting', 'draft'].some(s => statusLower.includes(s))) {
    return 'bg-warning';
  }
  if (['rejected', 'failed', 'error', 'cancelled'].some(s => statusLower.includes(s))) {
    return 'bg-danger';
  }
  if (['processing', 'in_progress', 'in progress'].some(s => statusLower.includes(s))) {
    return 'bg-info';
  }
  return 'bg-secondary';
}

interface ActionButtonsProps {
  allowedActions: string[];
  onApprove?: () => void;
  onReject?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
  onSubmit?: () => void;
  onView?: () => void;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

/**
 * Component that renders action buttons based on allowed actions from the API
 */
export function ActionButtons({
  allowedActions,
  onApprove,
  onReject,
  onEdit,
  onDelete,
  onSubmit,
  onView,
  size = 'sm',
  className = '',
}: ActionButtonsProps) {
  const buttonClass = `btn btn-${size === 'sm' ? 'sm' : size === 'lg' ? 'lg' : ''}`;
  const actions = allowedActions.map(a => a.toLowerCase());

  return (
    <div className={`d-flex gap-2 ${className}`}>
      {actions.includes('read') && onView && (
        <button className={`${buttonClass} btn-secondary`} onClick={onView}>
          View
        </button>
      )}
      {actions.includes('update') && onEdit && (
        <button className={`${buttonClass} btn-primary`} onClick={onEdit}>
          Edit
        </button>
      )}
      {actions.includes('submit') && onSubmit && (
        <button className={`${buttonClass} btn-success`} onClick={onSubmit}>
          Submit
        </button>
      )}
      {actions.includes('approve') && onApprove && (
        <button className={`${buttonClass} btn-success`} onClick={onApprove}>
          Approve
        </button>
      )}
      {actions.includes('reject') && onReject && (
        <button className={`${buttonClass} btn-warning`} onClick={onReject}>
          Reject
        </button>
      )}
      {actions.includes('delete') && onDelete && (
        <button className={`${buttonClass} btn-danger`} onClick={onDelete}>
          Delete
        </button>
      )}
    </div>
  );
}

interface ConditionalFieldProps {
  data: any;
  field: string;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

/**
 * Component that conditionally renders children only if the field exists in data
 */
export function ConditionalField({ data, field, children, fallback = null }: ConditionalFieldProps) {
  if (!data || !(field in data) || data[field] === undefined) {
    return <>{fallback}</>;
  }
  return <>{children}</>;
}

interface FieldRowProps {
  data: any;
  field: string;
  label: string;
  type?: 'text' | 'date' | 'datetime' | 'currency' | 'number' | 'badge';
  placeholder?: string;
}

/**
 * Component that renders a label-value row only if the field is visible
 */
export function FieldRow({ data, field, label, type = 'text', placeholder = '—' }: FieldRowProps) {
  if (!data || !(field in data) || data[field] === undefined) {
    return null;
  }

  return (
    <div className="row mb-2">
      <div className="col-4 text-muted">{label}:</div>
      <div className="col-8">
        <FieldAuthorizedValue
          data={data}
          field={field as keyof typeof data}
          type={type}
          placeholder={placeholder}
        />
      </div>
    </div>
  );
}

export default FieldAuthorizedValue;
