'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useParams } from 'next/navigation';
import apiClient from '@/lib/api';

type Provider = {
  id: number;
  providerNumber: string;
  firstName: string;
  lastName: string;
  middleName: string;
  ssn: string;
  ssnVerificationStatus: string;
  dateOfBirth: string;
  providerStatus: string;
  dojCountyCode: string;
  eligible: string;
  ineligibleReason: string;
  soc426Signed: boolean;
  orientationCompleted: boolean;
  soc846Signed: boolean;
  workweekAgreementSigned: boolean;
  backgroundCheckCleared: boolean;
  enrollmentDate: string;
  terminationDate: string;
  terminationReason: string;
  sickLeaveEligible: boolean;
  sickLeaveHoursAccrued: number;
  lastSickLeaveAccrualDate: string;
  residenceAddress: string;
  residenceCity: string;
  residenceState: string;
  residenceZip: string;
  phoneNumber: string;
  email: string;
  createdDate: string;
  createdBy: string;
  updatedDate: string;
  updatedBy: string;
};

type CoriRecord = {
  id: number;
  providerId: number;
  coriStatus: string;
  coriTier: number;
  coriSubmissionDate: string;
  coriExpirationDate: string;
  coriClearanceDate: string;
  hasRecipientWaiver: boolean;
  hasGeneralException: boolean;
  generalExceptionEndDate: string;
};

type Violation = {
  id: number;
  providerId: number;
  violationType: string;
  violationStatus: string;
  month: number;
  year: number;
  hoursClaimed: number;
  maximumAllowed: number;
  countyReviewOutcome: string;
  supervisorReviewOutcome: string;
  trainingCompleted: boolean;
};

type Assignment = {
  id: number;
  providerId: number;
  caseId: number;
  providerType: string;
  relationship: string;
  assignedHours: number;
  assignmentStatus: string;
  effectiveDate: string;
  terminationDate: string;
};

export default function ProviderDetailPage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const router = useRouter();
  const params = useParams();
  const providerId = params.id as string;

  const [loading, setLoading] = useState(true);
  const [provider, setProvider] = useState<Provider | null>(null);
  const [coriRecords, setCoriRecords] = useState<CoriRecord[]>([]);
  const [violations, setViolations] = useState<Violation[]>([]);
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [activeTab, setActiveTab] = useState('overview');
  const [showActionModal, setShowActionModal] = useState(false);
  const [actionType, setActionType] = useState('');
  const [actionReason, setActionReason] = useState('');

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchProviderDetails();
  }, [user, authLoading, providerId]);

  const fetchProviderDetails = async () => {
    try {
      setLoading(true);
      const [providerResponse, coriResponse, violationsResponse, assignmentsResponse] = await Promise.all([
        apiClient.get(`/providers/${providerId}`),
        apiClient.get(`/providers/${providerId}/cori`).catch(() => ({ data: [] })),
        apiClient.get(`/providers/${providerId}/violations`).catch(() => ({ data: [] })),
        apiClient.get(`/providers/${providerId}/assignments`).catch(() => ({ data: [] }))
      ]);

      setProvider(providerResponse.data);
      setCoriRecords(coriResponse.data || []);
      setViolations(violationsResponse.data || []);
      setAssignments(assignmentsResponse.data || []);
    } catch (err) {
      console.error('Error fetching provider details:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleProviderAction = async () => {
    try {
      let endpoint = '';
      let body: any = {};

      switch (actionType) {
        case 'approve':
          endpoint = `/providers/${providerId}/approve-enrollment`;
          break;
        case 'ineligible':
          endpoint = `/providers/${providerId}/set-ineligible`;
          body = { reason: actionReason };
          break;
        case 'reinstate':
          endpoint = `/providers/${providerId}/reinstate`;
          break;
        case 're-enroll':
          endpoint = `/providers/${providerId}/re-enroll`;
          break;
      }

      await apiClient.put(endpoint, body);
      setShowActionModal(false);
      setActionReason('');
      fetchProviderDetails();
    } catch (err) {
      console.error('Error performing provider action:', err);
      alert('Failed to perform action');
    }
  };

  const openActionModal = (type: string) => {
    setActionType(type);
    setShowActionModal(true);
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'bg-success';
      case 'ON_LEAVE': return 'bg-warning text-dark';
      case 'TERMINATED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  };

  const getViolationStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'PENDING_REVIEW': return 'bg-warning text-dark';
      case 'ACTIVE': return 'bg-danger';
      case 'INACTIVE': return 'bg-secondary';
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
          <p className="text-muted mb-0">Loading Provider Details...</p>
        </div>
      </div>
    );
  }

  if (!provider) {
    return (
      <div className="container-fluid py-4">
        <div className="alert alert-danger">Provider not found</div>
        <button className="btn btn-primary" onClick={() => router.push('/providers')}>
          Back to Providers
        </button>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.push('/providers')}>
            <i className="bi bi-arrow-left me-2"></i>Back to Providers
          </button>
          <h1 className="h3 mb-0">
            {provider.firstName} {provider.lastName}
            <span className={`badge ${getStatusBadgeClass(provider.providerStatus)} ms-3`}>
              {provider.providerStatus?.replace(/_/g, ' ')}
            </span>
          </h1>
          <p className="text-muted mb-0">Provider #: {provider.providerNumber || 'Not Assigned'}</p>
        </div>
        <div className="btn-group">
          {!provider.enrollmentDate && (
            <button className="btn btn-success" onClick={() => openActionModal('approve')}>
              <i className="bi bi-check-lg me-2"></i>Approve Enrollment
            </button>
          )}
          {provider.providerStatus === 'ACTIVE' && (
            <button className="btn btn-danger" onClick={() => openActionModal('ineligible')}>
              <i className="bi bi-x-lg me-2"></i>Set Ineligible
            </button>
          )}
          {provider.providerStatus === 'TERMINATED' && (
            <>
              <button className="btn btn-success" onClick={() => openActionModal('reinstate')}>
                <i className="bi bi-arrow-repeat me-2"></i>Reinstate
              </button>
              <button className="btn btn-primary" onClick={() => openActionModal('re-enroll')}>
                <i className="bi bi-plus-lg me-2"></i>Re-Enroll
              </button>
            </>
          )}
        </div>
      </div>

      {/* Quick Links */}
      <div className="mb-4">
        <div className="btn-group flex-wrap">
          <button className="btn btn-outline-primary" onClick={() => router.push(`/providers/${providerId}/workweek`)}>
            <i className="bi bi-calendar-week me-2"></i>Workweek Agreement
          </button>
          <button className="btn btn-outline-primary" onClick={() => router.push(`/providers/${providerId}/sick-leave`)}>
            <i className="bi bi-heart-pulse me-2"></i>Sick Leave
          </button>
          <button className="btn btn-outline-primary" onClick={() => router.push(`/providers/${providerId}/travel-time`)}>
            <i className="bi bi-car-front me-2"></i>Travel Time
          </button>
        </div>
      </div>

      {/* Tabs */}
      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'overview' ? 'active' : ''}`}
            onClick={() => setActiveTab('overview')}
          >
            Overview
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'enrollment' ? 'active' : ''}`}
            onClick={() => setActiveTab('enrollment')}
          >
            Enrollment
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'cori' ? 'active' : ''}`}
            onClick={() => setActiveTab('cori')}
          >
            CORI ({coriRecords.length})
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'violations' ? 'active' : ''}`}
            onClick={() => setActiveTab('violations')}
          >
            Violations ({violations.length})
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'assignments' ? 'active' : ''}`}
            onClick={() => setActiveTab('assignments')}
          >
            Assignments ({assignments.length})
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'sickleave' ? 'active' : ''}`}
            onClick={() => setActiveTab('sickleave')}
          >
            Sick Leave
          </button>
        </li>
      </ul>

      {/* Tab Content */}
      {activeTab === 'overview' && (
        <div className="row">
          <div className="col-md-6">
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Personal Information</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Provider Number</th>
                      <td>{provider.providerNumber || '-'}</td>
                    </tr>
                    <tr>
                      <th>Name</th>
                      <td>{provider.firstName} {provider.middleName} {provider.lastName}</td>
                    </tr>
                    <tr>
                      <th>Date of Birth</th>
                      <td>{provider.dateOfBirth || '-'}</td>
                    </tr>
                    <tr>
                      <th>SSN</th>
                      <td>
                        {provider.ssn ? '***-**-' + provider.ssn.slice(-4) : '-'}
                        <span className={`badge ms-2 ${provider.ssnVerificationStatus === 'VERIFIED' ? 'bg-success' : 'bg-warning text-dark'}`}>
                          {provider.ssnVerificationStatus || 'Not Verified'}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>DOJ County Code</th>
                      <td>{provider.dojCountyCode || '-'}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Contact Information</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Address</th>
                      <td>
                        {provider.residenceAddress || '-'}<br />
                        {provider.residenceCity}, {provider.residenceState} {provider.residenceZip}
                      </td>
                    </tr>
                    <tr>
                      <th>Phone</th>
                      <td>{provider.phoneNumber || '-'}</td>
                    </tr>
                    <tr>
                      <th>Email</th>
                      <td>{provider.email || '-'}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Status & Eligibility</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Status</th>
                      <td>
                        <span className={`badge ${getStatusBadgeClass(provider.providerStatus)}`}>
                          {provider.providerStatus?.replace(/_/g, ' ')}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>Eligible</th>
                      <td>
                        <span className={`badge ${provider.eligible === 'YES' ? 'bg-success' : 'bg-danger'}`}>
                          {provider.eligible || '-'}
                        </span>
                      </td>
                    </tr>
                    {provider.ineligibleReason && (
                      <tr>
                        <th>Ineligible Reason</th>
                        <td className="text-danger">{provider.ineligibleReason}</td>
                      </tr>
                    )}
                    <tr>
                      <th>Enrollment Date</th>
                      <td>{provider.enrollmentDate || '-'}</td>
                    </tr>
                    {provider.terminationDate && (
                      <>
                        <tr>
                          <th>Termination Date</th>
                          <td>{provider.terminationDate}</td>
                        </tr>
                        <tr>
                          <th>Termination Reason</th>
                          <td>{provider.terminationReason || '-'}</td>
                        </tr>
                      </>
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Summary</h5>
              </div>
              <div className="card-body">
                <div className="row text-center">
                  <div className="col-4">
                    <div className="fw-bold fs-4">{assignments.filter(a => a.assignmentStatus === 'ACTIVE').length}</div>
                    <small className="text-muted">Active Assignments</small>
                  </div>
                  <div className="col-4">
                    <div className="fw-bold fs-4 text-danger">{violations.filter(v => v.violationStatus !== 'INACTIVE').length}</div>
                    <small className="text-muted">Active Violations</small>
                  </div>
                  <div className="col-4">
                    <div className="fw-bold fs-4 text-success">{provider.sickLeaveHoursAccrued || 0}</div>
                    <small className="text-muted">Sick Leave Hours</small>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'enrollment' && (
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Enrollment Requirements</h5>
          </div>
          <div className="card-body">
            <div className="row">
              <div className="col-md-6">
                <div className="list-group">
                  <div className="list-group-item d-flex justify-content-between align-items-center">
                    <span>SOC 426 Signed</span>
                    {provider.soc426Signed ? (
                      <span className="badge bg-success"><i className="bi bi-check"></i> Complete</span>
                    ) : (
                      <span className="badge bg-danger"><i className="bi bi-x"></i> Pending</span>
                    )}
                  </div>
                  <div className="list-group-item d-flex justify-content-between align-items-center">
                    <span>Orientation Completed</span>
                    {provider.orientationCompleted ? (
                      <span className="badge bg-success"><i className="bi bi-check"></i> Complete</span>
                    ) : (
                      <span className="badge bg-danger"><i className="bi bi-x"></i> Pending</span>
                    )}
                  </div>
                  <div className="list-group-item d-flex justify-content-between align-items-center">
                    <span>SOC 846 Signed</span>
                    {provider.soc846Signed ? (
                      <span className="badge bg-success"><i className="bi bi-check"></i> Complete</span>
                    ) : (
                      <span className="badge bg-danger"><i className="bi bi-x"></i> Pending</span>
                    )}
                  </div>
                  <div className="list-group-item d-flex justify-content-between align-items-center">
                    <span>Workweek Agreement Signed</span>
                    {provider.workweekAgreementSigned ? (
                      <span className="badge bg-success"><i className="bi bi-check"></i> Complete</span>
                    ) : (
                      <span className="badge bg-danger"><i className="bi bi-x"></i> Pending</span>
                    )}
                  </div>
                  <div className="list-group-item d-flex justify-content-between align-items-center">
                    <span>Background Check Cleared</span>
                    {provider.backgroundCheckCleared ? (
                      <span className="badge bg-success"><i className="bi bi-check"></i> Complete</span>
                    ) : (
                      <span className="badge bg-danger"><i className="bi bi-x"></i> Pending</span>
                    )}
                  </div>
                </div>
              </div>
              <div className="col-md-6">
                <div className="alert alert-info">
                  <h6>Enrollment Status</h6>
                  <p className="mb-0">
                    {provider.enrollmentDate ? (
                      <>Provider was enrolled on <strong>{provider.enrollmentDate}</strong></>
                    ) : (
                      <>Provider enrollment is <strong>pending</strong>. All requirements must be completed before enrollment can be approved.</>
                    )}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'cori' && (
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">CORI Records</h5>
            <button className="btn btn-light btn-sm" onClick={() => router.push(`/providers/${providerId}/cori/new`)}>
              <i className="bi bi-plus-lg me-2"></i>New CORI
            </button>
          </div>
          <div className="card-body">
            {coriRecords.length === 0 ? (
              <p className="text-muted text-center py-4">No CORI records found</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Status</th>
                      <th>Tier</th>
                      <th>Submission Date</th>
                      <th>Clearance Date</th>
                      <th>Expiration Date</th>
                      <th>Waivers</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {coriRecords.map((cori) => (
                      <tr key={cori.id}>
                        <td>
                          <span className={`badge ${cori.coriStatus === 'CLEARED' ? 'bg-success' : 'bg-warning text-dark'}`}>
                            {cori.coriStatus}
                          </span>
                        </td>
                        <td>Tier {cori.coriTier}</td>
                        <td>{cori.coriSubmissionDate || '-'}</td>
                        <td>{cori.coriClearanceDate || '-'}</td>
                        <td>{cori.coriExpirationDate || '-'}</td>
                        <td>
                          {cori.hasRecipientWaiver && <span className="badge bg-info me-1">Recipient</span>}
                          {cori.hasGeneralException && <span className="badge bg-warning text-dark">General</span>}
                        </td>
                        <td>
                          <button className="btn btn-sm btn-outline-primary" onClick={() => router.push(`/providers/cori/${cori.id}`)}>
                            View
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'violations' && (
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Overtime Violations</h5>
            <button className="btn btn-light btn-sm" onClick={() => router.push(`/providers/${providerId}/violations/new`)}>
              <i className="bi bi-plus-lg me-2"></i>Record Violation
            </button>
          </div>
          <div className="card-body">
            {violations.length === 0 ? (
              <p className="text-muted text-center py-4">No violations found</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Period</th>
                      <th>Type</th>
                      <th>Hours Claimed</th>
                      <th>Maximum Allowed</th>
                      <th>Status</th>
                      <th>County Review</th>
                      <th>Supervisor Review</th>
                      <th>Training</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {violations.map((violation) => (
                      <tr key={violation.id}>
                        <td>{violation.month}/{violation.year}</td>
                        <td>{violation.violationType}</td>
                        <td className="text-danger fw-bold">{violation.hoursClaimed}</td>
                        <td>{violation.maximumAllowed}</td>
                        <td>
                          <span className={`badge ${getViolationStatusBadgeClass(violation.violationStatus)}`}>
                            {violation.violationStatus?.replace(/_/g, ' ')}
                          </span>
                        </td>
                        <td>{violation.countyReviewOutcome || '-'}</td>
                        <td>{violation.supervisorReviewOutcome || '-'}</td>
                        <td>
                          {violation.trainingCompleted ? (
                            <i className="bi bi-check-circle text-success"></i>
                          ) : (
                            <i className="bi bi-x-circle text-danger"></i>
                          )}
                        </td>
                        <td>
                          <button className="btn btn-sm btn-outline-primary" onClick={() => router.push(`/providers/violations/${violation.id}`)}>
                            Review
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'assignments' && (
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Case Assignments</h5>
            <button className="btn btn-light btn-sm" onClick={() => router.push(`/providers/assignments/new?providerId=${providerId}`)}>
              <i className="bi bi-plus-lg me-2"></i>New Assignment
            </button>
          </div>
          <div className="card-body">
            {assignments.length === 0 ? (
              <p className="text-muted text-center py-4">No assignments found</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Case ID</th>
                      <th>Type</th>
                      <th>Relationship</th>
                      <th>Assigned Hours</th>
                      <th>Status</th>
                      <th>Effective Date</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {assignments.map((assignment) => (
                      <tr key={assignment.id}>
                        <td>
                          <a href="#" onClick={(e) => { e.preventDefault(); router.push(`/cases/${assignment.caseId}`); }}>
                            Case #{assignment.caseId}
                          </a>
                        </td>
                        <td>{assignment.providerType}</td>
                        <td>{assignment.relationship}</td>
                        <td>{assignment.assignedHours} hrs</td>
                        <td>
                          <span className={`badge ${assignment.assignmentStatus === 'ACTIVE' ? 'bg-success' : 'bg-secondary'}`}>
                            {assignment.assignmentStatus}
                          </span>
                        </td>
                        <td>{assignment.effectiveDate || '-'}</td>
                        <td>
                          <button className="btn btn-sm btn-outline-primary" onClick={() => router.push(`/providers/assignments/${assignment.id}`)}>
                            View
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'sickleave' && (
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Sick Leave</h5>
          </div>
          <div className="card-body">
            <div className="row">
              <div className="col-md-6">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '50%' }}>Eligible for Sick Leave</th>
                      <td>
                        {provider.sickLeaveEligible ? (
                          <span className="badge bg-success">Yes</span>
                        ) : (
                          <span className="badge bg-secondary">No</span>
                        )}
                      </td>
                    </tr>
                    <tr>
                      <th>Hours Accrued</th>
                      <td className="fw-bold fs-5">{provider.sickLeaveHoursAccrued || 0} hours</td>
                    </tr>
                    <tr>
                      <th>Last Accrual Date</th>
                      <td>{provider.lastSickLeaveAccrualDate || '-'}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div className="col-md-6">
                <div className="alert alert-info">
                  <h6>Sick Leave Information</h6>
                  <p className="mb-0 small">
                    Providers accrue 1 hour of sick leave for every 30 hours worked, up to a maximum of 24 hours per year.
                    Sick leave accrues on the anniversary of the provider&apos;s enrollment date.
                  </p>
                </div>
                <button className="btn btn-primary" onClick={() => apiClient.put(`/providers/${providerId}/check-sick-leave-accrual`)}>
                  <i className="bi bi-arrow-clockwise me-2"></i>Check Accrual
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Action Modal */}
      {showActionModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  {actionType === 'approve' && 'Approve Enrollment'}
                  {actionType === 'ineligible' && 'Set Provider Ineligible'}
                  {actionType === 'reinstate' && 'Reinstate Provider'}
                  {actionType === 're-enroll' && 'Re-Enroll Provider'}
                </h5>
                <button type="button" className="btn-close" onClick={() => setShowActionModal(false)}></button>
              </div>
              <div className="modal-body">
                {actionType === 'ineligible' && (
                  <div className="mb-3">
                    <label className="form-label">Reason for Ineligibility</label>
                    <textarea
                      className="form-control"
                      rows={3}
                      value={actionReason}
                      onChange={(e) => setActionReason(e.target.value)}
                      placeholder="Enter reason..."
                      required
                    />
                  </div>
                )}
                {actionType === 'approve' && (
                  <p>Are you sure you want to approve this provider&apos;s enrollment? All requirements will be verified.</p>
                )}
                {actionType === 'reinstate' && (
                  <p>Are you sure you want to reinstate this provider? This action is only available within 30 days of termination.</p>
                )}
                {actionType === 're-enroll' && (
                  <p>Are you sure you want to re-enroll this provider? This will start a new enrollment process.</p>
                )}
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowActionModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className={`btn ${actionType === 'ineligible' ? 'btn-danger' : 'btn-success'}`}
                  onClick={handleProviderAction}
                  disabled={actionType === 'ineligible' && !actionReason}
                >
                  Confirm
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
