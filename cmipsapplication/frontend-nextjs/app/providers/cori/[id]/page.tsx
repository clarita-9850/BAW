'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useParams } from 'next/navigation';
import apiClient from '@/lib/api';

type CoriRecord = {
  id: number;
  providerId: number;
  providerNumber: string;
  providerFirstName: string;
  providerLastName: string;

  // CORI Details
  coriStatus: string;
  coriTier: number; // 1 or 2
  coriSubmissionDate: string;
  coriResultDate: string;
  coriClearanceDate: string;
  coriExpirationDate: string;

  // Tier 1 Crimes (Automatic Ineligibility - BR PM 23-28)
  tier1Offenses: string[];
  hasTier1Offense: boolean;

  // Tier 2 Crimes (Conditional with Waivers - BR PM 29-35)
  tier2Offenses: string[];
  hasTier2Offense: boolean;

  // General Exception (BR PM 36-41)
  hasGeneralException: boolean;
  generalExceptionStatus: string;
  generalExceptionRequestDate: string;
  generalExceptionApprovalDate: string;
  generalExceptionEndDate: string;
  generalExceptionReason: string;
  generalExceptionApprovedBy: string;

  // Recipient Waiver (BR PM 42-49)
  hasRecipientWaiver: boolean;
  recipientWaiverStatus: string;
  recipientWaiverRequestDate: string;
  recipientWaiverApprovalDate: string;
  recipientWaiverRecipientId: number;
  recipientWaiverRecipientName: string;
  recipientWaiverSignedDate: string;
  recipientWaiverExpirationDate: string;

  // Fingerprint Information
  fingerprintSubmissionDate: string;
  fingerprintAgencyCode: string;

  // DOJ Information
  dojResponseDate: string;
  dojStatus: string;

  createdDate: string;
  updatedDate: string;
  notes: string;
};

type Recipient = {
  id: number;
  firstName: string;
  lastName: string;
  cin: string;
};

export default function CoriDetailPage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const router = useRouter();
  const params = useParams();
  const coriId = params.id as string;

  const [loading, setLoading] = useState(true);
  const [cori, setCori] = useState<CoriRecord | null>(null);
  const [showExceptionModal, setShowExceptionModal] = useState(false);
  const [showWaiverModal, setShowWaiverModal] = useState(false);
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [history, setHistory] = useState<any[]>([]);

  const [exceptionForm, setExceptionForm] = useState({
    reason: '',
    endDate: ''
  });

  const [waiverForm, setWaiverForm] = useState({
    recipientId: '',
    signedDate: '',
    expirationDate: ''
  });

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchCoriDetails();
  }, [user, authLoading, coriId]);

  const fetchCoriDetails = async () => {
    try {
      setLoading(true);
      const [coriResponse, historyResponse] = await Promise.all([
        apiClient.get(`/providers/cori/${coriId}`),
        apiClient.get(`/providers/cori/${coriId}/history`).catch(() => ({ data: [] }))
      ]);
      setCori(coriResponse.data);
      setHistory(historyResponse.data || []);

      // Fetch recipients for waiver if provider has assignments
      if (coriResponse.data?.providerId) {
        const recipientsResponse = await apiClient.get(`/providers/${coriResponse.data.providerId}/recipients`).catch(() => ({ data: [] }));
        setRecipients(recipientsResponse.data || []);
      }
    } catch (err) {
      console.error('Error fetching CORI details:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRequestException = async () => {
    try {
      await apiClient.post(`/providers/cori/${coriId}/general-exception`, exceptionForm);
      setShowExceptionModal(false);
      setExceptionForm({ reason: '', endDate: '' });
      fetchCoriDetails();
    } catch (err) {
      console.error('Error requesting exception:', err);
      alert('Failed to request general exception');
    }
  };

  const handleApproveException = async () => {
    try {
      await apiClient.put(`/providers/cori/${coriId}/general-exception/approve`);
      fetchCoriDetails();
    } catch (err) {
      console.error('Error approving exception:', err);
      alert('Failed to approve general exception');
    }
  };

  const handleDenyException = async () => {
    try {
      await apiClient.put(`/providers/cori/${coriId}/general-exception/deny`);
      fetchCoriDetails();
    } catch (err) {
      console.error('Error denying exception:', err);
      alert('Failed to deny general exception');
    }
  };

  const handleRequestWaiver = async () => {
    try {
      await apiClient.post(`/providers/cori/${coriId}/recipient-waiver`, waiverForm);
      setShowWaiverModal(false);
      setWaiverForm({ recipientId: '', signedDate: '', expirationDate: '' });
      fetchCoriDetails();
    } catch (err) {
      console.error('Error requesting waiver:', err);
      alert('Failed to request recipient waiver');
    }
  };

  const handleApproveWaiver = async () => {
    try {
      await apiClient.put(`/providers/cori/${coriId}/recipient-waiver/approve`);
      fetchCoriDetails();
    } catch (err) {
      console.error('Error approving waiver:', err);
      alert('Failed to approve recipient waiver');
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'CLEARED':
      case 'APPROVED':
      case 'ACTIVE': return 'bg-success';
      case 'PENDING':
      case 'PENDING_REVIEW': return 'bg-warning text-dark';
      case 'DENIED':
      case 'INELIGIBLE':
      case 'EXPIRED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  };

  const getTierInfo = (tier: number) => {
    if (tier === 1) {
      return {
        color: 'danger',
        title: 'Tier 1 - Automatic Ineligibility',
        description: 'Crimes that result in automatic and permanent ineligibility as a provider.',
        examples: ['Murder', 'Kidnapping', 'Sexual Offenses', 'Child Abuse', 'Elder Abuse']
      };
    }
    return {
      color: 'warning',
      title: 'Tier 2 - Conditional Eligibility',
      description: 'Crimes that may allow continued eligibility with a Recipient Waiver or General Exception.',
      examples: ['Theft', 'Fraud', 'Drug Offenses', 'Assault', 'DUI']
    };
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading CORI Details...</p>
        </div>
      </div>
    );
  }

  if (!cori) {
    return (
      <div className="container-fluid py-4">
        <div className="alert alert-danger">CORI record not found</div>
        <button className="btn btn-primary" onClick={() => router.back()}>
          Go Back
        </button>
      </div>
    );
  }

  const tierInfo = getTierInfo(cori.coriTier);

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.push(`/providers/${cori.providerId}`)}>
            <i className="bi bi-arrow-left me-2"></i>Back to Provider
          </button>
          <h1 className="h3 mb-0">
            CORI Record - {cori.providerFirstName} {cori.providerLastName}
            <span className={`badge ${getStatusBadgeClass(cori.coriStatus)} ms-3`}>
              {cori.coriStatus}
            </span>
          </h1>
          <p className="text-muted mb-0">Provider #: {cori.providerNumber}</p>
        </div>
      </div>

      {/* Tier Alert */}
      {(cori.hasTier1Offense || cori.hasTier2Offense) && (
        <div className={`alert alert-${tierInfo.color} mb-4`}>
          <h5 className="alert-heading">
            <i className="bi bi-exclamation-triangle me-2"></i>
            {tierInfo.title}
          </h5>
          <p>{tierInfo.description}</p>
          <hr />
          {cori.hasTier1Offense && (
            <div className="mb-2">
              <strong>Tier 1 Offenses Found:</strong>
              <ul className="mb-0">
                {cori.tier1Offenses?.map((offense, idx) => (
                  <li key={idx}>{offense}</li>
                ))}
              </ul>
            </div>
          )}
          {cori.hasTier2Offense && (
            <div>
              <strong>Tier 2 Offenses Found:</strong>
              <ul className="mb-0">
                {cori.tier2Offenses?.map((offense, idx) => (
                  <li key={idx}>{offense}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      <div className="row">
        {/* CORI Details */}
        <div className="col-md-6">
          <div className="card mb-4">
            <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
              <h5 className="mb-0">CORI Information</h5>
            </div>
            <div className="card-body">
              <table className="table table-borderless">
                <tbody>
                  <tr>
                    <th style={{ width: '45%' }}>Status</th>
                    <td>
                      <span className={`badge ${getStatusBadgeClass(cori.coriStatus)}`}>
                        {cori.coriStatus}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <th>Tier</th>
                    <td>
                      <span className={`badge bg-${cori.coriTier === 1 ? 'danger' : 'warning'}`}>
                        Tier {cori.coriTier}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <th>Submission Date</th>
                    <td>{cori.coriSubmissionDate || '-'}</td>
                  </tr>
                  <tr>
                    <th>Result Date</th>
                    <td>{cori.coriResultDate || '-'}</td>
                  </tr>
                  <tr>
                    <th>Clearance Date</th>
                    <td>{cori.coriClearanceDate || '-'}</td>
                  </tr>
                  <tr>
                    <th>Expiration Date</th>
                    <td>
                      {cori.coriExpirationDate || '-'}
                      {cori.coriExpirationDate && new Date(cori.coriExpirationDate) < new Date() && (
                        <span className="badge bg-danger ms-2">Expired</span>
                      )}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div className="card mb-4">
            <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
              <h5 className="mb-0">DOJ Information</h5>
            </div>
            <div className="card-body">
              <table className="table table-borderless">
                <tbody>
                  <tr>
                    <th style={{ width: '45%' }}>Fingerprint Date</th>
                    <td>{cori.fingerprintSubmissionDate || '-'}</td>
                  </tr>
                  <tr>
                    <th>Agency Code</th>
                    <td>{cori.fingerprintAgencyCode || '-'}</td>
                  </tr>
                  <tr>
                    <th>DOJ Response Date</th>
                    <td>{cori.dojResponseDate || '-'}</td>
                  </tr>
                  <tr>
                    <th>DOJ Status</th>
                    <td>
                      <span className={`badge ${getStatusBadgeClass(cori.dojStatus)}`}>
                        {cori.dojStatus || 'Pending'}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Waivers and Exceptions */}
        <div className="col-md-6">
          {/* General Exception Card */}
          <div className="card mb-4">
            <div className="card-header d-flex justify-content-between align-items-center bg-info text-white">
              <h5 className="mb-0">General Exception (BR PM 36-41)</h5>
              {cori.hasTier2Offense && !cori.hasGeneralException && cori.coriStatus !== 'CLEARED' && (
                <button
                  className="btn btn-light btn-sm"
                  onClick={() => setShowExceptionModal(true)}
                >
                  <i className="bi bi-plus-lg me-2"></i>Request Exception
                </button>
              )}
            </div>
            <div className="card-body">
              {cori.hasGeneralException ? (
                <div>
                  <table className="table table-borderless">
                    <tbody>
                      <tr>
                        <th style={{ width: '45%' }}>Status</th>
                        <td>
                          <span className={`badge ${getStatusBadgeClass(cori.generalExceptionStatus)}`}>
                            {cori.generalExceptionStatus}
                          </span>
                        </td>
                      </tr>
                      <tr>
                        <th>Request Date</th>
                        <td>{cori.generalExceptionRequestDate || '-'}</td>
                      </tr>
                      <tr>
                        <th>Approval Date</th>
                        <td>{cori.generalExceptionApprovalDate || '-'}</td>
                      </tr>
                      <tr>
                        <th>End Date</th>
                        <td>
                          {cori.generalExceptionEndDate || '-'}
                          {cori.generalExceptionEndDate && new Date(cori.generalExceptionEndDate) < new Date() && (
                            <span className="badge bg-danger ms-2">Expired</span>
                          )}
                        </td>
                      </tr>
                      <tr>
                        <th>Reason</th>
                        <td>{cori.generalExceptionReason || '-'}</td>
                      </tr>
                      <tr>
                        <th>Approved By</th>
                        <td>{cori.generalExceptionApprovedBy || '-'}</td>
                      </tr>
                    </tbody>
                  </table>
                  {cori.generalExceptionStatus === 'PENDING' && (
                    <div className="d-flex gap-2">
                      <button className="btn btn-success" onClick={handleApproveException}>
                        <i className="bi bi-check-lg me-2"></i>Approve
                      </button>
                      <button className="btn btn-danger" onClick={handleDenyException}>
                        <i className="bi bi-x-lg me-2"></i>Deny
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-3 text-muted">
                  <p className="mb-0">No General Exception on file</p>
                  <small>A General Exception allows a provider with Tier 2 offenses to continue working for ALL recipients.</small>
                </div>
              )}
            </div>
          </div>

          {/* Recipient Waiver Card */}
          <div className="card mb-4">
            <div className="card-header d-flex justify-content-between align-items-center bg-warning text-dark">
              <h5 className="mb-0">Recipient Waiver (BR PM 42-49)</h5>
              {cori.hasTier2Offense && !cori.hasRecipientWaiver && cori.coriStatus !== 'CLEARED' && (
                <button
                  className="btn btn-dark btn-sm"
                  onClick={() => setShowWaiverModal(true)}
                >
                  <i className="bi bi-plus-lg me-2"></i>Request Waiver
                </button>
              )}
            </div>
            <div className="card-body">
              {cori.hasRecipientWaiver ? (
                <div>
                  <table className="table table-borderless">
                    <tbody>
                      <tr>
                        <th style={{ width: '45%' }}>Status</th>
                        <td>
                          <span className={`badge ${getStatusBadgeClass(cori.recipientWaiverStatus)}`}>
                            {cori.recipientWaiverStatus}
                          </span>
                        </td>
                      </tr>
                      <tr>
                        <th>Recipient</th>
                        <td>
                          <a
                            href="#"
                            onClick={(e) => { e.preventDefault(); router.push(`/recipients/${cori.recipientWaiverRecipientId}`); }}
                          >
                            {cori.recipientWaiverRecipientName}
                          </a>
                        </td>
                      </tr>
                      <tr>
                        <th>Request Date</th>
                        <td>{cori.recipientWaiverRequestDate || '-'}</td>
                      </tr>
                      <tr>
                        <th>Signed Date</th>
                        <td>{cori.recipientWaiverSignedDate || '-'}</td>
                      </tr>
                      <tr>
                        <th>Approval Date</th>
                        <td>{cori.recipientWaiverApprovalDate || '-'}</td>
                      </tr>
                      <tr>
                        <th>Expiration Date</th>
                        <td>
                          {cori.recipientWaiverExpirationDate || '-'}
                          {cori.recipientWaiverExpirationDate && new Date(cori.recipientWaiverExpirationDate) < new Date() && (
                            <span className="badge bg-danger ms-2">Expired</span>
                          )}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                  {cori.recipientWaiverStatus === 'PENDING' && (
                    <button className="btn btn-success" onClick={handleApproveWaiver}>
                      <i className="bi bi-check-lg me-2"></i>Approve Waiver
                    </button>
                  )}
                </div>
              ) : (
                <div className="text-center py-3 text-muted">
                  <p className="mb-0">No Recipient Waiver on file</p>
                  <small>A Recipient Waiver allows a provider with Tier 2 offenses to work for a SPECIFIC recipient who signs the waiver.</small>
                </div>
              )}
            </div>
          </div>

          {/* Eligibility Options Info */}
          {cori.hasTier2Offense && cori.coriStatus !== 'CLEARED' && (
            <div className="card mb-4">
              <div className="card-header bg-secondary text-white">
                <h5 className="mb-0">Eligibility Options for Tier 2 Offenses</h5>
              </div>
              <div className="card-body">
                <div className="row">
                  <div className="col-md-6">
                    <h6><i className="bi bi-shield-check me-2"></i>General Exception</h6>
                    <ul className="small">
                      <li>Applies to ALL recipients</li>
                      <li>Requires county approval</li>
                      <li>Has expiration date</li>
                      <li>Based on rehabilitation evidence</li>
                    </ul>
                  </div>
                  <div className="col-md-6">
                    <h6><i className="bi bi-person-check me-2"></i>Recipient Waiver</h6>
                    <ul className="small">
                      <li>Applies to ONE recipient</li>
                      <li>Recipient must sign waiver form</li>
                      <li>Recipient acknowledges criminal history</li>
                      <li>Expires when assignment ends</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* History */}
      <div className="card">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">CORI History</h5>
        </div>
        <div className="card-body">
          {history.length === 0 ? (
            <p className="text-muted text-center py-3">No history available</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-striped">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Action</th>
                    <th>Performed By</th>
                    <th>Notes</th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((item, index) => (
                    <tr key={index}>
                      <td>{item.timestamp}</td>
                      <td>{item.action}</td>
                      <td>{item.performedBy}</td>
                      <td>{item.notes || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* General Exception Modal */}
      {showExceptionModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Request General Exception</h5>
                <button type="button" className="btn-close" onClick={() => setShowExceptionModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="alert alert-info">
                  <h6>General Exception (BR PM 36-41)</h6>
                  <p className="mb-0 small">
                    A General Exception allows a provider with Tier 2 criminal offenses to continue
                    providing services to ALL recipients. The exception requires county approval and
                    documentation of rehabilitation.
                  </p>
                </div>
                <div className="mb-3">
                  <label className="form-label">Reason for Exception</label>
                  <textarea
                    className="form-control"
                    rows={4}
                    value={exceptionForm.reason}
                    onChange={(e) => setExceptionForm({ ...exceptionForm, reason: e.target.value })}
                    placeholder="Provide justification for the exception, including evidence of rehabilitation..."
                    required
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Exception End Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={exceptionForm.endDate}
                    onChange={(e) => setExceptionForm({ ...exceptionForm, endDate: e.target.value })}
                    min={new Date().toISOString().split('T')[0]}
                    required
                  />
                  <small className="text-muted">The exception will expire on this date</small>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowExceptionModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleRequestException}
                  disabled={!exceptionForm.reason || !exceptionForm.endDate}
                >
                  Submit Request
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Recipient Waiver Modal */}
      {showWaiverModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Request Recipient Waiver</h5>
                <button type="button" className="btn-close" onClick={() => setShowWaiverModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="alert alert-warning">
                  <h6>Recipient Waiver (BR PM 42-49)</h6>
                  <p className="mb-0 small">
                    A Recipient Waiver allows a provider with Tier 2 criminal offenses to provide
                    services to a SPECIFIC recipient. The recipient must acknowledge the provider&apos;s
                    criminal history and sign a waiver form (SOC 2271A).
                  </p>
                </div>
                <div className="mb-3">
                  <label className="form-label">Recipient</label>
                  <select
                    className="form-select"
                    value={waiverForm.recipientId}
                    onChange={(e) => setWaiverForm({ ...waiverForm, recipientId: e.target.value })}
                    required
                  >
                    <option value="">Select recipient...</option>
                    {recipients.map(r => (
                      <option key={r.id} value={r.id}>
                        {r.lastName}, {r.firstName} (CIN: {r.cin})
                      </option>
                    ))}
                  </select>
                  <small className="text-muted">Only recipients assigned to this provider are shown</small>
                </div>
                <div className="mb-3">
                  <label className="form-label">Waiver Signed Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={waiverForm.signedDate}
                    onChange={(e) => setWaiverForm({ ...waiverForm, signedDate: e.target.value })}
                    max={new Date().toISOString().split('T')[0]}
                    required
                  />
                  <small className="text-muted">Date when the recipient signed SOC 2271A</small>
                </div>
                <div className="mb-3">
                  <label className="form-label">Waiver Expiration Date (Optional)</label>
                  <input
                    type="date"
                    className="form-control"
                    value={waiverForm.expirationDate}
                    onChange={(e) => setWaiverForm({ ...waiverForm, expirationDate: e.target.value })}
                    min={new Date().toISOString().split('T')[0]}
                  />
                  <small className="text-muted">Leave blank if waiver should remain valid until assignment ends</small>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowWaiverModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleRequestWaiver}
                  disabled={!waiverForm.recipientId || !waiverForm.signedDate}
                >
                  Submit Request
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
