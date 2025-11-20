import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { apiClient, API_ENDPOINTS } from '../config/api';

interface PolicyCreationProps {
  onClose: () => void;
  onSave: () => void;
}

interface Role {
  id: number;
  name: string;
  displayName: string;
  description?: string;
  department?: string;
  location?: string;
  active: boolean;
}

interface ResourceField {
  name: string;
  type: string;
  required: boolean;
  description: string;
}

interface ResourceConfig {
  name: string;
  fields: ResourceField[];
  actions: string[];
}

const PolicyCreation: React.FC<PolicyCreationProps> = ({ onClose, onSave }) => {
  const { user } = useAuth();
  const [selectedResource, setSelectedResource] = useState<string>('timesheet');
  const [customResource, setCustomResource] = useState<string>('');
  const [availableRoles, setAvailableRoles] = useState<Role[]>([]);
  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
  const [rolePermissions, setRolePermissions] = useState<Record<string, Record<string, boolean>>>({});
  const [fieldPermissions, setFieldPermissions] = useState<Record<string, Record<string, Record<string, boolean>>>>({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Resource configurations
  const resourceConfigs: Record<string, ResourceConfig> = {
    timesheet: {
      name: 'Timesheet',
      fields: [
        { name: 'hours', type: 'number', required: true, description: 'Number of hours worked' },
        { name: 'date', type: 'date', required: true, description: 'Date of work' },
        { name: 'description', type: 'text', required: false, description: 'Work description' },
        { name: 'status', type: 'select', required: true, description: 'Timesheet status' },
        { name: 'provider_id', type: 'text', required: true, description: 'Provider identifier' },
        { name: 'signature', type: 'text', required: false, description: 'Digital signature' },
        { name: 'comments', type: 'text', required: false, description: 'Additional comments' }
      ],
      actions: ['CREATE', 'READ', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT']
    },
    case: {
      name: 'Case',
      fields: [
        { name: 'case_id', type: 'text', required: true, description: 'Case identifier' },
        { name: 'case_number', type: 'text', required: true, description: 'Case number' },
        { name: 'case_type', type: 'select', required: true, description: 'Type of case' },
        { name: 'status', type: 'select', required: true, description: 'Case status' },
        { name: 'priority', type: 'select', required: true, description: 'Case priority' },
        { name: 'assigned_to', type: 'text', required: false, description: 'Assigned caseworker' },
        { name: 'created_date', type: 'date', required: true, description: 'Case creation date' },
        { name: 'description', type: 'text', required: false, description: 'Case description' },
        { name: 'notes', type: 'text', required: false, description: 'Case notes' }
      ],
      actions: ['CREATE', 'READ', 'UPDATE', 'DELETE', 'ASSIGN', 'CLOSE', 'REOPEN']
    },
    person_search: {
      name: 'Person Search',
      fields: [
        { name: 'person_id', type: 'text', required: true, description: 'Person identifier' },
        { name: 'first_name', type: 'text', required: true, description: 'First name' },
        { name: 'last_name', type: 'text', required: true, description: 'Last name' },
        { name: 'ssn', type: 'text', required: false, description: 'Social Security Number' },
        { name: 'date_of_birth', type: 'date', required: false, description: 'Date of birth' },
        { name: 'address', type: 'text', required: false, description: 'Address information' },
        { name: 'phone', type: 'text', required: false, description: 'Phone number' },
        { name: 'email', type: 'text', required: false, description: 'Email address' },
        { name: 'search_criteria', type: 'text', required: true, description: 'Search criteria' }
      ],
      actions: ['SEARCH', 'READ', 'UPDATE', 'CREATE', 'DELETE']
    },
    payment: {
      name: 'Payment',
      fields: [
        { name: 'payment_id', type: 'text', required: true, description: 'Payment identifier' },
        { name: 'payment_amount', type: 'number', required: true, description: 'Payment amount' },
        { name: 'payment_type', type: 'select', required: true, description: 'Type of payment' },
        { name: 'payment_method', type: 'select', required: true, description: 'Payment method' },
        { name: 'recipient_id', type: 'text', required: true, description: 'Recipient identifier' },
        { name: 'payment_date', type: 'date', required: true, description: 'Payment date' },
        { name: 'status', type: 'select', required: true, description: 'Payment status' },
        { name: 'reference_number', type: 'text', required: false, description: 'Reference number' },
        { name: 'notes', type: 'text', required: false, description: 'Payment notes' }
      ],
      actions: ['CREATE', 'READ', 'UPDATE', 'DELETE', 'APPROVE', 'PROCESS', 'CANCEL']
    }
  };

  // Load available roles when component mounts
  useEffect(() => {
    fetchAvailableRoles();
  }, []);

  // Initialize permissions when resource or selected roles change
  useEffect(() => {
    if (selectedRoles.length > 0) {
      initializePermissions();
    }
  }, [selectedResource, selectedRoles]);

  const fetchAvailableRoles = async () => {
    try {
      const response = await apiClient.get('/roles/active');
      setAvailableRoles(response.data);
      // Select first two roles by default
      if (response.data.length >= 2) {
        setSelectedRoles([response.data[0].name, response.data[1].name]);
      } else if (response.data.length > 0) {
        setSelectedRoles([response.data[0].name]);
      }
    } catch (err) {
      setError('Failed to fetch available roles');
      console.error('Error fetching roles:', err);
    }
  };

  const initializePermissions = () => {
    const resource = resourceConfigs[selectedResource];
    if (!resource) return;

    // Initialize role permissions
    const newRolePermissions: Record<string, Record<string, boolean>> = {};
    selectedRoles.forEach(roleName => {
      newRolePermissions[roleName] = {};
      resource.actions.forEach(action => {
        // Set default permissions based on role
        if (roleName === 'ADMIN') {
          newRolePermissions[roleName][action] = true;
        } else if (roleName === 'SUPERVISOR') {
          newRolePermissions[roleName][action] = ['READ', 'UPDATE', 'APPROVE'].includes(action);
        } else if (roleName === 'CASE_WORKER') {
          newRolePermissions[roleName][action] = ['CREATE', 'READ', 'UPDATE'].includes(action);
        } else {
          newRolePermissions[roleName][action] = ['READ'].includes(action);
        }
      });
    });
    setRolePermissions(newRolePermissions);

    // Initialize field permissions
    const newFieldPermissions: Record<string, Record<string, Record<string, boolean>>> = {};
    resource.fields.forEach(field => {
      newFieldPermissions[field.name] = {};
      selectedRoles.forEach(roleName => {
        newFieldPermissions[field.name][roleName] = {
          read: true,
          write: roleName === 'ADMIN' || roleName === 'SUPERVISOR',
          hide: false
        };
      });
    });
    setFieldPermissions(newFieldPermissions);
  };

  const handleResourceChange = (resource: string) => {
    setSelectedResource(resource);
    if (resource !== 'custom') {
      setCustomResource('');
    }
  };


  const handleRolePermissionChange = (role: string, action: string, checked: boolean) => {
    setRolePermissions(prev => ({
      ...prev,
      [role]: {
        ...prev[role],
        [action]: checked
      }
    }));
  };

  const handleFieldPermissionChange = (field: string, role: string, permission: string, checked: boolean) => {
    setFieldPermissions(prev => ({
      ...prev,
      [field]: {
        ...prev[field],
        [role]: {
          ...prev[field][role],
          [permission]: checked
        }
      }
    }));
  };

  const handleSave = async () => {
    try {
      setLoading(true);
      setError('');

      const resource = selectedResource === 'custom' ? customResource : selectedResource;
      const resourceConfig = resourceConfigs[selectedResource];

      // Create policies for each role and action combination
      const policies = [];
      
      for (const roleName of selectedRoles) {
        for (const action of resourceConfig.actions) {
          if (rolePermissions[roleName]?.[action]) {
            policies.push({
              role: roleName,
              resource: `/api/${resource}`,
              action: action,
              allowed: true,
              description: `${roleName} can ${action} ${resourceConfig.name}`,
              priority: 10
            });
          }
        }
      }

      // Save all policies
      for (const policy of policies) {
        await apiClient.post(API_ENDPOINTS.policies.create, policy);
      }

      onSave();
      onClose();
    } catch (err) {
      setError('Failed to save policies');
      console.error('Error saving policies:', err);
    } finally {
      setLoading(false);
    }
  };

  const currentResource = resourceConfigs[selectedResource];

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-lg max-w-6xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold text-ca-primary-900">Create New Policy</h2>
            <button
              onClick={onClose}
              className="text-ca-primary-600 hover:text-ca-primary-900 text-xl"
            >
              ×
            </button>
          </div>

          {error && (
            <div className="alert alert-error mb-4">
              {error}
            </div>
          )}

          {/* Resource Selection */}
          <div className="form-group">
            <label className="form-label">Select Resource:</label>
            <select
              value={selectedResource}
              onChange={(e) => handleResourceChange(e.target.value)}
              className="input"
            >
              <option value="timesheet">Timesheet</option>
              <option value="case">Case</option>
              <option value="person_search">Person Search</option>
              <option value="payment">Payment</option>
              <option value="custom">Custom Resource</option>
            </select>
            
            {selectedResource === 'custom' && (
              <input
                type="text"
                placeholder="Enter Custom Resource Name"
                value={customResource}
                onChange={(e) => setCustomResource(e.target.value)}
                className="input mt-2"
              />
            )}
          </div>

          {/* Role Selection */}
          <div className="form-group">
            <label className="form-label">Select Roles:</label>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-2 mt-2">
              {availableRoles.map((role) => (
                <label key={role.id} className="flex items-center space-x-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedRoles.includes(role.name)}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setSelectedRoles([...selectedRoles, role.name]);
                      } else {
                        setSelectedRoles(selectedRoles.filter(r => r !== role.name));
                      }
                    }}
                    className="h-4 w-4 text-ca-highlight-600 focus:ring-ca-highlight-500 border-ca-primary-300 rounded"
                  />
                  <span className="text-sm text-ca-primary-900">{role.displayName}</span>
                </label>
              ))}
            </div>
            <p className="text-xs text-ca-primary-600 mt-2">
              Select the roles you want to configure permissions for. You can add new roles in Role Management.
            </p>
          </div>

            <div className="mt-4">
              <h3 className="text-lg font-medium text-ca-primary-900 mb-2">Role Permissions</h3>
              <div className="overflow-x-auto">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Role Name</th>
                      {currentResource.actions.map(action => (
                        <th key={action}>{action}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {selectedRoles.map((roleName) => {
                      const role = availableRoles.find(r => r.name === roleName);
                      return (
                        <tr key={roleName}>
                          <td>
                            <span className="font-medium text-ca-primary-900">
                              {role?.displayName || roleName}
                            </span>
                          </td>
                          {currentResource.actions.map(action => (
                            <td key={action}>
                              <input
                                type="checkbox"
                                checked={rolePermissions[roleName]?.[action] || false}
                                onChange={(e) => {
                                  setRolePermissions(prev => ({
                                    ...prev,
                                    [roleName]: {
                                      ...prev[roleName],
                                      [action]: e.target.checked
                                    }
                                  }));
                                }}
                                className="h-4 w-4 text-ca-highlight-600 focus:ring-ca-highlight-500 border-ca-primary-300 rounded"
                              />
                            </td>
                          ))}
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>

          {/* Field-Level Security */}
          <div className="form-group">
            <h3 className="text-lg font-medium text-ca-primary-900 mb-2">
              Field-Level Security for {currentResource.name}
            </h3>
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Field Name</th>
                    <th>Description</th>
                    {selectedRoles.map(roleName => {
                      const role = availableRoles.find(r => r.name === roleName);
                      return (
                        <th key={roleName} colSpan={3}>
                          {role?.displayName || roleName}
                        </th>
                      );
                    })}
                  </tr>
                  <tr>
                    <th></th>
                    <th></th>
                    {selectedRoles.map(roleName => (
                      <th key={`${roleName}-read`}>Read</th>
                    ))}
                    {selectedRoles.map(roleName => (
                      <th key={`${roleName}-write`}>Write</th>
                    ))}
                    {selectedRoles.map(roleName => (
                      <th key={`${roleName}-hide`}>Hide</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {currentResource.fields.map(field => (
                    <tr key={field.name}>
                      <td>
                        <div>
                          <span className="font-medium">{field.name}</span>
                          {field.required && <span className="text-bs-danger ml-1">*</span>}
                        </div>
                      </td>
                      <td>
                        <span className="text-sm text-ca-primary-600">{field.description}</span>
                      </td>
                      {selectedRoles.map(roleName => (
                        <td key={`${field.name}-${roleName}-read`}>
                          <input
                            type="checkbox"
                            checked={fieldPermissions[field.name]?.[roleName]?.read || false}
                            onChange={(e) => {
                              setFieldPermissions(prev => ({
                                ...prev,
                                [field.name]: {
                                  ...prev[field.name],
                                  [roleName]: {
                                    ...prev[field.name]?.[roleName],
                                    read: e.target.checked
                                  }
                                }
                              }));
                            }}
                            className="h-4 w-4 text-ca-highlight-600 focus:ring-ca-highlight-500 border-ca-primary-300 rounded"
                          />
                        </td>
                      ))}
                      {selectedRoles.map(roleName => (
                        <td key={`${field.name}-${roleName}-write`}>
                          <input
                            type="checkbox"
                            checked={fieldPermissions[field.name]?.[roleName]?.write || false}
                            onChange={(e) => {
                              setFieldPermissions(prev => ({
                                ...prev,
                                [field.name]: {
                                  ...prev[field.name],
                                  [roleName]: {
                                    ...prev[field.name]?.[roleName],
                                    write: e.target.checked
                                  }
                                }
                              }));
                            }}
                            className="h-4 w-4 text-ca-highlight-600 focus:ring-ca-highlight-500 border-ca-primary-300 rounded"
                          />
                        </td>
                      ))}
                      {selectedRoles.map(roleName => (
                        <td key={`${field.name}-${roleName}-hide`}>
                          <input
                            type="checkbox"
                            checked={fieldPermissions[field.name]?.[roleName]?.hide || false}
                            onChange={(e) => {
                              setFieldPermissions(prev => ({
                                ...prev,
                                [field.name]: {
                                  ...prev[field.name],
                                  [roleName]: {
                                    ...prev[field.name]?.[roleName],
                                    hide: e.target.checked
                                  }
                                }
                              }));
                            }}
                            className="h-4 w-4 text-ca-highlight-600 focus:ring-ca-highlight-500 border-ca-primary-300 rounded"
                          />
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Actions */}
          <div className="flex justify-end gap-3 mt-6">
            <button
              onClick={onClose}
              className="btn btn-secondary"
              disabled={loading}
            >
              Cancel
            </button>
            <button
              onClick={handleSave}
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'Saving...' : 'Save Policy'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PolicyCreation;

