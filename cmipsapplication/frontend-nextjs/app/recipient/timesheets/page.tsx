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
  payPeriodStart?: string;
  payPeriodEnd?: string;
  regularHours?: number;
  overtimeHours?: number;
  totalHours?: number;
  status?: string;
  comments?: string;
  submittedAt?: string;
};

type TimesheetResponse = {
  content: Timesheet[];
  totalElements: number;
  numberOfElements: number;
  allowedActions: string[];
};

export default function RecipientTimesheetsPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [timesheets, setTimesheets] = useState<Timesheet[]>([]);
  const [allowedActions, setAllowedActions] = useState<string[]>([]);
  const [filter, setFilter] = useState<string>('SUBMITTED');

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

  const handleApprove = async (id: number) => {
    if (!confirm('Are you sure you want to approve this timesheet?')) return;
    try {
      await apiClient.post(`/timesheets/${id}/approve`);
      alert('Timesheet approved!');
      fetchTimesheets();
    } catch (err: any) {
      alert('Failed to approve: ' + (err?.response?.data?.error || err.message));
    }
  };

  const handleReject = async (id: number) => {
    const reason = prompt('Enter reason for rejection:');
    if (!reason) return;
    try {
      await apiClient.post(`/timesheets/${id}/reject`, { reason });
      alert('Timesheet rejected.');
      fetchTimesheets();
    } catch (err: any) {
      alert('Failed to reject: ' + (err?.response?.data?.error || err.message));
    }
  };

  const filteredTimesheets = timesheets.filter(ts => {
    if (filter === 'all') return true;
    return ts.status?.toUpperCase() === filter.toUpperCase();
  });

  const pendingCount = timesheets.filter(t => t.status === 'SUBMITTED').length;

  if (!mounted || loading || authLoading) {
    return (
      <div className="d-flex align-items-center justify-content-center" style={{ minHeight: '400px' }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-4">
        <h1 className="h3 mb-1">Approve Timesheets</h1>
        <p className="text-muted">Review and approve timesheets from your providers</p>
      </div>

      {/* Pending Alert */}
      {pendingCount > 0 && (
        <div className="alert alert-warning mb-4">
          <strong>{pendingCount} timesheet(s)</strong> pending your approval
        </div>
      )}

      {/* Filters */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="d-flex gap-2 flex-wrap">
            <button
              className={`btn btn-sm ${filter === 'SUBMITTED' ? 'btn-warning' : 'btn-outline-warning'}`}
              onClick={() => setFilter('SUBMITTED')}
            >
              Pending Approval ({timesheets.filter(t => t.status === 'SUBMITTED').length})
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
            <button
              className={`btn btn-sm ${filter === 'all' ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={() => setFilter('all')}
            >
              All ({timesheets.length})
            </button>
          </div>
        </div>
      </div>

      {/* Actions Info */}
      {allowedActions.length > 0 && (
        <div className="alert alert-info mb-4">
          <small>Your permissions: {allowedActions.join(', ')}</small>
        </div>
      )}

      {/* Timesheets */}
      <div className="card">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0" style={{ color: 'white' }}>Timesheets ({filteredTimesheets.length})</h5>
        </div>
        <div className="card-body p-0">
          {filteredTimesheets.length === 0 ? (
            <p className="text-muted text-center py-5">No timesheets to display</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    {timesheets[0] && isFieldVisible(timesheets[0], 'employeeName') && <th>Provider</th>}
                    <th>Pay Period</th>
                    <th>Total Hours</th>
                    <th>Status</th>
                    {timesheets[0] && isFieldVisible(timesheets[0], 'submittedAt') && <th>Submitted</th>}
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTimesheets.map((ts) => (
                    <tr key={ts.id}>
                      {isFieldVisible(timesheets[0], 'employeeName') && (
                        <td><FieldAuthorizedValue data={ts} field="employeeName" /></td>
                      )}
                      <td>
                        <FieldAuthorizedValue data={ts} field="payPeriodStart" type="date" />
                        {' - '}
                        <FieldAuthorizedValue data={ts} field="payPeriodEnd" type="date" />
                      </td>
                      <td className="fw-bold">
                        <FieldAuthorizedValue data={ts} field="totalHours" type="number" /> hrs
                      </td>
                      <td>
                        <FieldAuthorizedValue data={ts} field="status" type="badge" />
                      </td>
                      {isFieldVisible(timesheets[0], 'submittedAt') && (
                        <td><FieldAuthorizedValue data={ts} field="submittedAt" type="date" /></td>
                      )}
                      <td>
                        <ActionButtons
                          allowedActions={allowedActions}
                          onView={() => alert(`View timesheet ${ts.id}`)}
                          onApprove={ts.status === 'SUBMITTED' ? () => handleApprove(ts.id) : undefined}
                          onReject={ts.status === 'SUBMITTED' ? () => handleReject(ts.id) : undefined}
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
