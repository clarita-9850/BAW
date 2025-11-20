import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import TimesheetManagement from './components/TimesheetManagement';
import CaseManagement from './components/CaseManagement';
import PersonSearch from './components/PersonSearch';
import PaymentManagement from './components/PaymentManagement';
import AdminDashboard from './components/AdminDashboard';
import PolicyManagement from './components/PolicyManagement';
import RoleManagement from './components/RoleManagement';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { API_BASE_URL } from './config/api';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-ca-secondary-50">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<AdminRoute />}>
              <Route index element={<AdminDashboard />} />
              <Route path="roles" element={<RoleManagement />} />
              <Route path="policies" element={<PolicyManagement />} />
            </Route>
            <Route path="/user" element={<ProtectedRoute />}>
              <Route index element={<Dashboard />} />
              <Route path="timesheets" element={<TimesheetManagement />} />
              <Route path="cases" element={<CaseManagement />} />
              <Route path="person-search" element={<PersonSearch />} />
              <Route path="payments" element={<PaymentManagement />} />
            </Route>
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

function ProtectedRoute() {
  const { user, loading } = useAuth();
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-ca-highlight-600"></div>
      </div>
    );
  }
  
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  
  return <Outlet />;
}

function AdminRoute() {
  const { user, loading } = useAuth();
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-ca-highlight-600"></div>
      </div>
    );
  }
  
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  
  if (user.role !== 'ADMIN' && user.role !== 'MANAGER') {
    return <Navigate to="/user" replace />;
  }
  
  return <Outlet />;
}

export default App;