'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/api';

type EvidenceRecord = {
  id: number;
  recipientId: number;
  recipientName: string;
  cin: string;
  caseId: number;
  evidenceType: string;
  evidenceDate: string;
  serviceMonth: number;
  serviceYear: number;
  description: string;
  hoursApproved: number;
  socAmount: number;
  socStatus: string;
  waiverProgram: string;
  assessmentType: string;
  actionTaken: string;
  effectiveDate: string;
  terminationDate?: string;
  createdDate: string;
  createdBy: string;
  countyCode: string;
};

export default function EvidenceHistoryPage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const router = useRouter();

  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<EvidenceRecord[]>([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [filters, setFilters] = useState({
    cin: '',
    recipientName: '',
    caseId: '',
    evidenceType: '',
    serviceMonth: '',
    serviceYear: '',
    startDate: '',
    endDate: '',
    countyCode: ''
  });

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
  }, [user, authLoading]);

  const handleSearch = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (filters.cin) params.append('cin', filters.cin);
      if (filters.recipientName) params.append('recipientName', filters.recipientName);
      if (filters.caseId) params.append('caseId', filters.caseId);
      if (filters.evidenceType) params.append('evidenceType', filters.evidenceType);
      if (filters.serviceMonth) params.append('serviceMonth', filters.serviceMonth);
      if (filters.serviceYear) params.append('serviceYear', filters.serviceYear);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      if (filters.countyCode) params.append('countyCode', filters.countyCode);

      const response = await apiClient.get(`/evidence-history?${params.toString()}`);
      setRecords(response.data || []);
      setHasSearched(true);
    } catch (err) {
      console.error('Error searching evidence history:', err);
      alert('Failed to search evidence history');
    } finally {
      setLoading(false);
    }
  };

  const handleClear = () => {
    setFilters({
      cin: '',
      recipientName: '',
      caseId: '',
      evidenceType: '',
      serviceMonth: '',
      serviceYear: '',
      startDate: '',
      endDate: '',
      countyCode: ''
    });
    setRecords([]);
    setHasSearched(false);
  };

  const getEvidenceTypeBadge = (type: string) => {
    switch (type) {
      case 'INITIAL_ELIGIBILITY': return 'bg-primary';
      case 'CHANGE_ELIGIBILITY': return 'bg-info';
      case 'REASSESSMENT': return 'bg-success';
      case 'TERMINATION': return 'bg-danger';
      case 'SOC_MET': return 'bg-warning text-dark';
      case 'HEALTH_CERT': return 'bg-secondary';
      default: return 'bg-secondary';
    }
  };

  if (!mounted || authLoading || !user) {
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

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="mb-4">
        <h1 className="h3 mb-0">Evidence History Search</h1>
        <p className="text-muted mb-0">Search for historical eligibility and evidence records (BR SE 25)</p>
      </div>

      {/* Business Rules Info */}
      <div className="card mb-4">
        <div className="card-header bg-info text-white">
          <h5 className="mb-0"><i className="bi bi-info-circle me-2"></i>Evidence History Search Rules (BR SE 25)</h5>
        </div>
        <div className="card-body">
          <div className="row">
            <div className="col-md-4">
              <h6>Search Capabilities</h6>
              <ul className="small mb-0">
                <li>Search by CIN or recipient name</li>
                <li>Search by case ID</li>
                <li>Filter by service month/year</li>
                <li>Filter by date range</li>
              </ul>
            </div>
            <div className="col-md-4">
              <h6>Evidence Types</h6>
              <ul className="small mb-0">
                <li>Initial Eligibility determinations</li>
                <li>Change assessments</li>
                <li>Reassessments</li>
                <li>SOC met/unmet events</li>
              </ul>
            </div>
            <div className="col-md-4">
              <h6>Historical Data</h6>
              <ul className="small mb-0">
                <li>All eligibility decisions stored</li>
                <li>Audit trail maintained</li>
                <li>Read-only access to history</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Search Filters */}
      <div className="card mb-4">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Search Filters</h5>
        </div>
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-3">
              <label className="form-label">CIN</label>
              <input
                type="text"
                className="form-control"
                value={filters.cin}
                onChange={(e) => setFilters({ ...filters, cin: e.target.value })}
                placeholder="Enter CIN..."
              />
            </div>
            <div className="col-md-3">
              <label className="form-label">Recipient Name</label>
              <input
                type="text"
                className="form-control"
                value={filters.recipientName}
                onChange={(e) => setFilters({ ...filters, recipientName: e.target.value })}
                placeholder="Last name, First name..."
              />
            </div>
            <div className="col-md-3">
              <label className="form-label">Case ID</label>
              <input
                type="text"
                className="form-control"
                value={filters.caseId}
                onChange={(e) => setFilters({ ...filters, caseId: e.target.value })}
                placeholder="Enter case ID..."
              />
            </div>
            <div className="col-md-3">
              <label className="form-label">Evidence Type</label>
              <select
                className="form-select"
                value={filters.evidenceType}
                onChange={(e) => setFilters({ ...filters, evidenceType: e.target.value })}
              >
                <option value="">All Types</option>
                <option value="INITIAL_ELIGIBILITY">Initial Eligibility</option>
                <option value="CHANGE_ELIGIBILITY">Change Assessment</option>
                <option value="REASSESSMENT">Reassessment</option>
                <option value="TERMINATION">Termination</option>
                <option value="SOC_MET">SOC Met</option>
                <option value="HEALTH_CERT">Health Certification</option>
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Service Month</label>
              <select
                className="form-select"
                value={filters.serviceMonth}
                onChange={(e) => setFilters({ ...filters, serviceMonth: e.target.value })}
              >
                <option value="">All Months</option>
                {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map(month => (
                  <option key={month} value={month}>{new Date(2000, month - 1).toLocaleString('default', { month: 'long' })}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Service Year</label>
              <select
                className="form-select"
                value={filters.serviceYear}
                onChange={(e) => setFilters({ ...filters, serviceYear: e.target.value })}
              >
                <option value="">All Years</option>
                {[2025, 2024, 2023, 2022, 2021, 2020].map(year => (
                  <option key={year} value={year}>{year}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Start Date</label>
              <input
                type="date"
                className="form-control"
                value={filters.startDate}
                onChange={(e) => setFilters({ ...filters, startDate: e.target.value })}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">End Date</label>
              <input
                type="date"
                className="form-control"
                value={filters.endDate}
                onChange={(e) => setFilters({ ...filters, endDate: e.target.value })}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">County</label>
              <input
                type="text"
                className="form-control"
                value={filters.countyCode}
                onChange={(e) => setFilters({ ...filters, countyCode: e.target.value })}
                placeholder="County code..."
              />
            </div>
            <div className="col-md-2 d-flex align-items-end gap-2">
              <button className="btn btn-primary" onClick={handleSearch} disabled={loading}>
                {loading ? (
                  <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                ) : (
                  <i className="bi bi-search me-2"></i>
                )}
                Search
              </button>
              <button className="btn btn-outline-secondary" onClick={handleClear}>
                Clear
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Results */}
      <div className="card">
        <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Search Results</h5>
          <span className="badge bg-light text-dark">{records.length} records found</span>
        </div>
        <div className="card-body">
          {!hasSearched ? (
            <p className="text-muted text-center py-5">Enter search criteria and click Search to find evidence history records.</p>
          ) : records.length === 0 ? (
            <p className="text-muted text-center py-5">No records found matching your search criteria.</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-striped table-hover">
                <thead>
                  <tr>
                    <th>Recipient</th>
                    <th>CIN</th>
                    <th>Case ID</th>
                    <th>Evidence Type</th>
                    <th>Service Period</th>
                    <th>Hours</th>
                    <th>SOC</th>
                    <th>Action</th>
                    <th>Effective Date</th>
                    <th>Created</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {records.map((record) => (
                    <tr key={record.id}>
                      <td>
                        <a href="#" onClick={(e) => { e.preventDefault(); router.push(`/recipients/${record.recipientId}`); }}>
                          {record.recipientName}
                        </a>
                      </td>
                      <td>{record.cin}</td>
                      <td>
                        <a href="#" onClick={(e) => { e.preventDefault(); router.push(`/cases/${record.caseId}`); }}>
                          #{record.caseId}
                        </a>
                      </td>
                      <td>
                        <span className={`badge ${getEvidenceTypeBadge(record.evidenceType)}`}>
                          {record.evidenceType?.replace(/_/g, ' ')}
                        </span>
                      </td>
                      <td>{record.serviceMonth}/{record.serviceYear}</td>
                      <td className="fw-bold">{record.hoursApproved}</td>
                      <td>
                        ${record.socAmount}
                        <br />
                        <small className={record.socStatus === 'MET' ? 'text-success' : 'text-warning'}>
                          {record.socStatus}
                        </small>
                      </td>
                      <td>{record.actionTaken}</td>
                      <td>{record.effectiveDate}</td>
                      <td>
                        {record.createdDate}
                        <br />
                        <small className="text-muted">{record.createdBy}</small>
                      </td>
                      <td>
                        <button
                          className="btn btn-sm btn-outline-primary"
                          onClick={() => router.push(`/evidence-history/${record.id}`)}
                        >
                          View Details
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
