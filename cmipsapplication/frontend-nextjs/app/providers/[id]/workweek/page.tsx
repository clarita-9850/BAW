'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useParams } from 'next/navigation';
import apiClient from '@/lib/api';

type WorkweekAgreement = {
  id: number;
  providerId: number;
  providerNumber: string;
  providerFirstName: string;
  providerLastName: string;

  // Agreement Details (BR PM 78-95)
  agreementStatus: string;
  agreementType: string; // STANDARD, ALTERNATIVE
  effectiveDate: string;
  expirationDate: string;
  signedDate: string;

  // Workweek Definition
  workweekStartDay: string; // SUNDAY, MONDAY, etc.
  overtimeThreshold: number; // Default 40 hours
  doubleTimeThreshold: number; // Default 9 hours/day

  // Alternative Workweek (if applicable)
  alternativeSchedule: string; // e.g., "4x10" for four 10-hour days
  alternativeDays: string[]; // Days of the week

  // Recipients covered
  recipientIds: number[];
  recipientNames: string[];

  // Modification History
  previousWorkweekStartDay: string;
  modificationReason: string;
  modificationDate: string;

  // Violation tracking
  violationsUnderThisAgreement: number;

  createdDate: string;
  createdBy: string;
  updatedDate: string;
  updatedBy: string;
};

type Recipient = {
  id: number;
  firstName: string;
  lastName: string;
  cin: string;
  caseId: number;
};

const DAYS_OF_WEEK = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

export default function WorkweekAgreementPage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const router = useRouter();
  const params = useParams();
  const providerId = params.id as string;

  const [loading, setLoading] = useState(true);
  const [agreements, setAgreements] = useState<WorkweekAgreement[]>([]);
  const [currentAgreement, setCurrentAgreement] = useState<WorkweekAgreement | null>(null);
  const [providerInfo, setProviderInfo] = useState<any>(null);
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showModifyModal, setShowModifyModal] = useState(false);

  const [newAgreement, setNewAgreement] = useState({
    agreementType: 'STANDARD',
    workweekStartDay: 'SUNDAY',
    overtimeThreshold: 40,
    doubleTimeThreshold: 9,
    alternativeSchedule: '',
    alternativeDays: [] as string[],
    recipientIds: [] as number[],
    signedDate: ''
  });

  const [modifyForm, setModifyForm] = useState({
    workweekStartDay: '',
    modificationReason: '',
    effectiveDate: ''
  });

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
      const [providerResponse, agreementsResponse, recipientsResponse] = await Promise.all([
        apiClient.get(`/providers/${providerId}`),
        apiClient.get(`/providers/${providerId}/workweek-agreements`).catch(() => ({ data: [] })),
        apiClient.get(`/providers/${providerId}/recipients`).catch(() => ({ data: [] }))
      ]);

      setProviderInfo(providerResponse.data);
      setAgreements(agreementsResponse.data || []);
      setRecipients(recipientsResponse.data || []);

      // Find current active agreement
      const active = (agreementsResponse.data || []).find(
        (a: WorkweekAgreement) => a.agreementStatus === 'ACTIVE'
      );
      setCurrentAgreement(active || null);
    } catch (err) {
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAgreement = async () => {
    try {
      await apiClient.post(`/providers/${providerId}/workweek-agreements`, newAgreement);
      setShowCreateModal(false);
      setNewAgreement({
        agreementType: 'STANDARD',
        workweekStartDay: 'SUNDAY',
        overtimeThreshold: 40,
        doubleTimeThreshold: 9,
        alternativeSchedule: '',
        alternativeDays: [],
        recipientIds: [],
        signedDate: ''
      });
      fetchData();
    } catch (err) {
      console.error('Error creating agreement:', err);
      alert('Failed to create workweek agreement');
    }
  };

  const handleModifyWorkweek = async () => {
    try {
      await apiClient.put(`/providers/workweek-agreements/${currentAgreement?.id}/modify`, modifyForm);
      setShowModifyModal(false);
      setModifyForm({ workweekStartDay: '', modificationReason: '', effectiveDate: '' });
      fetchData();
    } catch (err) {
      console.error('Error modifying workweek:', err);
      alert('Failed to modify workweek start day');
    }
  };

  const handleTerminateAgreement = async (agreementId: number) => {
    if (!confirm('Are you sure you want to terminate this workweek agreement?')) return;
    try {
      await apiClient.put(`/providers/workweek-agreements/${agreementId}/terminate`);
      fetchData();
    } catch (err) {
      console.error('Error terminating agreement:', err);
      alert('Failed to terminate agreement');
    }
  };

  const toggleRecipient = (recipientId: number) => {
    setNewAgreement(prev => ({
      ...prev,
      recipientIds: prev.recipientIds.includes(recipientId)
        ? prev.recipientIds.filter(id => id !== recipientId)
        : [...prev.recipientIds, recipientId]
    }));
  };

  const toggleAlternativeDay = (day: string) => {
    setNewAgreement(prev => ({
      ...prev,
      alternativeDays: prev.alternativeDays.includes(day)
        ? prev.alternativeDays.filter(d => d !== day)
        : [...prev.alternativeDays, day]
    }));
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'bg-success';
      case 'PENDING': return 'bg-warning text-dark';
      case 'TERMINATED':
      case 'EXPIRED': return 'bg-secondary';
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
          <p className="text-muted mb-0">Loading Workweek Agreements...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.push(`/providers/${providerId}`)}>
            <i className="bi bi-arrow-left me-2"></i>Back to Provider
          </button>
          <h1 className="h3 mb-0">Workweek Agreement Management</h1>
          <p className="text-muted mb-0">
            Provider: {providerInfo?.firstName} {providerInfo?.lastName} ({providerInfo?.providerNumber})
          </p>
        </div>
        {!currentAgreement && (
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
            <i className="bi bi-plus-lg me-2"></i>Create Agreement
          </button>
        )}
      </div>

      {/* Business Rules Info */}
      <div className="card mb-4">
        <div className="card-header bg-info text-white">
          <h5 className="mb-0"><i className="bi bi-info-circle me-2"></i>Workweek Agreement Rules (BR PM 78-95)</h5>
        </div>
        <div className="card-body">
          <div className="row">
            <div className="col-md-4">
              <h6>Standard Workweek</h6>
              <ul className="small mb-0">
                <li>7 consecutive 24-hour periods</li>
                <li>Overtime after 40 hours/week</li>
                <li>Double-time after 9 hours/day</li>
                <li>Default start: Sunday 12:00 AM</li>
              </ul>
            </div>
            <div className="col-md-4">
              <h6>Alternative Workweek</h6>
              <ul className="small mb-0">
                <li>4x10 or 3x12 schedules available</li>
                <li>Must be requested by provider</li>
                <li>Requires recipient agreement</li>
                <li>Different overtime calculations</li>
              </ul>
            </div>
            <div className="col-md-4">
              <h6>Modification Rules</h6>
              <ul className="small mb-0">
                <li>Start day can be changed once per FY</li>
                <li>Must have valid reason</li>
                <li>Effective date cannot be retroactive</li>
                <li>Requires new signed agreement</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Current Agreement */}
      {currentAgreement && (
        <div className="card mb-4 border-success">
          <div className="card-header d-flex justify-content-between align-items-center bg-success text-white">
            <h5 className="mb-0"><i className="bi bi-check-circle me-2"></i>Current Active Agreement</h5>
            <div className="btn-group">
              <button
                className="btn btn-light btn-sm"
                onClick={() => {
                  setModifyForm({
                    workweekStartDay: currentAgreement.workweekStartDay,
                    modificationReason: '',
                    effectiveDate: ''
                  });
                  setShowModifyModal(true);
                }}
              >
                <i className="bi bi-pencil me-2"></i>Modify Workweek
              </button>
              <button
                className="btn btn-danger btn-sm"
                onClick={() => handleTerminateAgreement(currentAgreement.id)}
              >
                <i className="bi bi-x-lg me-2"></i>Terminate
              </button>
            </div>
          </div>
          <div className="card-body">
            <div className="row">
              <div className="col-md-6">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Agreement Type</th>
                      <td>
                        <span className={`badge ${currentAgreement.agreementType === 'STANDARD' ? 'bg-primary' : 'bg-info'}`}>
                          {currentAgreement.agreementType}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>Workweek Start Day</th>
                      <td className="fw-bold">{currentAgreement.workweekStartDay}</td>
                    </tr>
                    <tr>
                      <th>Overtime Threshold</th>
                      <td>{currentAgreement.overtimeThreshold} hours/week</td>
                    </tr>
                    <tr>
                      <th>Double-Time Threshold</th>
                      <td>{currentAgreement.doubleTimeThreshold} hours/day</td>
                    </tr>
                    <tr>
                      <th>Effective Date</th>
                      <td>{currentAgreement.effectiveDate}</td>
                    </tr>
                    <tr>
                      <th>Signed Date</th>
                      <td>{currentAgreement.signedDate}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div className="col-md-6">
                {currentAgreement.agreementType === 'ALTERNATIVE' && (
                  <div className="mb-3">
                    <h6>Alternative Schedule</h6>
                    <p className="mb-1"><strong>Schedule:</strong> {currentAgreement.alternativeSchedule}</p>
                    <p className="mb-0"><strong>Days:</strong> {currentAgreement.alternativeDays?.join(', ')}</p>
                  </div>
                )}
                <div>
                  <h6>Recipients Covered</h6>
                  {currentAgreement.recipientNames?.length > 0 ? (
                    <ul className="list-unstyled">
                      {currentAgreement.recipientNames.map((name, idx) => (
                        <li key={idx}>
                          <i className="bi bi-person me-2"></i>{name}
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="text-muted small">All assigned recipients</p>
                  )}
                </div>
                {currentAgreement.previousWorkweekStartDay && (
                  <div className="alert alert-warning mt-3">
                    <small>
                      <strong>Last Modified:</strong> {currentAgreement.modificationDate}<br />
                      Changed from {currentAgreement.previousWorkweekStartDay} to {currentAgreement.workweekStartDay}<br />
                      <strong>Reason:</strong> {currentAgreement.modificationReason}
                    </small>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Agreement History */}
      <div className="card">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Agreement History</h5>
        </div>
        <div className="card-body">
          {agreements.length === 0 ? (
            <p className="text-muted text-center py-4">No workweek agreements found</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-striped">
                <thead>
                  <tr>
                    <th>Type</th>
                    <th>Start Day</th>
                    <th>Effective Date</th>
                    <th>Expiration</th>
                    <th>Status</th>
                    <th>Violations</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {agreements.map((agreement) => (
                    <tr key={agreement.id}>
                      <td>
                        <span className={`badge ${agreement.agreementType === 'STANDARD' ? 'bg-primary' : 'bg-info'}`}>
                          {agreement.agreementType}
                        </span>
                      </td>
                      <td>{agreement.workweekStartDay}</td>
                      <td>{agreement.effectiveDate}</td>
                      <td>{agreement.expirationDate || 'N/A'}</td>
                      <td>
                        <span className={`badge ${getStatusBadge(agreement.agreementStatus)}`}>
                          {agreement.agreementStatus}
                        </span>
                      </td>
                      <td>
                        {agreement.violationsUnderThisAgreement > 0 ? (
                          <span className="badge bg-danger">{agreement.violationsUnderThisAgreement}</span>
                        ) : (
                          <span className="badge bg-success">0</span>
                        )}
                      </td>
                      <td>
                        <button
                          className="btn btn-sm btn-outline-primary"
                          onClick={() => router.push(`/providers/workweek-agreements/${agreement.id}`)}
                        >
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

      {/* Create Agreement Modal */}
      {showCreateModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Create Workweek Agreement</h5>
                <button type="button" className="btn-close" onClick={() => setShowCreateModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label">Agreement Type</label>
                  <div className="btn-group w-100">
                    <button
                      type="button"
                      className={`btn ${newAgreement.agreementType === 'STANDARD' ? 'btn-primary' : 'btn-outline-primary'}`}
                      onClick={() => setNewAgreement({ ...newAgreement, agreementType: 'STANDARD' })}
                    >
                      Standard Workweek
                    </button>
                    <button
                      type="button"
                      className={`btn ${newAgreement.agreementType === 'ALTERNATIVE' ? 'btn-primary' : 'btn-outline-primary'}`}
                      onClick={() => setNewAgreement({ ...newAgreement, agreementType: 'ALTERNATIVE' })}
                    >
                      Alternative Workweek
                    </button>
                  </div>
                </div>

                <div className="mb-3">
                  <label className="form-label">Workweek Start Day</label>
                  <select
                    className="form-select"
                    value={newAgreement.workweekStartDay}
                    onChange={(e) => setNewAgreement({ ...newAgreement, workweekStartDay: e.target.value })}
                  >
                    {DAYS_OF_WEEK.map(day => (
                      <option key={day} value={day}>{day}</option>
                    ))}
                  </select>
                  <small className="text-muted">The 7-day period begins at 12:00 AM on this day</small>
                </div>

                <div className="row">
                  <div className="col-md-6">
                    <div className="mb-3">
                      <label className="form-label">Overtime Threshold (hours/week)</label>
                      <input
                        type="number"
                        className="form-control"
                        value={newAgreement.overtimeThreshold}
                        onChange={(e) => setNewAgreement({ ...newAgreement, overtimeThreshold: parseInt(e.target.value) })}
                        min={0}
                        max={66}
                      />
                    </div>
                  </div>
                  <div className="col-md-6">
                    <div className="mb-3">
                      <label className="form-label">Double-Time Threshold (hours/day)</label>
                      <input
                        type="number"
                        className="form-control"
                        value={newAgreement.doubleTimeThreshold}
                        onChange={(e) => setNewAgreement({ ...newAgreement, doubleTimeThreshold: parseInt(e.target.value) })}
                        min={0}
                        max={24}
                      />
                    </div>
                  </div>
                </div>

                {newAgreement.agreementType === 'ALTERNATIVE' && (
                  <>
                    <div className="mb-3">
                      <label className="form-label">Alternative Schedule</label>
                      <select
                        className="form-select"
                        value={newAgreement.alternativeSchedule}
                        onChange={(e) => setNewAgreement({ ...newAgreement, alternativeSchedule: e.target.value })}
                      >
                        <option value="">Select schedule...</option>
                        <option value="4x10">4 days x 10 hours</option>
                        <option value="3x12">3 days x 12 hours</option>
                        <option value="9/80">9/80 Schedule</option>
                      </select>
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Work Days</label>
                      <div className="btn-group flex-wrap">
                        {DAYS_OF_WEEK.map(day => (
                          <button
                            key={day}
                            type="button"
                            className={`btn ${newAgreement.alternativeDays.includes(day) ? 'btn-primary' : 'btn-outline-primary'}`}
                            onClick={() => toggleAlternativeDay(day)}
                          >
                            {day.substring(0, 3)}
                          </button>
                        ))}
                      </div>
                    </div>
                  </>
                )}

                <div className="mb-3">
                  <label className="form-label">Agreement Signed Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={newAgreement.signedDate}
                    onChange={(e) => setNewAgreement({ ...newAgreement, signedDate: e.target.value })}
                    max={new Date().toISOString().split('T')[0]}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label className="form-label">Recipients Covered (Optional)</label>
                  <div className="border rounded p-2" style={{ maxHeight: '150px', overflowY: 'auto' }}>
                    {recipients.length === 0 ? (
                      <p className="text-muted small mb-0">No recipients assigned to this provider</p>
                    ) : (
                      recipients.map(r => (
                        <div key={r.id} className="form-check">
                          <input
                            type="checkbox"
                            className="form-check-input"
                            id={`recipient-${r.id}`}
                            checked={newAgreement.recipientIds.includes(r.id)}
                            onChange={() => toggleRecipient(r.id)}
                          />
                          <label className="form-check-label" htmlFor={`recipient-${r.id}`}>
                            {r.lastName}, {r.firstName} (CIN: {r.cin})
                          </label>
                        </div>
                      ))
                    )}
                  </div>
                  <small className="text-muted">Leave unchecked to apply to all recipients</small>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleCreateAgreement}
                  disabled={!newAgreement.signedDate}
                >
                  Create Agreement
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modify Workweek Modal */}
      {showModifyModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Modify Workweek Start Day</h5>
                <button type="button" className="btn-close" onClick={() => setShowModifyModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="alert alert-warning">
                  <h6><i className="bi bi-exclamation-triangle me-2"></i>Important (BR PM 90-95)</h6>
                  <ul className="mb-0 small">
                    <li>Workweek start day can only be changed once per fiscal year</li>
                    <li>Changes affect overtime calculations for all future timesheets</li>
                    <li>A valid reason must be documented</li>
                  </ul>
                </div>

                <div className="mb-3">
                  <label className="form-label">Current Start Day</label>
                  <input type="text" className="form-control" value={currentAgreement?.workweekStartDay} disabled />
                </div>

                <div className="mb-3">
                  <label className="form-label">New Workweek Start Day</label>
                  <select
                    className="form-select"
                    value={modifyForm.workweekStartDay}
                    onChange={(e) => setModifyForm({ ...modifyForm, workweekStartDay: e.target.value })}
                  >
                    {DAYS_OF_WEEK.filter(d => d !== currentAgreement?.workweekStartDay).map(day => (
                      <option key={day} value={day}>{day}</option>
                    ))}
                  </select>
                </div>

                <div className="mb-3">
                  <label className="form-label">Reason for Change</label>
                  <textarea
                    className="form-control"
                    rows={3}
                    value={modifyForm.modificationReason}
                    onChange={(e) => setModifyForm({ ...modifyForm, modificationReason: e.target.value })}
                    placeholder="Explain why the workweek start day needs to be changed..."
                    required
                  />
                </div>

                <div className="mb-3">
                  <label className="form-label">Effective Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={modifyForm.effectiveDate}
                    onChange={(e) => setModifyForm({ ...modifyForm, effectiveDate: e.target.value })}
                    min={new Date().toISOString().split('T')[0]}
                    required
                  />
                  <small className="text-muted">Must be a future date</small>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModifyModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleModifyWorkweek}
                  disabled={!modifyForm.workweekStartDay || !modifyForm.modificationReason || !modifyForm.effectiveDate}
                >
                  Modify Workweek
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
