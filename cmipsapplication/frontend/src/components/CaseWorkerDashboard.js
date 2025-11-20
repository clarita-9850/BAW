import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { apiClient } from '../config/api';
import WorkView from './WorkView';
import NotificationCenter from './NotificationCenter';

const CaseWorkerDashboard = () => {
  const { user, logout, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState({
    totalCases: 0,
    pendingTimesheets: 0,
    evvViolations: 0,
    dueReassessments: 0
  });
  const [pendingTimesheets, setPendingTimesheets] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (authLoading) {
      return; // Wait for auth to initialize
    }
    
    if (!user || user.role !== 'CASE_WORKER') {
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
      const submitted = timesheetsResponse.data.content?.filter(ts => ts.status === 'SUBMITTED') || [];
      setPendingTimesheets(submitted);
      
      // Update stats
      setStats({
        totalCases: 145,
        pendingTimesheets: submitted.length,
        evvViolations: 3,
        dueReassessments: 5
      });
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
                <h1 className="text-xl font-bold text-ca-primary-900">CMIPS - Case Worker Portal</h1>
                <p className="text-sm text-ca-primary-600">Sacramento County - District Office 1</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <NotificationCenter userId={user?.username} />
              <span className="text-sm text-ca-primary-700">
                <strong>{user?.username}</strong> (Case Worker)
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
        {/* Statistics Cards */}
        <div className="ca-grid ca-grid-4 gap-4 mb-8">
          <div className="card">
            <div className="card-body text-center py-6">
              <div className="text-3xl font-bold text-ca-highlight-600">{stats.totalCases}</div>
              <p className="text-sm text-ca-primary-600 mt-1">CASES</p>
            </div>
          </div>
          
          <div className="card">
            <div className="card-body text-center py-6">
              <div className="text-3xl font-bold text-yellow-600">{stats.pendingTimesheets}</div>
              <p className="text-sm text-ca-primary-600 mt-1">TIMESHEETS PENDING</p>
            </div>
          </div>
          
          <div className="card">
            <div className="card-body text-center py-6">
              <div className="text-3xl font-bold text-red-600">{stats.evvViolations}</div>
              <p className="text-sm text-ca-primary-600 mt-1">EVV VIOLATIONS</p>
            </div>
          </div>
          
          <div className="card">
            <div className="card-body text-center py-6">
              <div className="text-3xl font-bold text-blue-600">{stats.dueReassessments}</div>
              <p className="text-sm text-ca-primary-600 mt-1">DUE REASSESSMENTS</p>
            </div>
          </div>
        </div>

        {/* WorkView - Tasks */}
        <div className="mb-6">
          <WorkView username={user?.username} />
        </div>

        {/* Priority Actions */}
        <div className="card mb-6">
          <div className="card-header">
            <h2 className="text-lg font-semibold text-ca-primary-900">🚨 PRIORITY ACTIONS</h2>
          </div>
          <div className="card-body">
            <div className="space-y-2">
              <div className="p-3 bg-yellow-50 border-l-4 border-yellow-500 rounded">
                <p className="text-ca-primary-900">• {stats.pendingTimesheets} Timesheets Pending Review</p>
              </div>
              <div className="p-3 bg-red-50 border-l-4 border-red-500 rounded">
                <p className="text-ca-primary-900">• {stats.evvViolations} EVV Violations Need Resolution</p>
              </div>
              <div className="p-3 bg-blue-50 border-l-4 border-blue-500 rounded">
                <p className="text-ca-primary-900">• {stats.dueReassessments} Cases Due for Reassessment</p>
              </div>
            </div>
          </div>
        </div>

        {/* Pending Timesheets */}
        <div className="card">
          <div className="card-header">
            <h2 className="text-lg font-semibold text-ca-primary-900">📊 PENDING TIMESHEETS</h2>
          </div>
          <div className="card-body">
            {pendingTimesheets.length === 0 ? (
              <p className="text-center text-ca-primary-600 py-4">No timesheets pending review</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Provider</th>
                      <th>Recipient</th>
                      <th>Pay Period</th>
                      <th>Hours</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pendingTimesheets.map((timesheet) => (
                      <tr key={timesheet.id}>
                        <td>{timesheet.employeeName}</td>
                        <td>-</td>
                        <td>{timesheet.payPeriodStart} to {timesheet.payPeriodEnd}</td>
                        <td className="font-bold">{timesheet.totalHours}</td>
                        <td>
                          <span className="badge badge-warning">{timesheet.status}</span>
                        </td>
                        <td>
                          <button
                            onClick={() => navigate(`/caseworker/timesheet/${timesheet.id}`)}
                            className="btn btn-primary btn-sm"
                          >
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
      </div>
    </div>
  );
};

export default CaseWorkerDashboard;





