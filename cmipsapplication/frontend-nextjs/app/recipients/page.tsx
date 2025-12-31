'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/api';

type Recipient = {
  id: number;
  personType: string;
  firstName: string;
  lastName: string;
  middleName: string;
  ssn: string;
  ssnVerificationStatus: string;
  dateOfBirth: string;
  gender: string;
  cin: string;
  countyCode: string;
  residenceCity: string;
  residenceState: string;
  primaryLanguage: string;
  interpreterNeeded: boolean;
  espRegistered: boolean;
  referralCloseDate: string;
  referralCloseReason: string;
};

export default function RecipientManagementPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [filteredRecipients, setFilteredRecipients] = useState<Recipient[]>([]);
  const [searchParams, setSearchParams] = useState({
    lastName: '',
    firstName: '',
    ssn: '',
    cin: '',
    countyCode: '',
    personType: ''
  });
  const [stats, setStats] = useState({
    total: 0,
    openReferrals: 0,
    applicants: 0,
    recipients: 0
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
    fetchRecipients();
  }, [user, authLoading]);

  const fetchRecipients = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/recipients');
      const data = response.data || [];
      setRecipients(data);
      setFilteredRecipients(data);

      // Calculate statistics
      setStats({
        total: data.length,
        openReferrals: data.filter((r: Recipient) => r.personType === 'OPEN_REFERRAL').length,
        applicants: data.filter((r: Recipient) => r.personType === 'APPLICANT').length,
        recipients: data.filter((r: Recipient) => r.personType === 'RECIPIENT').length
      });
    } catch (err) {
      console.error('Error fetching recipients:', err);
      setRecipients([]);
      setFilteredRecipients([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (searchParams.lastName) params.append('lastName', searchParams.lastName);
      if (searchParams.firstName) params.append('firstName', searchParams.firstName);
      if (searchParams.ssn) params.append('ssn', searchParams.ssn);
      if (searchParams.cin) params.append('cin', searchParams.cin);
      if (searchParams.countyCode) params.append('countyCode', searchParams.countyCode);

      const response = await apiClient.get(`/recipients/search?${params.toString()}`);
      let results = response.data || [];

      // Filter by person type if specified
      if (searchParams.personType) {
        results = results.filter((r: Recipient) => r.personType === searchParams.personType);
      }

      setFilteredRecipients(results);
    } catch (err) {
      console.error('Error searching recipients:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchParams({
      lastName: '',
      firstName: '',
      ssn: '',
      cin: '',
      countyCode: '',
      personType: ''
    });
    setFilteredRecipients(recipients);
  };

  const getPersonTypeBadgeClass = (type: string) => {
    switch (type) {
      case 'OPEN_REFERRAL': return 'bg-info';
      case 'CLOSED_REFERRAL': return 'bg-secondary';
      case 'APPLICANT': return 'bg-warning text-dark';
      case 'RECIPIENT': return 'bg-success';
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
          <p className="text-muted mb-0">Loading Recipient Management...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="h3 mb-0">Person/Recipient Search</h1>
        <button
          className="btn btn-primary"
          onClick={() => router.push('/recipients/new')}
        >
          <i className="bi bi-plus-lg me-2"></i>New Referral
        </button>
      </div>

      {/* Statistics Cards */}
      <div className="row mb-4">
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: 'var(--color-p2, #046b99)' }}>{stats.total}</div>
              <p className="text-muted small mb-0">TOTAL PERSONS</p>
            </div>
          </div>
        </div>
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#0dcaf0' }}>{stats.openReferrals}</div>
              <p className="text-muted small mb-0">OPEN REFERRALS</p>
            </div>
          </div>
        </div>
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#ffc107' }}>{stats.applicants}</div>
              <p className="text-muted small mb-0">APPLICANTS</p>
            </div>
          </div>
        </div>
        <div className="col-lg-3 col-md-6 mb-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#198754' }}>{stats.recipients}</div>
              <p className="text-muted small mb-0">RECIPIENTS</p>
            </div>
          </div>
        </div>
      </div>

      {/* Search Panel */}
      <div className="card mb-4">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Search Persons</h5>
        </div>
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-2">
              <label className="form-label">Last Name</label>
              <input
                type="text"
                className="form-control"
                value={searchParams.lastName}
                onChange={(e) => setSearchParams({ ...searchParams, lastName: e.target.value })}
                placeholder="Enter last name"
              />
            </div>
            <div className="col-md-2">
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
              <label className="form-label">SSN</label>
              <input
                type="text"
                className="form-control"
                value={searchParams.ssn}
                onChange={(e) => setSearchParams({ ...searchParams, ssn: e.target.value })}
                placeholder="XXX-XX-XXXX"
                maxLength={11}
              />
            </div>
            <div className="col-md-2">
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
              <label className="form-label">Person Type</label>
              <select
                className="form-select"
                value={searchParams.personType}
                onChange={(e) => setSearchParams({ ...searchParams, personType: e.target.value })}
              >
                <option value="">All Types</option>
                <option value="OPEN_REFERRAL">Open Referral</option>
                <option value="CLOSED_REFERRAL">Closed Referral</option>
                <option value="APPLICANT">Applicant</option>
                <option value="RECIPIENT">Recipient</option>
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
            <button className="btn btn-outline-primary" onClick={() => router.push('/recipients/referrals/open')}>
              <i className="bi bi-folder2-open me-2"></i>Open Referrals
            </button>
            <button className="btn btn-outline-primary" onClick={() => router.push('/recipients/referrals/closed')}>
              <i className="bi bi-folder-x me-2"></i>Closed Referrals
            </button>
            <button className="btn btn-outline-primary" onClick={() => router.push('/recipients/new')}>
              <i className="bi bi-person-plus me-2"></i>Create Referral
            </button>
          </div>
        </div>
      </div>

      {/* Recipients Table */}
      <div className="card">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0">Search Results ({filteredRecipients.length})</h5>
        </div>
        <div className="card-body">
          {filteredRecipients.length === 0 ? (
            <p className="text-center text-muted py-4">No persons found. Try adjusting your search criteria.</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-striped table-hover">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>CIN</th>
                    <th>Person Type</th>
                    <th>County</th>
                    <th>DOB</th>
                    <th>Language</th>
                    <th>ESP</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredRecipients.map((recipient) => (
                    <tr key={recipient.id}>
                      <td>
                        <a
                          href="#"
                          onClick={(e) => { e.preventDefault(); router.push(`/recipients/${recipient.id}`); }}
                          className="text-primary fw-bold text-decoration-none"
                        >
                          {recipient.lastName}, {recipient.firstName} {recipient.middleName || ''}
                        </a>
                      </td>
                      <td>{recipient.cin || '-'}</td>
                      <td>
                        <span className={`badge ${getPersonTypeBadgeClass(recipient.personType)}`}>
                          {recipient.personType?.replace(/_/g, ' ')}
                        </span>
                      </td>
                      <td>{recipient.countyCode || '-'}</td>
                      <td>{recipient.dateOfBirth || '-'}</td>
                      <td>
                        {recipient.primaryLanguage || '-'}
                        {recipient.interpreterNeeded && (
                          <span className="badge bg-warning text-dark ms-1" title="Interpreter Needed">
                            <i className="bi bi-translate"></i>
                          </span>
                        )}
                      </td>
                      <td>
                        {recipient.espRegistered ? (
                          <span className="badge bg-success">Registered</span>
                        ) : (
                          <span className="badge bg-secondary">No</span>
                        )}
                      </td>
                      <td>
                        <div className="btn-group btn-group-sm">
                          <button
                            className="btn btn-outline-primary"
                            onClick={() => router.push(`/recipients/${recipient.id}`)}
                            title="View"
                          >
                            <i className="bi bi-eye"></i>
                          </button>
                          <button
                            className="btn btn-outline-secondary"
                            onClick={() => router.push(`/recipients/${recipient.id}/edit`)}
                            title="Edit"
                          >
                            <i className="bi bi-pencil"></i>
                          </button>
                          {recipient.personType === 'OPEN_REFERRAL' && (
                            <button
                              className="btn btn-outline-success"
                              onClick={() => router.push(`/cases/new?recipientId=${recipient.id}`)}
                              title="Create Case"
                            >
                              <i className="bi bi-folder-plus"></i>
                            </button>
                          )}
                          {recipient.personType === 'RECIPIENT' && (
                            <button
                              className="btn btn-outline-info"
                              onClick={() => router.push(`/recipients/${recipient.id}/companion-cases`)}
                              title="Companion Cases"
                            >
                              <i className="bi bi-people"></i>
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
  );
}
