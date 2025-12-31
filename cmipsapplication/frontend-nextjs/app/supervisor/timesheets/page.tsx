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

export default function SupervisorTimesheetsPage() {
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
    if (!confirm('Approve this timesheet?')) return;
    try {
      await apiClient.post(`/timesheets/${id}/approve`);
      alert('Timesheet approved!');
      fetchTimesheets();
    } catch (err: any) {
      alert('Failed: ' + (err?.response?.data?.error || err.message));
    }
  };

  const handleReject = async (id: number) => {
    const reason = prompt('Rejection reason:');
    if (!reason) return;
    try {
      await apiClient.post(`/timesheets/${id}/reject`, { reason });
      alert('Timesheet rejected.');
      fetchTimesheets();
    } catch (err: any) {
      alert('Failed: ' + (err?.response?.data?.error || err.message));
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this timesheet? This cannot be undone.')) return;
    try {
      await apiClient.delete(`/timesheets/${id}`);
      alert('Timesheet deleted.');
      fetchTimesheets();
    } catch (err: any) {
      alert('Failed: ' + (err?.response?.data?.error || err.message));
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
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 className="h3 mb-1">Timesheet Management</h1>
          <p className="text-muted mb-0">Review, approve, and manage timesheets</p>
        </div>
        <button className="btn btn-outline-primary" onClick={fetchTimesheets}>
          Refresh
        </button>
      </div>

      {/* Stats */}
      <div className="row mb-4">
        <div className="col-md-3">
          <div className="card text-center bg-warning bg-opacity-10">
            <div className="card-body">
              <h3 className="text-warning">{timesheets.filter(t => t.status === 'SUBMITTED').length}</h3>
              <small>Pending Approval</small>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center bg-success bg-opacity-10">
            <div className="card-body">
              <h3 className="text-success">{timesheets.filter(t => t.status === 'APPROVED').length}</h3>
              <small>Approved</small>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center bg-danger bg-opacity-10">
            <div className="card-body">
              <h3 className="text-danger">{timesheets.filter(t => t.status === 'REJECTED').length}</h3>
              <small>Rejected</small>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center bg-secondary bg-opacity-10">
            <div className="card-body">
              <h3>{timesheets.filter(t => t.status === 'DRAFT').length}</h3>
              <small>Drafts</small>
            </div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="card mb-4">
        <div className="card-body py-2">
          <div className="d-flex gap-2 flex-wrap align-items-center">
            <span className="text-muted me-2">Filter:</span>
            {['SUBMITTED', 'APPROVED', 'REJECTED', 'DRAFT', 'all'].map(f => (
              <button
                key={f}
                className={`btn btn-sm ${filter === f ? 'btn-primary' : 'btn-outline-secondary'}`}
                onClick={() => setFilter(f)}
              >
                {f === 'all' ? 'All' : f}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Actions Info */}
      {allowedActions.length > 0 && (
        <div className="alert alert-info mb-4">
          <small><strong>Your permissions:</strong> {allowedActions.join(', ')}</small>
        </div>
      )}

      {/* Table */}
      <div className="card">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0" style={{ color: 'white' }}>
            Timesheets ({filteredTimesheets.length})
          </h5>
        </div>
        <div className="card-body p-0">
          {filteredTimesheets.length === 0 ? (
            <p className="text-muted text-center py-5">No timesheets found</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    {timesheets[0] && isFieldVisible(timesheets[0], 'employeeId') && <th>ID</th>}
                    {timesheets[0] && isFieldVisible(timesheets[0], 'employeeName') && <th>Employee</th>}
                    {timesheets[0] && isFieldVisible(timesheets[0], 'department') && <th>Dept</th>}
                    <th>Pay Period</th>
                    <th>Hours</th>
                    <th>Status</th>
                    {timesheets[0] && isFieldVisible(timesheets[0], 'submittedAt') && <th>Submitted</th>}
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTimesheets.map((ts) => (
                    <tr key={ts.id}>
                      {isFieldVisible(timesheets[0], 'employeeId') && (
                        <td><FieldAuthorizedValue data={ts} field="employeeId" /></td>
                      )}
                      {isFieldVisible(timesheets[0], 'employeeName') && (
                        <td><FieldAuthorizedValue data={ts} field="employeeName" /></td>
                      )}
                      {isFieldVisible(timesheets[0], 'department') && (
                        <td><FieldAuthorizedValue data={ts} field="department" /></td>
                      )}
                      <td>
                        <small>
                          <FieldAuthorizedValue data={ts} field="payPeriodStart" type="date" />
                          {' - '}
                          <FieldAuthorizedValue data={ts} field="payPeriodEnd" type="date" />
                        </small>
                      </td>
                      <td className="fw-bold">
                        <FieldAuthorizedValue data={ts} field="totalHours" type="number" />
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
                          onDelete={() => handleDelete(ts.id)}
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
