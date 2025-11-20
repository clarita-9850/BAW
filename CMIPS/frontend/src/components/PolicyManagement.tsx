import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { apiClient, API_ENDPOINTS } from '../config/api';
import PolicyCreation from './PolicyCreation';

interface Policy {
  id: number;
  role: string;
  resource: string;
  action: string;
  allowed: boolean;
  description?: string;
  priority: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

const PolicyManagement: React.FC = () => {
  const { user, token } = useAuth();
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showPolicyCreation, setShowPolicyCreation] = useState(false);

  useEffect(() => {
    fetchPolicies();
  }, []);

  const fetchPolicies = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(API_ENDPOINTS.policies.list);
      setPolicies(response.data);
    } catch (err) {
      setError('Failed to fetch policies');
      console.error('Error fetching policies:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this policy?')) {
      try {
        await apiClient.delete(API_ENDPOINTS.policies.delete(id));
        fetchPolicies();
      } catch (err) {
        setError('Failed to delete policy');
        console.error('Error deleting policy:', err);
      }
    }
  };

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

  const getActionColor = (action: string) => {
    switch (action) {
      case 'GET': return 'badge-primary';
      case 'POST': return 'badge-success';
      case 'PUT': return 'badge-warning';
      case 'DELETE': return 'badge-danger';
      default: return 'badge-secondary';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-ca-highlight-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-ca-secondary-50">
      <div className="container py-6">
        <div className="px-4 py-6 sm:px-0">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold text-ca-primary-900">Policy Management</h1>
            <button
              onClick={() => setShowPolicyCreation(true)}
              className="btn btn-primary"
            >
              Create New Policy
            </button>
          </div>

          {error && (
            <div className="alert alert-error mb-6">
              {error}
            </div>
          )}

          {/* Policies List */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-medium text-ca-primary-900">Current Policies</h2>
            </div>
            <div className="card-body">
              {policies.length === 0 ? (
                <div className="text-center py-8 text-ca-primary-500">
                  No policies found. Create your first policy to get started.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Role</th>
                        <th>Resource</th>
                        <th>Action</th>
                        <th>Allowed</th>
                        <th>Priority</th>
                        <th>Status</th>
                        <th>Description</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {policies.map((policy) => (
                        <tr key={policy.id}>
                          <td>
                            <span className={`badge ${getRoleColor(policy.role)}`}>
                              {policy.role}
                            </span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-900">{policy.resource}</span>
                          </td>
                          <td>
                            <span className={`badge ${getActionColor(policy.action)}`}>
                              {policy.action}
                            </span>
                          </td>
                          <td>
                            <span className={`badge ${policy.allowed ? 'badge-success' : 'badge-danger'}`}>
                              {policy.allowed ? 'Allowed' : 'Denied'}
                            </span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-900">{policy.priority}</span>
                          </td>
                          <td>
                            <span className={`badge ${policy.active ? 'badge-success' : 'badge-secondary'}`}>
                              {policy.active ? 'Active' : 'Inactive'}
                            </span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-600">
                              {policy.description || '-'}
                            </span>
                          </td>
                          <td>
                            <div className="flex space-x-2">
                              <button
                                onClick={() => handleDelete(policy.id)}
                                className="text-bs-danger hover:text-red-900 text-sm font-medium"
                              >
                                Delete
                              </button>
                            </div>
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

      {/* Policy Creation Modal */}
      {showPolicyCreation && (
        <PolicyCreation
          onClose={() => setShowPolicyCreation(false)}
          onSave={() => {
            fetchPolicies();
            setShowPolicyCreation(false);
          }}
        />
      )}
    </div>
  );
};

export default PolicyManagement;