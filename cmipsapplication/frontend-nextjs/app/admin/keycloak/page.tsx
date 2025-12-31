'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api';
import FieldMaskingTab from '@/app/supervisor/dashboard/components/FieldMaskingTab';

const API_BASE_URL = '/admin/keycloak';

interface User {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  enabled: boolean;
}

interface Role {
  id: string;
  name: string;
  description?: string;
}

interface Policy {
  id: string;
  name: string;
  type: string;
  description?: string;
  logic: string;
}

interface Permission {
  id: string;
  name: string;
  type: string;
  description?: string;
  decisionStrategy: string;
}

interface Resource {
  _id: string;
  name: string;
  type?: string;
  uris?: string[];
  scopes?: Array<{ id: string; name: string }>;
  attributes?: { [key: string]: string[] | string };
}

interface Group {
  id: string;
  name: string;
  path?: string;
  subGroups?: Group[];
  level?: number;
}

interface FeatureDelegation {
  id: string;
  feature: string;
  featureName: string;
  delegatedToRole: string;
  limitations: {
    allowedRoles?: string[]; // For create_user: which roles can be created
    allowedCounties?: string[]; // For create_user: which counties users can be created in
    allowedResourceTypes?: string[]; // For create_resource: which resource types
    allowedPermissionTypes?: string[]; // For create_permission: which permission types
  };
  createdAt?: string;
}

interface AdminFeature {
  id: string;
  name: string;
  description: string;
  requiresLimitations: {
    allowedRoles?: boolean;
    allowedCounties?: boolean;
    allowedResourceTypes?: boolean;
    allowedPermissionTypes?: boolean;
  };
}

export default function AdminKeycloakPageComponent() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const [activeTab, setActiveTab] = useState<'users' | 'roles' | 'policies' | 'permissions' | 'resources' | 'groups' | 'sharing' | 'fieldMasking'>('users');
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [resources, setResources] = useState<Resource[]>([]);
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [groups, setGroups] = useState<Group[]>([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error' | ''; text: string }>({ type: '', text: '' });
  
  // Feature Delegation state
  const [featureDelegations, setFeatureDelegations] = useState<FeatureDelegation[]>([]);
  const [delegationForm, setDelegationForm] = useState({
    feature: '',
    delegatedToRole: '',
    allowedRoles: [] as string[],
    allowedCounties: [] as string[],
    allowedResourceTypes: [] as string[],
    allowedPermissionTypes: [] as string[]
  });
  
  // Admin features that can be delegated
  const adminFeatures: AdminFeature[] = [
    {
      id: 'create_user',
      name: 'Create User',
      description: 'Allow role to create new users',
      requiresLimitations: {
        allowedRoles: true,
        allowedCounties: true
      }
    },
    {
      id: 'create_role',
      name: 'Create Role',
      description: 'Allow role to create new roles',
      requiresLimitations: {}
    },
    {
      id: 'create_resource',
      name: 'Create Resource',
      description: 'Allow role to create new resources',
      requiresLimitations: {
        allowedResourceTypes: true
      }
    },
    {
      id: 'create_permission',
      name: 'Create Permission',
      description: 'Allow role to create new permissions',
      requiresLimitations: {
        allowedPermissionTypes: true
      }
    },
    {
      id: 'create_policy',
      name: 'Create Policy',
      description: 'Allow role to create new policies',
      requiresLimitations: {}
    },
    {
      id: 'create_group',
      name: 'Create Group',
      description: 'Allow role to create new groups',
      requiresLimitations: {
        allowedCounties: true
      }
    },
    {
      id: 'assign_roles',
      name: 'Assign Roles',
      description: 'Allow role to assign roles to users',
      requiresLimitations: {
        allowedRoles: true
      }
    },
    {
      id: 'manage_groups',
      name: 'Manage Groups',
      description: 'Allow role to manage group memberships',
      requiresLimitations: {
        allowedCounties: true
      }
    }
  ];

  // County list (58 counties)
  const counties = Array.from({ length: 58 }, (_, i) => `County ${String.fromCharCode(65 + i)}`);

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
    scopeIds: '',
    policyIds: ''
  });

  // State for editing resource attributes
  const [editingResource, setEditingResource] = useState<string | null>(null);
  const [attributeForm, setAttributeForm] = useState({ key: '', value: '' });
  const [editingAttribute, setEditingAttribute] = useState<{ resourceId: string; oldKey: string } | null>(null);
  const [attributeEditForm, setAttributeEditForm] = useState({ key: '', value: '' });

  // Role form state
  const [roleForm, setRoleForm] = useState({ name: '', description: '' });

  // Group form state
  const [groupForm, setGroupForm] = useState({ name: '', parentGroupId: '' });
  const [selectedGroupMembers, setSelectedGroupMembers] = useState<{ [groupId: string]: User[] }>({});

  useEffect(() => {
    if (authLoading) return;
    if (!user || (user.role !== 'ADMIN' && !user.roles?.includes('ADMIN'))) {
      window.location.href = '/login';
      return;
    }
  }, [user, authLoading]);

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
    }
  }, [activeTab]);

  const showMessage = (type: 'success' | 'error', text: string) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 5000);
  };

  const handleApiError = (error: any, defaultMessage: string) => {
    if (error.response?.status === 401) {
      showMessage('error', 'Session expired. Please login again.');
      return;
    }
    showMessage('error', defaultMessage + ': ' + (error.response?.data?.message || error.message));
  };

  // ============================== USER MANAGEMENT ==============================

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`${API_BASE_URL}/users`);
      setUsers(response.data);
    } catch (error: any) {
      handleApiError(error, 'Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  const createUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!userForm.role) {
      showMessage('error', 'Please select a role for the user');
      return;
    }
    setLoading(true);
    try {
      const response = await apiClient.post(`${API_BASE_URL}/users`, userForm);
      showMessage('success', response.data.message || 'User created successfully');
      setUserForm({ username: '', email: '', password: '', firstName: '', lastName: '', role: '' });
      fetchUsers();
    } catch (error: any) {
      showMessage('error', 'Failed to create user: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const deleteUser = async (userId: string, username: string) => {
    if (!window.confirm(`Are you sure you want to delete user: ${username}?`)) return;
    setLoading(true);
    try {
      await apiClient.delete(`${API_BASE_URL}/users/${userId}`);
      showMessage('success', 'User deleted successfully');
      fetchUsers();
    } catch (error: any) {
      showMessage('error', 'Failed to delete user: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  // ============================== ROLE MANAGEMENT ==============================

  const fetchRoles = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`${API_BASE_URL}/roles`);
      setRoles(response.data);
    } catch (error: any) {
      handleApiError(error, 'Failed to fetch roles');
    } finally {
      setLoading(false);
    }
  };

  const createRole = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await apiClient.post(`${API_BASE_URL}/roles`, roleForm);
      showMessage('success', response.data.message || 'Role created successfully');
      setRoleForm({ name: '', description: '' });
      fetchRoles();
    } catch (error: any) {
      showMessage('error', 'Failed to create role: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const deleteRole = async (roleId: string, roleName: string) => {
    if (!window.confirm(`Are you sure you want to delete role "${roleName}"? This action cannot be undone.`)) return;
    setLoading(true);
    try {
      await apiClient.delete(`${API_BASE_URL}/roles/${roleId}`);
      showMessage('success', `Role "${roleName}" deleted successfully`);
      fetchRoles();
    } catch (error: any) {
      showMessage('error', 'Failed to delete role: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  // ============================== RESOURCE ATTRIBUTE MANAGEMENT ==============================

  const processResourceAttributes = (attributes: any): { [key: string]: string[] } => {
    if (!attributes) return {};
    const processed: { [key: string]: string[] } = {};
    if (Array.isArray(attributes)) {
      attributes.forEach((attr: any) => {
        if (attr.key && attr.value) {
          if (!processed[attr.key]) processed[attr.key] = [];
          processed[attr.key].push(attr.value);
        }
      });
    } else if (typeof attributes === 'object') {
      Object.entries(attributes).forEach(([key, value]) => {
        if (Array.isArray(value)) {
          processed[key] = value as string[];
        } else {
          processed[key] = [value as string];
        }
      });
    }
    return processed;
  };

  const addResourceAttribute = async (resourceId: string, resourceName: string) => {
    if (!attributeForm.key || !attributeForm.value) {
      showMessage('error', 'Both key and value are required');
      return;
    }
    try {
      setLoading(true);
      const currentResource = resources.find(r => r._id === resourceId);
      if (!currentResource) {
        showMessage('error', 'Resource not found');
        return;
      }
      const updatedAttributes = { ...processResourceAttributes(currentResource.attributes) };
      const valueArray = attributeForm.value.split(',').map(v => v.trim()).filter(v => v.length > 0);
      updatedAttributes[attributeForm.key] = valueArray;
      await apiClient.put(`${API_BASE_URL}/resources/${resourceId}/attributes`, updatedAttributes);
      showMessage('success', `Attribute added to ${resourceName}`);
      setAttributeForm({ key: '', value: '' });
      setEditingResource(null);
      fetchResources();
    } catch (error: any) {
      showMessage('error', error.response?.data?.message || 'Failed to add attribute');
    } finally {
      setLoading(false);
    }
  };

  const deleteResourceAttribute = async (resourceId: string, resourceName: string, attributeKey: string) => {
    if (!window.confirm(`Are you sure you want to delete attribute "${attributeKey}" from ${resourceName}?`)) return;
    try {
      setLoading(true);
      const currentResource = resources.find(r => r._id === resourceId);
      if (!currentResource) {
        showMessage('error', 'Resource not found');
        return;
      }
      const updatedAttributes = { ...processResourceAttributes(currentResource.attributes) };
      delete updatedAttributes[attributeKey];
      await apiClient.put(`${API_BASE_URL}/resources/${resourceId}/attributes`, updatedAttributes);
      showMessage('success', `Attribute "${attributeKey}" deleted from ${resourceName}`);
      fetchResources();
    } catch (error: any) {
      showMessage('error', error.response?.data?.message || 'Failed to delete attribute');
    } finally {
      setLoading(false);
    }
  };

  const startEditAttribute = (resourceId: string, key: string, value: string | string[]) => {
    setEditingAttribute({ resourceId, oldKey: key });
    setAttributeEditForm({ key, value: Array.isArray(value) ? value.join(', ') : value });
  };

  const cancelEditAttribute = () => {
    setEditingAttribute(null);
    setAttributeEditForm({ key: '', value: '' });
  };

  const updateResourceAttribute = async (resourceId: string, resourceName: string) => {
    if (!attributeEditForm.key || !attributeEditForm.value) {
      showMessage('error', 'Both key and value are required');
      return;
    }
    try {
      setLoading(true);
      const currentResource = resources.find(r => r._id === resourceId);
      if (!currentResource || !editingAttribute) {
        showMessage('error', 'Resource not found');
        return;
      }
      const updatedAttributes = { ...processResourceAttributes(currentResource.attributes) };
      if (editingAttribute.oldKey !== attributeEditForm.key) {
        delete updatedAttributes[editingAttribute.oldKey];
      }
      const valueArray = attributeEditForm.value.split(',').map(v => v.trim()).filter(v => v.length > 0);
      updatedAttributes[attributeEditForm.key] = valueArray;
      await apiClient.put(`${API_BASE_URL}/resources/${resourceId}/attributes`, updatedAttributes);
      showMessage('success', `Attribute updated in ${resourceName}`);
      setEditingAttribute(null);
      setAttributeEditForm({ key: '', value: '' });
      fetchResources();
    } catch (error: any) {
      showMessage('error', error.response?.data?.message || 'Failed to update attribute');
    } finally {
      setLoading(false);
    }
  };

  // ============================== AUTHORIZATION MANAGEMENT ==============================

  const fetchResources = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`${API_BASE_URL}/resources`);
      const processedResources = response.data.map((resource: Resource) => ({
        ...resource,
        attributes: processResourceAttributes(resource.attributes)
      }));
      setResources(processedResources);
    } catch (error: any) {
      handleApiError(error, 'Failed to fetch resources');
    } finally {
      setLoading(false);
    }
  };

  const fetchPolicies = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`${API_BASE_URL}/policies`);
      setPolicies(response.data);
    } catch (error: any) {
      handleApiError(error, 'Failed to fetch policies');
    } finally {
      setLoading(false);
    }
  };

  const createPolicy = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await apiClient.post(`${API_BASE_URL}/policies`, policyForm);
      showMessage('success', response.data.message || 'Policy created successfully');
      setPolicyForm({ name: '', description: '', roleName: 'PROVIDER' });
      fetchPolicies();
    } catch (error: any) {
      showMessage('error', 'Failed to create policy: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const fetchPermissions = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`${API_BASE_URL}/permissions`);
      setPermissions(response.data);
    } catch (error: any) {
      handleApiError(error, 'Failed to fetch permissions');
    } finally {
      setLoading(false);
    }
  };

  const createPermission = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const requestData = {
        ...permissionForm,
        scopeIds: permissionForm.scopeIds.split(',').map(s => s.trim()).filter(s => s),
        policyIds: permissionForm.policyIds.split(',').map(s => s.trim()).filter(s => s)
      };
      const response = await apiClient.post(`${API_BASE_URL}/permissions`, requestData);
      showMessage('success', response.data.message || 'Permission created successfully');
      setPermissionForm({ name: '', description: '', resourceId: '', scopeIds: '', policyIds: '' });
      fetchPermissions();
    } catch (error: any) {
      showMessage('error', 'Failed to create permission: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  // ============================== GROUP MANAGEMENT ==============================

  const fetchGroups = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(`${API_BASE_URL}/groups`);
      setGroups(response.data);
    } catch (error: any) {
      handleApiError(error, 'Failed to fetch groups');
    } finally {
      setLoading(false);
    }
  };

  const createGroup = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await apiClient.post(`${API_BASE_URL}/groups`, {
        name: groupForm.name,
        parentGroupId: groupForm.parentGroupId || null
      });
      showMessage('success', response.data.message || 'Group created successfully');
      setGroupForm({ name: '', parentGroupId: '' });
      fetchGroups();
    } catch (error: any) {
      handleApiError(error, 'Failed to create group');
    } finally {
      setLoading(false);
    }
  };

  const deleteGroup = async (groupId: string, groupName: string) => {
    if (!window.confirm(`Are you sure you want to delete group "${groupName}"?`)) return;
    setLoading(true);
    try {
      await apiClient.delete(`${API_BASE_URL}/groups/${groupId}`);
      showMessage('success', 'Group deleted successfully');
      fetchGroups();
    } catch (error: any) {
      handleApiError(error, 'Failed to delete group');
    } finally {
      setLoading(false);
    }
  };

  const fetchGroupMembers = async (groupId: string) => {
    try {
      const response = await apiClient.get(`${API_BASE_URL}/groups/${groupId}/members`);
      setSelectedGroupMembers(prev => ({ ...prev, [groupId]: response.data }));
    } catch (error: any) {
      handleApiError(error, 'Failed to fetch group members');
    }
  };

  const addUserToGroup = async (userId: string, groupId: string) => {
    setLoading(true);
    try {
      await apiClient.put(`${API_BASE_URL}/users/${userId}/groups/${groupId}`, {});
      showMessage('success', 'User added to group successfully');
      fetchGroupMembers(groupId);
      fetchUsers();
    } catch (error: any) {
      handleApiError(error, 'Failed to add user to group');
    } finally {
      setLoading(false);
    }
  };

  const removeUserFromGroup = async (userId: string, groupId: string) => {
    setLoading(true);
    try {
      await apiClient.delete(`${API_BASE_URL}/users/${userId}/groups/${groupId}`);
      showMessage('success', 'User removed from group successfully');
      fetchGroupMembers(groupId);
      fetchUsers();
    } catch (error: any) {
      handleApiError(error, 'Failed to remove user from group');
    } finally {
      setLoading(false);
    }
  };

  const flattenGroups = (groups: Group[], level = 0): Group[] => {
    let result: Group[] = [];
    groups.forEach(group => {
      result.push({ ...group, level });
      if (group.subGroups && group.subGroups.length > 0) {
        result = result.concat(flattenGroups(group.subGroups, level + 1));
      }
    });
    return result;
  };

  // ============================== FEATURE DELEGATION MANAGEMENT ==============================

  const fetchFeatureDelegations = async () => {
    setLoading(true);
    try {
      // TODO: Replace with actual API call when backend is ready
      // For now, using localStorage as a temporary store
      const stored = localStorage.getItem('featureDelegations');
      if (stored) {
        setFeatureDelegations(JSON.parse(stored));
      } else {
        setFeatureDelegations([]);
      }
    } catch (error: any) {
      handleApiError(error, 'Failed to fetch feature delegations');
    } finally {
      setLoading(false);
    }
  };

  const createFeatureDelegation = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!delegationForm.feature || !delegationForm.delegatedToRole) {
      showMessage('error', 'Please select a feature and role to delegate to');
      return;
    }

    const selectedFeature = adminFeatures.find(f => f.id === delegationForm.feature);
    if (!selectedFeature) {
      showMessage('error', 'Invalid feature selected');
      return;
    }

    // Validate limitations based on feature requirements
    if (selectedFeature.requiresLimitations.allowedRoles && delegationForm.allowedRoles.length === 0) {
      showMessage('error', 'Please select at least one allowed role');
      return;
    }
    if (selectedFeature.requiresLimitations.allowedCounties && delegationForm.allowedCounties.length === 0) {
      showMessage('error', 'Please select at least one allowed county');
      return;
    }

    setLoading(true);
    try {
      const newDelegation: FeatureDelegation = {
        id: `delegation-${Date.now()}`,
        feature: delegationForm.feature,
        featureName: selectedFeature.name,
        delegatedToRole: delegationForm.delegatedToRole,
        limitations: {
          allowedRoles: delegationForm.allowedRoles.length > 0 ? delegationForm.allowedRoles : undefined,
          allowedCounties: delegationForm.allowedCounties.length > 0 ? delegationForm.allowedCounties : undefined,
          allowedResourceTypes: delegationForm.allowedResourceTypes.length > 0 ? delegationForm.allowedResourceTypes : undefined,
          allowedPermissionTypes: delegationForm.allowedPermissionTypes.length > 0 ? delegationForm.allowedPermissionTypes : undefined
        },
        createdAt: new Date().toISOString()
      };

      const updated = [...featureDelegations, newDelegation];
      setFeatureDelegations(updated);
      localStorage.setItem('featureDelegations', JSON.stringify(updated));
      
      showMessage('success', `Feature "${selectedFeature.name}" delegated to ${delegationForm.delegatedToRole} successfully`);
      
      // Reset form
      setDelegationForm({
        feature: '',
        delegatedToRole: '',
        allowedRoles: [],
        allowedCounties: [],
        allowedResourceTypes: [],
        allowedPermissionTypes: []
      });
    } catch (error: any) {
      handleApiError(error, 'Failed to create feature delegation');
    } finally {
      setLoading(false);
    }
  };

  const deleteFeatureDelegation = async (delegationId: string) => {
    if (!window.confirm('Are you sure you want to remove this feature delegation?')) return;
    
    setLoading(true);
    try {
      const updated = featureDelegations.filter(d => d.id !== delegationId);
      setFeatureDelegations(updated);
      localStorage.setItem('featureDelegations', JSON.stringify(updated));
      showMessage('success', 'Feature delegation removed successfully');
    } catch (error: any) {
      handleApiError(error, 'Failed to remove feature delegation');
    } finally {
      setLoading(false);
    }
  };

  const toggleArrayItem = (array: string[], item: string): string[] => {
    if (array.includes(item)) {
      return array.filter(i => i !== item);
    } else {
      return [...array, item];
    }
  };

  // ============================== RENDER ==============================

  if (!mounted || authLoading) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading...</p>
        </div>
      </div>
    );
  }

  if (!user || (user.role !== 'ADMIN' && !user.roles?.includes('ADMIN'))) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <h1 className="card-title mb-3">🔒 Access Denied</h1>
          <p className="alert alert-danger">Administrator role required to access this page.</p>
          <p className="text-muted">You need the <strong>ADMIN</strong> role to access the Keycloak Administration interface.</p>
        </div>
      </div>
    );
  }

  const filteredRoles = roles.filter(role => 
    !role.name.startsWith('default-') && 
    !role.name.includes('uma_') && 
    !role.name.includes('offline_')
  );

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
      {/* Header */}
      <header role="banner" style={{ backgroundColor: 'white', borderBottom: '1px solid #e5e7eb', position: 'sticky', top: 0, zIndex: 100 }}>
        <div style={{ padding: '1rem 0', borderBottom: '1px solid #e5e7eb' }}>
          <div className="container">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-p2, #046b99)', margin: 0 }}>
                  CMIPSII Case Management Information Payroll System II
                </h1>
                <p className="text-muted mb-0" style={{ fontSize: '0.875rem', margin: '0.25rem 0 0 0' }}>
                  Admin Dashboard - Keycloak Administration
                </p>
              </div>
              <div className="d-flex align-items-center gap-3">
                <span className="text-muted">
                  Welcome, <strong>{user?.username || 'Admin'}</strong>
                </span>
                <button 
                  type="button" 
                  onClick={() => window.location.href = '/login'}
                  className="btn btn-danger"
                >
                  <span className="ca-gov-icon-logout" aria-hidden="true"></span>
                  Logout
                </button>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container" style={{ padding: '1.5rem 0' }}>
        <div className="mb-4">
          <h2 className="card-title mb-1">🔐 Keycloak Administration</h2>
          <p className="text-muted">Manage users, roles, policies, and permissions without accessing Keycloak console</p>
        </div>

        {message.text && (
          <div className={`alert mb-4 ${message.type === 'success' ? 'alert-success' : 'alert-danger'}`}>
            {message.text}
          </div>
        )}

        {/* Tabs */}
        <div className="mb-4">
          <ul className="nav nav-tabs" role="tablist">
            {(['users', 'roles', 'policies', 'permissions', 'resources', 'groups', 'sharing', 'fieldMasking'] as const).map((tab) => (
              <li key={tab} className="nav-item" role="presentation">
                <button
                  className={`nav-link ${activeTab === tab ? 'active' : ''}`}
                  onClick={() => {
                    setActiveTab(tab);
                    if (tab === 'sharing') {
                      fetchFeatureDelegations();
                    }
                  }}
                  type="button"
                >
                  {tab === 'users' && '👥 Users'}
                  {tab === 'roles' && '👤 Roles'}
                  {tab === 'policies' && '📋 Policies'}
                  {tab === 'permissions' && '🔑 Permissions'}
                  {tab === 'resources' && '📦 Resources'}
                  {tab === 'groups' && '👥 Groups'}
                  {tab === 'sharing' && '🔗 Sharing'}
                  {tab === 'fieldMasking' && '🔒 Field Masking'}
                </button>
              </li>
            ))}
          </ul>
        </div>

        {/* Tab Content */}
        <div className="card">
          <div className="card-body">
          {/* USERS TAB */}
          {activeTab === 'users' && (
            <div>
              <h2 className="card-title mb-4">Create New User</h2>
              <form onSubmit={createUser} className="bg-gray-50 p-6 rounded-lg mb-6">
                <div className="row mb-3">
                  <div className="col-md-6 mb-3">
                    <label className="form-label fw-semibold">Username *</label>
                    <input
                      type="text"
                      value={userForm.username}
                      onChange={(e) => setUserForm({ ...userForm, username: e.target.value })}
                      required
                      className="form-control"
                    />
                  </div>
                  <div className="col-md-6 mb-3">
                    <label className="form-label fw-semibold">Email *</label>
                    <input
                      type="email"
                      value={userForm.email}
                      onChange={(e) => setUserForm({ ...userForm, email: e.target.value })}
                      required
                      className="form-control"
                    />
                  </div>
                </div>
                <div className="row mb-3">
                  <div className="col-md-6 mb-3">
                    <label className="form-label fw-semibold">First Name</label>
                    <input
                      type="text"
                      value={userForm.firstName}
                      onChange={(e) => setUserForm({ ...userForm, firstName: e.target.value })}
                      className="form-control"
                    />
                  </div>
                  <div className="col-md-6 mb-3">
                    <label className="form-label fw-semibold">Last Name</label>
                    <input
                      type="text"
                      value={userForm.lastName}
                      onChange={(e) => setUserForm({ ...userForm, lastName: e.target.value })}
                      className="form-control"
                    />
                  </div>
                </div>
                <div className="row mb-3">
                  <div className="col-md-6 mb-3">
                    <label className="form-label fw-semibold">Password *</label>
                    <input
                      type="password"
                      value={userForm.password}
                      onChange={(e) => setUserForm({ ...userForm, password: e.target.value })}
                      required
                      className="form-control"
                    />
                  </div>
                  <div className="col-md-6 mb-3">
                    <label className="form-label fw-semibold">Role</label>
                    <select
                      value={userForm.role}
                      onChange={(e) => setUserForm({ ...userForm, role: e.target.value })}
                      className="form-control"
                    >
                      <option value="">Select a role</option>
                      {filteredRoles.map((role) => (
                        <option key={role.id} value={role.name}>
                          {role.name} {role.description ? `- ${role.description}` : ''}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
                <button
                  type="submit"
                  disabled={loading}
                  className="btn btn-primary"
                >
                  {loading ? 'Creating...' : 'Create User'}
                </button>
              </form>

              <h2 className="card-title mb-4">Existing Users</h2>
              {loading ? (
                <p>Loading users...</p>
              ) : (
                <div className="overflow-x-auto">
                  <div className="table-responsive">
                    <table className="table table-striped table-hover">
                      <thead>
                        <tr style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
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
                                className="btn btn-danger btn-sm"
                              >
                                Delete
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* ROLES TAB */}
          {activeTab === 'roles' && (
            <div>
              <h2 className="card-title mb-4">Create New Role</h2>
              <form onSubmit={createRole} className="card mb-4">
                <div className="card-body">
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Role Name *</label>
                    <input
                      type="text"
                      value={roleForm.name}
                      onChange={(e) => setRoleForm({ ...roleForm, name: e.target.value })}
                      placeholder="e.g., MANAGER, SUPERVISOR"
                      required
                      className="form-control"
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Description</label>
                    <input
                      type="text"
                      value={roleForm.description}
                      onChange={(e) => setRoleForm({ ...roleForm, description: e.target.value })}
                      placeholder="e.g., Manager role with full access"
                      className="form-control"
                    />
                  </div>
                <button
                  type="submit"
                  disabled={loading}
                  className="btn btn-primary"
                >
                  {loading ? 'Creating...' : 'Create Role'}
                </button>
                </div>
              </form>

              <h2 className="card-title mb-4">Existing Roles</h2>
              {loading ? (
                <p className="text-muted">Loading roles...</p>
              ) : (
                <div className="table-responsive">
                  <table className="table table-striped table-hover">
                    <thead>
                      <tr style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                        <th>Name</th>
                        <th>Description</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredRoles.map((role) => (
                        <tr key={role.id}>
                          <td className="fw-semibold">{role.name}</td>
                          <td>{role.description || '-'}</td>
                          <td>
                            <button
                              onClick={() => deleteRole(role.id, role.name)}
                              className="btn btn-danger btn-sm"
                            >
                              🗑️ Delete
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    </table>
                  </div>
                )}
            </div>
          )}

          {/* POLICIES TAB */}
          {activeTab === 'policies' && (
            <div>
              <h2 className="card-title mb-4">Create New Policy</h2>
              <form onSubmit={createPolicy} className="card mb-4">
                <div className="card-body">
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Policy Name *</label>
                    <input
                      type="text"
                      value={policyForm.name}
                      onChange={(e) => setPolicyForm({ ...policyForm, name: e.target.value })}
                      placeholder="e.g., Recipient Read Policy"
                      required
                      className="form-control"
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Description</label>
                    <input
                      type="text"
                      value={policyForm.description}
                      onChange={(e) => setPolicyForm({ ...policyForm, description: e.target.value })}
                      placeholder="e.g., Allows recipients to read timesheets"
                      className="form-control"
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Role *</label>
                    <select
                      value={policyForm.roleName}
                      onChange={(e) => setPolicyForm({ ...policyForm, roleName: e.target.value })}
                      className="form-select"
                    >
                      {filteredRoles.map((role) => (
                        <option key={role.id} value={role.name}>
                          {role.name}
                        </option>
                      ))}
                    </select>
                  </div>
                <button
                  type="submit"
                  disabled={loading}
                  className="btn btn-primary"
                >
                  {loading ? 'Creating...' : 'Create Policy'}
                </button>
                </div>
              </form>

              <h2 className="card-title mb-4">Existing Policies</h2>
              {loading ? (
                <p className="text-muted">Loading policies...</p>
              ) : (
                <div className="table-responsive">
                  <table className="table table-striped table-hover">
                    <thead>
                      <tr style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                        <th>Name</th>
                        <th>Type</th>
                        <th>Description</th>
                        <th>Logic</th>
                      </tr>
                    </thead>
                    <tbody>
                      {policies.map((policy) => (
                        <tr key={policy.id} className="hover:bg-gray-50">
                          <td className="px-4 py-3 border text-gray-900">{policy.name}</td>
                          <td className="px-4 py-3 border text-gray-900">{policy.type}</td>
                          <td className="px-4 py-3 border text-gray-900">{policy.description || '-'}</td>
                          <td className="px-4 py-3 border text-gray-900">{policy.logic}</td>
                        </tr>
                      ))}
                    </tbody>
                    </table>
                  </div>
                )}
            </div>
          )}

          {/* PERMISSIONS TAB */}
          {activeTab === 'permissions' && (
            <div>
              <h2 className="card-title mb-4">Create New Permission</h2>
              <form onSubmit={createPermission} className="card mb-4">
                <div className="card-body">
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Permission Name *</label>
                    <input
                      type="text"
                      value={permissionForm.name}
                      onChange={(e) => setPermissionForm({ ...permissionForm, name: e.target.value })}
                      placeholder="e.g., Recipient Read Timesheets"
                      required
                      className="form-control"
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Description</label>
                    <input
                      type="text"
                      value={permissionForm.description}
                      onChange={(e) => setPermissionForm({ ...permissionForm, description: e.target.value })}
                      placeholder="e.g., Allows recipients to read timesheet data"
                      className="form-control"
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Resource *</label>
                    <select
                      value={permissionForm.resourceId}
                      onChange={(e) => setPermissionForm({ ...permissionForm, resourceId: e.target.value })}
                      required
                      className="form-select"
                    >
                      <option value="">Select a resource</option>
                      {resources.map((resource) => (
                        <option key={resource._id} value={resource._id}>
                          {resource.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Scopes * (comma-separated scope IDs)</label>
                    <input
                      type="text"
                      value={permissionForm.scopeIds}
                      onChange={(e) => setPermissionForm({ ...permissionForm, scopeIds: e.target.value })}
                      placeholder="e.g., scope-id-1, scope-id-2"
                      required
                      className="form-control"
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Policies * (comma-separated policy IDs)</label>
                    <input
                      type="text"
                      value={permissionForm.policyIds}
                      onChange={(e) => setPermissionForm({ ...permissionForm, policyIds: e.target.value })}
                      placeholder="e.g., policy-id-1, policy-id-2"
                      required
                      className="form-control"
                    />
                  </div>
                <button
                  type="submit"
                  disabled={loading}
                  className="btn btn-primary"
                >
                  {loading ? 'Creating...' : 'Create Permission'}
                </button>
                </div>
              </form>

              <h2 className="card-title mb-4">Existing Permissions</h2>
              {loading ? (
                <p className="text-muted">Loading permissions...</p>
              ) : (
                <div className="table-responsive">
                  <table className="table table-striped table-hover">
                    <thead>
                      <tr style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
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
                </div>
              )}
            </div>
          )}

          {/* RESOURCES TAB */}
          {activeTab === 'resources' && (
            <div>
              <h2 className="card-title mb-4">Resources & Attributes</h2>
              {loading ? (
                <p>Loading resources...</p>
              ) : (
                <div>
                  {resources.map((resource) => (
                    <div key={resource._id} className="bg-gray-50 p-6 rounded-lg mb-4 border-l-4" style={{ borderLeftColor: 'var(--color-p2, #046b99)' }}>
                      <h3 className="card-title mb-2">{resource.name}</h3>
                      <p className="mb-1 text-gray-900"><strong>Type:</strong> {resource.type || '-'}</p>
                      <p className="mb-4 text-gray-900"><strong>URIs:</strong> {resource.uris?.join(', ') || '-'}</p>
                      
                      {resource.scopes && resource.scopes.length > 0 && (
                        <div className="mb-4 text-gray-900">
                          <strong>Scopes:</strong>
                          <ul className="list-disc list-inside ml-4">
                            {resource.scopes.map((scope) => (
                              <li key={scope.id}>{scope.name} (ID: {scope.id})</li>
                            ))}
                          </ul>
                        </div>
                      )}

                      <div className="mt-4">
                        <div className="flex justify-between items-center mb-2">
                          <strong>Attributes:</strong>
                          <button
                            onClick={() => {
                              setEditingResource(resource._id);
                              setAttributeForm({ key: '', value: '' });
                            }}
                            className="px-3 py-1 text-white rounded text-sm btn btn-primary btn-sm"
                          >
                            ➕ Add Attribute
                          </button>
                        </div>

                        {editingResource === resource._id && (
                          <div className="mt-4 p-4 bg-blue-50 border border-gray-300 rounded-lg">
                            <h4 className="font-bold mb-3">Add New Attribute</h4>
                            <div className="grid grid-cols-2 gap-4 mb-2">
                              <div>
                                <label className="block font-semibold mb-1">Attribute Key *</label>
                                <input
                                  type="text"
                                  value={attributeForm.key}
                                  onChange={(e) => setAttributeForm({ ...attributeForm, key: e.target.value })}
                                  placeholder="e.g., recipient_read_fields"
                                  className="w-full px-3 py-2 border border-gray-300 rounded text-gray-900 bg-white"
                                />
                              </div>
                              <div>
                                <label className="block font-semibold mb-1">Attribute Value *</label>
                                <input
                                  type="text"
                                  value={attributeForm.value}
                                  onChange={(e) => setAttributeForm({ ...attributeForm, value: e.target.value })}
                                  placeholder="e.g., id,date,hours"
                                  className="w-full px-3 py-2 border border-gray-300 rounded text-gray-900 bg-white"
                                />
                              </div>
                            </div>
                            <div className="flex gap-2">
                              <button
                                onClick={() => addResourceAttribute(resource._id, resource.name)}
                                disabled={loading || !attributeForm.key || !attributeForm.value}
                                className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:bg-gray-400"
                              >
                                Add
                              </button>
                              <button
                                onClick={() => {
                                  setEditingResource(null);
                                  setAttributeForm({ key: '', value: '' });
                                }}
                                className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
                              >
                                Cancel
                              </button>
                            </div>
                            <p className="text-xs text-gray-600 mt-2">
                              💡 Tip: For field-level authorization, use format: <code>role_scope_fields</code> (e.g., recipient_read_fields)
                            </p>
                          </div>
                        )}

                        {resource.attributes && Object.keys(resource.attributes).length > 0 ? (
                          <table className="w-full mt-4 border-collapse">
                            <thead>
                              <tr className="bg-gray-200">
                                <th className="px-4 py-2 text-left border w-1/3">Key</th>
                                <th className="px-4 py-2 text-left border w-1/2">Value</th>
                                <th className="px-4 py-2 text-center border w-1/6">Actions</th>
                              </tr>
                            </thead>
                            <tbody>
                              {Object.entries(resource.attributes).map(([key, value]) => {
                                const isEditing = editingAttribute?.resourceId === resource._id && editingAttribute?.oldKey === key;
                                const displayValue = Array.isArray(value) ? value.join(', ') : value;
                                
                                return (
                                  <tr key={key} className="border-b">
                                    {isEditing ? (
                                      <>
                                        <td className="px-4 py-2 border">
                                          <input
                                            type="text"
                                            value={attributeEditForm.key}
                                            onChange={(e) => setAttributeEditForm({ ...attributeEditForm, key: e.target.value })}
                                            className="w-full px-2 py-1 border rounded text-gray-900 bg-white"
                                          />
                                        </td>
                                        <td className="px-4 py-2 border">
                                          <input
                                            type="text"
                                            value={attributeEditForm.value}
                                            onChange={(e) => setAttributeEditForm({ ...attributeEditForm, value: e.target.value })}
                                            className="w-full px-2 py-1 border rounded text-gray-900 bg-white"
                                          />
                                        </td>
                                        <td className="px-4 py-2 border text-center">
                                          <button
                                            onClick={() => updateResourceAttribute(resource._id, resource.name)}
                                            className="px-2 py-1 bg-green-600 text-white rounded text-xs mr-2 hover:bg-green-700"
                                          >
                                            ✅ Save
                                          </button>
                                          <button
                                            onClick={cancelEditAttribute}
                                            className="px-2 py-1 bg-gray-600 text-white rounded text-xs hover:bg-gray-700"
                                          >
                                            ❌ Cancel
                                          </button>
                                        </td>
                                      </>
                                    ) : (
                                      <>
                                        <td className="px-4 py-2 border font-semibold text-gray-900">{key}</td>
                                        <td className="px-4 py-2 border font-mono text-gray-900">{displayValue}</td>
                                        <td className="px-4 py-2 border text-center">
                                          <button
                                            onClick={() => startEditAttribute(resource._id, key, displayValue)}
                                            className="px-2 py-1 bg-gray-600 text-white rounded text-xs mr-2 hover:bg-gray-700"
                                          >
                                            ✏️ Edit
                                          </button>
                                          <button
                                            onClick={() => deleteResourceAttribute(resource._id, resource.name, key)}
                                            className="px-2 py-1 bg-red-600 text-white rounded text-xs hover:bg-red-700"
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
                          <p className="text-gray-600 italic mt-4 p-4 bg-gray-100 rounded">
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

          {/* GROUPS TAB */}
          {activeTab === 'groups' && (
            <div>
              <h2 className="card-title mb-4">Create New Group</h2>
              <form onSubmit={createGroup} className="bg-gray-50 p-6 rounded-lg mb-6">
                <div className="mb-4">
                  <label className="block font-semibold text-gray-700 mb-2">Group Name *</label>
                  <input
                    type="text"
                    value={groupForm.name}
                    onChange={(e) => setGroupForm({ ...groupForm, name: e.target.value })}
                    placeholder="e.g., County-CTA"
                    required
                      className="form-select"
                  />
                </div>
                <div className="mb-4">
                  <label className="block font-semibold text-gray-700 mb-2">Parent Group (optional)</label>
                  <select
                    value={groupForm.parentGroupId}
                    onChange={(e) => setGroupForm({ ...groupForm, parentGroupId: e.target.value })}
                      className="form-select"
                  >
                    <option value="">None (Top-level group)</option>
                    {flattenGroups(groups).map((group) => (
                      <option key={group.id} value={group.id}>
                        {'  '.repeat(group.level || 0)}{group.name}
                      </option>
                    ))}
                  </select>
                </div>
                <button
                  type="submit"
                  disabled={loading}
                  className="btn btn-primary"
                >
                  {loading ? 'Creating...' : 'Create Group'}
                </button>
              </form>

              <h2 className="card-title mb-4">Existing Groups</h2>
              {loading ? (
                <p>Loading groups...</p>
              ) : (
                <div>
                  {groups.length === 0 ? (
                    <p>No groups found. Create a group to get started.</p>
                  ) : (
                    flattenGroups(groups).map((group) => (
                      <div
                        key={group.id}
                        className="bg-gray-50 p-4 rounded-lg mb-4 border-l-4" 
                        style={{ 
                          borderLeftColor: 'var(--color-p2, #046b99)',
                          marginLeft: `${(group.level || 0) * 20}px` 
                        }}
                      >
                        <div className="flex justify-between items-center">
                          <div>
                            <h3 className="font-bold text-lg">
                              {group.name}
                              {group.path && <span className="text-gray-600 text-sm ml-2">({group.path})</span>}
                            </h3>
                            {group.subGroups && group.subGroups.length > 0 && (
                              <p className="text-gray-600 text-sm">
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
                              className="px-3 py-1 text-white rounded text-sm mr-2 btn btn-primary btn-sm"
                            >
                              {selectedGroupMembers[group.id] ? '👁️ Hide Members' : '👥 View Members'}
                            </button>
                            <button
                              onClick={() => deleteGroup(group.id, group.name)}
                              className="btn btn-danger btn-sm"
                            >
                              🗑️ Delete
                            </button>
                          </div>
                        </div>

                        {selectedGroupMembers[group.id] && (
                          <div className="mt-4 p-4 bg-white rounded-lg">
                            <h4 className="font-bold mb-2">Group Members ({selectedGroupMembers[group.id].length})</h4>
                            {selectedGroupMembers[group.id].length === 0 ? (
                              <p className="text-gray-600 italic">No members in this group.</p>
                            ) : (
                              <>
                                <div className="table-responsive mt-2">
                                  <table className="table table-striped table-hover">
                                    <thead>
                                      <tr style={{ backgroundColor: 'var(--gray-200, #e5e7eb)' }}>
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
                                              className="btn btn-danger btn-sm"
                                            >
                                              Remove
                                            </button>
                                          </td>
                                        </tr>
                                      ))}
                                    </tbody>
                                  </table>
                                </div>
                                <div className="card mt-3">
                                  <div className="card-body">
                                    <h5 className="card-title mb-3">Add User to Group</h5>
                                    <select
                                      onChange={(e) => {
                                        if (e.target.value) {
                                          addUserToGroup(e.target.value, group.id);
                                          e.target.value = '';
                                        }
                                      }}
                                      className="form-select"
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
                              </>
                            )}
                          </div>
                        )}
                      </div>
                    ))
                  )}
                </div>
              )}
            </div>
          )}

          {/* Feature Delegation Tab */}
          {activeTab === 'sharing' && (
            <div className="space-y-6">
              <div className="bg-white p-6 rounded-lg shadow-md">
                <h2 className="card-title mb-4">🔗 Feature Delegation</h2>
                <p className="text-gray-600 mb-6">Delegate admin features to other roles with specific limitations</p>

                {/* Feature Delegation Form */}
                <form onSubmit={createFeatureDelegation} className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Admin Feature to Delegate *
                      </label>
                      <select
                        value={delegationForm.feature}
                        onChange={(e) => {
                          const feature = adminFeatures.find(f => f.id === e.target.value);
                          setDelegationForm({
                            ...delegationForm,
                            feature: e.target.value,
                            allowedRoles: [],
                            allowedCounties: [],
                            allowedResourceTypes: [],
                            allowedPermissionTypes: []
                          });
                        }}
                        className="form-control"
                        required
                      >
                        <option value="">Select a feature...</option>
                        {adminFeatures.map((feature) => (
                          <option key={feature.id} value={feature.id} className="text-gray-900">
                            {feature.name} - {feature.description}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Delegate To Role *
                      </label>
                      <select
                        value={delegationForm.delegatedToRole}
                        onChange={(e) => setDelegationForm({ ...delegationForm, delegatedToRole: e.target.value })}
                        className="form-control"
                        required
                      >
                        <option value="">Select a role...</option>
                        {filteredRoles.filter(r => r.name !== 'ADMIN').map((role) => (
                          <option key={role.id} value={role.name} className="text-gray-900">
                            {role.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  {/* Limitations based on selected feature */}
                  {delegationForm.feature && (() => {
                    const selectedFeature = adminFeatures.find(f => f.id === delegationForm.feature);
                    if (!selectedFeature) return null;

                    return (
                      <div className="space-y-4 p-4 bg-blue-50 rounded-lg border border-blue-200">
                        <h3 className="font-bold text-lg mb-3" style={{ color: 'var(--color-p2, #046b99)' }}>⚙️ Limitations</h3>
                        
                        {selectedFeature.requiresLimitations.allowedRoles && (
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Allowed Roles (which roles can be created/assigned) *
                            </label>
                            <div className="grid grid-cols-2 md:grid-cols-4 gap-2 max-h-40 overflow-y-auto border border-gray-300 rounded-md p-3 bg-white">
                              {filteredRoles.filter(r => r.name !== 'ADMIN').map((role) => (
                                <label key={role.id} className="flex items-center space-x-2 cursor-pointer">
                                  <input
                                    type="checkbox"
                                    checked={delegationForm.allowedRoles.includes(role.name)}
                                    onChange={() => setDelegationForm({
                                      ...delegationForm,
                                      allowedRoles: toggleArrayItem(delegationForm.allowedRoles, role.name)
                                    })}
                                    className="rounded border-gray-300" 
                                    style={{ 
                                      color: 'var(--color-p2, #046b99)', 
                                      ['--bs-focus-ring-color' as any]: 'var(--color-p2, #046b99)' 
                                    }}
                                  />
                                  <span className="text-sm text-gray-900 font-medium">{role.name}</span>
                                </label>
                              ))}
                            </div>
                          </div>
                        )}

                        {selectedFeature.requiresLimitations.allowedCounties && (
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Allowed Counties (which counties users/groups can be created in) *
                            </label>
                            <div className="grid grid-cols-3 md:grid-cols-6 gap-2 max-h-60 overflow-y-auto border border-gray-300 rounded-md p-3 bg-white">
                              {counties.map((county) => (
                                <label key={county} className="flex items-center space-x-2 cursor-pointer">
                                  <input
                                    type="checkbox"
                                    checked={delegationForm.allowedCounties.includes(county)}
                                    onChange={() => setDelegationForm({
                                      ...delegationForm,
                                      allowedCounties: toggleArrayItem(delegationForm.allowedCounties, county)
                                    })}
                                    className="rounded border-gray-300" 
                                    style={{ 
                                      color: 'var(--color-p2, #046b99)', 
                                      ['--bs-focus-ring-color' as any]: 'var(--color-p2, #046b99)' 
                                    }}
                                  />
                                  <span className="text-sm text-gray-900 font-medium">{county}</span>
                                </label>
                              ))}
                            </div>
                          </div>
                        )}

                        {selectedFeature.requiresLimitations.allowedResourceTypes && (
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Allowed Resource Types
                            </label>
                            <div className="flex flex-wrap gap-2">
                              {['Timesheet', 'EVV', 'Provider-Recipient', 'Custom'].map((type) => (
                                <label key={type} className="flex items-center space-x-2 cursor-pointer px-3 py-1 bg-white border border-gray-300 rounded">
                                  <input
                                    type="checkbox"
                                    checked={delegationForm.allowedResourceTypes.includes(type)}
                                    onChange={() => setDelegationForm({
                                      ...delegationForm,
                                      allowedResourceTypes: toggleArrayItem(delegationForm.allowedResourceTypes, type)
                                    })}
                                    className="rounded border-gray-300" 
                                    style={{ 
                                      color: 'var(--color-p2, #046b99)', 
                                      ['--bs-focus-ring-color' as any]: 'var(--color-p2, #046b99)' 
                                    }}
                                  />
                                  <span className="text-sm text-gray-900 font-medium">{type}</span>
                                </label>
                              ))}
                            </div>
                          </div>
                        )}

                        {selectedFeature.requiresLimitations.allowedPermissionTypes && (
                          <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                              Allowed Permission Types
                            </label>
                            <div className="flex flex-wrap gap-2">
                              {['scope', 'resource'].map((type) => (
                                <label key={type} className="flex items-center space-x-2 cursor-pointer px-3 py-1 bg-white border border-gray-300 rounded">
                                  <input
                                    type="checkbox"
                                    checked={delegationForm.allowedPermissionTypes.includes(type)}
                                    onChange={() => setDelegationForm({
                                      ...delegationForm,
                                      allowedPermissionTypes: toggleArrayItem(delegationForm.allowedPermissionTypes, type)
                                    })}
                                    className="rounded border-gray-300" 
                                    style={{ 
                                      color: 'var(--color-p2, #046b99)', 
                                      ['--bs-focus-ring-color' as any]: 'var(--color-p2, #046b99)' 
                                    }}
                                  />
                                  <span className="text-sm text-gray-900 font-medium">{type}</span>
                                </label>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    );
                  })()}

                  <button
                    type="submit"
                    disabled={loading}
                    className="btn btn-primary"
                  >
                    {loading ? 'Delegating...' : 'Delegate Feature'}
                  </button>
                </form>
              </div>

              {/* Feature Delegations List */}
              <div className="card">
                <div className="card-body">
                  <h2 className="card-title mb-3">📋 Current Feature Delegations</h2>
                  <p className="text-muted mb-4">View and manage existing feature delegations</p>

                  {loading ? (
                    <div className="text-center py-5">
                      <div className="spinner-border text-primary mb-3" role="status">
                        <span className="visually-hidden">Loading...</span>
                      </div>
                      <p className="text-muted">Loading delegations...</p>
                    </div>
                  ) : featureDelegations.length === 0 ? (
                    <div className="text-center py-5 text-muted">
                      <p className="h5">No feature delegations found.</p>
                      <p className="small mt-2">Create a delegation using the form above.</p>
                    </div>
                ) : (
                  <div className="table-responsive">
                    <table className="table table-striped table-hover">
                      <thead>
                        <tr style={{ backgroundColor: 'var(--gray-200, #e5e7eb)' }}>
                          <th className="fw-semibold">Feature</th>
                          <th className="fw-semibold">Delegated To</th>
                          <th className="fw-semibold">Limitations</th>
                          <th className="fw-semibold">Created</th>
                          <th className="fw-semibold">Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {featureDelegations.map((delegation) => (
                          <tr key={delegation.id}>
                            <td>
                              <div className="fw-semibold">{delegation.featureName}</div>
                              <div className="small text-muted">{delegation.feature}</div>
                            </td>
                            <td>
                              <span className="badge bg-primary">
                                {delegation.delegatedToRole}
                              </span>
                            </td>
                            <td>
                              <div className="small">
                                {delegation.limitations.allowedRoles && (
                                  <div>
                                    <span className="fw-medium">Roles:</span>{' '}
                                    {delegation.limitations.allowedRoles.join(', ')}
                                  </div>
                                )}
                                {delegation.limitations.allowedCounties && (
                                  <div>
                                    <span className="fw-medium">Counties:</span>{' '}
                                    {delegation.limitations.allowedCounties.length} selected
                                  </div>
                                )}
                                {delegation.limitations.allowedResourceTypes && (
                                  <div>
                                    <span className="fw-medium">Resource Types:</span>{' '}
                                    {delegation.limitations.allowedResourceTypes.join(', ')}
                                  </div>
                                )}
                                {delegation.limitations.allowedPermissionTypes && (
                                  <div>
                                    <span className="fw-medium">Permission Types:</span>{' '}
                                    {delegation.limitations.allowedPermissionTypes.join(', ')}
                                  </div>
                                )}
                                {!delegation.limitations.allowedRoles && 
                                 !delegation.limitations.allowedCounties && 
                                 !delegation.limitations.allowedResourceTypes && 
                                 !delegation.limitations.allowedPermissionTypes && (
                                  <span className="text-muted fst-italic">No limitations</span>
                                )}
                              </div>
                            </td>
                            <td className="small">
                              {delegation.createdAt ? new Date(delegation.createdAt).toLocaleDateString() : '-'}
                            </td>
                            <td>
                              <button
                                onClick={() => deleteFeatureDelegation(delegation.id)}
                                className="btn btn-danger btn-sm"
                              >
                                🗑️ Remove
                              </button>
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
          )}

          {/* FIELD MASKING TAB */}
          {activeTab === 'fieldMasking' && (
            <div>
              <FieldMaskingTab />
            </div>
          )}
          </div>
        </div>
      </main>
    </div>
  );
}
