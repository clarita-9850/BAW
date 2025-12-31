'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useParams } from 'next/navigation';
import apiClient from '@/lib/api';

type Violation = {
  id: number;
  providerId: number;
  providerNumber: string;
  providerFirstName: string;
  providerLastName: string;
  violationNumber: number; // 1-4 progressive
  violationType: string;
  violationStatus: string;
  month: number;
  year: number;
  hoursClaimed: number;
  maximumAllowed: number;
  excessHours: number;

  // Review workflow stages
  countyReviewStatus: string;
  countyReviewDate: string;
  countyReviewOutcome: string;
  countyReviewNotes: string;
  countyReviewedBy: string;

  supervisorReviewStatus: string;
  supervisorReviewDate: string;
  supervisorReviewOutcome: string;
  supervisorReviewNotes: string;
  supervisorReviewedBy: string;

  countyDisputeStatus: string;
  countyDisputeDate: string;
  countyDisputeOutcome: string;
  countyDisputeNotes: string;

  cdssReviewStatus: string;
  cdssReviewDate: string;
  cdssReviewOutcome: string;
  cdssReviewNotes: string;

  // Training requirement
  trainingRequired: boolean;
  trainingCompleted: boolean;
  trainingCompletedDate: string;
  trainingDeadline: string;

  // Suspension details (violations 3 & 4)
  suspensionStartDate: string;
  suspensionEndDate: string;
  suspensionDays: number;

  // Re-enrollment required (violation 4)
  requiresReEnrollment: boolean;
  reEnrollmentStatus: string;

  createdDate: string;
  updatedDate: string;
};

type ReviewAction = {
  action: string;
  outcome: string;
  notes: string;
};

export default function ViolationReviewPage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const router = useRouter();
  const params = useParams();
  const violationId = params.id as string;

  const [loading, setLoading] = useState(true);
  const [violation, setViolation] = useState<Violation | null>(null);
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [reviewStage, setReviewStage] = useState('');
  const [reviewAction, setReviewAction] = useState<ReviewAction>({
    action: '',
    outcome: '',
    notes: ''
  });
  const [showTrainingModal, setShowTrainingModal] = useState(false);
  const [trainingDate, setTrainingDate] = useState('');
  const [history, setHistory] = useState<any[]>([]);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchViolationDetails();
  }, [user, authLoading, violationId]);

  const fetchViolationDetails = async () => {
    try {
      setLoading(true);
      const [violationResponse, historyResponse] = await Promise.all([
        apiClient.get(`/providers/violations/${violationId}`),
        apiClient.get(`/providers/violations/${violationId}/history`).catch(() => ({ data: [] }))
      ]);
      setViolation(violationResponse.data);
      setHistory(historyResponse.data || []);
    } catch (err) {
      console.error('Error fetching violation details:', err);
    } finally {
      setLoading(false);
    }
  };

  const openReviewModal = (stage: string) => {
    setReviewStage(stage);
    setReviewAction({ action: '', outcome: '', notes: '' });
    setShowReviewModal(true);
  };

  const handleSubmitReview = async () => {
    try {
      let endpoint = '';
      switch (reviewStage) {
        case 'county':
          endpoint = `/providers/violations/${violationId}/county-review`;
          break;
        case 'supervisor':
          endpoint = `/providers/violations/${violationId}/supervisor-review`;
          break;
        case 'county-dispute':
          endpoint = `/providers/violations/${violationId}/county-dispute`;
          break;
        case 'cdss':
          endpoint = `/providers/violations/${violationId}/cdss-review`;
          break;
      }

      await apiClient.put(endpoint, {
        outcome: reviewAction.outcome,
        notes: reviewAction.notes
      });

      setShowReviewModal(false);
      fetchViolationDetails();
    } catch (err) {
      console.error('Error submitting review:', err);
      alert('Failed to submit review');
    }
  };

  const handleRecordTraining = async () => {
    try {
      await apiClient.put(`/providers/violations/${violationId}/record-training`, {
        completedDate: trainingDate
      });
      setShowTrainingModal(false);
      setTrainingDate('');
      fetchViolationDetails();
    } catch (err) {
      console.error('Error recording training:', err);
      alert('Failed to record training');
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'PENDING': return 'bg-warning text-dark';
      case 'COMPLETED':
      case 'UPHELD':
      case 'CONFIRMED': return 'bg-success';
      case 'OVERTURNED':
      case 'EXCEPTION_GRANTED': return 'bg-info';
      case 'ACTIVE': return 'bg-danger';
      case 'INACTIVE': return 'bg-secondary';
      default: return 'bg-secondary';
    }
  };

  const getViolationSeverityInfo = (violationNum: number) => {
    switch (violationNum) {
      case 1:
        return {
          level: 'First Violation',
          consequence: 'Written Warning',
          color: 'warning',
          description: 'Provider receives a written warning. No immediate penalty.'
        };
      case 2:
        return {
          level: 'Second Violation',
          consequence: 'Training Required',
          color: 'warning',
          description: 'Provider must complete overtime violation training within 30 days.'
        };
      case 3:
        return {
          level: 'Third Violation',
          consequence: '90-Day Suspension',
          color: 'danger',
          description: 'Provider is suspended for 90 days and cannot work or receive payment.'
        };
      case 4:
        return {
          level: 'Fourth Violation',
          consequence: '365-Day Suspension + Re-Enrollment',
          color: 'danger',
          description: 'Provider is suspended for 365 days and must re-enroll after suspension ends.'
        };
      default:
        return {
          level: 'Unknown',
          consequence: 'Unknown',
          color: 'secondary',
          description: ''
        };
    }
  };

  const getWorkflowSteps = () => {
    if (!violation) return [];

    const steps = [
      {
        id: 'county',
        title: 'County Review',
        status: violation.countyReviewStatus,
        date: violation.countyReviewDate,
        outcome: violation.countyReviewOutcome,
        notes: violation.countyReviewNotes,
        reviewedBy: violation.countyReviewedBy,
        canReview: violation.countyReviewStatus === 'PENDING',
        outcomes: ['UPHELD', 'OVERTURNED', 'EXCEPTION_GRANTED']
      },
      {
        id: 'supervisor',
        title: 'Supervisor Review',
        status: violation.supervisorReviewStatus,
        date: violation.supervisorReviewDate,
        outcome: violation.supervisorReviewOutcome,
        notes: violation.supervisorReviewNotes,
        reviewedBy: violation.supervisorReviewedBy,
        canReview: violation.countyReviewStatus === 'COMPLETED' &&
                   violation.countyReviewOutcome === 'UPHELD' &&
                   violation.supervisorReviewStatus === 'PENDING',
        outcomes: ['CONFIRMED', 'OVERTURNED']
      },
      {
        id: 'county-dispute',
        title: 'County Dispute',
        status: violation.countyDisputeStatus,
        date: violation.countyDisputeDate,
        outcome: violation.countyDisputeOutcome,
        notes: violation.countyDisputeNotes,
        canReview: violation.supervisorReviewStatus === 'COMPLETED' &&
                   violation.supervisorReviewOutcome === 'CONFIRMED' &&
                   violation.countyDisputeStatus === 'PENDING',
        outcomes: ['UPHELD', 'DISPUTE_FILED']
      },
      {
        id: 'cdss',
        title: 'CDSS Review',
        status: violation.cdssReviewStatus,
        date: violation.cdssReviewDate,
        outcome: violation.cdssReviewOutcome,
        notes: violation.cdssReviewNotes,
        canReview: violation.countyDisputeStatus === 'COMPLETED' &&
                   violation.countyDisputeOutcome === 'DISPUTE_FILED' &&
                   violation.cdssReviewStatus === 'PENDING',
        outcomes: ['UPHELD', 'OVERTURNED']
      }
    ];

    return steps;
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading Violation Details...</p>
        </div>
      </div>
    );
  }

  if (!violation) {
    return (
      <div className="container-fluid py-4">
        <div className="alert alert-danger">Violation not found</div>
        <button className="btn btn-primary" onClick={() => router.push('/providers/violations/pending')}>
          Back to Violations
        </button>
      </div>
    );
  }

  const severityInfo = getViolationSeverityInfo(violation.violationNumber);
  const workflowSteps = getWorkflowSteps();

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.push('/providers/violations/pending')}>
            <i className="bi bi-arrow-left me-2"></i>Back to Violations
          </button>
          <h1 className="h3 mb-0">
            Overtime Violation Review
            <span className={`badge bg-${severityInfo.color} ms-3`}>
              {severityInfo.level}
            </span>
          </h1>
          <p className="text-muted mb-0">
            Provider: {violation.providerFirstName} {violation.providerLastName} ({violation.providerNumber})
          </p>
        </div>
        <div>
          <span className={`badge ${getStatusBadgeClass(violation.violationStatus)} fs-6`}>
            {violation.violationStatus?.replace(/_/g, ' ')}
          </span>
        </div>
      </div>

      {/* Severity Alert */}
      <div className={`alert alert-${severityInfo.color} mb-4`}>
        <h5 className="alert-heading">{severityInfo.consequence}</h5>
        <p className="mb-0">{severityInfo.description}</p>
        {violation.violationNumber >= 3 && violation.suspensionStartDate && (
          <>
            <hr />
            <p className="mb-0">
              <strong>Suspension Period:</strong> {violation.suspensionStartDate} to {violation.suspensionEndDate} ({violation.suspensionDays} days)
            </p>
          </>
        )}
      </div>

      <div className="row">
        {/* Violation Details */}
        <div className="col-md-4">
          <div className="card mb-4">
            <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
              <h5 className="mb-0">Violation Details</h5>
            </div>
            <div className="card-body">
              <table className="table table-borderless">
                <tbody>
                  <tr>
                    <th style={{ width: '45%' }}>Period</th>
                    <td>{violation.month}/{violation.year}</td>
                  </tr>
                  <tr>
                    <th>Type</th>
                    <td>{violation.violationType?.replace(/_/g, ' ')}</td>
                  </tr>
                  <tr>
                    <th>Hours Claimed</th>
                    <td className="text-danger fw-bold">{violation.hoursClaimed}</td>
                  </tr>
                  <tr>
                    <th>Maximum Allowed</th>
                    <td>{violation.maximumAllowed}</td>
                  </tr>
                  <tr>
                    <th>Excess Hours</th>
                    <td className="text-danger fw-bold">{violation.excessHours}</td>
                  </tr>
                  <tr>
                    <th>Violation #</th>
                    <td>
                      <span className={`badge bg-${severityInfo.color}`}>
                        #{violation.violationNumber} of 4
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          {/* Training Requirement (Violation #2) */}
          {violation.trainingRequired && (
            <div className="card mb-4">
              <div className="card-header bg-warning text-dark">
                <h5 className="mb-0">Training Requirement</h5>
              </div>
              <div className="card-body">
                <p className="mb-2">
                  <strong>Status:</strong>{' '}
                  {violation.trainingCompleted ? (
                    <span className="badge bg-success">Completed</span>
                  ) : (
                    <span className="badge bg-danger">Required</span>
                  )}
                </p>
                {violation.trainingDeadline && (
                  <p className="mb-2">
                    <strong>Deadline:</strong> {violation.trainingDeadline}
                  </p>
                )}
                {violation.trainingCompletedDate && (
                  <p className="mb-2">
                    <strong>Completed:</strong> {violation.trainingCompletedDate}
                  </p>
                )}
                {!violation.trainingCompleted && (
                  <button
                    className="btn btn-success mt-2"
                    onClick={() => setShowTrainingModal(true)}
                  >
                    <i className="bi bi-check-lg me-2"></i>Record Training Completion
                  </button>
                )}
              </div>
            </div>
          )}

          {/* Re-Enrollment (Violation #4) */}
          {violation.requiresReEnrollment && (
            <div className="card mb-4">
              <div className="card-header bg-danger text-white">
                <h5 className="mb-0">Re-Enrollment Required</h5>
              </div>
              <div className="card-body">
                <p className="mb-2">
                  <strong>Status:</strong>{' '}
                  <span className={`badge ${violation.reEnrollmentStatus === 'COMPLETED' ? 'bg-success' : 'bg-warning text-dark'}`}>
                    {violation.reEnrollmentStatus || 'PENDING'}
                  </span>
                </p>
                <p className="small text-muted">
                  Provider must complete re-enrollment process after suspension ends.
                </p>
                {violation.suspensionEndDate && (
                  <button
                    className="btn btn-primary"
                    onClick={() => router.push(`/providers/${violation.providerId}`)}
                    disabled={new Date(violation.suspensionEndDate) > new Date()}
                  >
                    <i className="bi bi-arrow-right me-2"></i>Go to Re-Enrollment
                  </button>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Review Workflow */}
        <div className="col-md-8">
          <div className="card mb-4">
            <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
              <h5 className="mb-0">Review Workflow</h5>
            </div>
            <div className="card-body">
              {/* Workflow Progress */}
              <div className="d-flex justify-content-between mb-4 position-relative">
                {workflowSteps.map((step, index) => (
                  <div key={step.id} className="text-center" style={{ flex: 1 }}>
                    <div
                      className={`rounded-circle mx-auto d-flex align-items-center justify-content-center mb-2`}
                      style={{
                        width: '50px',
                        height: '50px',
                        backgroundColor: step.status === 'COMPLETED' ? '#198754' :
                                        step.status === 'PENDING' ? '#ffc107' : '#e9ecef',
                        color: step.status === 'COMPLETED' || step.status === 'PENDING' ? 'white' : '#6c757d'
                      }}
                    >
                      {step.status === 'COMPLETED' ? (
                        <i className="bi bi-check-lg"></i>
                      ) : (
                        <span>{index + 1}</span>
                      )}
                    </div>
                    <small className="text-muted">{step.title}</small>
                    {step.outcome && (
                      <div>
                        <span className={`badge ${getStatusBadgeClass(step.outcome)} mt-1`}>
                          {step.outcome}
                        </span>
                      </div>
                    )}
                  </div>
                ))}
              </div>

              <hr />

              {/* Step Details */}
              <div className="accordion" id="workflowAccordion">
                {workflowSteps.map((step, index) => (
                  <div className="accordion-item" key={step.id}>
                    <h2 className="accordion-header">
                      <button
                        className={`accordion-button ${index !== 0 ? 'collapsed' : ''}`}
                        type="button"
                        data-bs-toggle="collapse"
                        data-bs-target={`#collapse-${step.id}`}
                      >
                        <span className={`badge ${getStatusBadgeClass(step.status)} me-2`}>
                          {step.status || 'NOT STARTED'}
                        </span>
                        {step.title}
                      </button>
                    </h2>
                    <div
                      id={`collapse-${step.id}`}
                      className={`accordion-collapse collapse ${index === 0 ? 'show' : ''}`}
                      data-bs-parent="#workflowAccordion"
                    >
                      <div className="accordion-body">
                        {step.status === 'COMPLETED' ? (
                          <div>
                            <p><strong>Outcome:</strong> <span className={`badge ${getStatusBadgeClass(step.outcome)}`}>{step.outcome}</span></p>
                            <p><strong>Date:</strong> {step.date}</p>
                            {step.reviewedBy && <p><strong>Reviewed By:</strong> {step.reviewedBy}</p>}
                            {step.notes && <p><strong>Notes:</strong> {step.notes}</p>}
                          </div>
                        ) : step.canReview ? (
                          <div>
                            <p className="text-muted">This step is ready for review.</p>
                            <button
                              className="btn btn-primary"
                              onClick={() => openReviewModal(step.id)}
                            >
                              <i className="bi bi-pencil me-2"></i>Submit Review
                            </button>
                          </div>
                        ) : (
                          <p className="text-muted">Waiting for previous steps to complete.</p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Review History */}
          <div className="card">
            <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
              <h5 className="mb-0">Review History</h5>
            </div>
            <div className="card-body">
              {history.length === 0 ? (
                <p className="text-muted text-center py-3">No history available</p>
              ) : (
                <div className="timeline">
                  {history.map((item, index) => (
                    <div key={index} className="d-flex mb-3 pb-3 border-bottom">
                      <div className="me-3">
                        <div
                          className="rounded-circle d-flex align-items-center justify-content-center"
                          style={{
                            width: '40px',
                            height: '40px',
                            backgroundColor: 'var(--color-p2, #046b99)',
                            color: 'white'
                          }}
                        >
                          <i className="bi bi-clock-history"></i>
                        </div>
                      </div>
                      <div>
                        <p className="mb-1"><strong>{item.action}</strong></p>
                        <p className="mb-1 small text-muted">{item.timestamp} by {item.performedBy}</p>
                        {item.notes && <p className="mb-0 small">{item.notes}</p>}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Review Modal */}
      {showReviewModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  {reviewStage === 'county' && 'County Review'}
                  {reviewStage === 'supervisor' && 'Supervisor Review'}
                  {reviewStage === 'county-dispute' && 'County Dispute'}
                  {reviewStage === 'cdss' && 'CDSS Review'}
                </h5>
                <button type="button" className="btn-close" onClick={() => setShowReviewModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label">Outcome</label>
                  <select
                    className="form-select"
                    value={reviewAction.outcome}
                    onChange={(e) => setReviewAction({ ...reviewAction, outcome: e.target.value })}
                    required
                  >
                    <option value="">Select outcome...</option>
                    {workflowSteps.find(s => s.id === reviewStage)?.outcomes.map(outcome => (
                      <option key={outcome} value={outcome}>
                        {outcome.replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label">Notes</label>
                  <textarea
                    className="form-control"
                    rows={4}
                    value={reviewAction.notes}
                    onChange={(e) => setReviewAction({ ...reviewAction, notes: e.target.value })}
                    placeholder="Enter review notes..."
                  />
                </div>

                {/* Stage-specific guidance */}
                <div className="alert alert-info">
                  {reviewStage === 'county' && (
                    <>
                      <h6>County Review Guidelines (BR PM 56-59)</h6>
                      <ul className="mb-0 small">
                        <li><strong>UPHELD:</strong> Violation is confirmed, escalate to Supervisor Review</li>
                        <li><strong>OVERTURNED:</strong> Hours were correctly authorized (e.g., medical appointment overtime)</li>
                        <li><strong>EXCEPTION_GRANTED:</strong> Special circumstances warrant exception</li>
                      </ul>
                    </>
                  )}
                  {reviewStage === 'supervisor' && (
                    <>
                      <h6>Supervisor Review Guidelines (BR PM 60-63)</h6>
                      <ul className="mb-0 small">
                        <li><strong>CONFIRMED:</strong> Agree with County Review, violation stands</li>
                        <li><strong>OVERTURNED:</strong> Disagree with County Review, violation dismissed</li>
                      </ul>
                    </>
                  )}
                  {reviewStage === 'county-dispute' && (
                    <>
                      <h6>County Dispute Guidelines (BR PM 64-68)</h6>
                      <ul className="mb-0 small">
                        <li><strong>UPHELD:</strong> County accepts the violation determination</li>
                        <li><strong>DISPUTE_FILED:</strong> County disputes and escalates to CDSS</li>
                      </ul>
                    </>
                  )}
                  {reviewStage === 'cdss' && (
                    <>
                      <h6>CDSS Review Guidelines (BR PM 69-73)</h6>
                      <ul className="mb-0 small">
                        <li><strong>UPHELD:</strong> Final determination - violation confirmed</li>
                        <li><strong>OVERTURNED:</strong> Final determination - violation dismissed</li>
                      </ul>
                    </>
                  )}
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowReviewModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleSubmitReview}
                  disabled={!reviewAction.outcome}
                >
                  Submit Review
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Training Modal */}
      {showTrainingModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Record Training Completion</h5>
                <button type="button" className="btn-close" onClick={() => setShowTrainingModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label">Training Completion Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={trainingDate}
                    onChange={(e) => setTrainingDate(e.target.value)}
                    max={new Date().toISOString().split('T')[0]}
                    required
                  />
                </div>
                <div className="alert alert-info">
                  <p className="mb-0 small">
                    By recording training completion, you confirm that the provider has completed
                    the required overtime violation training as per BR PM 74-77.
                  </p>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowTrainingModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-success"
                  onClick={handleRecordTraining}
                  disabled={!trainingDate}
                >
                  Record Completion
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
