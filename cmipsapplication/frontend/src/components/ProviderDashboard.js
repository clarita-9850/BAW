import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { apiClient } from '../config/api';
import WorkView from './WorkView';
import NotificationCenter from './NotificationCenter';
import './WorkView.css';

const ProviderDashboard = () => {
  const { user, logout, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const [recipients, setRecipients] = useState([]);
  const [pendingActions, setPendingActions] = useState([]);
  const [loading, setLoading] = useState(true);
  // Address update moved to profile page

  useEffect(() => {
    if (authLoading) {
      return; // Wait for auth to initialize
    }
    
    if (!user || user.role !== 'PROVIDER') {
      navigate('/login');
      return;
    }
    fetchDashboardData();
  }, [user, navigate, authLoading]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      // Fetch assigned recipients from backend
      try {
        const recipientsData = await apiClient.get('/api/provider-recipient/my-recipients');
        if (recipientsData.data && recipientsData.data.length > 0) {
          // Map backend data to frontend format
          const mappedRecipients = recipientsData.data.map(rel => ({
            id: rel.id || rel.getId?.() || 1,
            name: rel.recipientName || rel.getRecipientName?.() || 'recipient1',
            status: rel.status || rel.getStatus?.() || 'Active',
            authorizedHours: rel.authorizedHoursPerMonth || rel.getAuthorizedHoursPerMonth?.() || 40,
            caseNumber: rel.caseNumber || rel.getCaseNumber?.() || 'CASE-001',
            address: { 
              street: '', 
              city: 'Sacramento', 
              state: 'CA', 
              zipCode: '' 
            }
          }));
          setRecipients(mappedRecipients);
        } else {
          // If no relationships exist, use recipient1 for testing
          setRecipients([{
            id: 1,
            name: 'recipient1',
            status: 'Active',
            authorizedHours: 40,
            caseNumber: 'CASE-001',
            address: { street: '', city: 'Sacramento', state: 'CA', zipCode: '' }
          }]);
        }
      } catch (apiError) {
        console.error('Error fetching recipients from API, using default:', apiError);
        // Fallback: Use recipient1 if API fails
        setRecipients([{
          id: 1,
          name: 'recipient1',
          status: 'Active',
          authorizedHours: 40,
          caseNumber: 'CASE-001',
          address: { street: '', city: 'Sacramento', state: 'CA', zipCode: '' }
        }]);
      }
      
      setPendingActions([
        { type: 'timesheet', message: 'Submit timesheet for Sep 15-30 (Due: Oct 5)', priority: 'high' },
        { type: 'review', message: 'Review rejected timesheet for Aug 2025', priority: 'medium' }
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

  const goToRecipientProfile = (recipient) => {
    navigate(`/provider/recipient/${recipient.id}/profile`);
  };

  // Address submission handled in RecipientProfile page

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
                Welcome, <strong>{user?.username}</strong> (Provider)
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
        {/* Quick Actions */}
        <div className="ca-grid ca-grid-2 gap-6 mb-8">
          <div className="card" style={{cursor: 'pointer'}} onClick={() => navigate('/provider/evv-checkin')}>
            <div className="card-body text-center py-8">
              <div className="text-4xl mb-2">📍</div>
              <h3 className="text-lg font-semibold text-ca-primary-900 mb-2">EVV CHECK-IN</h3>
              <p className="text-sm text-ca-primary-600">Start your service visit</p>
            </div>
          </div>

          <div className="card" style={{cursor: 'pointer'}} onClick={() => navigate('/provider/timesheets')}>
            <div className="card-body text-center py-8">
              <div className="text-4xl mb-2">📋</div>
              <h3 className="text-lg font-semibold text-ca-primary-900 mb-2">TIMESHEETS</h3>
              <p className="text-sm text-ca-primary-600">Submit & view timesheets</p>
            </div>
          </div>

          <div className="card" style={{cursor: 'pointer'}} onClick={() => navigate('/provider/payments')}>
            <div className="card-body text-center py-8">
              <div className="text-4xl mb-2">💰</div>
              <h3 className="text-lg font-semibold text-ca-primary-900 mb-2">PAYMENT HISTORY</h3>
              <p className="text-sm text-ca-primary-600">View your payments</p>
            </div>
          </div>

          <div className="card" style={{cursor: 'pointer'}} onClick={() => navigate('/provider/profile')}>
            <div className="card-body text-center py-8">
              <div className="text-4xl mb-2">👤</div>
              <h3 className="text-lg font-semibold text-ca-primary-900 mb-2">MY PROFILE</h3>
              <p className="text-sm text-ca-primary-600">Update my address & details</p>
            </div>
          </div>
        </div>

        {/* WorkView - Tasks */}
        <div className="mb-6">
          <WorkView username={user?.username} />
        </div>

        {/* My Recipients */}
        <div className="card mb-6">
          <div className="card-header">
            <h2 className="text-lg font-semibold text-ca-primary-900">📋 MY RECIPIENTS</h2>
          </div>
          <div className="card-body">
            {recipients.length === 0 ? (
              <p className="text-center text-ca-primary-600 py-4">No recipients assigned</p>
            ) : (
              <div className="space-y-3">
                {recipients.map((recipient) => (
                  <div key={recipient.id} className="flex justify-between items-center p-4 bg-ca-secondary-50 rounded">
                    <div>
                      <h3 className="font-semibold text-ca-primary-900">{recipient.name}</h3>
                      <p className="text-sm text-ca-primary-600">
                        Status: <span className="badge badge-success">{recipient.status}</span>
                        {' • '} Authorized: {recipient.authorizedHours} hours/month
                      </p>
                    </div>
                    <div className="flex space-x-2">
                      <button 
                        onClick={() => navigate(`/provider/timesheet/new/${recipient.id}`)}
                        className="btn btn-primary"
                      >
                        Submit Timesheet
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Admin Card - Only show if user has ADMIN role */}
        {user?.roles?.includes('ADMIN') && (
          <div className="card mb-6">
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
        )}

        {/* Pending Actions */}
        <div className="card">
          <div className="card-header">
            <h2 className="text-lg font-semibold text-ca-primary-900">⏰ PENDING ACTIONS</h2>
          </div>
          <div className="card-body">
            {pendingActions.length === 0 ? (
              <p className="text-center text-ca-primary-600 py-4">No pending actions</p>
            ) : (
              <div className="space-y-3">
                {pendingActions.map((action, index) => (
                  <div key={index} className={`p-4 rounded ${action.priority === 'high' ? 'bg-red-50 border-l-4 border-red-500' : 'bg-yellow-50 border-l-4 border-yellow-500'}`}>
                    <p className="text-ca-primary-900">{action.message}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Address modal removed; use RecipientProfile page */}
    </div>
  );
};

export default ProviderDashboard;


