'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import WorkView from '@/components/WorkView';
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

type OvertimeViolation = {
  id: number;
  providerId: number;
  providerName?: string;
  providerNumber?: string;
  violationType: string;
  violationStatus: string;
  violationNumber: number;
  serviceMonth: number;
  serviceYear: number;
  hoursWorked: number;
  maximumAllowed: number;
  countyReviewDueDate?: string;
  countyReviewOutcome?: string;
  supervisorReviewOutcome?: string;
  trainingDueDate?: string;
  trainingCompleted?: boolean;
  createdDate: string;
};

export default function CaseWorkerDashboard() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [mounted, setMounted] = useState(false);
  const [stats, setStats] = useState({
    totalCases: 0,
    pendingTimesheets: 0,
    evvViolations: 0,
    dueReassessments: 0
  });
  const [pendingTimesheets, setPendingTimesheets] = useState<Timesheet[]>([]);
  const [allowedActions, setAllowedActions] = useState<string[]>([]);
  const [overtimeViolations, setOvertimeViolations] = useState<OvertimeViolation[]>([]);
  const [showViolationModal, setShowViolationModal] = useState(false);
  const [selectedViolation, setSelectedViolation] = useState<OvertimeViolation | null>(null);
  const [reviewOutcome, setReviewOutcome] = useState<string>('');
  const [reviewComments, setReviewComments] = useState<string>('');

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    console.log('📊 [Dashboard] Effect running - mounted:', mounted, 'authLoading:', authLoading, 'user:', user);
    if (!mounted || authLoading) {
      console.log('📊 [Dashboard] Waiting for mount/auth...');
      return;
    }
    if (!user || (user.role !== 'CASE_WORKER' && !user.roles?.includes('CASE_WORKER'))) {
      console.log('📊 [Dashboard] ❌ Invalid user or role, redirecting to login');
      console.log('📊 [Dashboard] user:', user);
      console.log('📊 [Dashboard] user.role:', user?.role);
      console.log('📊 [Dashboard] user.roles:', user?.roles);
      window.location.href = '/login';
      return;
    }
    console.log('📊 [Dashboard] ✅ User authorized, fetching data');
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
        const submitted = timesheets.filter((ts: Timesheet) => ts.status === 'SUBMITTED');
        setPendingTimesheets(submitted);

        // Store allowed actions from the API response
        setAllowedActions(responseData.allowedActions || []);

        // Fetch overtime violations pending review
        const violationsResponse = await apiClient.get('/providers/violations/pending-review').catch(() => ({ data: [] }));
        const violations = violationsResponse.data || [];
        setOvertimeViolations(violations);

        // Update stats
        setStats({
          totalCases: 145,
          pendingTimesheets: submitted.length,
          evvViolations: violations.length,
          dueReassessments: 5
        });
      } catch (err) {
        console.error('Error fetching timesheets:', err);
        setPendingTimesheets([]);
        setAllowedActions([]);
      }
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleViolationReview = async () => {
    if (!selectedViolation || !reviewOutcome || !reviewComments) {
      alert('Please provide outcome and comments');
      return;
    }

    try {
      await apiClient.post(`/providers/violations/${selectedViolation.id}/county-review`, {
        outcome: reviewOutcome,
        comments: reviewComments
      });
      setShowViolationModal(false);
      setSelectedViolation(null);
      setReviewOutcome('');
      setReviewComments('');
      fetchDashboardData();
    } catch (err) {
      console.error('Error submitting violation review:', err);
      alert('Failed to submit review');
    }
  };

  const openViolationReview = (violation: OvertimeViolation) => {
    setSelectedViolation(violation);
    setShowViolationModal(true);
  };

  const getViolationTypeName = (type: string) => {
    switch (type) {
      case 'ONE_TO_ONE': return 'Single Recipient Overtime';
      case 'ONE_TO_MANY': return 'Multi-Recipient Overtime';
      case 'WPCS': return 'WPCS Overtime';
      case 'TRAVEL': return 'Exceeded Travel Maximum';
      default: return type;
    }
  };

  const getViolationStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING_REVIEW': return 'bg-warning text-dark';
      case 'ACTIVE': return 'bg-danger';
      case 'INACTIVE': return 'bg-secondary';
      case 'PENDING_UPHOLD': return 'bg-info';
      case 'PENDING_OVERRIDE': return 'bg-primary';
      default: return 'bg-secondary';
    }
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading Case Worker Dashboard...</p>
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
        {/* Statistics Cards */}
        <div className="row mb-4">
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: 'var(--color-p2, #046b99)' }}>{stats.totalCases}</div>
                <p className="text-muted small mb-0">CASES</p>
              </div>
            </div>
          </div>
          
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#ffc107' }}>{stats.pendingTimesheets}</div>
                <p className="text-muted small mb-0">TIMESHEETS PENDING</p>
              </div>
            </div>
          </div>
          
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#dc3545' }}>{stats.evvViolations}</div>
                <p className="text-muted small mb-0">OVERTIME VIOLATIONS</p>
              </div>
            </div>
          </div>
          
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#0dcaf0' }}>{stats.dueReassessments}</div>
                <p className="text-muted small mb-0">DUE REASSESSMENTS</p>
              </div>
            </div>
          </div>
        </div>

        {/* WorkView - Tasks */}
        <div className="mb-4">
          <WorkView username={user?.username || ''} />
        </div>

        {/* Priority Actions */}
        <div className="card mb-4">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>🚨 PRIORITY ACTIONS</h2>
          </div>
          <div className="card-body">
            <div>
              <div className="alert alert-warning mb-2" style={{ borderLeft: '4px solid #ffc107' }}>
                <p className="mb-0">• {stats.pendingTimesheets} Timesheets Pending Review</p>
              </div>
              <div className="alert alert-danger mb-2" style={{ borderLeft: '4px solid #dc3545' }}>
                <p className="mb-0">• {stats.evvViolations} Overtime Violations Need County Review</p>
              </div>
              <div className="alert alert-info mb-0" style={{ borderLeft: '4px solid #0dcaf0' }}>
                <p className="mb-0">• {stats.dueReassessments} Cases Due for Reassessment</p>
              </div>
            </div>
          </div>
        </div>

        {/* Pending Timesheets */}
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>📊 PENDING TIMESHEETS</h2>
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
              <div className="table-responsive">
                <table className="table table-striped table-hover">
                  <thead>
                    <tr>
                      <th>Provider</th>
                      {/* Conditionally show columns based on field availability */}
                      {pendingTimesheets[0] && isFieldVisible(pendingTimesheets[0], 'employeeId') && (
                        <th>Employee ID</th>
                      )}
                      {pendingTimesheets[0] && isFieldVisible(pendingTimesheets[0], 'department') && (
                        <th>Department</th>
                      )}
                      {pendingTimesheets[0] && isFieldVisible(pendingTimesheets[0], 'location') && (
                        <th>Location</th>
                      )}
                      <th>Pay Period</th>
                      <th>Hours</th>
                      <th>Status</th>
                      {pendingTimesheets[0] && isFieldVisible(pendingTimesheets[0], 'submittedAt') && (
                        <th>Submitted</th>
                      )}
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pendingTimesheets.map((timesheet) => (
                      <tr key={timesheet.id}>
                        <td>
                          <FieldAuthorizedValue data={timesheet} field="employeeName" />
                        </td>
                        {isFieldVisible(pendingTimesheets[0], 'employeeId') && (
                          <td>
                            <FieldAuthorizedValue data={timesheet} field="employeeId" />
                          </td>
                        )}
                        {isFieldVisible(pendingTimesheets[0], 'department') && (
                          <td>
                            <FieldAuthorizedValue data={timesheet} field="department" />
                          </td>
                        )}
                        {isFieldVisible(pendingTimesheets[0], 'location') && (
                          <td>
                            <FieldAuthorizedValue data={timesheet} field="location" />
                          </td>
                        )}
                        <td>
                          <FieldAuthorizedValue data={timesheet} field="payPeriodStart" type="date" />
                          {' to '}
                          <FieldAuthorizedValue data={timesheet} field="payPeriodEnd" type="date" />
                        </td>
                        <td className="fw-bold">
                          <FieldAuthorizedValue data={timesheet} field="totalHours" type="number" />
                        </td>
                        <td>
                          <FieldAuthorizedValue data={timesheet} field="status" type="badge" />
                        </td>
                        {isFieldVisible(pendingTimesheets[0], 'submittedAt') && (
                          <td>
                            <FieldAuthorizedValue data={timesheet} field="submittedAt" type="date" />
                          </td>
                        )}
                        <td>
                          <ActionButtons
                            allowedActions={allowedActions}
                            onView={() => router.push(`/caseworker/timesheet/${timesheet.id}`)}
                            onApprove={() => handleApproveTimesheet(timesheet.id)}
                            onReject={() => handleRejectTimesheet(timesheet.id)}
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

        {/* Overtime Violations Work Queue */}
        <div className="card mb-4">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>⚠️ OVERTIME VIOLATIONS - COUNTY REVIEW</h2>
            <span className="badge bg-light text-dark">{overtimeViolations.length} Pending</span>
          </div>
          <div className="card-body">
            {overtimeViolations.length === 0 ? (
              <p className="text-center text-muted py-4">No overtime violations pending review</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped table-hover">
                  <thead>
                    <tr>
                      <th>Provider</th>
                      <th>Violation #</th>
                      <th>Type</th>
                      <th>Service Period</th>
                      <th>Hours Worked</th>
                      <th>Maximum</th>
                      <th>Status</th>
                      <th>Review Due</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {overtimeViolations.map((violation) => (
                      <tr key={violation.id}>
                        <td>
                          <a href="#" onClick={(e) => { e.preventDefault(); router.push(`/providers/${violation.providerId}`); }}>
                            {violation.providerName || 'Provider'}
                          </a>
                          <br />
                          <small className="text-muted">{violation.providerNumber}</small>
                        </td>
                        <td>
                          <span className={`badge ${violation.violationNumber >= 3 ? 'bg-danger' : 'bg-warning text-dark'}`}>
                            #{violation.violationNumber}
                          </span>
                        </td>
                        <td>{getViolationTypeName(violation.violationType)}</td>
                        <td>{violation.serviceMonth}/{violation.serviceYear}</td>
                        <td className="text-danger fw-bold">{violation.hoursWorked}</td>
                        <td>{violation.maximumAllowed}</td>
                        <td>
                          <span className={`badge ${getViolationStatusBadge(violation.violationStatus)}`}>
                            {violation.violationStatus?.replace(/_/g, ' ')}
                          </span>
                        </td>
                        <td className={violation.countyReviewDueDate && new Date(violation.countyReviewDueDate) <= new Date() ? 'text-danger fw-bold' : ''}>
                          {violation.countyReviewDueDate ? new Date(violation.countyReviewDueDate).toLocaleDateString() : '-'}
                        </td>
                        <td>
                          <button
                            className="btn btn-sm btn-primary"
                            onClick={() => openViolationReview(violation)}
                          >
                            Review
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div className="alert alert-info mt-3 small">
              <strong>County Review Process (BR PVM 90+):</strong>
              <ul className="mb-0 mt-2">
                <li>County has <strong>3 business days</strong> to review overtime violations</li>
                <li><strong>Uphold:</strong> Violation stands, letters issued to provider</li>
                <li><strong>Override:</strong> Request supervisor review for exception</li>
                <li>Violation #2 allows optional training (14 days) to remove violation</li>
                <li>Violation #3 results in 90-day suspension</li>
                <li>Violation #4 results in 365-day suspension with re-enrollment required</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Violation Review Modal */}
      {showViolationModal && selectedViolation && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="modal-title text-white">
                  Review Overtime Violation - {selectedViolation.providerName}
                </h5>
                <button type="button" className="btn-close btn-close-white" onClick={() => setShowViolationModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="row mb-4">
                  <div className="col-md-6">
                    <h6>Violation Details</h6>
                    <table className="table table-sm table-borderless">
                      <tbody>
                        <tr>
                          <th>Provider:</th>
                          <td>{selectedViolation.providerName} ({selectedViolation.providerNumber})</td>
                        </tr>
                        <tr>
                          <th>Violation Number:</th>
                          <td>
                            <span className={`badge ${selectedViolation.violationNumber >= 3 ? 'bg-danger' : 'bg-warning text-dark'}`}>
                              #{selectedViolation.violationNumber}
                            </span>
                          </td>
                        </tr>
                        <tr>
                          <th>Type:</th>
                          <td>{getViolationTypeName(selectedViolation.violationType)}</td>
                        </tr>
                        <tr>
                          <th>Service Period:</th>
                          <td>{selectedViolation.serviceMonth}/{selectedViolation.serviceYear}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <div className="col-md-6">
                    <h6>Hours Summary</h6>
                    <table className="table table-sm table-borderless">
                      <tbody>
                        <tr>
                          <th>Hours Worked:</th>
                          <td className="text-danger fw-bold">{selectedViolation.hoursWorked}</td>
                        </tr>
                        <tr>
                          <th>Maximum Allowed:</th>
                          <td>{selectedViolation.maximumAllowed}</td>
                        </tr>
                        <tr>
                          <th>Overage:</th>
                          <td className="text-danger fw-bold">
                            {(selectedViolation.hoursWorked - selectedViolation.maximumAllowed).toFixed(2)} hours
                          </td>
                        </tr>
                        <tr>
                          <th>Review Due:</th>
                          <td>{selectedViolation.countyReviewDueDate ? new Date(selectedViolation.countyReviewDueDate).toLocaleDateString() : '-'}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>

                {selectedViolation.violationNumber >= 3 && (
                  <div className="alert alert-danger">
                    <strong>Warning:</strong> This is Violation #{selectedViolation.violationNumber}.
                    If upheld, the provider will be {selectedViolation.violationNumber === 3 ? 'suspended for 90 days' : 'suspended for 365 days and must re-enroll'}.
                  </div>
                )}

                {selectedViolation.violationNumber === 2 && (
                  <div className="alert alert-info">
                    <strong>Note:</strong> This is Violation #2. If upheld, the provider will have 14 days to complete optional training.
                    If training is completed, the violation will be removed. Training option is only available once.
                  </div>
                )}

                <div className="mb-3">
                  <label className="form-label fw-bold">Review Outcome *</label>
                  <select
                    className="form-select"
                    value={reviewOutcome}
                    onChange={(e) => setReviewOutcome(e.target.value)}
                  >
                    <option value="">Select Outcome...</option>
                    <option value="UPHELD">Uphold Violation</option>
                    <option value="OVERRIDE">Request Override (Supervisor Review)</option>
                  </select>
                </div>

                <div className="mb-3">
                  <label className="form-label fw-bold">Comments * (Required)</label>
                  <textarea
                    className="form-control"
                    rows={4}
                    value={reviewComments}
                    onChange={(e) => setReviewComments(e.target.value)}
                    placeholder="Enter detailed comments explaining your review decision (max 1000 characters)..."
                    maxLength={1000}
                  />
                  <small className="text-muted">{reviewComments.length}/1000 characters</small>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowViolationModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className={`btn ${reviewOutcome === 'UPHELD' ? 'btn-danger' : 'btn-primary'}`}
                  onClick={handleViolationReview}
                  disabled={!reviewOutcome || !reviewComments}
                >
                  {reviewOutcome === 'UPHELD' ? 'Uphold Violation' : 'Submit Review'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
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
