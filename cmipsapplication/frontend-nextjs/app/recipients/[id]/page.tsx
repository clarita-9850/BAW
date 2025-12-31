'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useParams } from 'next/navigation';
import apiClient from '@/lib/api';

type Recipient = {
  id: number;
  cin: string;
  firstName: string;
  lastName: string;
  middleName?: string;
  dateOfBirth: string;
  ssn: string;
  ssnVerificationStatus: string;
  gender: string;
  ethnicity?: string;
  primaryLanguage?: string;

  // Address
  residenceAddress: string;
  residenceCity: string;
  residenceState: string;
  residenceZip: string;
  mailingAddress?: string;
  mailingCity?: string;
  mailingState?: string;
  mailingZip?: string;

  // Contact
  phoneNumber?: string;
  alternatePhone?: string;
  email?: string;

  // Case Information
  caseId: number;
  caseStatus: string;
  countyCode: string;
  countyName?: string;

  // Living Situation (BR SE 01-05)
  livingSituation: string;
  livingArrangement: string;
  facilityType?: string;

  // SOC (BR SE 16-24)
  socStatus: string;
  socAmount: number;
  socMetDate?: string;
  socExemptReason?: string;

  // Eligibility
  eligibilityStatus: string;
  eligibilityDeterminationDate?: string;
  medicaidEligible: boolean;
  waiverProgram?: string;

  // Services
  totalAuthorizedHours: number;
  assessmentDueDate?: string;
  reassessmentDueDate?: string;

  // Audit
  createdDate: string;
  createdBy: string;
  updatedDate: string;
  updatedBy: string;
};

type CompanionCase = {
  id: number;
  cin: string;
  firstName: string;
  lastName: string;
  caseId: number;
  relationship: string;
  sharedAddress: boolean;
};

type ProviderAssignment = {
  id: number;
  providerId: number;
  providerName: string;
  providerNumber: string;
  providerType: string;
  relationship: string;
  assignedHours: number;
  status: string;
  effectiveDate: string;
  terminationDate?: string;
};

type ServiceAuthorization = {
  id: number;
  serviceType: string;
  authorizedHours: number;
  usedHours: number;
  remainingHours: number;
  effectiveDate: string;
  expirationDate: string;
  status: string;
};

export default function RecipientDetailPage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const router = useRouter();
  const params = useParams();
  const recipientId = params.id as string;

  const [loading, setLoading] = useState(true);
  const [recipient, setRecipient] = useState<Recipient | null>(null);
  const [companionCases, setCompanionCases] = useState<CompanionCase[]>([]);
  const [providers, setProviders] = useState<ProviderAssignment[]>([]);
  const [authorizations, setAuthorizations] = useState<ServiceAuthorization[]>([]);
  const [activeTab, setActiveTab] = useState('overview');
  const [showCompanionSearchModal, setShowCompanionSearchModal] = useState(false);
  const [companionSearchCin, setCompanionSearchCin] = useState('');
  const [companionSearchResults, setCompanionSearchResults] = useState<any[]>([]);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchRecipientDetails();
  }, [user, authLoading, recipientId]);

  const fetchRecipientDetails = async () => {
    try {
      setLoading(true);
      const [recipientRes, companionRes, providersRes, authRes] = await Promise.all([
        apiClient.get(`/recipients/${recipientId}`),
        apiClient.get(`/recipients/${recipientId}/companion-cases`).catch(() => ({ data: [] })),
        apiClient.get(`/recipients/${recipientId}/providers`).catch(() => ({ data: [] })),
        apiClient.get(`/recipients/${recipientId}/authorizations`).catch(() => ({ data: [] }))
      ]);

      setRecipient(recipientRes.data);
      setCompanionCases(companionRes.data || []);
      setProviders(providersRes.data || []);
      setAuthorizations(authRes.data || []);
    } catch (err) {
      console.error('Error fetching recipient details:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCompanionSearch = async () => {
    if (!companionSearchCin) return;
    try {
      const response = await apiClient.get(`/recipients/search?cin=${companionSearchCin}`);
      setCompanionSearchResults(response.data || []);
    } catch (err) {
      console.error('Error searching for companion:', err);
      alert('Failed to search for companion case');
    }
  };

  const handleLinkCompanion = async (companionId: number, relationship: string) => {
    try {
      await apiClient.post(`/recipients/${recipientId}/companion-cases`, {
        companionRecipientId: companionId,
        relationship
      });
      setShowCompanionSearchModal(false);
      setCompanionSearchCin('');
      setCompanionSearchResults([]);
      fetchRecipientDetails();
    } catch (err) {
      console.error('Error linking companion:', err);
      alert('Failed to link companion case');
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'bg-success';
      case 'PENDING': return 'bg-warning text-dark';
      case 'INACTIVE': return 'bg-secondary';
      case 'TERMINATED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  };

  const getSocStatusBadge = (status: string) => {
    switch (status) {
      case 'MET': return 'bg-success';
      case 'NOT_MET': return 'bg-danger';
      case 'EXEMPT': return 'bg-info';
      case 'NO_SOC': return 'bg-secondary';
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
          <p className="text-muted mb-0">Loading Recipient Details...</p>
        </div>
      </div>
    );
  }

  if (!recipient) {
    return (
      <div className="container-fluid py-4">
        <div className="alert alert-danger">Recipient not found</div>
        <button className="btn btn-primary" onClick={() => router.push('/recipients')}>
          Back to Recipients
        </button>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.push('/recipients')}>
            <i className="bi bi-arrow-left me-2"></i>Back to Recipients
          </button>
          <h1 className="h3 mb-0">
            {recipient.firstName} {recipient.lastName}
            <span className={`badge ${getStatusBadge(recipient.eligibilityStatus)} ms-3`}>
              {recipient.eligibilityStatus}
            </span>
          </h1>
          <p className="text-muted mb-0">CIN: {recipient.cin} | Case #{recipient.caseId}</p>
        </div>
        <div className="btn-group">
          <button className="btn btn-outline-primary" onClick={() => router.push(`/cases/${recipient.caseId}`)}>
            <i className="bi bi-folder me-2"></i>View Case
          </button>
          <button className="btn btn-outline-primary" onClick={() => router.push(`/eligibility/case/${recipient.caseId}`)}>
            <i className="bi bi-clipboard-check me-2"></i>Service Eligibility
          </button>
        </div>
      </div>

      {/* Quick Stats */}
      <div className="row mb-4">
        <div className="col-md-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold fs-2" style={{ color: 'var(--color-p2, #046b99)' }}>
                {recipient.totalAuthorizedHours}
              </div>
              <small className="text-muted">Authorized Hours</small>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className={`fw-bold fs-2 ${recipient.socStatus === 'MET' ? 'text-success' : 'text-warning'}`}>
                ${recipient.socAmount}
              </div>
              <small className="text-muted">Share of Cost</small>
              <br />
              <span className={`badge ${getSocStatusBadge(recipient.socStatus)}`}>
                {recipient.socStatus}
              </span>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold fs-4">{providers.filter(p => p.status === 'ACTIVE').length}</div>
              <small className="text-muted">Active Providers</small>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card text-center h-100">
            <div className="card-body">
              <div className="fw-bold fs-4">{companionCases.length}</div>
              <small className="text-muted">Companion Cases</small>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button className={`nav-link ${activeTab === 'overview' ? 'active' : ''}`} onClick={() => setActiveTab('overview')}>
            Overview
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${activeTab === 'providers' ? 'active' : ''}`} onClick={() => setActiveTab('providers')}>
            Providers ({providers.length})
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${activeTab === 'authorizations' ? 'active' : ''}`} onClick={() => setActiveTab('authorizations')}>
            Authorizations ({authorizations.length})
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${activeTab === 'companion' ? 'active' : ''}`} onClick={() => setActiveTab('companion')}>
            Companion Cases ({companionCases.length})
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${activeTab === 'soc' ? 'active' : ''}`} onClick={() => setActiveTab('soc')}>
            Share of Cost
          </button>
        </li>
      </ul>

      {/* Overview Tab */}
      {activeTab === 'overview' && (
        <div className="row">
          <div className="col-md-6">
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Personal Information</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>CIN</th>
                      <td>{recipient.cin}</td>
                    </tr>
                    <tr>
                      <th>Name</th>
                      <td>{recipient.firstName} {recipient.middleName} {recipient.lastName}</td>
                    </tr>
                    <tr>
                      <th>Date of Birth</th>
                      <td>{recipient.dateOfBirth}</td>
                    </tr>
                    <tr>
                      <th>SSN</th>
                      <td>
                        ***-**-{recipient.ssn?.slice(-4)}
                        <span className={`badge ms-2 ${recipient.ssnVerificationStatus === 'VERIFIED' ? 'bg-success' : 'bg-warning text-dark'}`}>
                          {recipient.ssnVerificationStatus}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>Gender</th>
                      <td>{recipient.gender}</td>
                    </tr>
                    <tr>
                      <th>Primary Language</th>
                      <td>{recipient.primaryLanguage || '-'}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Contact Information</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Residence</th>
                      <td>
                        {recipient.residenceAddress}<br />
                        {recipient.residenceCity}, {recipient.residenceState} {recipient.residenceZip}
                      </td>
                    </tr>
                    {recipient.mailingAddress && (
                      <tr>
                        <th>Mailing</th>
                        <td>
                          {recipient.mailingAddress}<br />
                          {recipient.mailingCity}, {recipient.mailingState} {recipient.mailingZip}
                        </td>
                      </tr>
                    )}
                    <tr>
                      <th>Phone</th>
                      <td>{recipient.phoneNumber || '-'}</td>
                    </tr>
                    <tr>
                      <th>Alternate Phone</th>
                      <td>{recipient.alternatePhone || '-'}</td>
                    </tr>
                    <tr>
                      <th>Email</th>
                      <td>{recipient.email || '-'}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Case & Eligibility</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Case ID</th>
                      <td>
                        <a href="#" onClick={(e) => { e.preventDefault(); router.push(`/cases/${recipient.caseId}`); }}>
                          #{recipient.caseId}
                        </a>
                      </td>
                    </tr>
                    <tr>
                      <th>Case Status</th>
                      <td>
                        <span className={`badge ${getStatusBadge(recipient.caseStatus)}`}>
                          {recipient.caseStatus}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>County</th>
                      <td>{recipient.countyName || recipient.countyCode}</td>
                    </tr>
                    <tr>
                      <th>Eligibility Status</th>
                      <td>
                        <span className={`badge ${getStatusBadge(recipient.eligibilityStatus)}`}>
                          {recipient.eligibilityStatus}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>Medicaid Eligible</th>
                      <td>
                        {recipient.medicaidEligible ? (
                          <span className="badge bg-success">Yes</span>
                        ) : (
                          <span className="badge bg-danger">No</span>
                        )}
                      </td>
                    </tr>
                    <tr>
                      <th>Waiver Program</th>
                      <td>{recipient.waiverProgram || '-'}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Living Situation (BR SE 01-05)</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Living Situation</th>
                      <td>{recipient.livingSituation}</td>
                    </tr>
                    <tr>
                      <th>Arrangement</th>
                      <td>{recipient.livingArrangement}</td>
                    </tr>
                    {recipient.facilityType && (
                      <tr>
                        <th>Facility Type</th>
                        <td>{recipient.facilityType}</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Assessment Dates</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Assessment Due</th>
                      <td className={recipient.assessmentDueDate && new Date(recipient.assessmentDueDate) <= new Date() ? 'text-danger fw-bold' : ''}>
                        {recipient.assessmentDueDate || '-'}
                      </td>
                    </tr>
                    <tr>
                      <th>Reassessment Due</th>
                      <td className={recipient.reassessmentDueDate && new Date(recipient.reassessmentDueDate) <= new Date() ? 'text-danger fw-bold' : ''}>
                        {recipient.reassessmentDueDate || '-'}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Providers Tab */}
      {activeTab === 'providers' && (
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Provider Assignments</h5>
            <button className="btn btn-light btn-sm" onClick={() => router.push(`/recipients/${recipientId}/assign-provider`)}>
              <i className="bi bi-plus-lg me-2"></i>Assign Provider
            </button>
          </div>
          <div className="card-body">
            {providers.length === 0 ? (
              <p className="text-muted text-center py-4">No providers assigned</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Provider</th>
                      <th>Type</th>
                      <th>Relationship</th>
                      <th>Hours</th>
                      <th>Status</th>
                      <th>Effective Date</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {providers.map((provider) => (
                      <tr key={provider.id}>
                        <td>
                          <a href="#" onClick={(e) => { e.preventDefault(); router.push(`/providers/${provider.providerId}`); }}>
                            {provider.providerName}
                          </a>
                          <br />
                          <small className="text-muted">{provider.providerNumber}</small>
                        </td>
                        <td>{provider.providerType}</td>
                        <td>{provider.relationship}</td>
                        <td className="fw-bold">{provider.assignedHours}</td>
                        <td>
                          <span className={`badge ${getStatusBadge(provider.status)}`}>
                            {provider.status}
                          </span>
                        </td>
                        <td>{provider.effectiveDate}</td>
                        <td>
                          <button className="btn btn-sm btn-outline-primary" onClick={() => router.push(`/providers/assignments/${provider.id}`)}>
                            View
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
      )}

      {/* Authorizations Tab */}
      {activeTab === 'authorizations' && (
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Service Authorizations</h5>
          </div>
          <div className="card-body">
            {authorizations.length === 0 ? (
              <p className="text-muted text-center py-4">No service authorizations found</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Service Type</th>
                      <th>Authorized</th>
                      <th>Used</th>
                      <th>Remaining</th>
                      <th>Effective</th>
                      <th>Expires</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {authorizations.map((auth) => (
                      <tr key={auth.id}>
                        <td>{auth.serviceType}</td>
                        <td>{auth.authorizedHours} hrs</td>
                        <td>{auth.usedHours} hrs</td>
                        <td className={auth.remainingHours <= 0 ? 'text-danger fw-bold' : ''}>
                          {auth.remainingHours} hrs
                        </td>
                        <td>{auth.effectiveDate}</td>
                        <td className={auth.expirationDate && new Date(auth.expirationDate) <= new Date() ? 'text-danger' : ''}>
                          {auth.expirationDate}
                        </td>
                        <td>
                          <span className={`badge ${getStatusBadge(auth.status)}`}>
                            {auth.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Companion Cases Tab */}
      {activeTab === 'companion' && (
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Companion Cases (BR SE 26-27)</h5>
            <button className="btn btn-light btn-sm" onClick={() => setShowCompanionSearchModal(true)}>
              <i className="bi bi-search me-2"></i>Search & Link
            </button>
          </div>
          <div className="card-body">
            <div className="alert alert-info mb-4">
              <h6>Companion Case Rules</h6>
              <ul className="mb-0 small">
                <li>Recipients at the same address may share providers</li>
                <li>Combined hours cannot exceed provider weekly maximum</li>
                <li>Providers serving multiple recipients at same address count as One-to-Many</li>
              </ul>
            </div>

            {companionCases.length === 0 ? (
              <p className="text-muted text-center py-4">No companion cases linked</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>CIN</th>
                      <th>Name</th>
                      <th>Case ID</th>
                      <th>Relationship</th>
                      <th>Shared Address</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {companionCases.map((companion) => (
                      <tr key={companion.id}>
                        <td>{companion.cin}</td>
                        <td>
                          <a href="#" onClick={(e) => { e.preventDefault(); router.push(`/recipients/${companion.id}`); }}>
                            {companion.firstName} {companion.lastName}
                          </a>
                        </td>
                        <td>
                          <a href="#" onClick={(e) => { e.preventDefault(); router.push(`/cases/${companion.caseId}`); }}>
                            #{companion.caseId}
                          </a>
                        </td>
                        <td>{companion.relationship}</td>
                        <td>
                          {companion.sharedAddress ? (
                            <i className="bi bi-check-circle text-success"></i>
                          ) : (
                            <i className="bi bi-x-circle text-muted"></i>
                          )}
                        </td>
                        <td>
                          <button className="btn btn-sm btn-outline-primary">View</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Share of Cost Tab */}
      {activeTab === 'soc' && (
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Share of Cost (BR SE 16-24)</h5>
          </div>
          <div className="card-body">
            <div className="row mb-4">
              <div className="col-md-4 text-center">
                <div className="card h-100">
                  <div className="card-body">
                    <div className="display-4 fw-bold" style={{ color: 'var(--color-p2, #046b99)' }}>
                      ${recipient.socAmount}
                    </div>
                    <p className="text-muted mb-0">Monthly SOC Amount</p>
                  </div>
                </div>
              </div>
              <div className="col-md-4 text-center">
                <div className="card h-100">
                  <div className="card-body">
                    <span className={`badge fs-4 ${getSocStatusBadge(recipient.socStatus)}`}>
                      {recipient.socStatus}
                    </span>
                    <p className="text-muted mb-2 mt-2">SOC Status</p>
                    {recipient.socMetDate && (
                      <small className="text-muted">Met on: {recipient.socMetDate}</small>
                    )}
                  </div>
                </div>
              </div>
              <div className="col-md-4 text-center">
                <div className="card h-100">
                  <div className="card-body">
                    <div className="fs-5 fw-bold">{recipient.socExemptReason || 'N/A'}</div>
                    <p className="text-muted mb-0">Exemption Reason</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="alert alert-info">
              <h6>Share of Cost Business Rules</h6>
              <ul className="mb-0 small">
                <li><strong>BR SE 16:</strong> SOC must be met before services can be authorized</li>
                <li><strong>BR SE 17:</strong> SOC is calculated monthly based on income</li>
                <li><strong>BR SE 18:</strong> Medical expenses may be applied to meet SOC</li>
                <li><strong>BR SE 19:</strong> SOC exemptions available for certain situations</li>
                <li><strong>BR SE 20-24:</strong> SOC tracking and reporting requirements</li>
              </ul>
            </div>
          </div>
        </div>
      )}

      {/* Companion Search Modal */}
      {showCompanionSearchModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="modal-title text-white">Search Companion Case</h5>
                <button type="button" className="btn-close btn-close-white" onClick={() => setShowCompanionSearchModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="input-group mb-4">
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Enter CIN to search..."
                    value={companionSearchCin}
                    onChange={(e) => setCompanionSearchCin(e.target.value)}
                  />
                  <button className="btn btn-primary" onClick={handleCompanionSearch}>
                    <i className="bi bi-search me-2"></i>Search
                  </button>
                </div>

                {companionSearchResults.length > 0 && (
                  <div className="table-responsive">
                    <table className="table table-striped">
                      <thead>
                        <tr>
                          <th>CIN</th>
                          <th>Name</th>
                          <th>Address</th>
                          <th>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {companionSearchResults.map((result: any) => (
                          <tr key={result.id}>
                            <td>{result.cin}</td>
                            <td>{result.firstName} {result.lastName}</td>
                            <td>{result.residenceAddress}, {result.residenceCity}</td>
                            <td>
                              <div className="btn-group btn-group-sm">
                                <button
                                  className="btn btn-success"
                                  onClick={() => {
                                    const relationship = prompt('Enter relationship (e.g., Spouse, Parent, Child):');
                                    if (relationship) handleLinkCompanion(result.id, relationship);
                                  }}
                                >
                                  Link
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}

                {companionSearchResults.length === 0 && companionSearchCin && (
                  <p className="text-muted text-center">No results found</p>
                )}
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowCompanionSearchModal(false)}>
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
