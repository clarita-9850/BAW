'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import NotificationCenter from '@/components/NotificationCenter';
import apiClient from '@/lib/api';
import { FieldAuthorizedValue, ActionButtons, ConditionalField } from '@/components/FieldAuthorizedValue';
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

type Provider = {
  id: number;
  name: string;
  status: string;
  role: string;
};

export default function RecipientDashboard() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [pendingTimesheets, setPendingTimesheets] = useState<Timesheet[]>([]);
  const [providers, setProviders] = useState<Provider[]>([]);
  const [allowedActions, setAllowedActions] = useState<string[]>([]);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || authLoading) return;
    if (!user || (user.role !== 'RECIPIENT' && !user.roles?.includes('RECIPIENT'))) {
      window.location.href = '/login';
      return;
    }
    fetchDashboardData();
  }, [user, authLoading, mounted]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      // Fetch pending timesheets with field-level authorization
      try {
        const timesheetsResponse = await apiClient.get<TimesheetResponse>('/timesheets');
        const responseData = timesheetsResponse.data;
        const timesheets = responseData.content || [];
        setPendingTimesheets(timesheets.filter((ts: Timesheet) => ts.status === 'SUBMITTED'));
        // Store allowed actions from the API response
        setAllowedActions(responseData.allowedActions || []);
      } catch (err) {
        console.error('Error fetching timesheets:', err);
        setPendingTimesheets([]);
        setAllowedActions([]);
      }
      
      // Fetch providers
      try {
        const providersResponse = await apiClient.get('/provider-recipient/my-providers');
        if (providersResponse.data && providersResponse.data.length > 0) {
          const mappedProviders = providersResponse.data.map((rel: any) => ({
            id: rel.id || 1,
            name: rel.providerName || 'Provider',
            status: rel.status || 'Active',
            role: rel.relationship || 'Primary Caregiver',
          }));
          setProviders(mappedProviders);
        } else {
          setProviders([
            { id: 1, name: 'John Doe', status: 'Active', role: 'Primary Caregiver' },
            { id: 2, name: 'Mary Johnson', status: 'Active', role: 'Backup' }
          ]);
        }
      } catch (err) {
        console.error('Error fetching providers:', err);
        setProviders([
          { id: 1, name: 'John Doe', status: 'Active', role: 'Primary Caregiver' },
          { id: 2, name: 'Mary Johnson', status: 'Active', role: 'Backup' }
        ]);
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
          <p className="text-muted mb-0">Loading Recipient Dashboard...</p>
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
              onClick={() => router.push('/recipient/timesheets')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>📋</div>
                <h3 className="card-title">
                  TIMESHEETS TO REVIEW
                  {pendingTimesheets.length > 0 && (
                    <span className="badge bg-danger ms-2">{pendingTimesheets.length} Pending</span>
                  )}
                </h3>
                <p className="text-muted small mb-0">Review & approve timesheets</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div
              className="card h-100"
              style={{ cursor: 'pointer' }}
              onClick={() => router.push('/recipient/providers')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>👥</div>
                <h3 className="card-title">MY PROVIDERS</h3>
                <p className="text-muted small mb-0">Manage your caregivers</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card h-100" style={{ cursor: 'pointer' }}>
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>📅</div>
                <h3 className="card-title">SERVICE SCHEDULE</h3>
                <p className="text-muted small mb-0">View upcoming services</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card h-100" style={{ cursor: 'pointer' }}>
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>❓</div>
                <h3 className="card-title">HELP & SUPPORT</h3>
                <p className="text-muted small mb-0">Get assistance</p>
              </div>
            </div>
          </div>
        </div>

        {/* Timesheets Awaiting Approval */}
        <div className="card mb-4">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>🔔 TIMESHEETS AWAITING YOUR APPROVAL</h2>
            {allowedActions.length > 0 && (
              <small className="text-white-50">
                Available actions: {allowedActions.join(', ')}
              </small>
            )}
          </div>
          <div className="card-body">
            {pendingTimesheets.length === 0 ? (
              <p className="text-center text-muted py-4">No timesheets pending review</p>
            ) : (
              <div>
                {pendingTimesheets.map((timesheet) => (
                  <div key={timesheet.id} className="alert alert-warning mb-3" style={{ borderLeft: '4px solid #ffc107' }}>
                    <div className="d-flex justify-content-between align-items-center">
                      <div>
                        <h5 className="fw-semibold mb-1">
                          <FieldAuthorizedValue data={timesheet} field="employeeName" /> -{' '}
                          <FieldAuthorizedValue data={timesheet} field="payPeriodStart" type="date" /> to{' '}
                          <FieldAuthorizedValue data={timesheet} field="payPeriodEnd" type="date" />
                        </h5>
                        <p className="text-muted small mb-0">
                          Total Hours: <FieldAuthorizedValue data={timesheet} field="totalHours" type="number" />
                          {isFieldVisible(timesheet, 'submittedAt') && (
                            <> • Submitted: <FieldAuthorizedValue data={timesheet} field="submittedAt" type="date" /></>
                          )}
                          {isFieldVisible(timesheet, 'comments') && timesheet.comments && (
                            <> • <FieldAuthorizedValue data={timesheet} field="comments" /></>
                          )}
                        </p>
                      </div>
                      <ActionButtons
                        allowedActions={allowedActions}
                        onView={() => router.push(`/recipient/timesheet/${timesheet.id}`)}
                        onApprove={() => handleApproveTimesheet(timesheet.id)}
                        onReject={() => handleRejectTimesheet(timesheet.id)}
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* My Providers */}
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>👥 MY PROVIDERS</h2>
          </div>
          <div className="card-body">
            {providers.length === 0 ? (
              <p className="text-center text-muted py-4">No providers assigned</p>
            ) : (
              <div>
                {providers.map((provider) => (
                  <div key={provider.id} className="card mb-3">
                    <div className="card-body">
                      <div className="d-flex justify-content-between align-items-center">
                        <div>
                          <h5 className="fw-semibold mb-1">{provider.name}</h5>
                          <p className="text-muted small mb-0">
                            <span className="badge bg-success">{provider.status}</span>
                            {' • '} {provider.role}
                          </p>
                        </div>
                        <button className="btn btn-secondary">
                          View Details
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );

  async function handleApproveTimesheet(id: number) {
    try {
      await apiClient.post(`/timesheets/${id}/approve`);
      fetchDashboardData();
    } catch (err) {
      console.error('Error approving timesheet:', err);
      alert('Failed to approve timesheet');
    }
  }

  async function handleRejectTimesheet(id: number) {
    const reason = prompt('Enter rejection reason:');
    if (!reason) return;
    try {
      await apiClient.post(`/timesheets/${id}/reject`, { reason });
      fetchDashboardData();
    } catch (err) {
      console.error('Error rejecting timesheet:', err);
      alert('Failed to reject timesheet');
    }
  }
}
