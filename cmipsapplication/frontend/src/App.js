import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import Login from './components/Login';
import Dashboard from './components/Dashboard';

// Provider Components
import ProviderDashboard from './components/ProviderDashboard';
import EVVCheckIn from './components/EVVCheckIn';
import ProviderTimesheetEntry from './components/ProviderTimesheetEntry';
import TimesheetManagement from './components/TimesheetManagement';
import RecipientProfile from './components/RecipientProfile';
import ProviderProfile from './components/ProviderProfile';

// Recipient Components
import RecipientDashboard from './components/RecipientDashboard';
import RecipientTimesheetReview from './components/RecipientTimesheetReview';

// Case Worker Components
import CaseWorkerDashboard from './components/CaseWorkerDashboard';
import CaseWorkerTimesheetReview from './components/CaseWorkerTimesheetReview';

// Other Components
import CaseManagement from './components/CaseManagement';
import PersonSearch from './components/PersonSearch';
import PaymentManagement from './components/PaymentManagement';

// Admin Components
import KeycloakAdminDashboard from './components/KeycloakAdminDashboard';

// Admin Route Component with Role Protection
const AdminRoute = () => {
  const { user, loading } = useAuth();
  
  // Wait for auth to initialize
  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-ca-highlight-600 mx-auto"></div>
        <p className="mt-4 text-ca-primary-600">Loading...</p>
      </div>
    );
  }
  
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  
  if (!user.roles || !user.roles.includes('ADMIN')) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <h1>🔒 Access Denied</h1>
        <p>You need the ADMIN role to access this page.</p>
        <p><strong>Current user:</strong> {user.username}</p>
        <p><strong>Current roles:</strong> {user.roles?.join(', ') || 'None'}</p>
      </div>
    );
  }
  
  return <KeycloakAdminDashboard />;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-ca-secondary-50">
          <Routes>
            {/* Public Routes */}
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<Dashboard />} />
            
            {/* Provider Routes */}
            <Route path="/provider/dashboard" element={<ProviderDashboard />} />
            <Route path="/provider/evv-checkin" element={<EVVCheckIn />} />
            <Route path="/provider/timesheets" element={<TimesheetManagement />} />
            <Route path="/provider/timesheet/new/:recipientId" element={<ProviderTimesheetEntry />} />
            <Route path="/provider/payments" element={<PaymentManagement />} />
            <Route path="/provider/training" element={<CaseManagement />} />
            <Route path="/provider/profile" element={<ProviderProfile />} />
            <Route path="/provider/recipient/:recipientId/profile" element={<RecipientProfile />} />
            
            {/* Recipient Routes */}
            <Route path="/recipient/dashboard" element={<RecipientDashboard />} />
            <Route path="/recipient/timesheets" element={<TimesheetManagement />} />
            <Route path="/recipient/timesheet/:timesheetId" element={<RecipientTimesheetReview />} />
            <Route path="/recipient/providers" element={<PersonSearch />} />
            
            {/* Case Worker Routes */}
            <Route path="/caseworker/dashboard" element={<CaseWorkerDashboard />} />
            <Route path="/caseworker/timesheet/:timesheetId" element={<CaseWorkerTimesheetReview />} />
            <Route path="/caseworker/cases" element={<CaseManagement />} />
            
            {/* Admin Routes */}
            <Route path="/admin/keycloak" element={<AdminRoute />} />
            
            {/* Legacy Routes */}
            <Route path="/timesheets" element={<TimesheetManagement />} />
            <Route path="/cases" element={<CaseManagement />} />
            <Route path="/persons" element={<PersonSearch />} />
            <Route path="/payments" element={<PaymentManagement />} />
            
            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
