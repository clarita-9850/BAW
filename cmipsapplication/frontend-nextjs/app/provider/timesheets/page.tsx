'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/api';
import { FieldAuthorizedValue, ActionButtons } from '@/components/FieldAuthorizedValue';
import { isFieldVisible } from '@/hooks/useFieldAuthorization';

type Timesheet = {
  id: number;
  userId?: string;
  employeeId?: string;
  employeeName?: string;
  department?: string;
  location?: string;
  payPeriodStart?: string;
  payPeriodEnd?: string;
  regularHours?: number;
  overtimeHours?: number;
  totalHours?: number;
  status?: string;
  comments?: string;
  supervisorComments?: string;
  approvedBy?: string;
  submittedAt?: string;
  createdAt?: string;
  updatedAt?: string;
};

type TimesheetResponse = {
  content: Timesheet[];
  totalElements: number;
  numberOfElements: number;
  allowedActions: string[];
};

export default function ProviderTimesheetsPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [timesheets, setTimesheets] = useState<Timesheet[]>([]);
  const [allowedActions, setAllowedActions] = useState<string[]>([]);
  const [filter, setFilter] = useState<string>('all');

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchTimesheets();
  }, [user, authLoading, mounted]);

  const fetchTimesheets = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get<TimesheetResponse>('/timesheets');
      setTimesheets(response.data.content || []);
      setAllowedActions(response.data.allowedActions || []);
    } catch (err) {
      console.error('Error fetching timesheets:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (id: number) => {
    try {
      await apiClient.post(`/timesheets/${id}/submit`);
      alert('Timesheet submitted successfully!');
      fetchTimesheets();
    } catch (err: any) {
      alert('Failed to submit timesheet: ' + (err?.response?.data?.error || err.message));
    }
  };

  const filteredTimesheets = timesheets.filter(ts => {
    if (filter === 'all') return true;
    return ts.status?.toUpperCase() === filter.toUpperCase();
  });

  const getStatusBadgeClass = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'APPROVED': return 'bg-success';
      case 'SUBMITTED': return 'bg-info';
      case 'REJECTED': return 'bg-danger';
      case 'DRAFT': return 'bg-warning';
      default: return 'bg-secondary';
    }
  };

  if (!mounted || loading || authLoading) {
    return (
      <div className="d-flex align-items-center justify-content-center" style={{ minHeight: '400px' }}>
        <div className="text-center">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted">Loading timesheets...</p>
        </div>
      </div>
    );
  }

  return (
    <div>
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 className="h3 mb-1">My Timesheets</h1>
          <p className="text-muted mb-0">View and manage your timesheets</p>
        </div>
        <div className="d-flex gap-2">
          {allowedActions.includes('create') && (
            <button
              className="btn btn-primary"
              onClick={() => router.push('/provider/timesheets/new')}
            >
              + New Timesheet
            </button>
          )}
        </div>
      </div>

      {/* Filters */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="d-flex gap-2 flex-wrap">
            <button
              className={`btn btn-sm ${filter === 'all' ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={() => setFilter('all')}
            >
              All ({timesheets.length})
            </button>
            <button
              className={`btn btn-sm ${filter === 'DRAFT' ? 'btn-warning' : 'btn-outline-warning'}`}
              onClick={() => setFilter('DRAFT')}
            >
              Draft ({timesheets.filter(t => t.status === 'DRAFT').length})
            </button>
            <button
              className={`btn btn-sm ${filter === 'SUBMITTED' ? 'btn-info' : 'btn-outline-info'}`}
              onClick={() => setFilter('SUBMITTED')}
            >
              Submitted ({timesheets.filter(t => t.status === 'SUBMITTED').length})
            </button>
            <button
              className={`btn btn-sm ${filter === 'APPROVED' ? 'btn-success' : 'btn-outline-success'}`}
              onClick={() => setFilter('APPROVED')}
            >
              Approved ({timesheets.filter(t => t.status === 'APPROVED').length})
            </button>
            <button
              className={`btn btn-sm ${filter === 'REJECTED' ? 'btn-danger' : 'btn-outline-danger'}`}
              onClick={() => setFilter('REJECTED')}
            >
              Rejected ({timesheets.filter(t => t.status === 'REJECTED').length})
            </button>
          </div>
        </div>
      </div>

      {/* Actions Info */}
      {allowedActions.length > 0 && (
        <div className="alert alert-info mb-4">
          <small>Available actions: {allowedActions.join(', ')}</small>
        </div>
      )}

      {/* Timesheets Table */}
      <div className="card">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0" style={{ color: 'white' }}>Timesheets ({filteredTimesheets.length})</h5>
        </div>
        <div className="card-body p-0">
          {filteredTimesheets.length === 0 ? (
            <div className="text-center py-5">
              <p className="text-muted mb-3">No timesheets found</p>
              {allowedActions.includes('create') && (
                <button
                  className="btn btn-primary"
                  onClick={() => router.push('/provider/timesheets/new')}
                >
                  Create Your First Timesheet
                </button>
              )}
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Pay Period</th>
                    {timesheets[0] && isFieldVisible(timesheets[0], 'regularHours') && <th>Regular</th>}
                    {timesheets[0] && isFieldVisible(timesheets[0], 'overtimeHours') && <th>OT</th>}
                    <th>Total</th>
                    <th>Status</th>
                    {timesheets[0] && isFieldVisible(timesheets[0], 'submittedAt') && <th>Submitted</th>}
                    {timesheets[0] && isFieldVisible(timesheets[0], 'comments') && <th>Comments</th>}
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTimesheets.map((ts) => (
                    <tr key={ts.id}>
                      <td>
                        <FieldAuthorizedValue data={ts} field="payPeriodStart" type="date" />
                        {' - '}
                        <FieldAuthorizedValue data={ts} field="payPeriodEnd" type="date" />
                      </td>
                      {isFieldVisible(timesheets[0], 'regularHours') && (
                        <td><FieldAuthorizedValue data={ts} field="regularHours" type="number" /></td>
                      )}
                      {isFieldVisible(timesheets[0], 'overtimeHours') && (
                        <td><FieldAuthorizedValue data={ts} field="overtimeHours" type="number" /></td>
                      )}
                      <td className="fw-bold">
                        <FieldAuthorizedValue data={ts} field="totalHours" type="number" />
                      </td>
                      <td>
                        <span className={`badge ${getStatusBadgeClass(ts.status || '')}`}>
                          {ts.status || 'Unknown'}
                        </span>
                      </td>
                      {isFieldVisible(timesheets[0], 'submittedAt') && (
                        <td><FieldAuthorizedValue data={ts} field="submittedAt" type="date" /></td>
                      )}
                      {isFieldVisible(timesheets[0], 'comments') && (
                        <td>
                          <small className="text-muted">
                            <FieldAuthorizedValue data={ts} field="comments" placeholder="" />
                          </small>
                        </td>
                      )}
                      <td>
                        <ActionButtons
                          allowedActions={allowedActions}
                          onView={() => router.push(`/provider/timesheets/${ts.id}`)}
                          onEdit={ts.status === 'DRAFT' || ts.status === 'REJECTED' ? () => router.push(`/provider/timesheets/${ts.id}/edit`) : undefined}
                          onSubmit={ts.status === 'DRAFT' ? () => handleSubmit(ts.id) : undefined}
                          size="sm"
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
