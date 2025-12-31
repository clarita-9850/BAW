'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/api';

type CaseEntity = {
  id: number;
  caseNumber: string;
  caseStatus: string;
  caseType: string;
  countyCode: string;
  caseOwnerId: string;
  recipientId: number;
  cin: string;
  referralDate: string;
  applicationDate: string;
  eligibilityDate: string;
  authorizedHoursMonthly: number;
  assessmentType: string;
  healthCareCertStatus: string;
  healthCareCertDueDate: string;
};

export default function CaseManagementPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [cases, setCases] = useState<CaseEntity[]>([]);
  const [filteredCases, setFilteredCases] = useState<CaseEntity[]>([]);
  const [searchParams, setSearchParams] = useState({
    caseNumber: '',
    cin: '',
    countyCode: '',
    status: '',
    caseOwnerId: ''
  });
  const [stats, setStats] = useState({
    total: 0,
    pending: 0,
    eligible: 0,
    dueForReassessment: 0
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
    fetchCases();
  }, [user, authLoading]);

  const fetchCases = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/cases');
      const caseData = response.data || [];
      setCases(caseData);
      setFilteredCases(caseData);

      // Calculate statistics
      setStats({
        total: caseData.length,
        pending: caseData.filter((c: CaseEntity) => c.caseStatus === 'PENDING').length,
        eligible: caseData.filter((c: CaseEntity) => c.caseStatus === 'ELIGIBLE').length,
        dueForReassessment: caseData.filter((c: CaseEntity) =>
          c.healthCareCertDueDate && new Date(c.healthCareCertDueDate) <= new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
        ).length
      });
    } catch (err) {
      console.error('Error fetching cases:', err);
      setCases([]);
      setFilteredCases([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (searchParams.caseNumber) params.append('caseNumber', searchParams.caseNumber);
      if (searchParams.cin) params.append('cin', searchParams.cin);
      if (searchParams.countyCode) params.append('countyCode', searchParams.countyCode);
      if (searchParams.status) params.append('status', searchParams.status);
      if (searchParams.caseOwnerId) params.append('caseOwnerId', searchParams.caseOwnerId);

      const response = await apiClient.get(`/cases/search?${params.toString()}`);
      setFilteredCases(response.data || []);
    } catch (err) {
      console.error('Error searching cases:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchParams({
      caseNumber: '',
      cin: '',
      countyCode: '',
      status: '',
      caseOwnerId: ''
    });
    setFilteredCases(cases);
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'PENDING': return 'bg-warning text-dark';
      case 'ELIGIBLE': return 'bg-success';
      case 'PRESUMPTIVE_ELIGIBLE': return 'bg-info';
      case 'ON_LEAVE': return 'bg-secondary';
      case 'DENIED': return 'bg-danger';
      case 'TERMINATED': return 'bg-dark';
      case 'APPLICATION_WITHDRAWN': return 'bg-secondary';
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
          <p className="text-muted mb-0">Loading Case Management...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="h3 mb-0">Case Management</h1>
        <button
          className="btn btn-primary"
          onClick={() => router.push('/cases/new')}
        >
          <i className="bi bi-plus-lg me-2"></i>New Case
        </button>
      </div>

      {/* Statistics Cards */}
      <div className="row mb-4">
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: 'var(--color-p2, #046b99)' }}>{stats.total}</div>
              <p className="text-muted small mb-0">TOTAL CASES</p>
            </div>
          </div>
        </div>
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#ffc107' }}>{stats.pending}</div>
              <p className="text-muted small mb-0">PENDING</p>
            </div>
          </div>
        </div>
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#198754' }}>{stats.eligible}</div>
              <p className="text-muted small mb-0">ELIGIBLE</p>
            </div>
          </div>
        </div>
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#dc3545' }}>{stats.dueForReassessment}</div>
              <p className="text-muted small mb-0">DUE FOR REASSESSMENT</p>
            </div>
          </div>
        </div>
      </div>

      {/* Search Panel */}
      <div className="card mb-4">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Search Cases</h5>
        </div>
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-3">
              <label className="form-label">Case Number</label>
              <input
                type="text"
                className="form-control"
                value={searchParams.caseNumber}
                onChange={(e) => setSearchParams({ ...searchParams, caseNumber: e.target.value })}
                placeholder="Enter case number"
              />
            </div>
            <div className="col-md-3">
              <label className="form-label">CIN</label>
              <input
                type="text"
                className="form-control"
                value={searchParams.cin}
                onChange={(e) => setSearchParams({ ...searchParams, cin: e.target.value })}
                placeholder="Enter CIN"
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
                <option value="PENDING">Pending</option>
                <option value="ELIGIBLE">Eligible</option>
                <option value="PRESUMPTIVE_ELIGIBLE">Presumptive Eligible</option>
                <option value="ON_LEAVE">On Leave</option>
                <option value="DENIED">Denied</option>
                <option value="TERMINATED">Terminated</option>
                <option value="APPLICATION_WITHDRAWN">Application Withdrawn</option>
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Case Owner</label>
              <input
                type="text"
                className="form-control"
                value={searchParams.caseOwnerId}
                onChange={(e) => setSearchParams({ ...searchParams, caseOwnerId: e.target.value })}
                placeholder="Owner ID"
              />
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

      {/* Cases Table */}
      <div className="card">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Cases ({filteredCases.length})</h5>
        </div>
        <div className="card-body">
          {filteredCases.length === 0 ? (
            <p className="text-center text-muted py-4">No cases found</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-striped table-hover">
                <thead>
                  <tr>
                    <th>Case Number</th>
                    <th>CIN</th>
                    <th>Type</th>
                    <th>Status</th>
                    <th>County</th>
                    <th>Case Owner</th>
                    <th>Authorized Hours</th>
                    <th>Assessment</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredCases.map((caseEntity) => (
                    <tr key={caseEntity.id}>
                      <td>
                        <a
                          href="#"
                          onClick={(e) => { e.preventDefault(); router.push(`/cases/${caseEntity.id}`); }}
                          className="text-primary fw-bold text-decoration-none"
                        >
                          {caseEntity.caseNumber || '-'}
                        </a>
                      </td>
                      <td>{caseEntity.cin || '-'}</td>
                      <td>{caseEntity.caseType || '-'}</td>
                      <td>
                        <span className={`badge ${getStatusBadgeClass(caseEntity.caseStatus)}`}>
                          {caseEntity.caseStatus?.replace(/_/g, ' ') || '-'}
                        </span>
                      </td>
                      <td>{caseEntity.countyCode || '-'}</td>
                      <td>{caseEntity.caseOwnerId || '-'}</td>
                      <td>{caseEntity.authorizedHoursMonthly ? `${caseEntity.authorizedHoursMonthly}/mo` : '-'}</td>
                      <td>{caseEntity.assessmentType?.replace(/_/g, ' ') || '-'}</td>
                      <td>
                        <div className="btn-group btn-group-sm">
                          <button
                            className="btn btn-outline-primary"
                            onClick={() => router.push(`/cases/${caseEntity.id}`)}
                            title="View"
                          >
                            <i className="bi bi-eye"></i>
                          </button>
                          <button
                            className="btn btn-outline-secondary"
                            onClick={() => router.push(`/cases/${caseEntity.id}/edit`)}
                            title="Edit"
                          >
                            <i className="bi bi-pencil"></i>
                          </button>
                          <button
                            className="btn btn-outline-info"
                            onClick={() => router.push(`/cases/${caseEntity.id}/notes`)}
                            title="Notes"
                          >
                            <i className="bi bi-sticky"></i>
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
