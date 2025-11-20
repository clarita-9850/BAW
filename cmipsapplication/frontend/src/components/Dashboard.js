import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Dashboard = () => {
  const { user, logout, loading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    console.log('🟢 Dashboard useEffect - loading:', loading, 'user:', !!user);
    
    if (loading) {
      console.log('🟢 Dashboard: Still loading, waiting...');
      return; // Wait for auth to initialize
    }
    
    if (!user) {
      console.log('🔴 Dashboard: No user, navigating to /login');
      navigate('/login');
      return;
    }
    
    console.log('🟢 Dashboard: User found, role:', user.role);
    
    // Route to role-specific dashboard
    if (user.role === 'PROVIDER') {
      console.log('🟢 Dashboard: Navigating to /provider/dashboard');
      navigate('/provider/dashboard');
    } else if (user.role === 'RECIPIENT') {
      navigate('/recipient/dashboard');
    } else if (user.role === 'CASE_WORKER') {
      navigate('/caseworker/dashboard');
    }
  }, [user, navigate, loading]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
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
                <h1 className="text-xl font-bold text-ca-primary-900">CMIPS MVP</h1>
                <p className="text-sm text-ca-primary-600">Case Management Information and Payrolling System</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-ca-primary-700">
                Welcome, <strong>{user.username}</strong> ({user.role})
              </span>
              <button 
                onClick={handleLogout} 
                className="btn btn-outline"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container py-8">
        <div className="ca-grid ca-grid-2 gap-6">
          {/* Timesheet Management Card */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-semibold text-ca-primary-900">Timesheet Management</h2>
            </div>
            <div className="card-body">
              <p className="text-ca-primary-600 mb-4">
                Manage your timesheet entries and view timesheet data with role-based access control.
              </p>
              <button 
                onClick={() => navigate('/timesheets')} 
                className="btn btn-primary"
              >
                Go to Timesheets
              </button>
            </div>
          </div>

          {/* Case Management Card */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-semibold text-ca-primary-900">Case Management</h2>
            </div>
            <div className="card-body">
              <p className="text-ca-primary-600 mb-4">
                Manage case files, track case status, and handle case assignments.
              </p>
              <button 
                onClick={() => navigate('/cases')} 
                className="btn btn-primary"
              >
                Go to Cases
              </button>
            </div>
          </div>

          {/* Person Search Card */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-semibold text-ca-primary-900">Person Search</h2>
            </div>
            <div className="card-body">
              <p className="text-ca-primary-600 mb-4">
                Search for persons, manage person records, and view person details.
              </p>
              <button 
                onClick={() => navigate('/persons')} 
                className="btn btn-primary"
              >
                Go to Person Search
              </button>
            </div>
          </div>

          {/* Keycloak Admin Card */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-semibold text-ca-primary-900">🔐 Keycloak Administration</h2>
            </div>
            <div className="card-body">
              <p className="text-ca-primary-600 mb-4">
                Manage users, roles, policies, and permissions without accessing Keycloak console.
              </p>
              <button 
                onClick={() => navigate('/admin/keycloak')} 
                className="btn btn-primary"
              >
                Go to Keycloak Admin
              </button>
            </div>
          </div>

          {/* Payment Management Card */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-semibold text-ca-primary-900">Payment Management</h2>
            </div>
            <div className="card-body">
              <p className="text-ca-primary-600 mb-4">
                Manage payments, track payment status, and handle payment processing.
              </p>
              <button 
                onClick={() => navigate('/payments')} 
                className="btn btn-primary"
              >
                Go to Payments
              </button>
            </div>
          </div>
        </div>

        {/* Role Information Card */}
        <div className="mt-8">
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-semibold text-ca-primary-900">Your Role & Permissions</h2>
            </div>
            <div className="card-body">
              <div className="mb-4">
                <span className="badge badge-primary">{user.role}</span>
              </div>
              <div>
                <h3 className="text-sm font-medium text-ca-primary-800 mb-2">Permissions:</h3>
                <ul className="text-sm text-ca-primary-600 space-y-1">
                  {user.role === 'PROVIDER' && (
                    <>
                      <li>• Create timesheet entries</li>
                      <li>• View own timesheets</li>
                      <li>• Update own timesheets</li>
                    </>
                  )}
                  {user.role === 'RECIPIENT' && (
                    <>
                      <li>• View timesheets</li>
                      <li>• Approve timesheets</li>
                    </>
                  )}
                  {user.role === 'CASE_WORKER' && (
                    <>
                      <li>• View all timesheets</li>
                      <li>• Approve timesheets</li>
                      <li>• Reject timesheets</li>
                    </>
                  )}
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
