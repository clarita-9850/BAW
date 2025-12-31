'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/api';

type Provider = {
  id: number;
  providerNumber: string;
  firstName: string;
  lastName: string;
  middleName: string;
  ssn: string;
  ssnVerificationStatus: string;
  dateOfBirth: string;
  providerStatus: string;
  dojCountyCode: string;
  eligible: string;
  ineligibleReason: string;
  soc426Signed: boolean;
  orientationCompleted: boolean;
  enrollmentDate: string;
  terminationDate: string;
  sickLeaveEligible: boolean;
  sickLeaveHoursAccrued: number;
};

export default function ProviderManagementPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [providers, setProviders] = useState<Provider[]>([]);
  const [filteredProviders, setFilteredProviders] = useState<Provider[]>([]);
  const [searchParams, setSearchParams] = useState({
    providerNumber: '',
    lastName: '',
    firstName: '',
    countyCode: '',
    status: ''
  });
  const [stats, setStats] = useState({
    total: 0,
    active: 0,
    onLeave: 0,
    pendingEnrollment: 0
  });

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchProviders();
  }, [user, authLoading]);

  const fetchProviders = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/providers');
      const providerData = response.data || [];
      setProviders(providerData);
      setFilteredProviders(providerData);

      // Calculate statistics
      setStats({
        total: providerData.length,
        active: providerData.filter((p: Provider) => p.providerStatus === 'ACTIVE').length,
        onLeave: providerData.filter((p: Provider) => p.providerStatus === 'ON_LEAVE').length,
        pendingEnrollment: providerData.filter((p: Provider) => !p.enrollmentDate).length
      });
    } catch (err) {
      console.error('Error fetching providers:', err);
      setProviders([]);
      setFilteredProviders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (searchParams.providerNumber) params.append('providerNumber', searchParams.providerNumber);
      if (searchParams.lastName) params.append('lastName', searchParams.lastName);
      if (searchParams.firstName) params.append('firstName', searchParams.firstName);
      if (searchParams.countyCode) params.append('countyCode', searchParams.countyCode);

      const response = await apiClient.get(`/providers/search?${params.toString()}`);
      let results = response.data || [];

      // Filter by status if specified
      if (searchParams.status) {
        results = results.filter((p: Provider) => p.providerStatus === searchParams.status);
      }

      setFilteredProviders(results);
    } catch (err) {
      console.error('Error searching providers:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchParams({
      providerNumber: '',
      lastName: '',
      firstName: '',
      countyCode: '',
      status: ''
    });
    setFilteredProviders(providers);
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'bg-success';
      case 'ON_LEAVE': return 'bg-warning text-dark';
      case 'TERMINATED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  };

  const getEligibleBadgeClass = (eligible: string) => {
    switch (eligible) {
      case 'YES': return 'bg-success';
      case 'NO': return 'bg-danger';
      case 'PENDING': return 'bg-warning text-dark';
      default: return 'bg-secondary';
    }
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading Provider Management...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="h3 mb-0">Provider Management</h1>
        <button
          className="btn btn-primary"
          onClick={() => router.push('/providers/new')}
        >
          <i className="bi bi-plus-lg me-2"></i>New Provider
        </button>
      </div>

      {/* Statistics Cards */}
      <div className="row mb-4">
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: 'var(--color-p2, #046b99)' }}>{stats.total}</div>
              <p className="text-muted small mb-0">TOTAL PROVIDERS</p>
            </div>
          </div>
        </div>
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#198754' }}>{stats.active}</div>
              <p className="text-muted small mb-0">ACTIVE</p>
            </div>
          </div>
        </div>
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#ffc107' }}>{stats.onLeave}</div>
              <p className="text-muted small mb-0">ON LEAVE</p>
            </div>
          </div>
        </div>
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#0dcaf0' }}>{stats.pendingEnrollment}</div>
              <p className="text-muted small mb-0">PENDING ENROLLMENT</p>
            </div>
          </div>
        </div>
      </div>

      {/* Search Panel */}
      <div className="card mb-4">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Search Providers</h5>
        </div>
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-2">
              <label className="form-label">Provider Number</label>
              <input
                type="text"
                className="form-control"
                value={searchParams.providerNumber}
                onChange={(e) => setSearchParams({ ...searchParams, providerNumber: e.target.value })}
                placeholder="Provider #"
              />
            </div>
            <div className="col-md-3">
              <label className="form-label">Last Name</label>
              <input
                type="text"
                className="form-control"
                value={searchParams.lastName}
                onChange={(e) => setSearchParams({ ...searchParams, lastName: e.target.value })}
                placeholder="Enter last name"
              />
            </div>
            <div className="col-md-3">
              <label className="form-label">First Name</label>
              <input
                type="text"
                className="form-control"
                value={searchParams.firstName}
                onChange={(e) => setSearchParams({ ...searchParams, firstName: e.target.value })}
                placeholder="Enter first name"
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">County Code</label>
              <input
                type="text"
                className="form-control"
                value={searchParams.countyCode}
                onChange={(e) => setSearchParams({ ...searchParams, countyCode: e.target.value })}
                placeholder="e.g., 19"
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">Status</label>
              <select
                className="form-select"
                value={searchParams.status}
                onChange={(e) => setSearchParams({ ...searchParams, status: e.target.value })}
              >
                <option value="">All Statuses</option>
                <option value="ACTIVE">Active</option>
                <option value="ON_LEAVE">On Leave</option>
                <option value="TERMINATED">Terminated</option>
              </select>
            </div>
          </div>
          <div className="mt-3">
            <button className="btn btn-primary me-2" onClick={handleSearch}>
              <i className="bi bi-search me-2"></i>Search
            </button>
            <button className="btn btn-outline-secondary" onClick={handleClearSearch}>
              <i className="bi bi-x-lg me-2"></i>Clear
            </button>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card mb-4">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Quick Actions</h5>
        </div>
        <div className="card-body">
          <div className="btn-group">
            <button className="btn btn-outline-primary" onClick={() => router.push('/providers/violations/pending')}>
              <i className="bi bi-exclamation-triangle me-2"></i>Pending Violation Reviews
            </button>
            <button className="btn btn-outline-primary" onClick={() => router.push('/providers/pending-reinstatement')}>
              <i className="bi bi-arrow-repeat me-2"></i>Eligible for Reinstatement
            </button>
            <button className="btn btn-outline-primary" onClick={() => router.push('/providers/eligible')}>
              <i className="bi bi-check-circle me-2"></i>Eligible Providers
            </button>
          </div>
        </div>
      </div>

      {/* Providers Table */}
      <div className="card">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Providers ({filteredProviders.length})</h5>
        </div>
        <div className="card-body">
          {filteredProviders.length === 0 ? (
            <p className="text-center text-muted py-4">No providers found</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-striped table-hover">
                <thead>
                  <tr>
                    <th>Provider #</th>
                    <th>Name</th>
                    <th>Status</th>
                    <th>Eligible</th>
                    <th>County</th>
                    <th>Enrollment Date</th>
                    <th>Orientation</th>
                    <th>Sick Leave</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredProviders.map((provider) => (
                    <tr key={provider.id}>
                      <td>
                        <a
                          href="#"
                          onClick={(e) => { e.preventDefault(); router.push(`/providers/${provider.id}`); }}
                          className="text-primary fw-bold text-decoration-none"
                        >
                          {provider.providerNumber || '-'}
                        </a>
                      </td>
                      <td>{provider.firstName} {provider.lastName}</td>
                      <td>
                        <span className={`badge ${getStatusBadgeClass(provider.providerStatus)}`}>
                          {provider.providerStatus?.replace(/_/g, ' ') || '-'}
                        </span>
                      </td>
                      <td>
                        <span className={`badge ${getEligibleBadgeClass(provider.eligible)}`}>
                          {provider.eligible || '-'}
                        </span>
                      </td>
                      <td>{provider.dojCountyCode || '-'}</td>
                      <td>{provider.enrollmentDate || '-'}</td>
                      <td>
                        {provider.orientationCompleted ? (
                          <i className="bi bi-check-circle text-success"></i>
                        ) : (
                          <i className="bi bi-x-circle text-danger"></i>
                        )}
                      </td>
                      <td>
                        {provider.sickLeaveEligible ? (
                          <span className="text-success">{provider.sickLeaveHoursAccrued || 0} hrs</span>
                        ) : (
                          <span className="text-muted">N/A</span>
                        )}
                      </td>
                      <td>
                        <div className="btn-group btn-group-sm">
                          <button
                            className="btn btn-outline-primary"
                            onClick={() => router.push(`/providers/${provider.id}`)}
                            title="View"
                          >
                            <i className="bi bi-eye"></i>
                          </button>
                          <button
                            className="btn btn-outline-secondary"
                            onClick={() => router.push(`/providers/${provider.id}/edit`)}
                            title="Edit"
                          >
                            <i className="bi bi-pencil"></i>
                          </button>
                          <button
                            className="btn btn-outline-info"
                            onClick={() => router.push(`/providers/${provider.id}/cori`)}
                            title="CORI"
                          >
                            <i className="bi bi-shield-check"></i>
                          </button>
                          <button
                            className="btn btn-outline-warning"
                            onClick={() => router.push(`/providers/${provider.id}/violations`)}
                            title="Violations"
                          >
                            <i className="bi bi-exclamation-triangle"></i>
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
  );
}
