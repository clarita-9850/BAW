import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { apiClient } from '../config/api';
import WorkView from './WorkView';
import NotificationCenter from './NotificationCenter';

const RecipientDashboard = () => {
  const { user, logout, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const [pendingTimesheets, setPendingTimesheets] = useState([]);
  const [providers, setProviders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (authLoading) {
      return; // Wait for auth to initialize
    }
    
    if (!user || user.role !== 'RECIPIENT') {
      navigate('/login');
      return;
    }
    fetchDashboardData();
  }, [user, navigate, authLoading]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      // Fetch pending timesheets
      const timesheetsResponse = await apiClient.get('/timesheets');
      // Handle paginated response from backend
      const timesheets = timesheetsResponse.data.content || timesheetsResponse.data;
      setPendingTimesheets(timesheets.filter(ts => ts.status === 'SUBMITTED'));
      
      // Mock providers data
      setProviders([
        { id: 1, name: 'John Doe', status: 'Active', role: 'Primary Caregiver' },
        { id: 2, name: 'Mary Johnson', status: 'Active', role: 'Backup' }
      ]);
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-ca-secondary-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-ca-highlight-600 mx-auto"></div>
          <p className="mt-4 text-ca-primary-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-ca-secondary-50">
      {/* Header */}
      <div className="ca-header">
        <div className="container">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-4">
              <div className="ca-logo">CA</div>
              <div>
                <h1 className="text-xl font-bold text-ca-primary-900">IHSS Electronic Services Portal</h1>
                <p className="text-sm text-ca-primary-600">In-Home Supportive Services</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <NotificationCenter userId={user?.username} />
              <span className="text-sm text-ca-primary-700">
                Welcome, <strong>{user?.username}</strong> (Recipient)
              </span>
              <button onClick={handleLogout} className="btn btn-outline">
                Logout
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container py-8">
        {/* WorkView - Tasks */}
        <div className="mb-6">
          <WorkView username={user?.username} />
        </div>

        {/* Quick Actions */}
        <div className="ca-grid ca-grid-2 gap-6 mb-8">
          <div className="card" style={{cursor: 'pointer'}} onClick={() => navigate('/recipient/timesheets')}>
            <div className="card-body text-center py-8">
              <div className="text-4xl mb-2">📋</div>
              <h3 className="text-lg font-semibold text-ca-primary-900 mb-2">
                TIMESHEETS TO REVIEW
                {pendingTimesheets.length > 0 && (
                  <span className="badge badge-danger ml-2">{pendingTimesheets.length} Pending</span>
                )}
              </h3>
              <p className="text-sm text-ca-primary-600">Review & approve timesheets</p>
            </div>
          </div>

          <div className="card" style={{cursor: 'pointer'}} onClick={() => navigate('/recipient/providers')}>
            <div className="card-body text-center py-8">
              <div className="text-4xl mb-2">👥</div>
              <h3 className="text-lg font-semibold text-ca-primary-900 mb-2">MY PROVIDERS</h3>
              <p className="text-sm text-ca-primary-600">Manage your caregivers</p>
            </div>
          </div>

          <div className="card" style={{cursor: 'pointer'}}>
            <div className="card-body text-center py-8">
              <div className="text-4xl mb-2">📅</div>
              <h3 className="text-lg font-semibold text-ca-primary-900 mb-2">SERVICE SCHEDULE</h3>
              <p className="text-sm text-ca-primary-600">View upcoming services</p>
            </div>
          </div>

          <div className="card" style={{cursor: 'pointer'}}>
            <div className="card-body text-center py-8">
              <div className="text-4xl mb-2">❓</div>
              <h3 className="text-lg font-semibold text-ca-primary-900 mb-2">HELP & SUPPORT</h3>
              <p className="text-sm text-ca-primary-600">Get assistance</p>
            </div>
          </div>
        </div>

        {/* Timesheets Awaiting Approval */}
        <div className="card mb-6">
          <div className="card-header">
            <h2 className="text-lg font-semibold text-ca-primary-900">🔔 TIMESHEETS AWAITING YOUR APPROVAL</h2>
          </div>
          <div className="card-body">
            {pendingTimesheets.length === 0 ? (
              <p className="text-center text-ca-primary-600 py-4">No timesheets pending review</p>
            ) : (
              <div className="space-y-4">
                {pendingTimesheets.map((timesheet) => (
                  <div key={timesheet.id} className="p-4 bg-yellow-50 border-l-4 border-yellow-500 rounded">
                    <div className="flex justify-between items-center">
                      <div>
                        <h3 className="font-semibold text-ca-primary-900">
                          {timesheet.employeeName} - {timesheet.payPeriodStart} to {timesheet.payPeriodEnd}
                        </h3>
                        <p className="text-sm text-ca-primary-600">
                          Total Hours: {timesheet.totalHours} • Submitted: {new Date(timesheet.createdAt).toLocaleDateString()}
                        </p>
                      </div>
                      <button
                        onClick={() => navigate(`/recipient/timesheet/${timesheet.id}`)}
                        className="btn btn-primary"
                        style={{fontSize: '16px'}}
                      >
                        Review & Approve
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* My Providers */}
        <div className="card">
          <div className="card-header">
            <h2 className="text-lg font-semibold text-ca-primary-900">👥 MY PROVIDERS</h2>
          </div>
          <div className="card-body">
            {providers.length === 0 ? (
              <p className="text-center text-ca-primary-600 py-4">No providers assigned</p>
            ) : (
              <div className="space-y-3">
                {providers.map((provider) => (
                  <div key={provider.id} className="flex justify-between items-center p-4 bg-ca-secondary-50 rounded">
                    <div>
                      <h3 className="font-semibold text-ca-primary-900">{provider.name}</h3>
                      <p className="text-sm text-ca-primary-600">
                        <span className="badge badge-success">{provider.status}</span>
                        {' • '} {provider.role}
                      </p>
                    </div>
                    <button className="btn btn-secondary">
                      View Details
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default RecipientDashboard;


