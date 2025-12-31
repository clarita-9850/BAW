'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useParams } from 'next/navigation';
import apiClient from '@/lib/api';

type TravelTimeRecord = {
  id: number;
  providerId: number;
  providerNumber: string;
  providerFirstName: string;
  providerLastName: string;

  // Travel Details (BR PM 126-145)
  travelDate: string;
  travelType: string; // BETWEEN_RECIPIENTS, TO_MEDICAL, ERRAND
  fromLocation: string;
  toLocation: string;
  fromRecipientId?: number;
  fromRecipientName?: string;
  toRecipientId?: number;
  toRecipientName?: string;

  // Time Details
  departureTime: string;
  arrivalTime: string;
  travelMinutes: number;
  waitMinutes: number;
  totalMinutes: number;

  // Mileage
  milesClaimed: number;
  mileageRate: number;
  mileageReimbursement: number;

  // Status
  status: string;
  approvedBy: string;
  approvalDate: string;
  denialReason: string;

  // Related Timesheet
  timesheetId?: number;
  payPeriod: string;

  notes: string;
  createdDate: string;
  updatedDate: string;
};

type TravelTimeConfig = {
  maxDailyTravelHours: number;
  maxWeeklyTravelHours: number;
  mileageRate: number;
  requiresPreApproval: boolean;
  allowedTravelTypes: string[];
};

type TravelTimeSummary = {
  totalTravelMinutesThisWeek: number;
  totalTravelMinutesThisMonth: number;
  totalMilesThisMonth: number;
  totalReimbursementThisMonth: number;
  pendingRequests: number;
};

export default function TravelTimePage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const router = useRouter();
  const params = useParams();
  const providerId = params.id as string;

  const [loading, setLoading] = useState(true);
  const [records, setRecords] = useState<TravelTimeRecord[]>([]);
  const [summary, setSummary] = useState<TravelTimeSummary | null>(null);
  const [config, setConfig] = useState<TravelTimeConfig | null>(null);
  const [providerInfo, setProviderInfo] = useState<any>(null);
  const [recipients, setRecipients] = useState<any[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [activeTab, setActiveTab] = useState('records');
  const [filters, setFilters] = useState({
    status: '',
    travelType: '',
    startDate: '',
    endDate: ''
  });

  const [newRecord, setNewRecord] = useState({
    travelDate: '',
    travelType: 'BETWEEN_RECIPIENTS',
    fromRecipientId: '',
    toRecipientId: '',
    fromLocation: '',
    toLocation: '',
    departureTime: '',
    arrivalTime: '',
    waitMinutes: 0,
    milesClaimed: 0,
    notes: ''
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
      const [providerResponse, recordsResponse, summaryResponse, configResponse, recipientsResponse] = await Promise.all([
        apiClient.get(`/providers/${providerId}`),
        apiClient.get(`/providers/${providerId}/travel-time`).catch(() => ({ data: [] })),
        apiClient.get(`/providers/${providerId}/travel-time/summary`).catch(() => ({ data: null })),
        apiClient.get(`/travel-time/config`).catch(() => ({ data: null })),
        apiClient.get(`/providers/${providerId}/recipients`).catch(() => ({ data: [] }))
      ]);

      setProviderInfo(providerResponse.data);
      setRecords(recordsResponse.data || []);
      setSummary(summaryResponse.data);
      setConfig(configResponse.data);
      setRecipients(recipientsResponse.data || []);
    } catch (err) {
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddTravelTime = async () => {
    try {
      await apiClient.post(`/providers/${providerId}/travel-time`, newRecord);
      setShowAddModal(false);
      setNewRecord({
        travelDate: '',
        travelType: 'BETWEEN_RECIPIENTS',
        fromRecipientId: '',
        toRecipientId: '',
        fromLocation: '',
        toLocation: '',
        departureTime: '',
        arrivalTime: '',
        waitMinutes: 0,
        milesClaimed: 0,
        notes: ''
      });
      fetchData();
    } catch (err) {
      console.error('Error adding travel time:', err);
      alert('Failed to add travel time record');
    }
  };

  const handleApprove = async (recordId: number) => {
    try {
      await apiClient.put(`/travel-time/${recordId}/approve`);
      fetchData();
    } catch (err) {
      console.error('Error approving record:', err);
      alert('Failed to approve record');
    }
  };

  const handleDeny = async (recordId: number) => {
    const reason = prompt('Enter denial reason:');
    if (!reason) return;
    try {
      await apiClient.put(`/travel-time/${recordId}/deny`, { reason });
      fetchData();
    } catch (err) {
      console.error('Error denying record:', err);
      alert('Failed to deny record');
    }
  };

  const calculateTravelMinutes = () => {
    if (!newRecord.departureTime || !newRecord.arrivalTime) return 0;
    const [depHours, depMins] = newRecord.departureTime.split(':').map(Number);
    const [arrHours, arrMins] = newRecord.arrivalTime.split(':').map(Number);
    const depTotal = depHours * 60 + depMins;
    const arrTotal = arrHours * 60 + arrMins;
    return Math.max(0, arrTotal - depTotal);
  };

  const filteredRecords = records.filter(r => {
    if (filters.status && r.status !== filters.status) return false;
    if (filters.travelType && r.travelType !== filters.travelType) return false;
    if (filters.startDate && r.travelDate < filters.startDate) return false;
    if (filters.endDate && r.travelDate > filters.endDate) return false;
    return true;
  });

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'APPROVED': return 'bg-success';
      case 'PENDING': return 'bg-warning text-dark';
      case 'DENIED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  };

  const getTravelTypeBadge = (type: string) => {
    switch (type) {
      case 'BETWEEN_RECIPIENTS': return { bg: 'primary', label: 'Between Recipients' };
      case 'TO_MEDICAL': return { bg: 'info', label: 'Medical Appointment' };
      case 'ERRAND': return { bg: 'secondary', label: 'Errand' };
      default: return { bg: 'secondary', label: type };
    }
  };

  const formatMinutes = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours}h ${mins}m`;
    }
    return `${mins}m`;
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading Travel Time Information...</p>
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
          <h1 className="h3 mb-0">Travel Time Management</h1>
          <p className="text-muted mb-0">
            Provider: {providerInfo?.firstName} {providerInfo?.lastName} ({providerInfo?.providerNumber})
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowAddModal(true)}>
          <i className="bi bi-plus-lg me-2"></i>Add Travel Time
        </button>
      </div>

      {/* Business Rules Info */}
      <div className="card mb-4">
        <div className="card-header bg-info text-white">
          <h5 className="mb-0"><i className="bi bi-info-circle me-2"></i>Travel Time Rules (BR PM 126-145)</h5>
        </div>
        <div className="card-body">
          <div className="row">
            <div className="col-md-3">
              <h6>Eligible Travel</h6>
              <ul className="small mb-0">
                <li>Travel between recipients</li>
                <li>Accompanying to appointments</li>
                <li>Authorized errands</li>
              </ul>
            </div>
            <div className="col-md-3">
              <h6>Time Limits</h6>
              <ul className="small mb-0">
                <li>Max {config?.maxDailyTravelHours || 7} hours/day</li>
                <li>Max {config?.maxWeeklyTravelHours || 7} hours/week</li>
                <li>Count toward overtime</li>
              </ul>
            </div>
            <div className="col-md-3">
              <h6>Mileage Reimbursement</h6>
              <ul className="small mb-0">
                <li>Rate: ${config?.mileageRate || 0.67}/mile</li>
                <li>Must document route</li>
                <li>Direct route only</li>
              </ul>
            </div>
            <div className="col-md-3">
              <h6>Documentation</h6>
              <ul className="small mb-0">
                <li>From/To locations</li>
                <li>Departure/Arrival times</li>
                <li>Purpose of travel</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Summary Cards */}
      {summary && (
        <div className="row mb-4">
          <div className="col-md-3">
            <div className="card text-center h-100 border-primary">
              <div className="card-body">
                <div className="fw-bold fs-3 text-primary">{formatMinutes(summary.totalTravelMinutesThisWeek)}</div>
                <p className="text-muted small mb-0">This Week</p>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center h-100">
              <div className="card-body">
                <div className="fw-bold fs-3">{formatMinutes(summary.totalTravelMinutesThisMonth)}</div>
                <p className="text-muted small mb-0">This Month</p>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center h-100">
              <div className="card-body">
                <div className="fw-bold fs-3">{summary.totalMilesThisMonth}</div>
                <p className="text-muted small mb-0">Miles This Month</p>
              </div>
            </div>
          </div>
          <div className="col-md-3">
            <div className="card text-center h-100 border-success">
              <div className="card-body">
                <div className="fw-bold fs-3 text-success">${summary.totalReimbursementThisMonth.toFixed(2)}</div>
                <p className="text-muted small mb-0">Mileage Reimbursement</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Weekly Limit Progress */}
      {summary && config && (
        <div className="card mb-4">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Weekly Travel Time Limit</h5>
          </div>
          <div className="card-body">
            <div className="d-flex align-items-center">
              <div className="flex-grow-1 me-3">
                <div className="progress" style={{ height: '25px' }}>
                  <div
                    className={`progress-bar ${summary.totalTravelMinutesThisWeek >= config.maxWeeklyTravelHours * 60 ? 'bg-danger' : 'bg-success'}`}
                    style={{ width: `${Math.min(100, (summary.totalTravelMinutesThisWeek / (config.maxWeeklyTravelHours * 60)) * 100)}%` }}
                  >
                    {formatMinutes(summary.totalTravelMinutesThisWeek)}
                  </div>
                </div>
              </div>
              <div className="text-muted">
                of {config.maxWeeklyTravelHours} hours max
              </div>
            </div>
            {summary.totalTravelMinutesThisWeek >= config.maxWeeklyTravelHours * 60 && (
              <div className="alert alert-danger mt-3 mb-0">
                <i className="bi bi-exclamation-triangle me-2"></i>
                Weekly travel time limit reached. Additional travel time may not be approved.
              </div>
            )}
          </div>
        </div>
      )}

      {/* Filters */}
      <div className="card mb-4">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Filters</h5>
        </div>
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-3">
              <label className="form-label">Status</label>
              <select
                className="form-select"
                value={filters.status}
                onChange={(e) => setFilters({ ...filters, status: e.target.value })}
              >
                <option value="">All Statuses</option>
                <option value="PENDING">Pending</option>
                <option value="APPROVED">Approved</option>
                <option value="DENIED">Denied</option>
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label">Travel Type</label>
              <select
                className="form-select"
                value={filters.travelType}
                onChange={(e) => setFilters({ ...filters, travelType: e.target.value })}
              >
                <option value="">All Types</option>
                <option value="BETWEEN_RECIPIENTS">Between Recipients</option>
                <option value="TO_MEDICAL">Medical Appointment</option>
                <option value="ERRAND">Errand</option>
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Start Date</label>
              <input
                type="date"
                className="form-control"
                value={filters.startDate}
                onChange={(e) => setFilters({ ...filters, startDate: e.target.value })}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">End Date</label>
              <input
                type="date"
                className="form-control"
                value={filters.endDate}
                onChange={(e) => setFilters({ ...filters, endDate: e.target.value })}
              />
            </div>
            <div className="col-md-2 d-flex align-items-end">
              <button
                className="btn btn-outline-secondary"
                onClick={() => setFilters({ status: '', travelType: '', startDate: '', endDate: '' })}
              >
                Clear Filters
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Records Table */}
      <div className="card">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Travel Time Records ({filteredRecords.length})</h5>
        </div>
        <div className="card-body">
          {filteredRecords.length === 0 ? (
            <p className="text-muted text-center py-4">No travel time records found</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-striped table-hover">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Type</th>
                    <th>From</th>
                    <th>To</th>
                    <th>Time</th>
                    <th>Miles</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredRecords.map((record) => {
                    const typeInfo = getTravelTypeBadge(record.travelType);
                    return (
                      <tr key={record.id}>
                        <td>{record.travelDate}</td>
                        <td>
                          <span className={`badge bg-${typeInfo.bg}`}>{typeInfo.label}</span>
                        </td>
                        <td>
                          {record.fromRecipientName ? (
                            <span title={record.fromLocation}>
                              <i className="bi bi-person me-1"></i>
                              {record.fromRecipientName}
                            </span>
                          ) : (
                            record.fromLocation
                          )}
                          <br />
                          <small className="text-muted">{record.departureTime}</small>
                        </td>
                        <td>
                          {record.toRecipientName ? (
                            <span title={record.toLocation}>
                              <i className="bi bi-person me-1"></i>
                              {record.toRecipientName}
                            </span>
                          ) : (
                            record.toLocation
                          )}
                          <br />
                          <small className="text-muted">{record.arrivalTime}</small>
                        </td>
                        <td>
                          <strong>{formatMinutes(record.travelMinutes)}</strong>
                          {record.waitMinutes > 0 && (
                            <span className="text-muted small">
                              <br />+ {record.waitMinutes}m wait
                            </span>
                          )}
                        </td>
                        <td>
                          {record.milesClaimed}
                          <br />
                          <small className="text-success">${record.mileageReimbursement.toFixed(2)}</small>
                        </td>
                        <td>
                          <span className={`badge ${getStatusBadge(record.status)}`}>
                            {record.status}
                          </span>
                        </td>
                        <td>
                          {record.status === 'PENDING' && (
                            <div className="btn-group btn-group-sm">
                              <button
                                className="btn btn-success"
                                onClick={() => handleApprove(record.id)}
                                title="Approve"
                              >
                                <i className="bi bi-check"></i>
                              </button>
                              <button
                                className="btn btn-danger"
                                onClick={() => handleDeny(record.id)}
                                title="Deny"
                              >
                                <i className="bi bi-x"></i>
                              </button>
                            </div>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Add Travel Time Modal */}
      {showAddModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Add Travel Time</h5>
                <button type="button" className="btn-close" onClick={() => setShowAddModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Travel Date</label>
                    <input
                      type="date"
                      className="form-control"
                      value={newRecord.travelDate}
                      onChange={(e) => setNewRecord({ ...newRecord, travelDate: e.target.value })}
                      max={new Date().toISOString().split('T')[0]}
                      required
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Travel Type</label>
                    <select
                      className="form-select"
                      value={newRecord.travelType}
                      onChange={(e) => setNewRecord({ ...newRecord, travelType: e.target.value })}
                    >
                      <option value="BETWEEN_RECIPIENTS">Between Recipients</option>
                      <option value="TO_MEDICAL">Medical Appointment</option>
                      <option value="ERRAND">Authorized Errand</option>
                    </select>
                  </div>

                  {newRecord.travelType === 'BETWEEN_RECIPIENTS' && (
                    <>
                      <div className="col-md-6">
                        <label className="form-label">From Recipient</label>
                        <select
                          className="form-select"
                          value={newRecord.fromRecipientId}
                          onChange={(e) => setNewRecord({ ...newRecord, fromRecipientId: e.target.value })}
                        >
                          <option value="">Select recipient...</option>
                          {recipients.map(r => (
                            <option key={r.id} value={r.id}>
                              {r.lastName}, {r.firstName}
                            </option>
                          ))}
                        </select>
                      </div>
                      <div className="col-md-6">
                        <label className="form-label">To Recipient</label>
                        <select
                          className="form-select"
                          value={newRecord.toRecipientId}
                          onChange={(e) => setNewRecord({ ...newRecord, toRecipientId: e.target.value })}
                        >
                          <option value="">Select recipient...</option>
                          {recipients.map(r => (
                            <option key={r.id} value={r.id}>
                              {r.lastName}, {r.firstName}
                            </option>
                          ))}
                        </select>
                      </div>
                    </>
                  )}

                  <div className="col-md-6">
                    <label className="form-label">From Location</label>
                    <input
                      type="text"
                      className="form-control"
                      value={newRecord.fromLocation}
                      onChange={(e) => setNewRecord({ ...newRecord, fromLocation: e.target.value })}
                      placeholder="Starting address"
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">To Location</label>
                    <input
                      type="text"
                      className="form-control"
                      value={newRecord.toLocation}
                      onChange={(e) => setNewRecord({ ...newRecord, toLocation: e.target.value })}
                      placeholder="Destination address"
                    />
                  </div>

                  <div className="col-md-4">
                    <label className="form-label">Departure Time</label>
                    <input
                      type="time"
                      className="form-control"
                      value={newRecord.departureTime}
                      onChange={(e) => setNewRecord({ ...newRecord, departureTime: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Arrival Time</label>
                    <input
                      type="time"
                      className="form-control"
                      value={newRecord.arrivalTime}
                      onChange={(e) => setNewRecord({ ...newRecord, arrivalTime: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Travel Time</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formatMinutes(calculateTravelMinutes())}
                      disabled
                    />
                  </div>

                  <div className="col-md-4">
                    <label className="form-label">Wait Time (minutes)</label>
                    <input
                      type="number"
                      className="form-control"
                      value={newRecord.waitMinutes}
                      onChange={(e) => setNewRecord({ ...newRecord, waitMinutes: parseInt(e.target.value) || 0 })}
                      min={0}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Miles</label>
                    <input
                      type="number"
                      className="form-control"
                      value={newRecord.milesClaimed}
                      onChange={(e) => setNewRecord({ ...newRecord, milesClaimed: parseFloat(e.target.value) || 0 })}
                      min={0}
                      step={0.1}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Mileage Reimbursement</label>
                    <input
                      type="text"
                      className="form-control"
                      value={`$${(newRecord.milesClaimed * (config?.mileageRate || 0.67)).toFixed(2)}`}
                      disabled
                    />
                  </div>

                  <div className="col-12">
                    <label className="form-label">Notes</label>
                    <textarea
                      className="form-control"
                      value={newRecord.notes}
                      onChange={(e) => setNewRecord({ ...newRecord, notes: e.target.value })}
                      rows={2}
                      placeholder="Additional notes about the travel..."
                    />
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowAddModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleAddTravelTime}
                  disabled={!newRecord.travelDate || !newRecord.departureTime || !newRecord.arrivalTime}
                >
                  Add Travel Time
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
