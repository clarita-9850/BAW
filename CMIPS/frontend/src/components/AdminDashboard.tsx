import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import { apiClient } from '../config/api';

interface Stats {
  totalRoles: number;
  activeRoles: number;
  inactiveRoles: number;
}

const AdminDashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/admin/stats');
      setStats(response.data);
    } catch (err) {
      setError('Failed to fetch statistics');
      console.error('Error fetching stats:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const adminFeatures = [
    {
      name: 'Role Management',
      description: 'Create and manage user roles',
      path: '/roles',
      icon: '👥',
      color: 'bg-blue-500'
    },
    {
      name: 'Policy Management',
      description: 'Configure access policies and permissions',
      path: '/policies',
      icon: '⚙️',
      color: 'bg-green-500'
    }
  ];

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-secondary-50">
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          {/* Header */}
          <div className="mb-8">
            <div className="flex justify-between items-start">
              <div>
                <h1 className="text-3xl font-bold text-secondary-900">Admin Dashboard</h1>
                <p className="mt-2 text-secondary-600">
                  Manage roles, policies, and system configuration
                </p>
              </div>
              <div className="flex items-center space-x-4">
                <span className="text-sm text-secondary-600">
                  Welcome, {user?.firstName} {user?.lastName}
                </span>
                <button
                  onClick={handleLogout}
                  className="btn btn-secondary"
                >
                  Logout
                </button>
              </div>
            </div>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-6">
              {error}
            </div>
          )}

          {/* Statistics Cards */}
          {stats && (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
              <div className="card">
                <div className="card-body">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                        <span className="text-white text-sm font-medium">👥</span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-secondary-500">Total Roles</p>
                      <p className="text-2xl font-semibold text-secondary-900">{stats.totalRoles}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="card">
                <div className="card-body">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center">
                        <span className="text-white text-sm font-medium">✅</span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-secondary-500">Active Roles</p>
                      <p className="text-2xl font-semibold text-secondary-900">{stats.activeRoles}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="card">
                <div className="card-body">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-red-500 rounded-full flex items-center justify-center">
                        <span className="text-white text-sm font-medium">❌</span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-secondary-500">Inactive Roles</p>
                      <p className="text-2xl font-semibold text-secondary-900">{stats.inactiveRoles}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Admin Features */}
          <div className="mb-8">
            <h2 className="text-xl font-semibold text-secondary-900 mb-6">Administrative Features</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {adminFeatures.map((feature, index) => (
                <Link
                  key={index}
                  to={feature.path}
                  className="card hover:shadow-lg transition-shadow cursor-pointer"
                >
                  <div className="card-body">
                    <div className="flex items-center space-x-3">
                      <div className={`w-12 h-12 ${feature.color} rounded-lg flex items-center justify-center`}>
                        <span className="text-white text-xl">{feature.icon}</span>
                      </div>
                      <div>
                        <h3 className="font-medium text-secondary-900">{feature.name}</h3>
                        <p className="text-sm text-secondary-600">{feature.description}</p>
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
              <h2 className="text-lg font-medium text-secondary-900">Policy-Driven Access Control</h2>
            </div>
            <div className="card-body">
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <span className="text-blue-400">ℹ️</span>
                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-blue-800">
                      Policy Management
                    </h3>
                    <div className="mt-2 text-sm text-blue-700">
                      <p>
                        As an administrator, you can configure access policies that control what users
                        can do in the system. Policies are evaluated in real-time and can be updated
                        without restarting the application.
                      </p>
                      <ul className="mt-2 list-disc list-inside space-y-1">
                        <li>Define role-based access to resources and actions</li>
                        <li>Set up wildcard policies for flexible access control</li>
                        <li>Test policies before applying them</li>
                        <li>Monitor policy effectiveness and user access patterns</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="mt-8">
            <h2 className="text-xl font-semibold text-secondary-900 mb-6">Quick Actions</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="card">
                <div className="card-body">
                  <h3 className="font-medium text-secondary-900 mb-2">Test Policy Access</h3>
                  <p className="text-sm text-secondary-600 mb-4">
                    Test how different roles can access different resources and actions.
                  </p>
                  <button className="btn btn-primary">
                    Test Policies
                  </button>
                </div>
              </div>
              
              <div className="card">
                <div className="card-body">
                  <h3 className="font-medium text-secondary-900 mb-2">View System Logs</h3>
                  <p className="text-sm text-secondary-600 mb-4">
                    Monitor system activity and access patterns.
                  </p>
                  <button className="btn btn-secondary">
                    View Logs
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;




