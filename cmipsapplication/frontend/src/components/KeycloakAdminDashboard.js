import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../contexts/AuthContext';
import './AdminDashboard.css';

const API_BASE_URL = 'http://localhost:8081/api/admin/keycloak';

/**
 * Keycloak Admin Dashboard
 * User-friendly interface for managing Keycloak without accessing the admin console
 */
const KeycloakAdminDashboard = () => {
  const { user, refreshTokenIfNeeded, loading: authLoading } = useAuth();
  const [activeTab, setActiveTab] = useState('users');
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [resources, setResources] = useState([]);
  const [policies, setPolicies] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  // User form state
  const [userForm, setUserForm] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    role: ''
  });

  // Policy form state
  const [policyForm, setPolicyForm] = useState({
    name: '',
    description: '',
    roleName: 'PROVIDER'
  });

  // Permission form state
  const [permissionForm, setPermissionForm] = useState({
    name: '',
    description: '',
    resourceId: '',
    scopeIds: [],
    policyIds: []
  });

  // State for editing resource attributes
  const [editingResource, setEditingResource] = useState(null);
  const [attributeForm, setAttributeForm] = useState({
    key: '',
    value: ''
  });
  const [editingAttribute, setEditingAttribute] = useState(null); // { resourceId, oldKey, key, value }
  const [attributeEditForm, setAttributeEditForm] = useState({
    key: '',
    value: ''
  });

  // Role form state
  const [roleForm, setRoleForm] = useState({
    name: '',
    description: ''
  });

  // Group form state
  const [groupForm, setGroupForm] = useState({
    name: '',
    parentGroupId: ''
  });
  const [selectedGroupMembers, setSelectedGroupMembers] = useState({}); // { groupId: [members] }

  const getAuthHeaders = async () => {
    // Admin endpoints now require user authentication with ADMIN role
    const token = await refreshTokenIfNeeded();
    return {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
  };

  const showMessage = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 5000);
  };

  const handleApiError = (error, defaultMessage) => {
    if (error.response?.status === 401) {
      showMessage('error', 'Session expired. Please login again.');
      // Don't show the default message for 401 errors
      return;
    }
    showMessage('error', defaultMessage + ': ' + (error.response?.data?.message || error.message));
  };

  // ============================== USER MANAGEMENT ==============================

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_BASE_URL}/users`, {
        headers: await getAuthHeaders()
      });
      setUsers(response.data);
    } catch (error) {
      handleApiError(error, 'Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  const createUser = async (e) => {
    e.preventDefault();
    
    // Validate that a role is selected
    if (!userForm.role) {
      showMessage('error', 'Please select a role for the user');
      return;
    }
    
    setLoading(true);
    try {
      const response = await axios.post(`${API_BASE_URL}/users`, userForm, {
        headers: await getAuthHeaders()
      });
      showMessage('success', response.data.message || 'User created successfully');
      setUserForm({
        username: '',
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        role: ''
      });
      fetchUsers();
    } catch (error) {
      showMessage('error', 'Failed to create user: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const deleteUser = async (userId, username) => {
    if (!window.confirm(`Are you sure you want to delete user: ${username}?`)) {
      return;
    }
    
    setLoading(true);
    try {
      await axios.delete(`${API_BASE_URL}/users/${userId}`, {
        headers: await getAuthHeaders()
      });
      showMessage('success', 'User deleted successfully');
      fetchUsers();
    } catch (error) {
      showMessage('error', 'Failed to delete user: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  // ============================== ROLE MANAGEMENT ==============================

  const fetchRoles = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_BASE_URL}/roles`, {
        headers: await getAuthHeaders()
      });
      setRoles(response.data);
    } catch (error) {
      handleApiError(error, 'Failed to fetch roles');
    } finally {
      setLoading(false);
    }
  };

  // ============================== RESOURCE ATTRIBUTE MANAGEMENT ==============================

  const addResourceAttribute = async (resourceId, resourceName) => {
    if (!attributeForm.key || !attributeForm.value) {
      showMessage('error', 'Both key and value are required');
      return;
    }

    try {
      setLoading(true);
      const response = await axios.post(
        `${API_BASE_URL}/resources/${resourceId}/attributes`,
        {
          key: attributeForm.key,
          value: attributeForm.value
        },
        { headers: await getAuthHeaders() }
      );

      showMessage('success', `Attribute added to ${resourceName}`);
      setAttributeForm({ key: '', value: '' });
      setEditingResource(null);
      fetchResources(); // Refresh resources
    } catch (error) {
      console.error('Error adding attribute:', error);
      showMessage('error', error.response?.data?.message || 'Failed to add attribute');
    } finally {
      setLoading(false);
    }
  };

  const deleteResourceAttribute = async (resourceId, resourceName, attributeKey) => {
    if (!window.confirm(`Are you sure you want to delete attribute "${attributeKey}" from ${resourceName}?`)) {
      return;
    }

    try {
      setLoading(true);
      await axios.delete(
        `${API_BASE_URL}/resources/${resourceId}/attributes/${attributeKey}`,
        { headers: await getAuthHeaders() }
      );

      showMessage('success', `Attribute "${attributeKey}" deleted from ${resourceName}`);
      fetchResources(); // Refresh resources
    } catch (error) {
      console.error('Error deleting attribute:', error);
      showMessage('error', error.response?.data?.message || 'Failed to delete attribute');
    } finally {
      setLoading(false);
    }
  };

  const startEditAttribute = (resourceId, key, value) => {
    setEditingAttribute({ resourceId, oldKey: key });
    setAttributeEditForm({ key, value });
  };

  const cancelEditAttribute = () => {
    setEditingAttribute(null);
    setAttributeEditForm({ key: '', value: '' });
  };

  const updateResourceAttribute = async (resourceId, resourceName) => {
    if (!attributeEditForm.key || !attributeEditForm.value) {
      showMessage('error', 'Both key and value are required');
      return;
    }

    try {
      setLoading(true);
      
      // Get current resource to access all attributes
      const currentResource = resources.find(r => r._id === resourceId);
      if (!currentResource) {
        showMessage('error', 'Resource not found');
        return;
      }
      
      // Create updated attributes map
      const updatedAttributes = { ...currentResource.attributes };
      
      console.log('🔍 Current attributes before update:', currentResource.attributes);
      console.log('🔍 Editing attribute:', editingAttribute);
      console.log('🔍 Edit form data:', attributeEditForm);
      
      // If key changed, remove old key
      if (editingAttribute.oldKey !== attributeEditForm.key) {
        delete updatedAttributes[editingAttribute.oldKey];
      }
      
      // Add/update the attribute - handle Keycloak's array format
      // Keycloak stores attributes as arrays, so we need to convert comma-separated values to array
      const valueArray = attributeEditForm.value.split(',').map(v => v.trim()).filter(v => v.length > 0);
      updatedAttributes[attributeEditForm.key] = valueArray;
      
      console.log('🔍 Updated attributes to send:', updatedAttributes);
      
      // Send PUT request with all attributes
      await axios.put(
        `${API_BASE_URL}/resources/${resourceId}/attributes`,
        updatedAttributes,
        { headers: await getAuthHeaders() }
      );

      showMessage('success', `Attribute updated in ${resourceName}`);
      setEditingAttribute(null);
      setAttributeEditForm({ key: '', value: '' });
      fetchResources(); // Refresh resources
    } catch (error) {
      console.error('Error updating attribute:', error);
      showMessage('error', error.response?.data?.message || 'Failed to update attribute');
    } finally {
      setLoading(false);
    }
  };

  // ============================== ROLE MANAGEMENT ==============================

  const createRole = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await axios.post(`${API_BASE_URL}/roles`, roleForm, {
        headers: await getAuthHeaders()
      });
      showMessage('success', response.data.message || 'Role created successfully');
      setRoleForm({
        name: '',
        description: ''
      });
      fetchRoles();
    } catch (error) {
      showMessage('error', 'Failed to create role: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const deleteRole = async (roleId, roleName) => {
    if (!window.confirm(`Are you sure you want to delete role "${roleName}"? This action cannot be undone.`)) {
      return;
    }

    try {
      setLoading(true);
      await axios.delete(`${API_BASE_URL}/roles/${roleId}`, {
        headers: await getAuthHeaders()
      });

      showMessage('success', `Role "${roleName}" deleted successfully`);
      fetchRoles();
    } catch (error) {
      showMessage('error', 'Failed to delete role: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  // Process attributes to handle Keycloak's format where same key can have multiple values
  const processResourceAttributes = (attributes) => {
    if (!attributes) return {};
    
    const processed = {};
    
    // Handle different attribute formats from Keycloak
    if (Array.isArray(attributes)) {
      // If attributes is an array of objects with key/value pairs
      attributes.forEach(attr => {
        if (attr.key && attr.value) {
          if (!processed[attr.key]) {
            processed[attr.key] = [];
          }
          processed[attr.key].push(attr.value);
        }
      });
    } else if (typeof attributes === 'object') {
      // If attributes is already a map
      Object.entries(attributes).forEach(([key, value]) => {
        if (Array.isArray(value)) {
          processed[key] = value;
        } else {
          processed[key] = [value];
        }
      });
    }
    
    return processed;
  };

  // ============================== AUTHORIZATION MANAGEMENT ==============================

  const fetchResources = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_BASE_URL}/resources`, {
        headers: await getAuthHeaders()
      });
      console.log('🔍 Raw resources data:', response.data);
      console.log('🔍 First resource attributes:', response.data[0]?.attributes);
      
      // Process attributes for each resource
      const processedResources = response.data.map(resource => ({
        ...resource,
        attributes: processResourceAttributes(resource.attributes)
      }));
      
      console.log('🔍 Processed resources:', processedResources);
      setResources(processedResources);
    } catch (error) {
      handleApiError(error, 'Failed to fetch resources');
    } finally {
      setLoading(false);
    }
  };

  const fetchPolicies = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_BASE_URL}/policies`, {
        headers: await getAuthHeaders()
      });
      setPolicies(response.data);
    } catch (error) {
      handleApiError(error, 'Failed to fetch policies');
    } finally {
      setLoading(false);
    }
  };

  const createPolicy = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await axios.post(`${API_BASE_URL}/policies`, policyForm, {
        headers: await getAuthHeaders()
      });
      showMessage('success', response.data.message || 'Policy created successfully');
      setPolicyForm({
        name: '',
        description: '',
        roleName: 'PROVIDER'
      });
      fetchPolicies();
    } catch (error) {
      showMessage('error', 'Failed to create policy: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const fetchPermissions = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_BASE_URL}/permissions`, {
        headers: await getAuthHeaders()
      });
      setPermissions(response.data);
    } catch (error) {
      handleApiError(error, 'Failed to fetch permissions');
    } finally {
      setLoading(false);
    }
  };

  const createPermission = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await axios.post(`${API_BASE_URL}/permissions`, permissionForm, {
        headers: await getAuthHeaders()
      });
      showMessage('success', response.data.message || 'Permission created successfully');
      setPermissionForm({
        name: '',
        description: '',
        resourceId: '',
        scopeIds: [],
        policyIds: []
      });
      fetchPermissions();
    } catch (error) {
      showMessage('error', 'Failed to create permission: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  // ============================== GROUP MANAGEMENT ==============================

  const fetchGroups = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_BASE_URL}/groups`, {
        headers: await getAuthHeaders()
      });
      setGroups(response.data);
    } catch (error) {
      handleApiError(error, 'Failed to fetch groups');
    } finally {
      setLoading(false);
    }
  };

  const createGroup = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await axios.post(`${API_BASE_URL}/groups`, {
        name: groupForm.name,
        parentGroupId: groupForm.parentGroupId || null
      }, {
        headers: await getAuthHeaders()
      });
      showMessage('success', response.data.message || 'Group created successfully');
      setGroupForm({ name: '', parentGroupId: '' });
      fetchGroups();
    } catch (error) {
      handleApiError(error, 'Failed to create group');
    } finally {
      setLoading(false);
    }
  };

  const deleteGroup = async (groupId, groupName) => {
    if (!window.confirm(`Are you sure you want to delete group "${groupName}"?`)) {
      return;
    }
    setLoading(true);
    try {
      await axios.delete(`${API_BASE_URL}/groups/${groupId}`, {
        headers: await getAuthHeaders()
      });
      showMessage('success', 'Group deleted successfully');
      fetchGroups();
    } catch (error) {
      handleApiError(error, 'Failed to delete group');
    } finally {
      setLoading(false);
    }
  };

  const fetchGroupMembers = async (groupId) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/groups/${groupId}/members`, {
        headers: await getAuthHeaders()
      });
      setSelectedGroupMembers(prev => ({
        ...prev,
        [groupId]: response.data
      }));
    } catch (error) {
      handleApiError(error, 'Failed to fetch group members');
    }
  };

  const addUserToGroup = async (userId, groupId) => {
    setLoading(true);
    try {
      await axios.put(`${API_BASE_URL}/users/${userId}/groups/${groupId}`, {}, {
        headers: await getAuthHeaders()
      });
      showMessage('success', 'User added to group successfully');
      fetchGroupMembers(groupId);
      fetchUsers();
    } catch (error) {
      handleApiError(error, 'Failed to add user to group');
    } finally {
      setLoading(false);
    }
  };

  const removeUserFromGroup = async (userId, groupId) => {
    setLoading(true);
    try {
      await axios.delete(`${API_BASE_URL}/users/${userId}/groups/${groupId}`, {
        headers: await getAuthHeaders()
      });
      showMessage('success', 'User removed from group successfully');
      fetchGroupMembers(groupId);
      fetchUsers();
    } catch (error) {
      handleApiError(error, 'Failed to remove user from group');
    } finally {
      setLoading(false);
    }
  };

  // Helper function to flatten groups tree for display
  const flattenGroups = (groups, level = 0) => {
    let result = [];
    groups.forEach(group => {
      result.push({ ...group, level });
      if (group.subGroups && group.subGroups.length > 0) {
        result = result.concat(flattenGroups(group.subGroups, level + 1));
      }
    });
    return result;
  };

  // Load data based on active tab
  useEffect(() => {
    switch (activeTab) {
      case 'users':
        fetchUsers();
        fetchRoles();
        break;
      case 'roles':
        fetchRoles();
        break;
      case 'policies':
        fetchPolicies();
        fetchRoles();
        break;
      case 'permissions':
        fetchPermissions();
        fetchResources();
        fetchPolicies();
        break;
      case 'resources':
        fetchResources();
        break;
      case 'groups':
        fetchGroups();
        fetchUsers();
        break;
      default:
        break;
    }
  }, [activeTab]);

  // ============================== RENDER ==============================

  // Show loading state while auth is initializing
  if (authLoading) {
    return (
      <div className="admin-container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <div className="spinner"></div>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  // Check if user has ADMIN role
  if (!user || !user.roles || !user.roles.includes('ADMIN')) {
    return (
      <div className="admin-container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <h1>🔒 Access Denied</h1>
          <p style={{ color: '#e74c3c', fontSize: '1.2rem', marginBottom: '20px' }}>
            Administrator role required to access this page.
          </p>
          <p>
            You need the <strong>ADMIN</strong> role to access the Keycloak Administration interface.
          </p>
          <div style={{ marginTop: '30px' }}>
            <p><strong>Current user:</strong> {user?.username || 'Not logged in'}</p>
            <p><strong>Current roles:</strong> {user?.roles?.join(', ') || 'None'}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-container">
      <h1 className="admin-title">🔐 Keycloak Administration</h1>
      <p className="admin-subtitle">Manage users, roles, policies, and permissions without accessing Keycloak console</p>

      {message.text && (
        <div className={`message ${message.type}`}>
          {message.text}
        </div>
      )}

      <div className="tabs">
        <button
          className={activeTab === 'users' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('users')}
        >
          👥 Users
        </button>
        <button
          className={activeTab === 'roles' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('roles')}
        >
          👤 Roles
        </button>
        <button
          className={activeTab === 'policies' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('policies')}
        >
          📋 Policies
        </button>
        <button
          className={activeTab === 'permissions' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('permissions')}
        >
          🔑 Permissions
        </button>
        <button
          className={activeTab === 'resources' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('resources')}
        >
          📦 Resources
        </button>
        <button
          className={activeTab === 'groups' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('groups')}
        >
          👥 Groups
        </button>
      </div>

      <div className="tab-content">
        {/* ============================== USERS TAB ============================== */}
        {activeTab === 'users' && (
          <div>
            <h2>Create New User</h2>
            <form onSubmit={createUser} className="admin-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Username *</label>
                  <input
                    type="text"
                    value={userForm.username}
                    onChange={(e) => setUserForm({ ...userForm, username: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Email *</label>
                  <input
                    type="email"
                    value={userForm.email}
                    onChange={(e) => setUserForm({ ...userForm, email: e.target.value })}
                    required
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>First Name</label>
                  <input
                    type="text"
                    value={userForm.firstName}
                    onChange={(e) => setUserForm({ ...userForm, firstName: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Last Name</label>
                  <input
                    type="text"
                    value={userForm.lastName}
                    onChange={(e) => setUserForm({ ...userForm, lastName: e.target.value })}
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Password *</label>
                  <input
                    type="password"
                    value={userForm.password}
                    onChange={(e) => setUserForm({ ...userForm, password: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Role</label>
                  <select
                    value={userForm.role}
                    onChange={(e) => setUserForm({ ...userForm, role: e.target.value })}
                  >
                    <option value="">Select a role</option>
                    {roles.filter(role => !role.name.startsWith('default-') && !role.name.includes('uma_') && !role.name.includes('offline_')).map((role) => (
                      <option key={role.id} value={role.name}>
                        {role.name} {role.description ? `- ${role.description}` : ''}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? 'Creating...' : 'Create User'}
              </button>
            </form>

            <h2>Existing Users</h2>
            {loading ? (
              <p>Loading users...</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Username</th>
                    <th>Email</th>
                    <th>First Name</th>
                    <th>Last Name</th>
                    <th>Enabled</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <tr key={user.id}>
                      <td>{user.username}</td>
                      <td>{user.email}</td>
                      <td>{user.firstName || '-'}</td>
                      <td>{user.lastName || '-'}</td>
                      <td>{user.enabled ? '✅ Yes' : '❌ No'}</td>
                      <td>
                        <button
                          onClick={() => deleteUser(user.id, user.username)}
                          className="btn-danger-small"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {/* ============================== ROLES TAB ============================== */}
        {activeTab === 'roles' && (
          <div>
            <h2>Create New Role</h2>
            <form onSubmit={createRole} className="admin-form">
              <div className="form-group">
                <label>Role Name *</label>
                <input
                  type="text"
                  value={roleForm.name}
                  onChange={(e) => setRoleForm({ ...roleForm, name: e.target.value })}
                  placeholder="e.g., MANAGER, SUPERVISOR"
                  required
                />
              </div>
              <div className="form-group">
                <label>Description</label>
                <input
                  type="text"
                  value={roleForm.description}
                  onChange={(e) => setRoleForm({ ...roleForm, description: e.target.value })}
                  placeholder="e.g., Manager role with full access"
                />
              </div>
              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? 'Creating...' : 'Create Role'}
              </button>
            </form>

            <h2>Existing Roles</h2>
            {loading ? (
              <p>Loading roles...</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {roles.filter(role => !role.name.startsWith('default-') && !role.name.includes('uma_') && !role.name.includes('offline_')).map((role) => (
                    <tr key={role.id}>
                      <td><strong>{role.name}</strong></td>
                      <td>{role.description || '-'}</td>
                      <td>
                        <button
                          onClick={() => deleteRole(role.id, role.name)}
                          className="btn-danger-small"
                          title="Delete role"
                        >
                          🗑️ Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {/* ============================== POLICIES TAB ============================== */}
        {activeTab === 'policies' && (
          <div>
            <h2>Create New Policy</h2>
            <form onSubmit={createPolicy} className="admin-form">
              <div className="form-group">
                <label>Policy Name *</label>
                <input
                  type="text"
                  value={policyForm.name}
                  onChange={(e) => setPolicyForm({ ...policyForm, name: e.target.value })}
                  placeholder="e.g., Recipient Read Policy"
                  required
                />
              </div>
              <div className="form-group">
                <label>Description</label>
                <input
                  type="text"
                  value={policyForm.description}
                  onChange={(e) => setPolicyForm({ ...policyForm, description: e.target.value })}
                  placeholder="e.g., Allows recipients to read timesheets"
                />
              </div>
              <div className="form-group">
                <label>Role *</label>
                <select
                  value={policyForm.roleName}
                  onChange={(e) => setPolicyForm({ ...policyForm, roleName: e.target.value })}
                >
                  {roles.filter(r => !r.name.startsWith('default-')).map((role) => (
                    <option key={role.id} value={role.name}>
                      {role.name}
                    </option>
                  ))}
                </select>
              </div>
              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? 'Creating...' : 'Create Policy'}
              </button>
            </form>

            <h2>Existing Policies</h2>
            {loading ? (
              <p>Loading policies...</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th>Logic</th>
                  </tr>
                </thead>
                <tbody>
                  {policies.map((policy) => (
                    <tr key={policy.id}>
                      <td>{policy.name}</td>
                      <td>{policy.type}</td>
                      <td>{policy.description || '-'}</td>
                      <td>{policy.logic}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {/* ============================== PERMISSIONS TAB ============================== */}
        {activeTab === 'permissions' && (
          <div>
            <h2>Create New Permission</h2>
            <form onSubmit={createPermission} className="admin-form">
              <div className="form-group">
                <label>Permission Name *</label>
                <input
                  type="text"
                  value={permissionForm.name}
                  onChange={(e) => setPermissionForm({ ...permissionForm, name: e.target.value })}
                  placeholder="e.g., Recipient Read Timesheets"
                  required
                />
              </div>
              <div className="form-group">
                <label>Description</label>
                <input
                  type="text"
                  value={permissionForm.description}
                  onChange={(e) => setPermissionForm({ ...permissionForm, description: e.target.value })}
                  placeholder="e.g., Allows recipients to read timesheet data"
                />
              </div>
              <div className="form-group">
                <label>Resource *</label>
                <select
                  value={permissionForm.resourceId}
                  onChange={(e) => setPermissionForm({ ...permissionForm, resourceId: e.target.value })}
                  required
                >
                  <option value="">Select a resource</option>
                  {resources.map((resource) => (
                    <option key={resource._id} value={resource._id}>
                      {resource.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Scopes * (comma-separated scope IDs)</label>
                <input
                  type="text"
                  value={permissionForm.scopeIds.join(',')}
                  onChange={(e) => setPermissionForm({ 
                    ...permissionForm, 
                    scopeIds: e.target.value.split(',').map(s => s.trim()).filter(s => s) 
                  })}
                  placeholder="e.g., scope-id-1, scope-id-2"
                  required
                />
              </div>
              <div className="form-group">
                <label>Policies * (comma-separated policy IDs)</label>
                <input
                  type="text"
                  value={permissionForm.policyIds.join(',')}
                  onChange={(e) => setPermissionForm({ 
                    ...permissionForm, 
                    policyIds: e.target.value.split(',').map(s => s.trim()).filter(s => s) 
                  })}
                  placeholder="e.g., policy-id-1, policy-id-2"
                  required
                />
              </div>
              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? 'Creating...' : 'Create Permission'}
              </button>
            </form>

            <h2>Existing Permissions</h2>
            {loading ? (
              <p>Loading permissions...</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th>Decision Strategy</th>
                  </tr>
                </thead>
                <tbody>
                  {permissions.map((permission) => (
                    <tr key={permission.id}>
                      <td>{permission.name}</td>
                      <td>{permission.type}</td>
                      <td>{permission.description || '-'}</td>
                      <td>{permission.decisionStrategy}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {/* ============================== RESOURCES TAB ============================== */}
        {activeTab === 'resources' && (
          <div>
            <h2>Resources & Attributes</h2>
            {loading ? (
              <p>Loading resources...</p>
            ) : (
              <div>
                {resources.map((resource) => (
                  <div key={resource._id} className="resource-card">
                    <h3>{resource.name}</h3>
                    <p><strong>Type:</strong> {resource.type || '-'}</p>
                    <p><strong>URIs:</strong> {resource.uris?.join(', ') || '-'}</p>
                    
                    {resource.scopes && resource.scopes.length > 0 && (
                      <div style={{ marginTop: '10px' }}>
                        <strong>Scopes:</strong>
                        <ul>
                          {resource.scopes.map((scope) => (
                            <li key={scope.id}>{scope.name} (ID: {scope.id})</li>
                          ))}
                        </ul>
                      </div>
                    )}

                    <div style={{ marginTop: '15px' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <strong>Attributes:</strong>
                        <button
                          onClick={() => {
                            setEditingResource(resource._id);
                            setAttributeForm({ key: '', value: '' });
                          }}
                          className="btn-primary-small"
                          style={{ fontSize: '12px', padding: '5px 10px' }}
                        >
                          ➕ Add Attribute
                        </button>
                      </div>

                      {editingResource === resource._id && (
                        <div style={{ 
                          marginTop: '10px', 
                          padding: '15px', 
                          backgroundColor: '#f0f8ff',
                          border: '1px solid #ccc',
                          borderRadius: '5px'
                        }}>
                          <h4 style={{ marginTop: 0 }}>Add New Attribute</h4>
                          <div style={{ display: 'flex', gap: '10px', alignItems: 'flex-end' }}>
                            <div style={{ flex: 1 }}>
                              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                                Attribute Key *
                              </label>
                              <input
                                type="text"
                                value={attributeForm.key}
                                onChange={(e) => setAttributeForm({ ...attributeForm, key: e.target.value })}
                                placeholder="e.g., recipient_read_fields"
                                style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px' }}
                              />
                            </div>
                            <div style={{ flex: 1 }}>
                              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                                Attribute Value *
                              </label>
                              <input
                                type="text"
                                value={attributeForm.value}
                                onChange={(e) => setAttributeForm({ ...attributeForm, value: e.target.value })}
                                placeholder="e.g., id,date,hours"
                                style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px' }}
                              />
                            </div>
                            <button
                              onClick={() => addResourceAttribute(resource._id, resource.name)}
                              className="btn-primary"
                              disabled={loading || !attributeForm.key || !attributeForm.value}
                              style={{ padding: '8px 15px' }}
                            >
                              Add
                            </button>
                            <button
                              onClick={() => {
                                setEditingResource(null);
                                setAttributeForm({ key: '', value: '' });
                              }}
                              className="btn-secondary"
                              style={{ padding: '8px 15px' }}
                            >
                              Cancel
                            </button>
                          </div>
                          <p style={{ fontSize: '12px', color: '#666', marginTop: '10px', marginBottom: 0 }}>
                            💡 Tip: For field-level authorization, use format: <code>role_scope_fields</code> (e.g., recipient_read_fields)
                          </p>
                        </div>
                      )}

                      {resource.attributes && Object.keys(resource.attributes).length > 0 ? (
                        <table style={{ 
                          width: '100%', 
                          marginTop: '10px', 
                          borderCollapse: 'collapse',
                          fontSize: '14px'
                        }}>
                          <thead>
                            <tr style={{ backgroundColor: '#f5f5f5' }}>
                              <th style={{ 
                                padding: '10px', 
                                textAlign: 'left', 
                                borderBottom: '2px solid #ddd',
                                width: '30%'
                              }}>Key</th>
                              <th style={{ 
                                padding: '10px', 
                                textAlign: 'left', 
                                borderBottom: '2px solid #ddd',
                                width: '55%'
                              }}>Value</th>
                              <th style={{ 
                                padding: '10px', 
                                textAlign: 'center', 
                                borderBottom: '2px solid #ddd',
                                width: '15%'
                              }}>Actions</th>
                            </tr>
                          </thead>
                          <tbody>
                            {Object.entries(resource.attributes).map(([key, value]) => {
                              const isEditing = editingAttribute?.resourceId === resource._id && editingAttribute?.oldKey === key;
                              
                              return (
                                <tr key={key} style={{ borderBottom: '1px solid #eee' }}>
                                  {isEditing ? (
                                    <>
                                      <td style={{ padding: '10px' }}>
                                        <input
                                          type="text"
                                          value={attributeEditForm.key}
                                          onChange={(e) => setAttributeEditForm({ ...attributeEditForm, key: e.target.value })}
                                          placeholder="Attribute key"
                                          style={{ width: '100%', padding: '5px' }}
                                        />
                                      </td>
                                      <td style={{ padding: '10px' }}>
                                        <input
                                          type="text"
                                          value={attributeEditForm.value}
                                          onChange={(e) => setAttributeEditForm({ ...attributeEditForm, value: e.target.value })}
                                          placeholder="Attribute value"
                                          style={{ width: '100%', padding: '5px' }}
                                        />
                                      </td>
                                      <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <button
                                          onClick={() => updateResourceAttribute(resource._id, resource.name)}
                                          className="btn-primary-small"
                                          style={{ 
                                            marginRight: '8px', 
                                            fontSize: '12px', 
                                            padding: '6px 12px',
                                            backgroundColor: '#28a745',
                                            color: 'white',
                                            border: 'none',
                                            borderRadius: '4px',
                                            cursor: 'pointer'
                                          }}
                                          title="Save changes"
                                        >
                                          ✅ Save
                                        </button>
                                        <button
                                          onClick={cancelEditAttribute}
                                          className="btn-secondary-small"
                                          style={{ 
                                            fontSize: '12px', 
                                            padding: '6px 12px',
                                            backgroundColor: '#6c757d',
                                            color: 'white',
                                            border: 'none',
                                            borderRadius: '4px',
                                            cursor: 'pointer'
                                          }}
                                          title="Cancel editing"
                                        >
                                          ❌ Cancel
                                        </button>
                                      </td>
                                    </>
                                  ) : (
                                    <>
                                      <td style={{ padding: '10px' }}>
                                        <strong>{key}</strong>
                                      </td>
                                      <td style={{ padding: '10px', fontFamily: 'monospace', color: '#0066cc' }}>
                                        {Array.isArray(value) ? value.join(', ') : value}
                                      </td>
                                      <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <button
                                          onClick={() => startEditAttribute(resource._id, key, Array.isArray(value) ? value.join(', ') : value)}
                                          className="btn-secondary-small"
                                          style={{ 
                                            marginRight: '8px', 
                                            fontSize: '12px', 
                                            padding: '6px 12px',
                                            backgroundColor: '#6c757d',
                                            color: 'white',
                                            border: 'none',
                                            borderRadius: '4px',
                                            cursor: 'pointer'
                                          }}
                                          title="Edit attribute"
                                        >
                                          ✏️ Edit
                                        </button>
                                        <button
                                          onClick={() => deleteResourceAttribute(resource._id, resource.name, key)}
                                          className="btn-danger-small"
                                          style={{ 
                                            fontSize: '12px', 
                                            padding: '6px 12px',
                                            backgroundColor: '#dc3545',
                                            color: 'white',
                                            border: 'none',
                                            borderRadius: '4px',
                                            cursor: 'pointer'
                                          }}
                                          title="Delete attribute"
                                        >
                                          🗑️ Delete
                                        </button>
                                      </td>
                                    </>
                                  )}
                                </tr>
                              );
                            })}
                          </tbody>
                        </table>
                      ) : (
                        <p style={{ 
                          color: '#666', 
                          fontStyle: 'italic', 
                          marginTop: '10px',
                          padding: '10px',
                          backgroundColor: '#f9f9f9',
                          borderRadius: '4px'
                        }}>
                          No attributes defined. Click "Add Attribute" to add field-level authorization rules.
                        </p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ============================== GROUPS TAB ============================== */}
        {activeTab === 'groups' && (
          <div>
            <h2>Create New Group</h2>
            <form onSubmit={createGroup} className="admin-form">
              <div className="form-group">
                <label>Group Name *</label>
                <input
                  type="text"
                  value={groupForm.name}
                  onChange={(e) => setGroupForm({ ...groupForm, name: e.target.value })}
                  placeholder="e.g., County-CTA"
                  required
                />
              </div>
              <div className="form-group">
                <label>Parent Group (optional)</label>
                <select
                  value={groupForm.parentGroupId}
                  onChange={(e) => setGroupForm({ ...groupForm, parentGroupId: e.target.value })}
                >
                  <option value="">None (Top-level group)</option>
                  {flattenGroups(groups).map((group) => (
                    <option key={group.id} value={group.id}>
                      {'  '.repeat(group.level || 0)}{group.name}
                    </option>
                  ))}
                </select>
              </div>
              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? 'Creating...' : 'Create Group'}
              </button>
            </form>

            <h2>Existing Groups</h2>
            {loading ? (
              <p>Loading groups...</p>
            ) : (
              <div>
                {groups.length === 0 ? (
                  <p>No groups found. Create a group to get started.</p>
                ) : (
                  flattenGroups(groups).map((group) => (
                    <div key={group.id} style={{
                      marginBottom: '20px',
                      padding: '15px',
                      border: '1px solid #ddd',
                      borderRadius: '5px',
                      backgroundColor: '#f9f9f9',
                      marginLeft: `${(group.level || 0) * 20}px`
                    }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <h3 style={{ margin: '0 0 10px 0' }}>
                            {group.name}
                            {group.path && <span style={{ color: '#666', fontSize: '0.9em', marginLeft: '10px' }}>({group.path})</span>}
                          </h3>
                          {group.subGroups && group.subGroups.length > 0 && (
                            <p style={{ color: '#666', fontSize: '0.9em', margin: 0 }}>
                              {group.subGroups.length} sub-group{group.subGroups.length !== 1 ? 's' : ''}
                            </p>
                          )}
                        </div>
                        <div>
                          <button
                            onClick={() => {
                              if (selectedGroupMembers[group.id]) {
                                setSelectedGroupMembers(prev => {
                                  const newState = { ...prev };
                                  delete newState[group.id];
                                  return newState;
                                });
                              } else {
                                fetchGroupMembers(group.id);
                              }
                            }}
                            className="btn-primary-small"
                            style={{ marginRight: '10px' }}
                          >
                            {selectedGroupMembers[group.id] ? '👁️ Hide Members' : '👥 View Members'}
                          </button>
                          <button
                            onClick={() => deleteGroup(group.id, group.name)}
                            className="btn-danger-small"
                          >
                            🗑️ Delete
                          </button>
                        </div>
                      </div>

                      {selectedGroupMembers[group.id] && (
                        <div style={{ marginTop: '15px', padding: '15px', backgroundColor: 'white', borderRadius: '5px' }}>
                          <h4 style={{ marginTop: 0 }}>Group Members ({selectedGroupMembers[group.id].length})</h4>
                          {selectedGroupMembers[group.id].length === 0 ? (
                            <p style={{ color: '#666', fontStyle: 'italic' }}>No members in this group.</p>
                          ) : (
                            <table className="data-table" style={{ marginTop: '10px' }}>
                              <thead>
                                <tr>
                                  <th>Username</th>
                                  <th>Email</th>
                                  <th>First Name</th>
                                  <th>Last Name</th>
                                  <th>Actions</th>
                                </tr>
                              </thead>
                              <tbody>
                                {selectedGroupMembers[group.id].map((member) => (
                                  <tr key={member.id}>
                                    <td>{member.username}</td>
                                    <td>{member.email}</td>
                                    <td>{member.firstName || '-'}</td>
                                    <td>{member.lastName || '-'}</td>
                                    <td>
                                      <button
                                        onClick={() => removeUserFromGroup(member.id, group.id)}
                                        className="btn-danger-small"
                                      >
                                        Remove
                                      </button>
                                    </td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          )}
                          <div style={{ marginTop: '15px', padding: '15px', backgroundColor: '#f0f8ff', borderRadius: '5px' }}>
                            <h4 style={{ marginTop: 0 }}>Add User to Group</h4>
                            <select
                              onChange={(e) => {
                                if (e.target.value) {
                                  addUserToGroup(e.target.value, group.id);
                                  e.target.value = '';
                                }
                              }}
                              style={{ width: '100%', padding: '8px', marginTop: '10px' }}
                            >
                              <option value="">Select a user to add...</option>
                              {users
                                .filter(user => !selectedGroupMembers[group.id]?.some(m => m.id === user.id))
                                .map((user) => (
                                  <option key={user.id} value={user.id}>
                                    {user.username} ({user.email})
                                  </option>
                                ))}
                            </select>
                          </div>
                        </div>
                      )}
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default KeycloakAdminDashboard;

