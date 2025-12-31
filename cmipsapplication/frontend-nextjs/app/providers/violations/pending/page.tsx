'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/api';

type Violation = {
  id: number;
  providerId: number;
  providerNumber: string;
  providerFirstName: string;
  providerLastName: string;
  violationNumber: number;
  violationType: string;
  violationStatus: string;
  month: number;
  year: number;
  hoursClaimed: number;
  maximumAllowed: number;
  excessHours: number;
  currentReviewStage: string;
  trainingRequired: boolean;
  trainingCompleted: boolean;
  suspensionStartDate: string;
  suspensionEndDate: string;
  createdDate: string;
};

export default function PendingViolationsPage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [violations, setViolations] = useState<Violation[]>([]);
  const [filteredViolations, setFilteredViolations] = useState<Violation[]>([]);
  const [filters, setFilters] = useState({
    reviewStage: '',
    violationNumber: '',
    month: '',
    year: new Date().getFullYear().toString()
  });
  const [stats, setStats] = useState({
    total: 0,
    countyReview: 0,
    supervisorReview: 0,
    cdssReview: 0,
    trainingPending: 0
  });

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchViolations();
  }, [user, authLoading]);

  const fetchViolations = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/providers/violations/pending');
      const data = response.data || [];
      setViolations(data);
      setFilteredViolations(data);
      calculateStats(data);
    } catch (err) {
      console.error('Error fetching violations:', err);
      setViolations([]);
      setFilteredViolations([]);
    } finally {
      setLoading(false);
    }
  };

  const calculateStats = (data: Violation[]) => {
    setStats({
      total: data.length,
      countyReview: data.filter(v => v.currentReviewStage === 'COUNTY_REVIEW').length,
      supervisorReview: data.filter(v => v.currentReviewStage === 'SUPERVISOR_REVIEW').length,
      cdssReview: data.filter(v => v.currentReviewStage === 'CDSS_REVIEW').length,
      trainingPending: data.filter(v => v.trainingRequired && !v.trainingCompleted).length
    });
  };

  const applyFilters = () => {
    let filtered = [...violations];

    if (filters.reviewStage) {
      filtered = filtered.filter(v => v.currentReviewStage === filters.reviewStage);
    }
    if (filters.violationNumber) {
      filtered = filtered.filter(v => v.violationNumber === parseInt(filters.violationNumber));
    }
    if (filters.month) {
      filtered = filtered.filter(v => v.month === parseInt(filters.month));
    }
    if (filters.year) {
      filtered = filtered.filter(v => v.year === parseInt(filters.year));
    }

    setFilteredViolations(filtered);
  };

  const clearFilters = () => {
    setFilters({
      reviewStage: '',
      violationNumber: '',
      month: '',
      year: new Date().getFullYear().toString()
    });
    setFilteredViolations(violations);
  };

  useEffect(() => {
    applyFilters();
  }, [filters]);

  const getViolationBadge = (num: number) => {
    const colors = {
      1: 'warning',
      2: 'warning',
      3: 'danger',
      4: 'danger'
    };
    const labels = {
      1: 'Warning',
      2: 'Training',
      3: '90-Day',
      4: '365-Day'
    };
    return (
      <span className={`badge bg-${colors[num as keyof typeof colors] || 'secondary'}`}>
        #{num} - {labels[num as keyof typeof labels] || 'Unknown'}
      </span>
    );
  };

  const getReviewStageBadge = (stage: string) => {
    const config: Record<string, { bg: string; label: string }> = {
      'COUNTY_REVIEW': { bg: 'primary', label: 'County Review' },
      'SUPERVISOR_REVIEW': { bg: 'info', label: 'Supervisor Review' },
      'COUNTY_DISPUTE': { bg: 'warning', label: 'County Dispute' },
      'CDSS_REVIEW': { bg: 'danger', label: 'CDSS Review' },
      'COMPLETED': { bg: 'success', label: 'Completed' }
    };
    const c = config[stage] || { bg: 'secondary', label: stage };
    return <span className={`badge bg-${c.bg}`}>{c.label}</span>;
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading Pending Violations...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 className="h3 mb-0">Pending Overtime Violations</h1>
          <p className="text-muted mb-0">Review and manage provider overtime violations</p>
        </div>
        <button
          className="btn btn-outline-secondary"
          onClick={() => router.push('/providers')}
        >
          <i className="bi bi-arrow-left me-2"></i>Back to Providers
        </button>
      </div>

      {/* Statistics Cards */}
      <div className="row mb-4">
        <div className="col-lg-2 col-md-4 col-6 mb-3">
          <div className="card text-center h-100 border-primary">
            <div className="card-body">
              <div className="fw-bold mb-1" style={{ fontSize: '2rem', color: 'var(--color-p2, #046b99)' }}>{stats.total}</div>
              <p className="text-muted small mb-0">TOTAL PENDING</p>
            </div>
          </div>
        </div>
        <div className="col-lg-2 col-md-4 col-6 mb-3">
          <div className="card text-center h-100 border-primary">
            <div className="card-body">
              <div className="fw-bold mb-1" style={{ fontSize: '2rem', color: '#0d6efd' }}>{stats.countyReview}</div>
              <p className="text-muted small mb-0">COUNTY REVIEW</p>
            </div>
          </div>
        </div>
        <div className="col-lg-2 col-md-4 col-6 mb-3">
          <div className="card text-center h-100 border-info">
            <div className="card-body">
              <div className="fw-bold mb-1" style={{ fontSize: '2rem', color: '#0dcaf0' }}>{stats.supervisorReview}</div>
              <p className="text-muted small mb-0">SUPERVISOR REVIEW</p>
            </div>
          </div>
        </div>
        <div className="col-lg-2 col-md-4 col-6 mb-3">
          <div className="card text-center h-100 border-danger">
            <div className="card-body">
              <div className="fw-bold mb-1" style={{ fontSize: '2rem', color: '#dc3545' }}>{stats.cdssReview}</div>
              <p className="text-muted small mb-0">CDSS REVIEW</p>
            </div>
          </div>
        </div>
        <div className="col-lg-2 col-md-4 col-6 mb-3">
          <div className="card text-center h-100 border-warning">
            <div className="card-body">
              <div className="fw-bold mb-1" style={{ fontSize: '2rem', color: '#ffc107' }}>{stats.trainingPending}</div>
              <p className="text-muted small mb-0">TRAINING PENDING</p>
            </div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="card mb-4">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Filters</h5>
        </div>
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-3">
              <label className="form-label">Review Stage</label>
              <select
                className="form-select"
                value={filters.reviewStage}
                onChange={(e) => setFilters({ ...filters, reviewStage: e.target.value })}
              >
                <option value="">All Stages</option>
                <option value="COUNTY_REVIEW">County Review</option>
                <option value="SUPERVISOR_REVIEW">Supervisor Review</option>
                <option value="COUNTY_DISPUTE">County Dispute</option>
                <option value="CDSS_REVIEW">CDSS Review</option>
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label">Violation Number</label>
              <select
                className="form-select"
                value={filters.violationNumber}
                onChange={(e) => setFilters({ ...filters, violationNumber: e.target.value })}
              >
                <option value="">All Violations</option>
                <option value="1">1st Violation (Warning)</option>
                <option value="2">2nd Violation (Training)</option>
                <option value="3">3rd Violation (90-Day)</option>
                <option value="4">4th Violation (365-Day)</option>
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Month</label>
              <select
                className="form-select"
                value={filters.month}
                onChange={(e) => setFilters({ ...filters, month: e.target.value })}
              >
                <option value="">All Months</option>
                {Array.from({ length: 12 }, (_, i) => (
                  <option key={i + 1} value={i + 1}>
                    {new Date(2000, i, 1).toLocaleString('default', { month: 'long' })}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Year</label>
              <select
                className="form-select"
                value={filters.year}
                onChange={(e) => setFilters({ ...filters, year: e.target.value })}
              >
                <option value="">All Years</option>
                {Array.from({ length: 5 }, (_, i) => {
                  const year = new Date().getFullYear() - i;
                  return <option key={year} value={year}>{year}</option>;
                })}
              </select>
            </div>
            <div className="col-md-2 d-flex align-items-end">
              <button className="btn btn-outline-secondary" onClick={clearFilters}>
                <i className="bi bi-x-lg me-2"></i>Clear
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Progressive Discipline Info */}
      <div className="card mb-4">
        <div className="card-header bg-info text-white">
          <h5 className="mb-0"><i className="bi bi-info-circle me-2"></i>Progressive Discipline Guide (BR PM 50-55)</h5>
        </div>
        <div className="card-body">
          <div className="row">
            <div className="col-md-3">
              <div className="card h-100 border-warning">
                <div className="card-body text-center">
                  <span className="badge bg-warning text-dark mb-2 fs-6">#1</span>
                  <h6>First Violation</h6>
                  <p className="small mb-0">Written Warning Only</p>
                </div>
              </div>
            </div>
            <div className="col-md-3">
              <div className="card h-100 border-warning">
                <div className="card-body text-center">
                  <span className="badge bg-warning text-dark mb-2 fs-6">#2</span>
                  <h6>Second Violation</h6>
                  <p className="small mb-0">Training Required (30 days)</p>
                </div>
              </div>
            </div>
            <div className="col-md-3">
              <div className="card h-100 border-danger">
                <div className="card-body text-center">
                  <span className="badge bg-danger mb-2 fs-6">#3</span>
                  <h6>Third Violation</h6>
                  <p className="small mb-0">90-Day Suspension</p>
                </div>
              </div>
            </div>
            <div className="col-md-3">
              <div className="card h-100 border-danger">
                <div className="card-body text-center">
                  <span className="badge bg-danger mb-2 fs-6">#4</span>
                  <h6>Fourth Violation</h6>
                  <p className="small mb-0">365-Day Suspension + Re-Enrollment</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Violations Table */}
      <div className="card">
        <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Pending Violations ({filteredViolations.length})</h5>
          <button className="btn btn-light btn-sm" onClick={fetchViolations}>
            <i className="bi bi-arrow-clockwise me-2"></i>Refresh
          </button>
        </div>
        <div className="card-body">
          {filteredViolations.length === 0 ? (
            <p className="text-muted text-center py-4">No pending violations found</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-striped table-hover">
                <thead>
                  <tr>
                    <th>Provider</th>
                    <th>Period</th>
                    <th>Violation #</th>
                    <th>Excess Hours</th>
                    <th>Review Stage</th>
                    <th>Training</th>
                    <th>Suspension</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredViolations.map((violation) => (
                    <tr key={violation.id}>
                      <td>
                        <a
                          href="#"
                          onClick={(e) => { e.preventDefault(); router.push(`/providers/${violation.providerId}`); }}
                          className="text-primary text-decoration-none fw-bold"
                        >
                          {violation.providerLastName}, {violation.providerFirstName}
                        </a>
                        <br />
                        <small className="text-muted">{violation.providerNumber}</small>
                      </td>
                      <td>{violation.month}/{violation.year}</td>
                      <td>{getViolationBadge(violation.violationNumber)}</td>
                      <td className="text-danger fw-bold">
                        +{violation.excessHours} hrs
                        <br />
                        <small className="text-muted">
                          ({violation.hoursClaimed}/{violation.maximumAllowed})
                        </small>
                      </td>
                      <td>{getReviewStageBadge(violation.currentReviewStage)}</td>
                      <td>
                        {violation.trainingRequired ? (
                          violation.trainingCompleted ? (
                            <span className="badge bg-success"><i className="bi bi-check"></i> Done</span>
                          ) : (
                            <span className="badge bg-danger"><i className="bi bi-exclamation"></i> Required</span>
                          )
                        ) : (
                          <span className="text-muted">N/A</span>
                        )}
                      </td>
                      <td>
                        {violation.suspensionStartDate ? (
                          <span className="badge bg-danger">
                            {violation.suspensionStartDate} - {violation.suspensionEndDate}
                          </span>
                        ) : (
                          <span className="text-muted">N/A</span>
                        )}
                      </td>
                      <td>
                        <button
                          className="btn btn-sm btn-primary"
                          onClick={() => router.push(`/providers/violations/${violation.id}`)}
                        >
                          <i className="bi bi-eye me-1"></i>Review
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
  );
}
