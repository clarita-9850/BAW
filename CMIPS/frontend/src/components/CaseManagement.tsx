import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { apiClient, API_ENDPOINTS } from '../config/api';

interface Case {
  id: number;
  caseNumber: string;
  caseType: string;
  status: string;
  priority: string;
  assignedTo: string;
  createdDate: string;
  description: string;
  notes: string;
}

const CaseManagement: React.FC = () => {
  const { user } = useAuth();
  const [cases, setCases] = useState<Case[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingCase, setEditingCase] = useState<Case | null>(null);
  const [formData, setFormData] = useState({
    caseNumber: '',
    caseType: '',
    status: 'OPEN',
    priority: 'MEDIUM',
    assignedTo: '',
    description: '',
    notes: ''
  });

  useEffect(() => {
    fetchCases();
  }, []);

  const fetchCases = async () => {
    try {
      setLoading(true);
      // Mock data for now - in real implementation, this would call the backend
      const mockCases: Case[] = [
        {
          id: 1,
          caseNumber: 'CASE-2024-001',
          caseType: 'Child Welfare',
          status: 'OPEN',
          priority: 'HIGH',
          assignedTo: 'John Doe',
          createdDate: '2024-01-15',
          description: 'Child welfare case requiring immediate attention',
          notes: 'Initial assessment completed'
        },
        {
          id: 2,
          caseNumber: 'CASE-2024-002',
          caseType: 'Adult Services',
          status: 'IN_PROGRESS',
          priority: 'MEDIUM',
          assignedTo: 'Jane Smith',
          createdDate: '2024-01-16',
          description: 'Adult services case for elderly client',
          notes: 'Waiting for medical assessment'
        }
      ];
      setCases(mockCases);
    } catch (err) {
      setError('Failed to fetch cases');
      console.error('Error fetching cases:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingCase) {
        // Update existing case
        const updatedCase = { ...editingCase, ...formData };
        setCases(cases.map(c => c.id === editingCase.id ? updatedCase : c));
      } else {
        // Create new case
        const newCase: Case = {
          id: Date.now(),
          ...formData,
          createdDate: new Date().toISOString().split('T')[0]
        };
        setCases([...cases, newCase]);
      }

      setShowForm(false);
      setEditingCase(null);
      setFormData({
        caseNumber: '',
        caseType: '',
        status: 'OPEN',
        priority: 'MEDIUM',
        assignedTo: '',
        description: '',
        notes: ''
      });
    } catch (err) {
      setError('Failed to save case');
      console.error('Error saving case:', err);
    }
  };

  const handleEdit = (caseItem: Case) => {
    setEditingCase(caseItem);
    setFormData({
      caseNumber: caseItem.caseNumber,
      caseType: caseItem.caseType,
      status: caseItem.status,
      priority: caseItem.priority,
      assignedTo: caseItem.assignedTo,
      description: caseItem.description,
      notes: caseItem.notes
    });
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this case?')) {
      setCases(cases.filter(c => c.id !== id));
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return 'badge-primary';
      case 'IN_PROGRESS': return 'badge-warning';
      case 'CLOSED': return 'badge-success';
      case 'CANCELLED': return 'badge-danger';
      default: return 'badge-secondary';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH': return 'badge-danger';
      case 'MEDIUM': return 'badge-warning';
      case 'LOW': return 'badge-success';
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
            <h1 className="text-2xl font-bold text-ca-primary-900">Case Management</h1>
            <button
              onClick={() => setShowForm(true)}
              className="btn btn-primary"
            >
              Create New Case
            </button>
          </div>

          {error && (
            <div className="alert alert-error mb-6">
              {error}
            </div>
          )}

          {/* Case Form */}
          {showForm && (
            <div className="card mb-6">
              <div className="card-header">
                <h2 className="text-lg font-medium text-ca-primary-900">
                  {editingCase ? 'Edit Case' : 'Create New Case'}
                </h2>
              </div>
              <div className="card-body">
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">Case Number</label>
                      <input
                        type="text"
                        required
                        className="input mt-1"
                        value={formData.caseNumber}
                        onChange={(e) => setFormData({ ...formData, caseNumber: e.target.value })}
                      />
                    </div>
                    
                    <div>
                      <label className="form-label">Case Type</label>
                      <select
                        required
                        className="input mt-1"
                        value={formData.caseType}
                        onChange={(e) => setFormData({ ...formData, caseType: e.target.value })}
                      >
                        <option value="">Select Case Type</option>
                        <option value="Child Welfare">Child Welfare</option>
                        <option value="Adult Services">Adult Services</option>
                        <option value="Family Services">Family Services</option>
                        <option value="Mental Health">Mental Health</option>
                      </select>
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">Status</label>
                      <select
                        required
                        className="input mt-1"
                        value={formData.status}
                        onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                      >
                        <option value="OPEN">Open</option>
                        <option value="IN_PROGRESS">In Progress</option>
                        <option value="CLOSED">Closed</option>
                        <option value="CANCELLED">Cancelled</option>
                      </select>
                    </div>
                    
                    <div>
                      <label className="form-label">Priority</label>
                      <select
                        required
                        className="input mt-1"
                        value={formData.priority}
                        onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                      >
                        <option value="HIGH">High</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="LOW">Low</option>
                      </select>
                    </div>
                  </div>
                  
                  <div>
                    <label className="form-label">Assigned To</label>
                    <input
                      type="text"
                      className="input mt-1"
                      value={formData.assignedTo}
                      onChange={(e) => setFormData({ ...formData, assignedTo: e.target.value })}
                    />
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
                    <label className="form-label">Notes</label>
                    <textarea
                      className="input mt-1"
                      rows={2}
                      value={formData.notes}
                      onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                    />
                  </div>
                  
                  <div className="flex justify-end space-x-3">
                    <button
                      type="button"
                      onClick={() => {
                        setShowForm(false);
                        setEditingCase(null);
                        setFormData({
                          caseNumber: '',
                          caseType: '',
                          status: 'OPEN',
                          priority: 'MEDIUM',
                          assignedTo: '',
                          description: '',
                          notes: ''
                        });
                      }}
                      className="btn btn-secondary"
                    >
                      Cancel
                    </button>
                    <button type="submit" className="btn btn-primary">
                      {editingCase ? 'Update' : 'Create'} Case
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* Cases List */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-medium text-ca-primary-900">Cases</h2>
            </div>
            <div className="card-body">
              {cases.length === 0 ? (
                <div className="text-center py-8 text-ca-primary-500">
                  No cases found. Create your first case to get started.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Case Number</th>
                        <th>Type</th>
                        <th>Status</th>
                        <th>Priority</th>
                        <th>Assigned To</th>
                        <th>Created Date</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {cases.map((caseItem) => (
                        <tr key={caseItem.id}>
                          <td>
                            <span className="font-medium text-ca-primary-900">{caseItem.caseNumber}</span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-900">{caseItem.caseType}</span>
                          </td>
                          <td>
                            <span className={`badge ${getStatusColor(caseItem.status)}`}>
                              {caseItem.status.replace('_', ' ')}
                            </span>
                          </td>
                          <td>
                            <span className={`badge ${getPriorityColor(caseItem.priority)}`}>
                              {caseItem.priority}
                            </span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-900">{caseItem.assignedTo || 'Unassigned'}</span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-600">{caseItem.createdDate}</span>
                          </td>
                          <td>
                            <div className="flex space-x-2">
                              <button
                                onClick={() => handleEdit(caseItem)}
                                className="text-ca-highlight-600 hover:text-ca-highlight-900 text-sm font-medium"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleDelete(caseItem.id)}
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

export default CaseManagement;


