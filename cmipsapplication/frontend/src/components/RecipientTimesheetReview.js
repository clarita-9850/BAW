import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { apiClient } from '../config/api';

const RecipientTimesheetReview = () => {
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const { timesheetId } = useParams();
  const [timesheet, setTimesheet] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showSignature, setShowSignature] = useState(false);
  const [showReject, setShowReject] = useState(false);
  const [rejectionReason, setRejectionReason] = useState('');
  const [signatureName, setSignatureName] = useState('');
  const [certified, setCertified] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (authLoading) return;
    if (!user || user.role !== 'RECIPIENT') {
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
      setSignatureName(user?.username || '');
    } catch (err) {
      console.error('Error fetching timesheet:', err);
      setError('Failed to load timesheet');
    } finally {
      setLoading(false);
    }
  };

  const parseServiceHours = (comments) => {
    // Parse service hours from comments
    const match = {
      personalCare: 0,
      domestic: 0,
      medical: 0,
      protective: 0
    };
    
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
    if (!certified) {
      setError('Please certify the timesheet before approving');
      return;
    }

    if (!signatureName.trim()) {
      setError('Please enter your name for electronic signature');
      return;
    }

    try {
      await apiClient.post(`/timesheets/${timesheetId}/approve`, {
        signature: signatureName,
        certifiedDate: new Date().toISOString()
      });
      
      alert('Timesheet approved successfully!');
      navigate('/recipient/dashboard');
    } catch (err) {
      console.error('Error approving timesheet:', err);
      setError(err.response?.data?.message || 'Failed to approve timesheet');
    }
  };

  const handleReject = async () => {
    if (!rejectionReason.trim()) {
      setError('Please provide a reason for rejection');
      return;
    }

    try {
      await apiClient.post(`/timesheets/${timesheetId}/reject`, rejectionReason);
      
      alert('Timesheet rejected. Provider has been notified.');
      navigate('/recipient/dashboard');
    } catch (err) {
      console.error('Error rejecting timesheet:', err);
      setError(err.response?.data?.message || 'Failed to reject timesheet');
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
          <button onClick={() => navigate('/recipient/dashboard')} className="btn btn-primary mt-4">
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
            <button onClick={() => navigate('/recipient/dashboard')} className="btn btn-outline">
              Back to Dashboard
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container py-8">
        <div className="max-w-4xl mx-auto">
          {/* Error Alert */}
          {error && (
            <div className="alert alert-error mb-4">{error}</div>
          )}

          {/* Timesheet Details */}
          <div className="card mb-6">
            <div className="card-header">
              <h2 className="text-xl font-bold text-ca-primary-900">TIMESHEET DETAILS</h2>
            </div>
            <div className="card-body space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-ca-primary-600">Provider:</p>
                  <p className="font-semibold text-ca-primary-900">{timesheet.employeeName}</p>
                </div>
                <div>
                  <p className="text-sm text-ca-primary-600">Pay Period:</p>
                  <p className="font-semibold text-ca-primary-900">
                    {timesheet.payPeriodStart} - {timesheet.payPeriodEnd}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-ca-primary-600">Submission Date:</p>
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

          {/* Hours by Service Type */}
          <div className="card mb-6">
            <div className="card-header">
              <h2 className="text-lg font-semibold text-ca-primary-900">HOURS BY SERVICE TYPE</h2>
            </div>
            <div className="card-body">
              <div className="space-y-3">
                <div className="flex justify-between items-center p-3 bg-ca-secondary-50 rounded">
                  <span className="text-ca-primary-900">Personal Care:</span>
                  <span className="font-bold text-lg text-ca-primary-900">{serviceHours.personalCare} hours</span>
                </div>
                <div className="flex justify-between items-center p-3 bg-ca-secondary-50 rounded">
                  <span className="text-ca-primary-900">Domestic Services:</span>
                  <span className="font-bold text-lg text-ca-primary-900">{serviceHours.domestic} hours</span>
                </div>
                <div className="flex justify-between items-center p-3 bg-ca-secondary-50 rounded">
                  <span className="text-ca-primary-900">Medical Accompaniment:</span>
                  <span className="font-bold text-lg text-ca-primary-900">{serviceHours.medical} hours</span>
                </div>
                <div className="flex justify-between items-center p-3 bg-ca-secondary-50 rounded">
                  <span className="text-ca-primary-900">Protective Supervision:</span>
                  <span className="font-bold text-lg text-ca-primary-900">{serviceHours.protective} hours</span>
                </div>
                
                <div className="mt-4 p-4 bg-ca-highlight-50 border-2 border-ca-highlight-600 rounded">
                  <div className="flex justify-between items-center">
                    <span className="text-lg font-bold text-ca-primary-900">Total Hours:</span>
                    <span className="text-2xl font-bold text-green-700">{timesheet.totalHours} hours</span>
                  </div>
                  <p className="text-sm text-ca-primary-600 mt-1">✅ Within authorized limits</p>
                </div>
              </div>
            </div>
          </div>

          {/* Comments */}
          {timesheet.comments && (
            <div className="card mb-6">
              <div className="card-header">
                <h2 className="text-lg font-semibold text-ca-primary-900">PROVIDER COMMENTS</h2>
              </div>
              <div className="card-body">
                <p className="text-ca-primary-900">{timesheet.comments}</p>
              </div>
            </div>
          )}

          {/* Certification & Signature */}
          {!showReject && (
            <div className="card mb-6">
              <div className="card-header">
                <h2 className="text-lg font-semibold text-ca-primary-900">CERTIFICATION</h2>
              </div>
              <div className="card-body space-y-4">
                <label className="flex items-start p-3 border rounded" style={{cursor: 'pointer'}}>
                  <input
                    type="checkbox"
                    checked={certified}
                    onChange={(e) => setCertified(e.target.checked)}
                    className="mt-1 mr-3"
                    style={{width: '20px', height: '20px'}}
                  />
                  <div>
                    <p className="font-semibold text-ca-primary-900">I certify that:</p>
                    <ul className="text-sm text-ca-primary-700 mt-2 space-y-1">
                      <li>☑ The hours listed are accurate</li>
                      <li>☑ Services were provided as indicated</li>
                      <li>☑ I received the services listed above</li>
                    </ul>
                  </div>
                </label>

                {certified && (
                  <div className="space-y-3">
                    <div className="form-group">
                      <label className="form-label text-lg">Electronic Signature *</label>
                      <input
                        type="text"
                        required
                        className="input"
                        style={{fontSize: '18px', padding: '12px', fontFamily: 'cursive'}}
                        placeholder="Type your full name"
                        value={signatureName}
                        onChange={(e) => setSignatureName(e.target.value)}
                      />
                      <p className="text-xs text-ca-primary-600 mt-1">
                        Date: {new Date().toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Rejection Form */}
          {showReject && (
            <div className="card mb-6">
              <div className="card-header bg-red-50">
                <h2 className="text-lg font-semibold text-red-900">REJECT TIMESHEET</h2>
              </div>
              <div className="card-body">
                <div className="form-group">
                  <label className="form-label text-lg">Reason for Rejection *</label>
                  <textarea
                    required
                    className="input"
                    rows="4"
                    style={{fontSize: '16px'}}
                    placeholder="Please explain why you are rejecting this timesheet..."
                    value={rejectionReason}
                    onChange={(e) => setRejectionReason(e.target.value)}
                  />
                  <p className="text-xs text-ca-primary-600 mt-1">
                    Provider will be able to revise and resubmit
                  </p>
                </div>
              </div>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex justify-center space-x-4">
            {!showReject ? (
              <>
                <button
                  onClick={() => setShowReject(true)}
                  className="btn btn-danger"
                  style={{fontSize: '18px', padding: '16px 32px'}}
                >
                  ✗ REJECT
                </button>
                <button
                  onClick={handleApprove}
                  disabled={!certified || !signatureName.trim()}
                  className="btn btn-success"
                  style={{fontSize: '18px', padding: '16px 48px'}}
                >
                  ✓ APPROVE & SIGN
                </button>
              </>
            ) : (
              <>
                <button
                  onClick={() => {
                    setShowReject(false);
                    setRejectionReason('');
                  }}
                  className="btn btn-secondary"
                  style={{fontSize: '18px', padding: '16px 32px'}}
                >
                  Cancel
                </button>
                <button
                  onClick={handleReject}
                  disabled={!rejectionReason.trim()}
                  className="btn btn-danger"
                  style={{fontSize: '18px', padding: '16px 48px'}}
                >
                  CONFIRM REJECTION
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default RecipientTimesheetReview;


