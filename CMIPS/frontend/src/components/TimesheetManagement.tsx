import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { apiClient, API_ENDPOINTS } from '../config/api';

interface Timesheet {
  id: number;
  userId: number;
  date: string;
  hours: number;
  description?: string;
  status: string;
  comments?: string;
  createdAt: string;
  updatedAt: string;
}

const TimesheetManagement: React.FC = () => {
  const { user, token } = useAuth();
  const [timesheets, setTimesheets] = useState<Timesheet[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingTimesheet, setEditingTimesheet] = useState<Timesheet | null>(null);
  const [formData, setFormData] = useState({
    date: '',
    hours: '',
    description: '',
    comments: ''
  });

  useEffect(() => {
    fetchTimesheets();
  }, []);

  const fetchTimesheets = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(API_ENDPOINTS.timesheets.list);
      setTimesheets(response.data);
    } catch (err) {
      setError('Failed to fetch timesheets');
      console.error('Error fetching timesheets:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const timesheetData = {
        date: formData.date,
        hours: parseFloat(formData.hours),
        description: formData.description,
        comments: formData.comments
      };

      if (editingTimesheet) {
        await apiClient.put(API_ENDPOINTS.timesheets.update(editingTimesheet.id), timesheetData);
      } else {
        await apiClient.post(API_ENDPOINTS.timesheets.create, timesheetData);
      }

      setShowForm(false);
      setEditingTimesheet(null);
      setFormData({ date: '', hours: '', description: '', comments: '' });
      fetchTimesheets();
    } catch (err) {
      setError('Failed to save timesheet');
      console.error('Error saving timesheet:', err);
    }
  };

  const handleEdit = (timesheet: Timesheet) => {
    setEditingTimesheet(timesheet);
    setFormData({
      date: timesheet.date,
      hours: timesheet.hours.toString(),
      description: timesheet.description || '',
      comments: timesheet.comments || ''
    });
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this timesheet?')) {
      try {
        await apiClient.delete(API_ENDPOINTS.timesheets.delete(id));
        fetchTimesheets();
      } catch (err) {
        setError('Failed to delete timesheet');
        console.error('Error deleting timesheet:', err);
      }
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'SUBMITTED': return 'badge-primary';
      case 'APPROVED': return 'badge-success';
      case 'REJECTED': return 'badge-danger';
      case 'REVISION_REQUESTED': return 'badge-warning';
      default: return 'badge-secondary';
    }
  };

  const canEdit = (timesheet: Timesheet) => {
    return timesheet.userId === user?.id || user?.role === 'ADMIN' || user?.role === 'MANAGER';
  };

  const canDelete = (timesheet: Timesheet) => {
    return timesheet.userId === user?.id || user?.role === 'ADMIN' || user?.role === 'MANAGER';
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
            <h1 className="text-2xl font-bold text-ca-primary-900">Timesheet Management</h1>
            <button
              onClick={() => setShowForm(true)}
              className="btn btn-primary"
            >
              Add Timesheet
            </button>
          </div>

          {error && (
            <div className="alert alert-error mb-6">
              {error}
            </div>
          )}

          {/* Timesheet Form */}
          {showForm && (
            <div className="card mb-6">
              <div className="card-header">
                <h2 className="text-lg font-medium text-ca-primary-900">
                  {editingTimesheet ? 'Edit Timesheet' : 'Add New Timesheet'}
                </h2>
              </div>
              <div className="card-body">
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">Date</label>
                      <input
                        type="date"
                        required
                        className="input mt-1"
                        value={formData.date}
                        onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                      />
                    </div>
                    <div>
                      <label className="form-label">Hours</label>
                      <input
                        type="number"
                        step="0.1"
                        min="0"
                        required
                        className="input mt-1"
                        value={formData.hours}
                        onChange={(e) => setFormData({ ...formData, hours: e.target.value })}
                      />
                    </div>
                  </div>
                  
                  <div>
                    <label className="form-label">Description</label>
                    <textarea
                      className="input mt-1"
                      rows={3}
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    />
                  </div>
                  
                  <div>
                    <label className="form-label">Comments</label>
                    <textarea
                      className="input mt-1"
                      rows={2}
                      value={formData.comments}
                      onChange={(e) => setFormData({ ...formData, comments: e.target.value })}
                    />
                  </div>
                  
                  <div className="flex justify-end space-x-3">
                    <button
                      type="button"
                      onClick={() => {
                        setShowForm(false);
                        setEditingTimesheet(null);
                        setFormData({ date: '', hours: '', description: '', comments: '' });
                      }}
                      className="btn btn-secondary"
                    >
                      Cancel
                    </button>
                    <button type="submit" className="btn btn-primary">
                      {editingTimesheet ? 'Update' : 'Create'} Timesheet
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* Timesheets List */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-medium text-ca-primary-900">Timesheets</h2>
            </div>
            <div className="card-body">
              {timesheets.length === 0 ? (
                <div className="text-center py-8 text-ca-primary-500">
                  No timesheets found. Create your first timesheet to get started.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Hours</th>
                        <th>Description</th>
                        <th>Status</th>
                        <th>Created</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {timesheets.map((timesheet) => (
                        <tr key={timesheet.id}>
                          <td>{new Date(timesheet.date).toLocaleDateString()}</td>
                          <td>{timesheet.hours}</td>
                          <td className="max-w-xs truncate">{timesheet.description || '-'}</td>
                          <td>
                            <span className={`badge ${getStatusColor(timesheet.status)}`}>
                              {timesheet.status.replace('_', ' ')}
                            </span>
                          </td>
                          <td>{new Date(timesheet.createdAt).toLocaleDateString()}</td>
                          <td>
                            <div className="flex space-x-2">
                              {canEdit(timesheet) && (
                                <button
                                  onClick={() => handleEdit(timesheet)}
                                  className="text-ca-highlight-600 hover:text-ca-highlight-900 text-sm font-medium"
                                >
                                  Edit
                                </button>
                              )}
                              {canDelete(timesheet) && (
                                <button
                                  onClick={() => handleDelete(timesheet.id)}
                                  className="text-bs-danger hover:text-red-900 text-sm font-medium"
                                >
                                  Delete
                                </button>
                              )}
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

export default TimesheetManagement;
