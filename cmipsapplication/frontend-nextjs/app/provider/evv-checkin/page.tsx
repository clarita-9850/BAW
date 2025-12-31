'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/api';

type Recipient = {
  id: number;
  recipientId: number;
  recipientName: string;
  relationship: string;
  status: string;
  authorizedHoursPerMonth: number;
  caseNumber: string;
};

type EVVRecord = {
  id: number;
  recipientId: number;
  recipientName?: string;
  serviceType: string;
  checkInTime: string;
  checkOutTime?: string;
  status: string;
  totalMinutes?: number;
};

export default function EVVCheckInPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [activeVisits, setActiveVisits] = useState<EVVRecord[]>([]);
  const [recentVisits, setRecentVisits] = useState<EVVRecord[]>([]);

  // Check-in form state
  const [selectedRecipient, setSelectedRecipient] = useState<string>('');
  const [serviceType, setServiceType] = useState<string>('PERSONAL_CARE');
  const [checkingIn, setCheckingIn] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchData();
  }, [user, authLoading, mounted]);

  const fetchData = async () => {
    try {
      setLoading(true);

      // Fetch recipients
      try {
        const recipientsRes = await apiClient.get('/provider-recipient/my-recipients');
        setRecipients(recipientsRes.data || []);
      } catch {
        setRecipients([{
          id: 1,
          recipientId: 1,
          recipientName: 'recipient1',
          relationship: 'Client',
          status: 'Active',
          authorizedHoursPerMonth: 40,
          caseNumber: 'CASE-001'
        }]);
      }

      // Fetch EVV records
      try {
        const evvRes = await apiClient.get('/evv/my-records');
        const records = evvRes.data || [];
        setActiveVisits(records.filter((r: EVVRecord) => r.status === 'CHECKED_IN'));
        setRecentVisits(records.filter((r: EVVRecord) => r.status !== 'CHECKED_IN').slice(0, 10));
      } catch {
        setActiveVisits([]);
        setRecentVisits([]);
      }
    } catch (err) {
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCheckIn = async () => {
    if (!selectedRecipient) {
      alert('Please select a recipient');
      return;
    }

    try {
      setCheckingIn(true);
      await apiClient.post('/evv/check-in', {
        recipientId: parseInt(selectedRecipient),
        serviceType,
        checkInTime: new Date().toISOString(),
      });
      alert('Check-in successful!');
      setSelectedRecipient('');
      fetchData();
    } catch (err: any) {
      alert('Check-in failed: ' + (err?.response?.data?.error || err.message));
    } finally {
      setCheckingIn(false);
    }
  };

  const handleCheckOut = async (evvId: number) => {
    try {
      await apiClient.post(`/evv/${evvId}/check-out`, {
        checkOutTime: new Date().toISOString(),
      });
      alert('Check-out successful!');
      fetchData();
    } catch (err: any) {
      alert('Check-out failed: ' + (err?.response?.data?.error || err.message));
    }
  };

  const formatDateTime = (dateStr: string) => {
    return new Date(dateStr).toLocaleString();
  };

  const formatDuration = (minutes: number) => {
    const hrs = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hrs}h ${mins}m`;
  };

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
      <div className="mb-4">
        <h1 className="h3 mb-1">EVV Check-In/Out</h1>
        <p className="text-muted">Electronic Visit Verification for your service visits</p>
      </div>

      <div className="row">
        {/* Check-In Form */}
        <div className="col-lg-6 mb-4">
          <div className="card h-100">
            <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
              <h5 className="mb-0" style={{ color: 'white' }}>Start New Visit</h5>
            </div>
            <div className="card-body">
              <div className="mb-3">
                <label className="form-label">Select Recipient <span className="text-danger">*</span></label>
                <select
                  className="form-select"
                  value={selectedRecipient}
                  onChange={(e) => setSelectedRecipient(e.target.value)}
                >
                  <option value="">-- Select Recipient --</option>
                  {recipients.map((r) => (
                    <option key={r.id} value={r.recipientId}>
                      {r.recipientName} ({r.caseNumber})
                    </option>
                  ))}
                </select>
              </div>

              <div className="mb-3">
                <label className="form-label">Service Type</label>
                <select
                  className="form-select"
                  value={serviceType}
                  onChange={(e) => setServiceType(e.target.value)}
                >
                  <option value="PERSONAL_CARE">Personal Care</option>
                  <option value="DOMESTIC">Domestic Services</option>
                  <option value="PARAMEDICAL">Paramedical Services</option>
                  <option value="PROTECTIVE_SUPERVISION">Protective Supervision</option>
                </select>
              </div>

              <div className="alert alert-info">
                <small>
                  <strong>Current Time:</strong> {new Date().toLocaleString()}
                </small>
              </div>

              <button
                className="btn btn-success btn-lg w-100"
                onClick={handleCheckIn}
                disabled={checkingIn || !selectedRecipient}
              >
                {checkingIn ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2"></span>
                    Checking In...
                  </>
                ) : (
                  <>📍 Check In Now</>
                )}
              </button>
            </div>
          </div>
        </div>

        {/* Active Visits */}
        <div className="col-lg-6 mb-4">
          <div className="card h-100">
            <div className="card-header bg-warning">
              <h5 className="mb-0">Active Visits ({activeVisits.length})</h5>
            </div>
            <div className="card-body">
              {activeVisits.length === 0 ? (
                <p className="text-muted text-center py-4">No active visits</p>
              ) : (
                <div className="list-group list-group-flush">
                  {activeVisits.map((visit) => (
                    <div key={visit.id} className="list-group-item px-0">
                      <div className="d-flex justify-content-between align-items-start">
                        <div>
                          <h6 className="mb-1">{visit.recipientName || `Recipient ${visit.recipientId}`}</h6>
                          <small className="text-muted">
                            Checked in: {formatDateTime(visit.checkInTime)}<br />
                            Service: {visit.serviceType}
                          </small>
                        </div>
                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => handleCheckOut(visit.id)}
                        >
                          Check Out
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Recent Visits */}
      <div className="card">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0" style={{ color: 'white' }}>Recent Visits</h5>
        </div>
        <div className="card-body p-0">
          {recentVisits.length === 0 ? (
            <p className="text-muted text-center py-4">No recent visits</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Recipient</th>
                    <th>Service Type</th>
                    <th>Check In</th>
                    <th>Check Out</th>
                    <th>Duration</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {recentVisits.map((visit) => (
                    <tr key={visit.id}>
                      <td>{visit.recipientName || `Recipient ${visit.recipientId}`}</td>
                      <td>{visit.serviceType}</td>
                      <td>{formatDateTime(visit.checkInTime)}</td>
                      <td>{visit.checkOutTime ? formatDateTime(visit.checkOutTime) : '-'}</td>
                      <td>{visit.totalMinutes ? formatDuration(visit.totalMinutes) : '-'}</td>
                      <td>
                        <span className={`badge ${visit.status === 'COMPLETED' ? 'bg-success' : 'bg-secondary'}`}>
                          {visit.status}
                        </span>
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
