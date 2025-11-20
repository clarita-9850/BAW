import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { apiClient } from '../config/api';

const ProviderTimesheetEntry = () => {
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const { recipientId } = useParams();
  const [recipient, setRecipient] = useState(null);
  const [formData, setFormData] = useState({
    payPeriodStart: '',
    payPeriodEnd: '',
    personalCareHours: '',
    domesticServicesHours: '',
    medicalAccompanimentHours: '',
    protectiveSupervisionHours: '',
    department: 'IHSS Services',
    location: 'Sacramento County',
    comments: ''
  });
  const [totalHours, setTotalHours] = useState(0);
  const [authorizedHours, setAuthorizedHours] = useState(40);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (authLoading) return;
    if (!user || user.role !== 'PROVIDER') {
      navigate('/login');
      return;
    }
    
    // Mock recipient data
    setRecipient({ id: recipientId || 1, name: 'Jane Smith', authorizedHours: 40 });
    setAuthorizedHours(40);
  }, [user, recipientId, navigate, authLoading]);

  useEffect(() => {
    const total = 
      (parseFloat(formData.personalCareHours) || 0) +
      (parseFloat(formData.domesticServicesHours) || 0) +
      (parseFloat(formData.medicalAccompanimentHours) || 0) +
      (parseFloat(formData.protectiveSupervisionHours) || 0);
    setTotalHours(total);
  }, [formData.personalCareHours, formData.domesticServicesHours, formData.medicalAccompanimentHours, formData.protectiveSupervisionHours]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (totalHours > authorizedHours) {
      setError(`Total hours (${totalHours}) exceed authorized hours (${authorizedHours})`);
      return;
    }

    if (totalHours === 0) {
      setError('Please enter at least some service hours');
      return;
    }

    setLoading(true);
    try {
      const timesheetData = {
        employeeId: user?.userId || user?.username,
        employeeName: user?.username,
        payPeriodStart: formData.payPeriodStart,
        payPeriodEnd: formData.payPeriodEnd,
        regularHours: totalHours,
        overtimeHours: 0,
        holidayHours: 0,
        sickHours: 0,
        vacationHours: 0,
        department: formData.department,
        location: formData.location,
        comments: `Personal Care: ${formData.personalCareHours || 0}hrs, Domestic: ${formData.domesticServicesHours || 0}hrs, Medical: ${formData.medicalAccompanimentHours || 0}hrs, Protective: ${formData.protectiveSupervisionHours || 0}hrs. ${formData.comments}`
      };

      await apiClient.post('/timesheets', timesheetData);
      setSuccess('Timesheet submitted successfully!');
      
      setTimeout(() => {
        navigate('/provider/dashboard');
      }, 2000);
      
    } catch (err) {
      console.error('Error submitting timesheet:', err);
      setError(err.response?.data?.message || 'Failed to submit timesheet');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-ca-secondary-50">
      {/* Header */}
      <div className="ca-header">
        <div className="container">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-4">
              <div className="ca-logo">CA</div>
              <h1 className="text-xl font-bold text-ca-primary-900">Submit Timesheet</h1>
            </div>
            <button onClick={() => navigate('/provider/dashboard')} className="btn btn-outline">
              Back to Dashboard
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container py-8">
        <div className="max-w-4xl mx-auto">
          {/* Alerts */}
          {error && (
            <div className="alert alert-error mb-4">{error}</div>
          )}
          {success && (
            <div className="alert alert-success mb-4">{success}</div>
          )}

          <div className="card">
            <div className="card-header">
              <h2 className="text-xl font-bold text-ca-primary-900">
                TIMESHEET FOR: {recipient?.name || 'Loading...'}
              </h2>
              <p className="text-sm text-ca-primary-600 mt-1">
                Authorized Hours: {authorizedHours} hours/month
              </p>
            </div>
            <div className="card-body">
              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Pay Period */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="form-group">
                    <label className="form-label text-lg">Pay Period Start Date *</label>
                    <input
                      type="date"
                      required
                      className="input"
                      style={{fontSize: '16px', padding: '12px'}}
                      value={formData.payPeriodStart}
                      onChange={(e) => setFormData({...formData, payPeriodStart: e.target.value})}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label text-lg">Pay Period End Date *</label>
                    <input
                      type="date"
                      required
                      className="input"
                      style={{fontSize: '16px', padding: '12px'}}
                      value={formData.payPeriodEnd}
                      onChange={(e) => setFormData({...formData, payPeriodEnd: e.target.value})}
                    />
                  </div>
                </div>

                {/* Service Hours Breakdown */}
                <div className="p-6 bg-ca-highlight-50 rounded">
                  <h3 className="text-lg font-bold text-ca-primary-900 mb-4">SERVICE HOURS BREAKDOWN</h3>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="form-group">
                      <label className="form-label text-base">Personal Care Hours</label>
                      <input
                        type="number"
                        min="0"
                        step="0.5"
                        className="input"
                        style={{fontSize: '18px', padding: '12px', fontWeight: 'bold'}}
                        placeholder="0.0"
                        value={formData.personalCareHours}
                        onChange={(e) => setFormData({...formData, personalCareHours: e.target.value})}
                      />
                      <p className="text-xs text-ca-primary-600 mt-1">Bathing, grooming, dressing, feeding</p>
                    </div>

                    <div className="form-group">
                      <label className="form-label text-base">Domestic Services Hours</label>
                      <input
                        type="number"
                        min="0"
                        step="0.5"
                        className="input"
                        style={{fontSize: '18px', padding: '12px', fontWeight: 'bold'}}
                        placeholder="0.0"
                        value={formData.domesticServicesHours}
                        onChange={(e) => setFormData({...formData, domesticServicesHours: e.target.value})}
                      />
                      <p className="text-xs text-ca-primary-600 mt-1">Meal prep, housework, laundry</p>
                    </div>

                    <div className="form-group">
                      <label className="form-label text-base">Medical Accompaniment Hours</label>
                      <input
                        type="number"
                        min="0"
                        step="0.5"
                        className="input"
                        style={{fontSize: '18px', padding: '12px', fontWeight: 'bold'}}
                        placeholder="0.0"
                        value={formData.medicalAccompanimentHours}
                        onChange={(e) => setFormData({...formData, medicalAccompanimentHours: e.target.value})}
                      />
                      <p className="text-xs text-ca-primary-600 mt-1">Transportation to medical appointments</p>
                    </div>

                    <div className="form-group">
                      <label className="form-label text-base">Protective Supervision Hours</label>
                      <input
                        type="number"
                        min="0"
                        step="0.5"
                        className="input"
                        style={{fontSize: '18px', padding: '12px', fontWeight: 'bold'}}
                        placeholder="0.0"
                        value={formData.protectiveSupervisionHours}
                        onChange={(e) => setFormData({...formData, protectiveSupervisionHours: e.target.value})}
                      />
                      <p className="text-xs text-ca-primary-600 mt-1">Supervision for safety</p>
                    </div>
                  </div>

                  {/* Total Hours Summary */}
                  <div className="mt-6 p-4 bg-white rounded border-2 border-ca-highlight-600">
                    <div className="flex justify-between items-center">
                      <div>
                        <p className="text-lg font-bold text-ca-primary-900">Total Hours:</p>
                        <p className="text-sm text-ca-primary-600">Authorized: {authorizedHours} hours/month</p>
                      </div>
                      <div className="text-right">
                        <p className={`text-3xl font-bold ${totalHours > authorizedHours ? 'text-red-700' : 'text-green-700'}`}>
                          {totalHours.toFixed(1)} hrs
                        </p>
                        {totalHours > authorizedHours ? (
                          <p className="text-sm text-red-700 font-semibold">⚠️ Exceeds authorized hours!</p>
                        ) : (
                          <p className="text-sm text-green-700">✅ Within limits</p>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Comments */}
                <div className="form-group">
                  <label className="form-label text-lg">Additional Comments</label>
                  <textarea
                    className="input"
                    rows="4"
                    style={{fontSize: '16px'}}
                    placeholder="Any additional notes about the services provided..."
                    value={formData.comments}
                    onChange={(e) => setFormData({...formData, comments: e.target.value})}
                  />
                </div>

                {/* Action Buttons */}
                <div className="flex justify-end space-x-4 pt-4">
                  <button
                    type="button"
                    onClick={() => navigate('/provider/dashboard')}
                    className="btn btn-secondary"
                    style={{fontSize: '16px', padding: '12px 24px'}}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={loading || totalHours === 0}
                    className="btn btn-primary"
                    style={{fontSize: '16px', padding: '12px 32px'}}
                  >
                    {loading ? 'SUBMITTING...' : 'SUBMIT FOR APPROVAL'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProviderTimesheetEntry;

