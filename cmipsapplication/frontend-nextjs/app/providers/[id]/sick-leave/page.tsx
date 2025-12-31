'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useParams } from 'next/navigation';
import apiClient from '@/lib/api';

type SickLeaveBalance = {
  providerId: number;
  providerNumber: string;
  providerFirstName: string;
  providerLastName: string;

  // Eligibility (BR PM 96-105)
  isEligible: boolean;
  eligibilityDate: string;
  ineligibilityReason: string;

  // Current Balance
  currentBalance: number; // hours
  maxAnnualAccrual: number; // 8, 16, or 24 hours based on tier
  maxCarryover: number; // 48 hours max

  // Accrual Information (BR PM 106-115)
  accrualRate: string; // "1 hour per 30 hours worked"
  hoursWorkedThisFY: number;
  hoursAccruedThisFY: number;
  lastAccrualDate: string;
  nextAccrualDate: string;

  // Tier Information
  tier: number; // 1, 2, or 3
  tierDescription: string;

  // Fiscal Year Information
  fiscalYearStart: string;
  fiscalYearEnd: string;
  enrollmentAnniversary: string;
};

type SickLeaveTransaction = {
  id: number;
  providerId: number;
  transactionType: string; // ACCRUAL, USAGE, ADJUSTMENT, CARRYOVER
  hours: number;
  balanceAfter: number;
  transactionDate: string;
  reason: string;
  relatedTimesheetId?: number;
  processedBy: string;
  notes: string;
};

type SickLeaveUsageRequest = {
  id: number;
  providerId: number;
  recipientId: number;
  recipientName: string;
  requestDate: string;
  usageDate: string;
  hoursRequested: number;
  status: string;
  reason: string;
  approvedBy: string;
  approvalDate: string;
  denialReason: string;
};

export default function SickLeavePage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const router = useRouter();
  const params = useParams();
  const providerId = params.id as string;

  const [loading, setLoading] = useState(true);
  const [balance, setBalance] = useState<SickLeaveBalance | null>(null);
  const [transactions, setTransactions] = useState<SickLeaveTransaction[]>([]);
  const [usageRequests, setUsageRequests] = useState<SickLeaveUsageRequest[]>([]);
  const [showUsageModal, setShowUsageModal] = useState(false);
  const [showAdjustmentModal, setShowAdjustmentModal] = useState(false);
  const [activeTab, setActiveTab] = useState('balance');

  const [usageForm, setUsageForm] = useState({
    recipientId: '',
    usageDate: '',
    hoursRequested: 0,
    reason: ''
  });

  const [adjustmentForm, setAdjustmentForm] = useState({
    adjustmentType: 'ADD',
    hours: 0,
    reason: ''
  });

  const [recipients, setRecipients] = useState<any[]>([]);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchData();
  }, [user, authLoading, providerId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [balanceResponse, transactionsResponse, requestsResponse, recipientsResponse] = await Promise.all([
        apiClient.get(`/providers/${providerId}/sick-leave/balance`),
        apiClient.get(`/providers/${providerId}/sick-leave/transactions`).catch(() => ({ data: [] })),
        apiClient.get(`/providers/${providerId}/sick-leave/requests`).catch(() => ({ data: [] })),
        apiClient.get(`/providers/${providerId}/recipients`).catch(() => ({ data: [] }))
      ]);

      setBalance(balanceResponse.data);
      setTransactions(transactionsResponse.data || []);
      setUsageRequests(requestsResponse.data || []);
      setRecipients(recipientsResponse.data || []);
    } catch (err) {
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRequestUsage = async () => {
    try {
      await apiClient.post(`/providers/${providerId}/sick-leave/request`, usageForm);
      setShowUsageModal(false);
      setUsageForm({ recipientId: '', usageDate: '', hoursRequested: 0, reason: '' });
      fetchData();
    } catch (err) {
      console.error('Error requesting sick leave:', err);
      alert('Failed to submit sick leave request');
    }
  };

  const handleApproveRequest = async (requestId: number) => {
    try {
      await apiClient.put(`/providers/sick-leave/requests/${requestId}/approve`);
      fetchData();
    } catch (err) {
      console.error('Error approving request:', err);
      alert('Failed to approve request');
    }
  };

  const handleDenyRequest = async (requestId: number, reason: string) => {
    try {
      await apiClient.put(`/providers/sick-leave/requests/${requestId}/deny`, { reason });
      fetchData();
    } catch (err) {
      console.error('Error denying request:', err);
      alert('Failed to deny request');
    }
  };

  const handleAdjustment = async () => {
    try {
      await apiClient.post(`/providers/${providerId}/sick-leave/adjustment`, adjustmentForm);
      setShowAdjustmentModal(false);
      setAdjustmentForm({ adjustmentType: 'ADD', hours: 0, reason: '' });
      fetchData();
    } catch (err) {
      console.error('Error making adjustment:', err);
      alert('Failed to process adjustment');
    }
  };

  const handleTriggerAccrual = async () => {
    try {
      await apiClient.post(`/providers/${providerId}/sick-leave/trigger-accrual`);
      fetchData();
    } catch (err) {
      console.error('Error triggering accrual:', err);
      alert('Failed to trigger accrual');
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'APPROVED': return 'bg-success';
      case 'PENDING': return 'bg-warning text-dark';
      case 'DENIED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  };

  const getTransactionTypeBadge = (type: string) => {
    switch (type) {
      case 'ACCRUAL': return 'bg-success';
      case 'USAGE': return 'bg-danger';
      case 'ADJUSTMENT': return 'bg-info';
      case 'CARRYOVER': return 'bg-primary';
      default: return 'bg-secondary';
    }
  };

  const getTierInfo = (tier: number) => {
    switch (tier) {
      case 1:
        return {
          name: 'Tier 1',
          maxAccrual: 8,
          requirement: 'Less than 100 hours worked/FY',
          color: 'secondary'
        };
      case 2:
        return {
          name: 'Tier 2',
          maxAccrual: 16,
          requirement: '100-199 hours worked/FY',
          color: 'primary'
        };
      case 3:
        return {
          name: 'Tier 3',
          maxAccrual: 24,
          requirement: '200+ hours worked/FY',
          color: 'success'
        };
      default:
        return {
          name: 'Not Eligible',
          maxAccrual: 0,
          requirement: 'N/A',
          color: 'danger'
        };
    }
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading Sick Leave Information...</p>
        </div>
      </div>
    );
  }

  const tierInfo = balance ? getTierInfo(balance.tier) : null;

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.push(`/providers/${providerId}`)}>
            <i className="bi bi-arrow-left me-2"></i>Back to Provider
          </button>
          <h1 className="h3 mb-0">Sick Leave Management</h1>
          <p className="text-muted mb-0">
            Provider: {balance?.providerFirstName} {balance?.providerLastName} ({balance?.providerNumber})
          </p>
        </div>
        <div className="btn-group">
          <button className="btn btn-primary" onClick={() => setShowUsageModal(true)}>
            <i className="bi bi-calendar-plus me-2"></i>Request Usage
          </button>
          <button className="btn btn-outline-secondary" onClick={() => setShowAdjustmentModal(true)}>
            <i className="bi bi-sliders me-2"></i>Adjustment
          </button>
        </div>
      </div>

      {/* Business Rules Info */}
      <div className="card mb-4">
        <div className="card-header bg-info text-white">
          <h5 className="mb-0"><i className="bi bi-info-circle me-2"></i>Sick Leave Rules (BR PM 96-125)</h5>
        </div>
        <div className="card-body">
          <div className="row">
            <div className="col-md-3">
              <h6>Accrual Rate</h6>
              <p className="small mb-0">1 hour per 30 hours worked, up to annual maximum based on tier.</p>
            </div>
            <div className="col-md-3">
              <h6>Annual Maximums</h6>
              <ul className="small mb-0">
                <li>Tier 1 (&lt;100 hrs): 8 hours</li>
                <li>Tier 2 (100-199 hrs): 16 hours</li>
                <li>Tier 3 (200+ hrs): 24 hours</li>
              </ul>
            </div>
            <div className="col-md-3">
              <h6>Carryover</h6>
              <p className="small mb-0">Up to 48 hours may be carried over to the next fiscal year.</p>
            </div>
            <div className="col-md-3">
              <h6>Eligibility</h6>
              <p className="small mb-0">Provider must work 30+ hours to begin accruing. Eligible to use after 90 days.</p>
            </div>
          </div>
        </div>
      </div>

      {/* Balance Card */}
      {balance && (
        <div className="row mb-4">
          <div className="col-md-4">
            <div className="card h-100 border-primary">
              <div className="card-header bg-primary text-white">
                <h5 className="mb-0">Current Balance</h5>
              </div>
              <div className="card-body text-center">
                <div className="display-3 fw-bold text-primary">{balance.currentBalance}</div>
                <p className="text-muted mb-0">hours available</p>
                {balance.isEligible ? (
                  <span className="badge bg-success mt-2">Eligible</span>
                ) : (
                  <span className="badge bg-danger mt-2">Not Eligible: {balance.ineligibilityReason}</span>
                )}
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card h-100">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Accrual Information</h5>
              </div>
              <div className="card-body">
                <table className="table table-sm table-borderless mb-0">
                  <tbody>
                    <tr>
                      <th>Tier</th>
                      <td>
                        <span className={`badge bg-${tierInfo?.color}`}>{tierInfo?.name}</span>
                      </td>
                    </tr>
                    <tr>
                      <th>Annual Max</th>
                      <td>{balance.maxAnnualAccrual} hours</td>
                    </tr>
                    <tr>
                      <th>Accrued This FY</th>
                      <td>{balance.hoursAccruedThisFY} hours</td>
                    </tr>
                    <tr>
                      <th>Hours Worked</th>
                      <td>{balance.hoursWorkedThisFY} hours</td>
                    </tr>
                    <tr>
                      <th>Last Accrual</th>
                      <td>{balance.lastAccrualDate || 'N/A'}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div className="col-md-4">
            <div className="card h-100">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Fiscal Year Info</h5>
              </div>
              <div className="card-body">
                <table className="table table-sm table-borderless mb-0">
                  <tbody>
                    <tr>
                      <th>FY Period</th>
                      <td>{balance.fiscalYearStart} - {balance.fiscalYearEnd}</td>
                    </tr>
                    <tr>
                      <th>Anniversary</th>
                      <td>{balance.enrollmentAnniversary}</td>
                    </tr>
                    <tr>
                      <th>Max Carryover</th>
                      <td>{balance.maxCarryover} hours</td>
                    </tr>
                    <tr>
                      <th>Eligibility Date</th>
                      <td>{balance.eligibilityDate || 'N/A'}</td>
                    </tr>
                  </tbody>
                </table>
                <button
                  className="btn btn-outline-primary btn-sm mt-2"
                  onClick={handleTriggerAccrual}
                >
                  <i className="bi bi-arrow-clockwise me-2"></i>Check Accrual
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tabs */}
      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'balance' ? 'active' : ''}`}
            onClick={() => setActiveTab('balance')}
          >
            Tier Progress
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'transactions' ? 'active' : ''}`}
            onClick={() => setActiveTab('transactions')}
          >
            Transaction History ({transactions.length})
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'requests' ? 'active' : ''}`}
            onClick={() => setActiveTab('requests')}
          >
            Usage Requests ({usageRequests.length})
          </button>
        </li>
      </ul>

      {/* Tier Progress */}
      {activeTab === 'balance' && balance && (
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Tier Progress</h5>
          </div>
          <div className="card-body">
            <div className="row">
              {[1, 2, 3].map(tier => {
                const info = getTierInfo(tier);
                const isCurrentTier = balance.tier === tier;
                const hoursNeeded = tier === 1 ? 0 : tier === 2 ? 100 : 200;
                const progressPercent = Math.min(100, (balance.hoursWorkedThisFY / hoursNeeded) * 100);

                return (
                  <div key={tier} className="col-md-4">
                    <div className={`card h-100 ${isCurrentTier ? 'border-primary border-2' : ''}`}>
                      <div className={`card-header bg-${info.color} ${info.color !== 'secondary' ? 'text-white' : ''}`}>
                        <h6 className="mb-0">
                          {info.name}
                          {isCurrentTier && <span className="badge bg-light text-dark ms-2">Current</span>}
                        </h6>
                      </div>
                      <div className="card-body">
                        <p className="small text-muted">{info.requirement}</p>
                        <p className="mb-1"><strong>Max Accrual:</strong> {info.maxAccrual} hours/year</p>
                        {tier > 1 && (
                          <div>
                            <div className="progress mb-1">
                              <div
                                className={`progress-bar bg-${info.color}`}
                                style={{ width: `${progressPercent}%` }}
                              ></div>
                            </div>
                            <small className="text-muted">
                              {balance.hoursWorkedThisFY}/{hoursNeeded} hours worked
                            </small>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* Transaction History */}
      {activeTab === 'transactions' && (
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Transaction History</h5>
          </div>
          <div className="card-body">
            {transactions.length === 0 ? (
              <p className="text-muted text-center py-4">No transactions found</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Date</th>
                      <th>Type</th>
                      <th>Hours</th>
                      <th>Balance After</th>
                      <th>Reason</th>
                      <th>Processed By</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.map((tx) => (
                      <tr key={tx.id}>
                        <td>{tx.transactionDate}</td>
                        <td>
                          <span className={`badge ${getTransactionTypeBadge(tx.transactionType)}`}>
                            {tx.transactionType}
                          </span>
                        </td>
                        <td className={tx.transactionType === 'USAGE' ? 'text-danger' : 'text-success'}>
                          {tx.transactionType === 'USAGE' ? '-' : '+'}{tx.hours}
                        </td>
                        <td>{tx.balanceAfter}</td>
                        <td>{tx.reason || '-'}</td>
                        <td>{tx.processedBy}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Usage Requests */}
      {activeTab === 'requests' && (
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Usage Requests</h5>
          </div>
          <div className="card-body">
            {usageRequests.length === 0 ? (
              <p className="text-muted text-center py-4">No usage requests found</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Request Date</th>
                      <th>Usage Date</th>
                      <th>Recipient</th>
                      <th>Hours</th>
                      <th>Reason</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usageRequests.map((request) => (
                      <tr key={request.id}>
                        <td>{request.requestDate}</td>
                        <td>{request.usageDate}</td>
                        <td>{request.recipientName}</td>
                        <td>{request.hoursRequested}</td>
                        <td>{request.reason}</td>
                        <td>
                          <span className={`badge ${getStatusBadge(request.status)}`}>
                            {request.status}
                          </span>
                        </td>
                        <td>
                          {request.status === 'PENDING' && (
                            <div className="btn-group btn-group-sm">
                              <button
                                className="btn btn-success"
                                onClick={() => handleApproveRequest(request.id)}
                              >
                                Approve
                              </button>
                              <button
                                className="btn btn-danger"
                                onClick={() => {
                                  const reason = prompt('Enter denial reason:');
                                  if (reason) handleDenyRequest(request.id, reason);
                                }}
                              >
                                Deny
                              </button>
                            </div>
                          )}
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

      {/* Usage Request Modal */}
      {showUsageModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Request Sick Leave Usage</h5>
                <button type="button" className="btn-close" onClick={() => setShowUsageModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="alert alert-info">
                  <strong>Available Balance:</strong> {balance?.currentBalance} hours
                </div>
                <div className="mb-3">
                  <label className="form-label">Recipient</label>
                  <select
                    className="form-select"
                    value={usageForm.recipientId}
                    onChange={(e) => setUsageForm({ ...usageForm, recipientId: e.target.value })}
                    required
                  >
                    <option value="">Select recipient...</option>
                    {recipients.map(r => (
                      <option key={r.id} value={r.id}>
                        {r.lastName}, {r.firstName}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label">Usage Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={usageForm.usageDate}
                    onChange={(e) => setUsageForm({ ...usageForm, usageDate: e.target.value })}
                    required
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Hours Requested</label>
                  <input
                    type="number"
                    className="form-control"
                    value={usageForm.hoursRequested}
                    onChange={(e) => setUsageForm({ ...usageForm, hoursRequested: parseFloat(e.target.value) })}
                    min={0.25}
                    max={balance?.currentBalance || 0}
                    step={0.25}
                    required
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Reason</label>
                  <textarea
                    className="form-control"
                    value={usageForm.reason}
                    onChange={(e) => setUsageForm({ ...usageForm, reason: e.target.value })}
                    rows={2}
                    placeholder="Reason for sick leave..."
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowUsageModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleRequestUsage}
                  disabled={!usageForm.recipientId || !usageForm.usageDate || !usageForm.hoursRequested}
                >
                  Submit Request
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Adjustment Modal */}
      {showAdjustmentModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Manual Adjustment</h5>
                <button type="button" className="btn-close" onClick={() => setShowAdjustmentModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="alert alert-warning">
                  <i className="bi bi-exclamation-triangle me-2"></i>
                  Manual adjustments should only be made to correct errors or for administrative purposes.
                </div>
                <div className="mb-3">
                  <label className="form-label">Adjustment Type</label>
                  <select
                    className="form-select"
                    value={adjustmentForm.adjustmentType}
                    onChange={(e) => setAdjustmentForm({ ...adjustmentForm, adjustmentType: e.target.value })}
                  >
                    <option value="ADD">Add Hours</option>
                    <option value="SUBTRACT">Subtract Hours</option>
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label">Hours</label>
                  <input
                    type="number"
                    className="form-control"
                    value={adjustmentForm.hours}
                    onChange={(e) => setAdjustmentForm({ ...adjustmentForm, hours: parseFloat(e.target.value) })}
                    min={0.25}
                    step={0.25}
                    required
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Reason (Required)</label>
                  <textarea
                    className="form-control"
                    value={adjustmentForm.reason}
                    onChange={(e) => setAdjustmentForm({ ...adjustmentForm, reason: e.target.value })}
                    rows={3}
                    placeholder="Explain the reason for this adjustment..."
                    required
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowAdjustmentModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-warning"
                  onClick={handleAdjustment}
                  disabled={!adjustmentForm.hours || !adjustmentForm.reason}
                >
                  Apply Adjustment
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
