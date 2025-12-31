'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import NotificationCenter from '@/components/NotificationCenter';
import apiClient from '@/lib/api';
import { FieldAuthorizedValue, ActionButtons } from '@/components/FieldAuthorizedValue';
import { isFieldVisible } from '@/hooks/useFieldAuthorization';

type Recipient = {
  id: number;
  name: string;
  status: string;
  authorizedHours: number;
  caseNumber: string;
};

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

export default function ProviderDashboard() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [pendingActions, setPendingActions] = useState<Array<{ type: string; message: string; priority: string }>>([]);
  const [myTimesheets, setMyTimesheets] = useState<Timesheet[]>([]);
  const [allowedActions, setAllowedActions] = useState<string[]>([]);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || authLoading) return;
    if (!user || (user.role !== 'PROVIDER' && !user.roles?.includes('PROVIDER'))) {
      window.location.href = '/login';
      return;
    }
    fetchDashboardData();
  }, [user, authLoading, mounted]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      // Fetch assigned recipients from backend
      try {
        const recipientsData = await apiClient.get('/provider-recipient/my-recipients');
        if (recipientsData.data && recipientsData.data.length > 0) {
          const mappedRecipients = recipientsData.data.map((rel: any) => ({
            id: rel.id || 1,
            name: rel.recipientName || 'recipient1',
            status: rel.status || 'Active',
            authorizedHours: rel.authorizedHoursPerMonth || 40,
            caseNumber: rel.caseNumber || 'CASE-001',
          }));
          setRecipients(mappedRecipients);
        } else {
          setRecipients([{
            id: 1,
            name: 'recipient1',
            status: 'Active',
            authorizedHours: 40,
            caseNumber: 'CASE-001',
          }]);
        }
      } catch (apiError) {
        console.error('Error fetching recipients from API, using default:', apiError);
        setRecipients([{
          id: 1,
          name: 'recipient1',
          status: 'Active',
          authorizedHours: 40,
          caseNumber: 'CASE-001',
        }]);
      }

      // Fetch my timesheets with field-level authorization
      try {
        const timesheetsResponse = await apiClient.get<TimesheetResponse>('/timesheets');
        const responseData = timesheetsResponse.data;
        setMyTimesheets(responseData.content || []);
        setAllowedActions(responseData.allowedActions || []);

        // Update pending actions based on timesheet status
        const drafts = (responseData.content || []).filter(ts => ts.status === 'DRAFT');
        const rejected = (responseData.content || []).filter(ts => ts.status === 'REJECTED');
        const actions: Array<{ type: string; message: string; priority: string }> = [];

        if (drafts.length > 0) {
          actions.push({ type: 'timesheet', message: `${drafts.length} draft timesheet(s) ready to submit`, priority: 'high' });
        }
        if (rejected.length > 0) {
          actions.push({ type: 'review', message: `${rejected.length} rejected timesheet(s) need revision`, priority: 'medium' });
        }
        setPendingActions(actions.length > 0 ? actions : []);
      } catch (err) {
        console.error('Error fetching timesheets:', err);
        setMyTimesheets([]);
        setAllowedActions([]);
        setPendingActions([]);
      }
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading Provider Dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div>
      {/* Notification Center */}
      <div className="mb-3 d-flex justify-content-end">
        <NotificationCenter userId={user?.username || ''} />
      </div>

      {/* Main Content */}
      <div>
        {/* Quick Actions */}
        <div className="row mb-4">
          <div className="col-lg-3 col-md-6 mb-3">
            <div
              className="card h-100"
              style={{ cursor: 'pointer' }}
              onClick={() => router.push('/provider/evv-checkin')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>📍</div>
                <h3 className="card-title">EVV CHECK-IN</h3>
                <p className="text-muted small mb-0">Start your service visit</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div
              className="card h-100"
              style={{ cursor: 'pointer' }}
              onClick={() => router.push('/provider/timesheets')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>📋</div>
                <h3 className="card-title">TIMESHEETS</h3>
                <p className="text-muted small mb-0">Submit & view timesheets</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div
              className="card h-100"
              style={{ cursor: 'pointer' }}
              onClick={() => router.push('/provider/payments')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>💰</div>
                <h3 className="card-title">PAYMENT HISTORY</h3>
                <p className="text-muted small mb-0">View your payments</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div
              className="card h-100"
              style={{ cursor: 'pointer' }}
              onClick={() => router.push('/provider/profile')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>👤</div>
                <h3 className="card-title">MY PROFILE</h3>
                <p className="text-muted small mb-0">Update my address & details</p>
              </div>
            </div>
          </div>
        </div>

        {/* My Recipients */}
        <div className="card mb-4">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>📋 MY RECIPIENTS</h2>
          </div>
          <div className="card-body">
            {recipients.length === 0 ? (
              <p className="text-center text-muted py-4">No recipients assigned</p>
            ) : (
              <div>
                {recipients.map((recipient) => (
                  <div key={recipient.id} className="card mb-3">
                    <div className="card-body">
                      <div className="d-flex justify-content-between align-items-center">
                        <div>
                          <h5 className="fw-semibold mb-1">{recipient.name}</h5>
                          <p className="text-muted small mb-0">
                            Status: <span className="badge bg-success">{recipient.status}</span>
                            {' • '} Authorized: {recipient.authorizedHours} hours/month
                          </p>
                        </div>
                        <button
                          onClick={() => router.push(`/provider/timesheet/new/${recipient.id}`)}
                          className="btn btn-primary"
                        >
                          Submit Timesheet
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Pending Actions */}
        {pendingActions.length > 0 && (
          <div className="card mb-4">
            <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
              <h2 className="card-title mb-0" style={{ color: 'white' }}>⏰ PENDING ACTIONS</h2>
            </div>
            <div className="card-body">
              <div>
                {pendingActions.map((action, index) => (
                  <div
                    key={index}
                    className={`alert mb-3 ${action.priority === 'high' ? 'alert-danger' : 'alert-warning'}`}
                    style={{ borderLeft: '4px solid ' + (action.priority === 'high' ? '#dc3545' : '#ffc107') }}
                  >
                    <p className="mb-0">{action.message}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* My Timesheets */}
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>📊 MY TIMESHEETS</h2>
            {allowedActions.length > 0 && (
              <small className="text-white-50">
                Available actions: {allowedActions.join(', ')}
              </small>
            )}
          </div>
          <div className="card-body">
            {myTimesheets.length === 0 ? (
              <p className="text-center text-muted py-4">No timesheets found</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped table-hover">
                  <thead>
                    <tr>
                      <th>Pay Period</th>
                      <th>Hours</th>
                      <th>Status</th>
                      {myTimesheets[0] && isFieldVisible(myTimesheets[0], 'submittedAt') && (
                        <th>Submitted</th>
                      )}
                      {myTimesheets[0] && isFieldVisible(myTimesheets[0], 'comments') && (
                        <th>Comments</th>
                      )}
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {myTimesheets.slice(0, 10).map((timesheet) => (
                      <tr key={timesheet.id}>
                        <td>
                          <FieldAuthorizedValue data={timesheet} field="payPeriodStart" type="date" />
                          {' - '}
                          <FieldAuthorizedValue data={timesheet} field="payPeriodEnd" type="date" />
                        </td>
                        <td className="fw-bold">
                          <FieldAuthorizedValue data={timesheet} field="totalHours" type="number" />
                        </td>
                        <td>
                          <FieldAuthorizedValue data={timesheet} field="status" type="badge" />
                        </td>
                        {isFieldVisible(myTimesheets[0], 'submittedAt') && (
                          <td>
                            <FieldAuthorizedValue data={timesheet} field="submittedAt" type="date" />
                          </td>
                        )}
                        {isFieldVisible(myTimesheets[0], 'comments') && (
                          <td>
                            <FieldAuthorizedValue data={timesheet} field="comments" placeholder="" />
                          </td>
                        )}
                        <td>
                          <ActionButtons
                            allowedActions={allowedActions}
                            onView={() => router.push(`/provider/timesheet/${timesheet.id}`)}
                            onEdit={timesheet.status === 'DRAFT' || timesheet.status === 'REJECTED' ? () => router.push(`/provider/timesheet/${timesheet.id}/edit`) : undefined}
                            onSubmit={timesheet.status === 'DRAFT' ? () => handleSubmitTimesheet(timesheet.id) : undefined}
                            size="sm"
                          />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {myTimesheets.length > 10 && (
                  <div className="text-center mt-3">
                    <button
                      onClick={() => router.push('/provider/timesheets')}
                      className="btn btn-outline-primary"
                    >
                      View All Timesheets ({myTimesheets.length})
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );

  async function handleSubmitTimesheet(id: number) {
    try {
      await apiClient.post(`/timesheets/${id}/submit`);
      fetchDashboardData();
    } catch (err) {
      console.error('Error submitting timesheet:', err);
      alert('Failed to submit timesheet');
    }
  }
}

