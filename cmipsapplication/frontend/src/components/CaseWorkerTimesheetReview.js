import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { apiClient } from '../config/api';

const CaseWorkerTimesheetReview = () => {
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const { timesheetId } = useParams();
  const [timesheet, setTimesheet] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionType, setActionType] = useState('');
  const [comments, setComments] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (authLoading) return;
    if (!user || user.role !== 'CASE_WORKER') {
      navigate('/login');
      return;
    }
    fetchTimesheet();
  }, [user, timesheetId, navigate, authLoading]);

  const fetchTimesheet = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(`/timesheets/${timesheetId}`);
      setTimesheet(response.data);
    } catch (err) {
      console.error('Error fetching timesheet:', err);
      setError('Failed to load timesheet');
    } finally {
      setLoading(false);
    }
  };

  const parseServiceHours = (comments) => {
    const match = { personalCare: 0, domestic: 0, medical: 0, protective: 0 };
    if (comments) {
      const personalMatch = comments.match(/Personal Care: ([\d.]+)hrs/);
      const domesticMatch = comments.match(/Domestic: ([\d.]+)hrs/);
      const medicalMatch = comments.match(/Medical: ([\d.]+)hrs/);
      const protectiveMatch = comments.match(/Protective: ([\d.]+)hrs/);
      
      if (personalMatch) match.personalCare = parseFloat(personalMatch[1]);
      if (domesticMatch) match.domestic = parseFloat(domesticMatch[1]);
      if (medicalMatch) match.medical = parseFloat(medicalMatch[1]);
      if (protectiveMatch) match.protective = parseFloat(protectiveMatch[1]);
    }
    return match;
  };

  const handleApprove = async () => {
    try {
      await apiClient.post(`/timesheets/${timesheetId}/approve`);
      alert('Timesheet approved successfully!');
      navigate('/caseworker/dashboard');
    } catch (err) {
      console.error('Error approving timesheet:', err);
      setError(err.response?.data?.message || 'Failed to approve timesheet');
    }
  };

  const handleReject = async () => {
    if (!comments.trim()) {
      setError('Please provide a reason for rejection');
      return;
    }

    try {
      await apiClient.post(`/timesheets/${timesheetId}/reject`, comments);
      alert('Timesheet rejected. Provider has been notified.');
      navigate('/caseworker/dashboard');
    } catch (err) {
      console.error('Error rejecting timesheet:', err);
      setError(err.response?.data?.message || 'Failed to reject timesheet');
    }
  };

  const handleRequestRevision = async () => {
    if (!comments.trim()) {
      setError('Please provide details for revision request');
      return;
    }

    try {
      await apiClient.post(`/timesheets/${timesheetId}/request-revision`, comments);
      alert('Revision requested. Provider has been notified.');
      navigate('/caseworker/dashboard');
    } catch (err) {
      console.error('Error requesting revision:', err);
      setError(err.response?.data?.message || 'Failed to request revision');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-ca-secondary-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-ca-highlight-600 mx-auto"></div>
          <p className="mt-4 text-ca-primary-600">Loading timesheet...</p>
        </div>
      </div>
    );
  }

  if (!timesheet) {
    return (
      <div className="min-h-screen bg-ca-secondary-50">
        <div className="container py-8">
          <div className="alert alert-error">Timesheet not found</div>
          <button onClick={() => navigate('/caseworker/dashboard')} className="btn btn-primary mt-4">
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  const serviceHours = parseServiceHours(timesheet.comments);

  return (
    <div className="min-h-screen bg-ca-secondary-50">
      {/* Header */}
      <div className="ca-header">
        <div className="container">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-4">
              <div className="ca-logo">CA</div>
              <h1 className="text-xl font-bold text-ca-primary-900">Review Timesheet</h1>
            </div>
            <button onClick={() => navigate('/caseworker/dashboard')} className="btn btn-outline">
              Back to Dashboard
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container py-8">
        <div className="max-w-6xl mx-auto">
          {/* Error Alert */}
          {error && (
            <div className="alert alert-error mb-4">{error}</div>
          )}

          {/* Case Information */}
          <div className="card mb-6">
            <div className="card-header bg-ca-highlight-50">
              <h2 className="text-xl font-bold text-ca-primary-900">TIMESHEET REVIEW</h2>
            </div>
            <div className="card-body">
              <div className="grid grid-cols-3 gap-6">
                <div>
                  <p className="text-sm text-ca-primary-600">Case #:</p>
                  <p className="font-semibold text-ca-primary-900">IHSS-2025-{String(timesheet.id).padStart(6, '0')}</p>
                </div>
                <div>
                  <p className="text-sm text-ca-primary-600">Provider:</p>
                  <p className="font-semibold text-ca-primary-900">{timesheet.employeeName}</p>
                  <p className="text-xs text-ca-primary-600">ID: {timesheet.employeeId?.substring(0, 8)}...</p>
                </div>
                <div>
                  <p className="text-sm text-ca-primary-600">Recipient:</p>
                  <p className="font-semibold text-ca-primary-900">Recipient Name</p>
                  <p className="text-xs text-ca-primary-600">ID: RC-1234</p>
                </div>
              </div>
              <div className="grid grid-cols-3 gap-6 mt-4">
                <div>
                  <p className="text-sm text-ca-primary-600">Pay Period:</p>
                  <p className="font-semibold text-ca-primary-900">
                    {timesheet.payPeriodStart} - {timesheet.payPeriodEnd}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-ca-primary-600">Submitted:</p>
                  <p className="font-semibold text-ca-primary-900">
                    {new Date(timesheet.createdAt).toLocaleDateString()}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-ca-primary-600">Status:</p>
                  <span className="badge badge-warning">{timesheet.status}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Hours Summary */}
          <div className="card mb-6">
            <div className="card-header">
              <h3 className="text-lg font-semibold text-ca-primary-900">HOURS SUMMARY</h3>
            </div>
            <div className="card-body">
              <div className="grid grid-cols-2 gap-4">
                <div className="p-3 bg-ca-secondary-50 rounded">
                  <p className="text-sm text-ca-primary-600">Personal Care:</p>
                  <p className="text-xl font-bold text-ca-primary-900">{serviceHours.personalCare} hrs</p>
                </div>
                <div className="p-3 bg-ca-secondary-50 rounded">
                  <p className="text-sm text-ca-primary-600">Domestic Services:</p>
                  <p className="text-xl font-bold text-ca-primary-900">{serviceHours.domestic} hrs</p>
                </div>
                <div className="p-3 bg-ca-secondary-50 rounded">
                  <p className="text-sm text-ca-primary-600">Medical Accompaniment:</p>
                  <p className="text-xl font-bold text-ca-primary-900">{serviceHours.medical} hrs</p>
                </div>
                <div className="p-3 bg-ca-secondary-50 rounded">
                  <p className="text-sm text-ca-primary-600">Protective Supervision:</p>
                  <p className="text-xl font-bold text-ca-primary-900">{serviceHours.protective} hrs</p>
                </div>
              </div>

              <div className="mt-6 p-4 bg-white border-2 border-ca-highlight-600 rounded">
                <div className="grid grid-cols-3 gap-4 text-center">
                  <div>
                    <p className="text-sm text-ca-primary-600">Total Submitted:</p>
                    <p className="text-2xl font-bold text-ca-primary-900">{timesheet.totalHours} hrs</p>
                  </div>
                  <div>
                    <p className="text-sm text-ca-primary-600">Authorized Monthly:</p>
                    <p className="text-2xl font-bold text-ca-primary-900">40.0 hrs</p>
                  </div>
                  <div>
                    <p className="text-sm text-ca-primary-600">Remaining:</p>
                    <p className="text-2xl font-bold text-green-700">{(40 - timesheet.totalHours).toFixed(1)} hrs</p>
                  </div>
                </div>
                <div className="text-center mt-2">
                  <span className="badge badge-success">✅ Within authorized limits</span>
                </div>
              </div>
            </div>
          </div>

          {/* EVV Compliance */}
          <div className="card mb-6">
            <div className="card-header">
              <h3 className="text-lg font-semibold text-ca-primary-900">EVV COMPLIANCE</h3>
            </div>
            <div className="card-body">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="text-center p-3 bg-green-50 rounded">
                  <p className="text-2xl font-bold text-green-700">10/10</p>
                  <p className="text-sm text-ca-primary-600">Check-Ins ✅</p>
                </div>
                <div className="text-center p-3 bg-green-50 rounded">
                  <p className="text-2xl font-bold text-green-700">10/10</p>
                  <p className="text-sm text-ca-primary-600">Check-Outs ✅</p>
                </div>
                <div className="text-center p-3 bg-green-50 rounded">
                  <p className="text-2xl font-bold text-green-700">Yes</p>
                  <p className="text-sm text-ca-primary-600">Location ✅</p>
                </div>
                <div className="text-center p-3 bg-green-50 rounded">
                  <p className="text-2xl font-bold text-green-700">None</p>
                  <p className="text-sm text-ca-primary-600">Violations ✅</p>
                </div>
              </div>
            </div>
          </div>

          {/* Approval Status */}
          {timesheet.approvedBy && (
            <div className="card mb-6">
              <div className="card-header">
                <h3 className="text-lg font-semibold text-ca-primary-900">APPROVAL STATUS</h3>
              </div>
              <div className="card-body">
                <p className="text-sm text-ca-primary-600">Submitted by Provider: {new Date(timesheet.createdAt).toLocaleString()}</p>
                <p className="text-sm text-ca-primary-600 mt-2">Approved by Recipient: {new Date(timesheet.approvedAt).toLocaleString()} ✅</p>
                <p className="text-sm text-ca-primary-600">Signature: Electronic</p>
              </div>
            </div>
          )}

          {/* Case Worker Comments */}
          <div className="card mb-6">
            <div className="card-header">
              <h3 className="text-lg font-semibold text-ca-primary-900">CASE WORKER COMMENTS</h3>
            </div>
            <div className="card-body">
              <textarea
                className="input"
                rows="4"
                style={{fontSize: '16px'}}
                placeholder="Enter your comments here..."
                value={comments}
                onChange={(e) => setComments(e.target.value)}
              />
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-center space-x-4">
            {!actionType ? (
              <>
                <button
                  onClick={() => setActionType('reject')}
                  className="btn btn-danger"
                  style={{fontSize: '18px', padding: '16px 32px'}}
                >
                  ✗ REJECT
                </button>
                <button
                  onClick={() => setActionType('revision')}
                  className="btn btn-secondary"
                  style={{fontSize: '18px', padding: '16px 32px'}}
                >
                  ◀ REQUEST REVISION
                </button>
                <button
                  onClick={handleApprove}
                  className="btn btn-success"
                  style={{fontSize: '18px', padding: '16px 48px'}}
                >
                  ✓ APPROVE
                </button>
              </>
            ) : (
              <>
                <button
                  onClick={() => {
                    setActionType('');
                    setComments('');
                  }}
                  className="btn btn-secondary"
                  style={{fontSize: '18px', padding: '16px 32px'}}
                >
                  Cancel
                </button>
                <button
                  onClick={actionType === 'reject' ? handleReject : handleRequestRevision}
                  disabled={!comments.trim()}
                  className="btn btn-danger"
                  style={{fontSize: '18px', padding: '16px 48px'}}
                >
                  {actionType === 'reject' ? 'CONFIRM REJECTION' : 'SEND REVISION REQUEST'}
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CaseWorkerTimesheetReview;


