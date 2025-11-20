import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { apiClient, API_ENDPOINTS } from '../config/api';

interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  department: string;
  location: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

const UserManagement: React.FC = () => {
  const { user, token } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    role: '',
    department: '',
    location: ''
  });

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(API_ENDPOINTS.admin.users);
      setUsers(response.data);
    } catch (err) {
      setError('Failed to fetch users');
      console.error('Error fetching users:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingUser) {
        await apiClient.put(API_ENDPOINTS.admin.updateUserRole(editingUser.id), {
          role: formData.role
        });
      }

      setShowForm(false);
      setEditingUser(null);
      setFormData({ username: '', email: '', firstName: '', lastName: '', role: '', department: '', location: '' });
      fetchUsers();
    } catch (err) {
      setError('Failed to update user');
      console.error('Error updating user:', err);
    }
  };

  const handleEdit = (user: User) => {
    setEditingUser(user);
    setFormData({
      username: user.username,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      role: user.role,
      department: user.department,
      location: user.location
    });
    setShowForm(true);
  };

  const handleToggleActive = async (id: number, active: boolean) => {
    try {
      if (active) {
        await apiClient.put(API_ENDPOINTS.admin.activateUser(id));
      } else {
        await apiClient.put(API_ENDPOINTS.admin.deactivateUser(id));
      }
      fetchUsers();
    } catch (err) {
      setError('Failed to update user status');
      console.error('Error updating user status:', err);
    }
  };

  const getRoleColor = (role: string) => {
    switch (role) {
      case 'ADMIN': return 'bg-red-100 text-red-800';
      case 'MANAGER': return 'bg-blue-100 text-blue-800';
      case 'SUPERVISOR': return 'bg-green-100 text-green-800';
      case 'CASE_WORKER': return 'bg-yellow-100 text-yellow-800';
      case 'AUDITOR': return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

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
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold text-secondary-900">User Management</h1>
            <button
              onClick={() => setShowForm(true)}
              className="btn btn-primary"
            >
              Add User
            </button>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-6">
              {error}
            </div>
          )}

          {/* User Form */}
          {showForm && (
            <div className="card mb-6">
              <div className="card-header">
                <h2 className="text-lg font-medium text-secondary-900">
                  {editingUser ? 'Edit User' : 'Add New User'}
                </h2>
              </div>
              <div className="card-body">
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-secondary-700">Username</label>
                      <input
                        type="text"
                        required
                        className="input mt-1"
                        value={formData.username}
                        onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                        disabled={!!editingUser}
                      />
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium text-secondary-700">Email</label>
                      <input
                        type="email"
                        required
                        className="input mt-1"
                        value={formData.email}
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                      />
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-secondary-700">First Name</label>
                      <input
                        type="text"
                        required
                        className="input mt-1"
                        value={formData.firstName}
                        onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                      />
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium text-secondary-700">Last Name</label>
                      <input
                        type="text"
                        required
                        className="input mt-1"
                        value={formData.lastName}
                        onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                      />
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-secondary-700">Role</label>
                      <select
                        required
                        className="input mt-1"
                        value={formData.role}
                        onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                      >
                        <option value="">Select Role</option>
                        <option value="ADMIN">Admin</option>
                        <option value="MANAGER">Manager</option>
                        <option value="SUPERVISOR">Supervisor</option>
                        <option value="CASE_WORKER">Case Worker</option>
                        <option value="AUDITOR">Auditor</option>
                      </select>
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium text-secondary-700">Department</label>
                      <input
                        type="text"
                        className="input mt-1"
                        value={formData.department}
                        onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                      />
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium text-secondary-700">Location</label>
                      <input
                        type="text"
                        className="input mt-1"
                        value={formData.location}
                        onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                      />
                    </div>
                  </div>
                  
                  <div className="flex justify-end space-x-3">
                    <button
                      type="button"
                      onClick={() => {
                        setShowForm(false);
                        setEditingUser(null);
                        setFormData({ username: '', email: '', firstName: '', lastName: '', role: '', department: '', location: '' });
                      }}
                      className="btn btn-secondary"
                    >
                      Cancel
                    </button>
                    <button type="submit" className="btn btn-primary">
                      {editingUser ? 'Update' : 'Create'} User
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* Users List */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-medium text-secondary-900">Users</h2>
            </div>
            <div className="card-body">
              {users.length === 0 ? (
                <div className="text-center py-8 text-secondary-500">
                  No users found.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Username</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Department</th>
                        <th>Location</th>
                        <th>Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {users.map((user) => (
                        <tr key={user.id}>
                          <td className="font-medium">{user.username}</td>
                          <td>{user.firstName} {user.lastName}</td>
                          <td>{user.email}</td>
                          <td>
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getRoleColor(user.role)}`}>
                              {user.role.replace('_', ' ')}
                            </span>
                          </td>
                          <td>{user.department || '-'}</td>
                          <td>{user.location || '-'}</td>
                          <td>
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${user.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                              {user.active ? 'Active' : 'Inactive'}
                            </span>
                          </td>
                          <td>
                            <div className="flex space-x-2">
                              <button
                                onClick={() => handleEdit(user)}
                                className="text-primary-600 hover:text-primary-900 text-sm font-medium"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleToggleActive(user.id, !user.active)}
                                className={`text-sm font-medium ${user.active ? 'text-red-600 hover:text-red-900' : 'text-green-600 hover:text-green-900'}`}
                              >
                                {user.active ? 'Deactivate' : 'Activate'}
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

export default UserManagement;
