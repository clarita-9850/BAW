import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Link } from 'react-router-dom';

const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();

  const getRoleColor = (role: string) => {
    switch (role) {
      case 'ADMIN': return 'badge-danger';
      case 'MANAGER': return 'badge-primary';
      case 'SUPERVISOR': return 'badge-success';
      case 'CASEWORKER': return 'badge-warning';
      case 'AUDITOR': return 'badge-secondary';
      default: return 'badge-secondary';
    }
  };

  const getRoleDescription = (role: string) => {
    switch (role) {
      case 'ADMIN': return 'Full access to all system features including user management and policy configuration';
      case 'MANAGER': return 'Can manage users and timesheets, view reports and analytics';
      case 'SUPERVISOR': return 'Can manage timesheets and view team performance';
      case 'CASEWORKER': return 'Can create, edit, and manage timesheets';
      case 'AUDITOR': return 'Can view timesheets and generate audit reports';
      default: return 'Basic user access';
    }
  };

  const getAvailableFeatures = (role: string) => {
    const features = [];
    
    if (role === 'ADMIN') {
      features.push({
        name: 'Policy Management',
        description: 'Configure access policies and permissions',
        path: '/admin/policies',
        icon: '⚙️'
      });
      
      features.push({
        name: 'Role Management',
        description: 'Manage roles and their permissions',
        path: '/admin/roles',
        icon: '👥'
      });
    } else {
      // For regular users, show all available screens
      features.push({
        name: 'Timesheet Management',
        description: 'Create, edit, and manage timesheets',
        path: '/timesheets',
        icon: '📝'
      });
      
      features.push({
        name: 'Case Management',
        description: 'Manage cases and case information',
        path: '/cases',
        icon: '📋'
      });
      
      features.push({
        name: 'Person Search',
        description: 'Search and manage person records',
        path: '/person-search',
        icon: '🔍'
      });
      
      features.push({
        name: 'Payment Management',
        description: 'Manage payments and financial records',
        path: '/payments',
        icon: '💰'
      });
    }
    
    return features;
  };

  const features = getAvailableFeatures(user?.role || '');

  return (
    <div className="min-h-screen bg-ca-secondary-50">
      {/* Utility Header */}
      <div className="ca-utility-header">
        <div className="container">
          <div className="flex justify-between items-center">
            <div className="flex items-center space-x-4">
              <div className="ca-logo">CA</div>
              <p className="text-sm text-ca-primary-600">
                Official website of the State of California
              </p>
            </div>
            <div className="text-sm text-ca-primary-600">
              CMIPS - Case Management Information and Payrolling System
            </div>
          </div>
        </div>
      </div>

      {/* Main Header */}
      <header className="ca-header">
        <div className="container">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-4">
              <h1 className="text-xl font-semibold text-ca-primary-900">
                CMIPS Dashboard
              </h1>
            </div>
            
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <span className="text-sm text-ca-primary-600">Welcome,</span>
                <span className="font-medium text-ca-primary-900">{user?.firstName} {user?.lastName}</span>
                <span className={`badge ${getRoleColor(user?.role || '')}`}>
                  {user?.role?.replace('_', ' ')}
                </span>
              </div>
              
              <button
                onClick={logout}
                className="btn btn-outline"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container py-6">
        <div className="px-4 py-6 sm:px-0">
          {/* User Info Card */}
          <div className="card mb-8">
            <div className="card-header">
              <h2 className="text-lg font-medium text-ca-primary-900">User Information</h2>
            </div>
            <div className="card-body">
              <div className="ca-grid ca-grid-4">
                <div>
                  <label className="form-label">Username</label>
                  <p className="text-sm text-ca-primary-900">{user?.username}</p>
                </div>
                <div>
                  <label className="form-label">Email</label>
                  <p className="text-sm text-ca-primary-900">{user?.email}</p>
                </div>
                <div>
                  <label className="form-label">Department</label>
                  <p className="text-sm text-ca-primary-900">{user?.department}</p>
                </div>
                <div>
                  <label className="form-label">Location</label>
                  <p className="text-sm text-ca-primary-900">{user?.location}</p>
                </div>
              </div>
              
              <div className="mt-4">
                <label className="form-label">Role Description</label>
                <p className="text-sm text-ca-primary-900">{getRoleDescription(user?.role || '')}</p>
              </div>
            </div>
          </div>

          {/* Features Grid */}
          <div className="mb-8">
            <h2 className="text-lg font-medium text-ca-primary-900 mb-4">
              {user?.role === 'ADMIN' ? 'Admin Management Tools' : 'Available Features'}
            </h2>
            <div className={`ca-grid ${user?.role === 'ADMIN' ? 'ca-grid-2' : 'ca-grid-4'}`}>
              {features.map((feature, index) => (
                <Link
                  key={index}
                  to={feature.path}
                  className="card hover:shadow-ca-md transition-shadow cursor-pointer"
                >
                  <div className="card-body">
                    <div className="flex items-center space-x-3">
                      <span className="text-2xl">{feature.icon}</span>
                      <div>
                        <h3 className="font-medium text-ca-primary-900">{feature.name}</h3>
                        <p className="text-sm text-ca-primary-600">{feature.description}</p>
                      </div>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          </div>

          {/* Policy Information */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-medium text-ca-primary-900">Policy-Driven Access Control</h2>
            </div>
            <div className="card-body">
              <div className="alert alert-info">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <span className="text-ca-highlight-600">ℹ️</span>
                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-ca-highlight-800">
                      How Policy-Driven Access Works
                    </h3>
                    <div className="mt-2 text-sm text-ca-highlight-700">
                      <p>
                        This system uses policy-driven authorization where access to resources and actions
                        is controlled by configurable policies stored in the database. Each user role has
                        specific permissions that can be modified at runtime without code changes.
                      </p>
                      <ul className="mt-2 list-disc list-inside space-y-1">
                        <li>Policies define which roles can access which resources</li>
                        <li>Actions (GET, POST, PUT, DELETE) are controlled per policy</li>
                        <li>Policies can be updated in real-time by administrators</li>
                        <li>Access is enforced at the API Gateway level</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
