import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../config/api';

const TimesheetManagement = () => {
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const [timesheets, setTimesheets] = useState([]);
  const [availableFields, setAvailableFields] = useState([]);
  const [allowedActions, setAllowedActions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingTimesheet, setEditingTimesheet] = useState(null);
  const [formData, setFormData] = useState({
    payPeriodStart: '',
    payPeriodEnd: '',
    regularHours: '',
    overtimeHours: '',
    department: '',
    location: '',
    comments: ''
  });

  useEffect(() => {
    if (authLoading) return; // Wait for auth to initialize
    if (!user) {
      navigate('/login');
      return;
    }
    fetchTimesheets();
  }, [user, navigate, authLoading]);

  const fetchTimesheets = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await apiClient.get('/timesheets');
      // Handle paginated response from backend
      const timesheets = response.data.content || response.data;
      const actions = response.data.allowedActions || [];
      
      setTimesheets(timesheets);
      setAllowedActions(actions);
      
      // Extract available fields dynamically from the first timesheet
      if (timesheets.length > 0) {
        const fields = Object.keys(timesheets[0]);
        setAvailableFields(fields);
        console.log('Available fields from backend:', fields);
      }
      
      console.log('Allowed actions from backend:', actions);
      console.log('Actions length:', actions.length);
      console.log('Actions type:', typeof actions);
      
      // Debug: Show which buttons will be displayed
      if (actions.length > 0) {
        console.log('✅ Actions found - Buttons that will show:');
        actions.forEach(action => console.log(`  - ${action} button`));
      } else {
        console.log('❌ No actions found - No action buttons will show');
        console.log('💡 Add provider_actions attribute in Keycloak to show buttons');
      }
    } catch (err) {
      console.error('Error fetching timesheets:', err);
      if (err.response?.status === 403) {
        setError('Access denied: You do not have permission to view timesheets');
      } else if (err.response?.status === 401) {
        setError('Authentication failed. Please login again.');
      } else {
        setError('Failed to fetch timesheets. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    // Validation
    if (!formData.payPeriodStart || !formData.payPeriodEnd) {
      setError('Please enter both start and end dates');
      return;
    }
    
    if (!formData.regularHours || parseFloat(formData.regularHours) <= 0) {
      setError('Please enter regular hours (must be greater than 0)');
      return;
    }
    
    if (!formData.department || !formData.location) {
      setError('Please enter department and location');
      return;
    }
    
    try {
      const timesheetData = {
        employeeId: user?.userId || user?.username,
        employeeName: user?.username || 'Unknown',
        payPeriodStart: formData.payPeriodStart,
        payPeriodEnd: formData.payPeriodEnd,
        regularHours: parseFloat(formData.regularHours) || 0,
        overtimeHours: parseFloat(formData.overtimeHours) || 0,
        holidayHours: 0,
        sickHours: 0,
        vacationHours: 0,
        department: formData.department,
        location: formData.location,
        comments: formData.comments || ''
      };

      if (editingTimesheet) {
        // Get the timesheet ID using our helper function
        const timesheetId = await getTimesheetId(editingTimesheet);
        
        if (timesheetId) {
          await apiClient.put(`/timesheets/${timesheetId}`, timesheetData);
          setSuccess('Timesheet updated successfully!');
        } else {
          // If we can't find the ID, show an error with helpful message
          setError('Cannot edit timesheet: ID not available. Please add "id" to your Keycloak provider_read_fields configuration.');
          return;
        }
      } else {
        await apiClient.post('/timesheets', timesheetData);
        setSuccess('Timesheet created successfully!');
      }

      setShowForm(false);
      setEditingTimesheet(null);
      setFormData({
        payPeriodStart: '',
        payPeriodEnd: '',
        regularHours: '',
        overtimeHours: '',
        department: '',
        location: '',
        comments: ''
      });
      fetchTimesheets();
    } catch (err) {
      console.error('Error saving timesheet:', err);
      
      // Better error handling
      if (err.response?.status === 400) {
        if (err.response?.data?.message) {
          setError(err.response.data.message);
        } else if (typeof err.response?.data === 'string' && err.response.data.includes('already exists')) {
          setError('A timesheet already exists for this pay period. Please choose different dates or edit the existing timesheet.');
        } else {
          setError('Invalid data. Please check all fields and try again.');
        }
      } else if (err.response?.status === 403) {
        setError('You do not have permission to create timesheets.');
      } else {
        setError('Failed to save timesheet. Please try again.');
      }
    }
  };

  const handleEdit = (timesheet) => {
    setEditingTimesheet(timesheet);
    setFormData({
      payPeriodStart: timesheet.payPeriodStart || '',
      payPeriodEnd: timesheet.payPeriodEnd || '',
      regularHours: timesheet.regularHours?.toString() || '',
      overtimeHours: timesheet.overtimeHours?.toString() || '',
      department: timesheet.department || '',
      location: timesheet.location || '',
      comments: timesheet.comments || ''
    });
    setShowForm(true);
  };

  const handleDelete = async (timesheet) => {
    if (window.confirm('Are you sure you want to delete this timesheet?')) {
      try {
        const timesheetId = await getTimesheetId(timesheet);
        if (timesheetId) {
          // Send delete request with optional request body for field filtering
          const deleteData = {
            reason: "Deleted by user"
          };
          
          await apiClient.delete(`/timesheets/${timesheetId}`, { data: deleteData });
          setSuccess('Timesheet deleted successfully!');
          fetchTimesheets();
        } else {
          setError('Cannot delete timesheet: ID not available. Please add "id" to your Keycloak provider_read_fields configuration.');
        }
      } catch (err) {
        console.error('Error deleting timesheet:', err);
        if (err.response?.status === 403) {
          setError('Access denied: You do not have permission to delete timesheets');
        } else if (err.response?.status === 400) {
          setError(err.response?.data?.message || 'Cannot delete this timesheet');
        } else {
          setError('Failed to delete timesheet');
        }
      }
    }
  };

  const handleApprove = async (timesheet) => {
    try {
      const timesheetId = await getTimesheetId(timesheet);
      if (timesheetId) {
        // Send approve request with supervisor comments
        const approveData = {
          supervisorComments: "Approved by supervisor"
        };
        
        const response = await apiClient.post(`/timesheets/${timesheetId}/approve`, approveData);
        setSuccess('Timesheet approved successfully!');
        fetchTimesheets();
      } else {
        setError('Cannot approve timesheet: ID not available. Please add "id" to your Keycloak provider_read_fields configuration.');
      }
    } catch (err) {
      console.error('Error approving timesheet:', err);
      if (err.response?.status === 403) {
        setError('Access denied: You do not have permission to approve timesheets');
      } else {
        setError(err.response?.data?.message || 'Failed to approve timesheet. Please try again.');
      }
    }
  };

  const handleReject = async (timesheet) => {
    const reason = prompt('Please enter rejection reason:');
    if (reason) {
      try {
        const timesheetId = await getTimesheetId(timesheet);
        if (timesheetId) {
          // Send reject request with supervisor comments
          const rejectData = {
            supervisorComments: reason
          };
          
          const response = await apiClient.post(`/timesheets/${timesheetId}/reject`, rejectData);
          setSuccess('Timesheet rejected successfully!');
          fetchTimesheets();
        } else {
          setError('Cannot reject timesheet: ID not available. Please add "id" to your Keycloak provider_read_fields configuration.');
        }
      } catch (err) {
        console.error('Error rejecting timesheet:', err);
        if (err.response?.status === 403) {
          setError('Access denied: You do not have permission to reject timesheets');
        } else {
          setError(err.response?.data?.message || 'Failed to reject timesheet. Please try again.');
        }
      }
    }
  };

  const getStatusBadge = (status) => {
    const badges = {
      DRAFT: 'badge-secondary',
      SUBMITTED: 'badge-primary',
      APPROVED: 'badge-success',
      REJECTED: 'badge-danger',
      REVISION_REQUESTED: 'badge-warning'
    };
    return badges[status] || 'badge-secondary';
  };

  // Helper function to get user-friendly field names
  const getFieldDisplayName = (fieldName) => {
    const fieldNames = {
      'id': 'ID',
      'userId': 'User ID',
      'employeeName': 'Employee Name',
      'department': 'Department',
      'location': 'Location',
      'payPeriodStart': 'Pay Period Start',
      'payPeriodEnd': 'Pay Period End',
      'regularHours': 'Regular Hours',
      'overtimeHours': 'Overtime Hours',
      'holidayHours': 'Holiday Hours',
      'sickHours': 'Sick Hours',
      'vacationHours': 'Vacation Hours',
      'totalHours': 'Total Hours',
      'status': 'Status',
      'comments': 'Comments',
      'supervisorComments': 'Supervisor Comments',
      'approvedBy': 'Approved By',
      'approvedAt': 'Approved At',
      'submittedBy': 'Submitted By',
      'submittedAt': 'Submitted At',
      'createdAt': 'Created At',
      'updatedAt': 'Updated At'
    };
    return fieldNames[fieldName] || fieldName.charAt(0).toUpperCase() + fieldName.slice(1);
  };

  // Helper function to format field values
  const formatFieldValue = (fieldName, value) => {
    if (value === null || value === undefined || value === '') {
      return <span className="text-gray-400 italic">Not accessible</span>;
    }
    
    if (fieldName === 'status') {
      return <span className={`badge ${getStatusBadge(value)}`}>{value}</span>;
    }
    
    if (fieldName.includes('Hours') && typeof value === 'number') {
      return value.toFixed(2);
    }
    
    if (fieldName.includes('Date') || fieldName.includes('At')) {
      return new Date(value).toLocaleDateString();
    }
    
    return value;
  };

  // Helper function to determine if a field should be shown in the table
  const shouldShowField = (fieldName) => {
    // Always show these fields
    const alwaysShow = ['status'];
    if (alwaysShow.includes(fieldName)) return true;
    
    // Hide internal/system fields
    const hideFields = ['id', 'userId', 'createdAt', 'updatedAt'];
    if (hideFields.includes(fieldName)) return false;
    
    // Show all other fields that are available
    return true;
  };

  // Helper function to get timesheet ID (since it might be filtered out)
  const getTimesheetId = async (timesheet) => {
    // If ID is available, use it
    if (timesheet.id) {
      return timesheet.id;
    }
    
    // If ID is not available, we need to find it by matching other fields
    // This is a fallback when the ID field is filtered out by Keycloak
    try {
      // Get all timesheets with full data to find the matching ID
      const response = await apiClient.get('/timesheets');
      const allTimesheets = response.data.content || response.data;
      
      // Find matching timesheet by comparing available fields
      const matchingTimesheet = allTimesheets.find(t => 
        t.employeeName === timesheet.employeeName &&
        t.location === timesheet.location &&
        t.overtimeHours === timesheet.overtimeHours
      );
      
      return matchingTimesheet?.id || null;
    } catch (error) {
      console.error('Error getting timesheet ID:', error);
      return null;
    }
  };

  return (
    <div className="min-h-screen bg-ca-secondary-50">
      {/* Header */}
      <div className="ca-header">
        <div className="container">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-4">
              <div className="ca-logo">CA</div>
              <h1 className="text-xl font-bold text-ca-primary-900">Timesheet Management</h1>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-ca-primary-700">
                {user?.username} ({user?.role})
              </span>
              {allowedActions.length > 0 && (
                <span className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded">
                  Actions: {allowedActions.join(', ')}
                </span>
              )}
              {allowedActions.length === 0 && (
                <span className="text-xs bg-red-100 text-red-800 px-2 py-1 rounded">
                  No actions configured
                </span>
              )}
              <button onClick={() => navigate('/')} className="btn btn-outline">
                Back to Dashboard
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container py-8">
        {/* Alerts */}
        {error && (
          <div className="alert alert-error mb-4">
            {error}
          </div>
        )}
        {success && (
          <div className="alert alert-success mb-4">
            {success}
          </div>
        )}

        {/* Action Buttons */}
        <div className="mb-6 flex justify-between items-center">
          <h2 className="text-2xl font-bold text-ca-primary-900">My Timesheets</h2>
          {/* Show "New Timesheet" button only if user has 'create' action permission */}
          {allowedActions.includes('create') && (
            <button
              onClick={() => {
                if (showForm) {
                  setShowForm(false);
                  setEditingTimesheet(null);
                } else {
                  setShowForm(true);
                  setEditingTimesheet(null);
                  setError('');
                  setSuccess('');
                }
              }}
              className="btn btn-primary"
            >
              {showForm ? 'Cancel' : '+ New Timesheet'}
            </button>
          )}
        </div>

        {/* Timesheet Form */}
        {showForm && (
          <div className="card mb-6">
            <div className="card-header">
              <h3 className="text-lg font-semibold text-ca-primary-900">
                {editingTimesheet ? 'Edit Timesheet' : 'Create New Timesheet'}
              </h3>
            </div>
            <div className="card-body">
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="form-group">
                    <label className="form-label">Pay Period Start Date *</label>
                    <input
                      type="date"
                      required
                      className="input"
                      value={formData.payPeriodStart}
                      onChange={(e) => setFormData({...formData, payPeriodStart: e.target.value})}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Pay Period End Date *</label>
                    <input
                      type="date"
                      required
                      className="input"
                      value={formData.payPeriodEnd}
                      onChange={(e) => setFormData({...formData, payPeriodEnd: e.target.value})}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Regular Hours</label>
                    <input
                      type="number"
                      min="0"
                      step="0.5"
                      className="input"
                      placeholder="e.g., 40"
                      value={formData.regularHours}
                      onChange={(e) => setFormData({...formData, regularHours: e.target.value})}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Overtime Hours</label>
                    <input
                      type="number"
                      min="0"
                      step="0.5"
                      className="input"
                      placeholder="e.g., 5"
                      value={formData.overtimeHours}
                      onChange={(e) => setFormData({...formData, overtimeHours: e.target.value})}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Department *</label>
                    <input
                      type="text"
                      required
                      className="input"
                      placeholder="e.g., IT"
                      value={formData.department}
                      onChange={(e) => setFormData({...formData, department: e.target.value})}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Location *</label>
                    <input
                      type="text"
                      required
                      className="input"
                      placeholder="e.g., Sacramento"
                      value={formData.location}
                      onChange={(e) => setFormData({...formData, location: e.target.value})}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Comments</label>
                    <textarea
                      className="input"
                      rows="3"
                      placeholder="Additional notes..."
                      value={formData.comments}
                      onChange={(e) => setFormData({...formData, comments: e.target.value})}
                    />
                  </div>
                </div>
                <div className="flex justify-end space-x-2">
                  <button
                    type="button"
                    onClick={() => {
                      setShowForm(false);
                      setEditingTimesheet(null);
                      setFormData({
                        startDate: '',
                        endDate: '',
                        hoursWorked: '',
                        department: '',
                        location: '',
                        comments: ''
                      });
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
            <h3 className="text-lg font-semibold text-ca-primary-900">Timesheets</h3>
          </div>
          <div className="card-body">
            {loading ? (
              <div className="text-center py-8">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-ca-highlight-600 mx-auto"></div>
                <p className="mt-4 text-ca-primary-600">Loading timesheets...</p>
              </div>
            ) : timesheets.length === 0 ? (
              <div className="text-center py-8 text-ca-primary-600">
                No timesheets found. Create your first timesheet!
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="table">
                  <thead>
                    <tr>
                      {/* Dynamically generate table headers based on available fields */}
                      {availableFields.filter(shouldShowField).map((fieldName) => (
                        <th key={fieldName}>{getFieldDisplayName(fieldName)}</th>
                      ))}
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {timesheets.map((timesheet) => (
                      <tr key={timesheet.id}>
                        {/* Dynamically generate table cells based on available fields */}
                        {availableFields.filter(shouldShowField).map((fieldName) => (
                          <td key={fieldName}>
                            {formatFieldValue(fieldName, timesheet[fieldName])}
                          </td>
                        ))}
                        <td>
                          <div className="flex space-x-2">
                            {/* Dynamically render action buttons based on allowedActions from Keycloak */}
                            {allowedActions.includes('edit') && (
                              <button
                                onClick={() => handleEdit(timesheet)}
                                className="btn btn-sm btn-secondary"
                                title="Edit this timesheet"
                              >
                                Edit
                              </button>
                            )}
                            {allowedActions.includes('delete') && (
                              <button
                                onClick={() => handleDelete(timesheet)}
                                className="btn btn-sm btn-danger"
                                title="Delete this timesheet"
                              >
                                Delete
                              </button>
                            )}
                            {allowedActions.includes('approve') && (
                              <button
                                onClick={() => handleApprove(timesheet)}
                                className="btn btn-sm btn-success"
                                title="Approve this timesheet"
                              >
                                Approve
                              </button>
                            )}
                            {allowedActions.includes('reject') && (
                              <button
                                onClick={() => handleReject(timesheet)}
                                className="btn btn-sm btn-danger"
                                title="Reject this timesheet"
                              >
                                Reject
                              </button>
                            )}
                            {allowedActions.length === 0 && (
                              <span className="text-gray-400 italic text-sm">
                                No actions available
                                <br />
                                <span className="text-xs">(Add provider_actions in Keycloak)</span>
                              </span>
                            )}
                            {allowedActions.length > 0 && !timesheet.id && (
                              <span className="text-yellow-600 italic text-sm">
                                Actions available but ID missing
                                <br />
                                <span className="text-xs">(Add "id" to provider_read_fields in Keycloak)</span>
                              </span>
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
  );
};

export default TimesheetManagement;
