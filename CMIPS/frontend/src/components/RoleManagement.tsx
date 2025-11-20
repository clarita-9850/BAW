import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { apiClient, API_ENDPOINTS } from '../config/api';

interface Role {
  id: number;
  name: string;
  displayName: string;
  description?: string;
  department?: string;
  location?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

const RoleManagement: React.FC = () => {
  const { user, token } = useAuth();
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingRole, setEditingRole] = useState<Role | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    displayName: '',
    description: '',
    active: true
  });

  useEffect(() => {
    fetchRoles();
  }, []);

  const fetchRoles = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/roles');
      setRoles(response.data);
    } catch (err) {
      setError('Failed to fetch roles');
      console.error('Error fetching roles:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingRole) {
        await apiClient.put(`/roles/${editingRole.id}`, formData);
      } else {
        await apiClient.post('/roles', formData);
      }

      setShowForm(false);
      setEditingRole(null);
      setFormData({
        name: '',
        displayName: '',
        description: '',
        active: true
      });
      fetchRoles();
    } catch (err) {
      setError('Failed to save role');
      console.error('Error saving role:', err);
    }
  };

  const handleEdit = (role: Role) => {
    setEditingRole(role);
    setFormData({
      name: role.name,
      displayName: role.displayName,
      description: role.description || '',
      active: role.active
    });
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this role?')) {
      try {
        await apiClient.delete(`/roles/${id}`);
        fetchRoles();
      } catch (err) {
        setError('Failed to delete role');
        console.error('Error deleting role:', err);
      }
    }
  };

  const handleToggleStatus = async (id: number) => {
    try {
      await apiClient.patch(`/roles/${id}/toggle`);
      fetchRoles();
    } catch (err) {
      setError('Failed to toggle role status');
      console.error('Error toggling role status:', err);
    }
  };

  const getStatusColor = (active: boolean) => {
    return active ? 'badge-success' : 'badge-secondary';
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
            <h1 className="text-2xl font-bold text-ca-primary-900">Role Management</h1>
            <button
              onClick={() => setShowForm(true)}
              className="btn btn-primary"
            >
              Create New Role
            </button>
          </div>

          {error && (
            <div className="alert alert-error mb-6">
              {error}
            </div>
          )}

          {/* Role Form */}
          {showForm && (
            <div className="card mb-6">
              <div className="card-header">
                <h2 className="text-lg font-medium text-ca-primary-900">
                  {editingRole ? 'Edit Role' : 'Create New Role'}
                </h2>
              </div>
              <div className="card-body">
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">Role Name (Code)</label>
                      <input
                        type="text"
                        required
                        className="input mt-1"
                        placeholder="e.g., CASE_WORKER"
                        value={formData.name}
                        onChange={(e) => setFormData({ ...formData, name: e.target.value.toUpperCase() })}
                      />
                      <p className="text-xs text-ca-primary-600 mt-1">
                        Use uppercase with underscores (e.g., CASE_WORKER)
                      </p>
                    </div>
                    
                    <div>
                      <label className="form-label">Display Name</label>
                      <input
                        type="text"
                        required
                        className="input mt-1"
                        placeholder="e.g., Case Worker"
                        value={formData.displayName}
                        onChange={(e) => setFormData({ ...formData, displayName: e.target.value })}
                      />
                    </div>
                  </div>
                  
                  <div>
                    <label className="form-label">Description</label>
                    <textarea
                      className="input mt-1"
                      rows={3}
                      placeholder="Describe the role's responsibilities and permissions"
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    />
                  </div>
                  
                  <div className="flex items-center">
                    <input
                      type="checkbox"
                      id="active"
                      checked={formData.active}
                      onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                      className="h-4 w-4 text-ca-highlight-600 focus:ring-ca-highlight-500 border-ca-primary-300 rounded"
                    />
                    <label htmlFor="active" className="ml-2 block text-sm text-ca-primary-900">
                      Active Role
                    </label>
                  </div>
                  
                  <div className="flex justify-end space-x-3">
                    <button
                      type="button"
                      onClick={() => {
                        setShowForm(false);
                        setEditingRole(null);
                        setFormData({
                          name: '',
                          displayName: '',
                          description: '',
                          active: true
                        });
                      }}
                      className="btn btn-secondary"
                    >
                      Cancel
                    </button>
                    <button type="submit" className="btn btn-primary">
                      {editingRole ? 'Update' : 'Create'} Role
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* Roles List */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-medium text-ca-primary-900">Available Roles</h2>
            </div>
            <div className="card-body">
              {roles.length === 0 ? (
                <div className="text-center py-8 text-ca-primary-500">
                  No roles found. Create your first role to get started.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Role Code</th>
                        <th>Display Name</th>
                        <th>Description</th>
                        <th>Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {roles.map((role) => (
                        <tr key={role.id}>
                          <td>
                            <span className="font-mono text-sm text-ca-primary-900">{role.name}</span>
                          </td>
                          <td>
                            <span className="font-medium text-ca-primary-900">{role.displayName}</span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-600">
                              {role.description || '-'}
                            </span>
                          </td>
                          <td>
                            <span className={`badge ${getStatusColor(role.active)}`}>
                              {role.active ? 'Active' : 'Inactive'}
                            </span>
                          </td>
                          <td>
                            <div className="flex space-x-2">
                              <button
                                onClick={() => handleEdit(role)}
                                className="text-ca-highlight-600 hover:text-ca-highlight-900 text-sm font-medium"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleToggleStatus(role.id)}
                                className="text-ca-primary-600 hover:text-ca-primary-900 text-sm font-medium"
                              >
                                {role.active ? 'Deactivate' : 'Activate'}
                              </button>
                              <button
                                onClick={() => handleDelete(role.id)}
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
    </div>
  );
};

export default RoleManagement;

