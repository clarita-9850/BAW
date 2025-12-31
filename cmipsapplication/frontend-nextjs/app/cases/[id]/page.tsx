'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useParams } from 'next/navigation';
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
  authorizationStartDate: string;
  authorizationEndDate: string;
  authorizedHoursMonthly: number;
  authorizedHoursWeekly: number;
  assessmentType: string;
  healthCareCertStatus: string;
  healthCareCertDueDate: string;
  mediCalAidCode: string;
  fundingSource: string;
  shareOfCost: number;
  waiverProgram: string;
  denialReason: string;
  terminationReason: string;
  createdDate: string;
  createdBy: string;
  updatedDate: string;
  updatedBy: string;
};

type CaseNote = {
  id: number;
  caseId: number;
  noteType: string;
  subject: string;
  content: string;
  createdDate: string;
  createdBy: string;
  status: string;
};

type CaseContact = {
  id: number;
  caseId: number;
  contactType: string;
  firstName: string;
  lastName: string;
  relationship: string;
  phoneNumber: string;
  email: string;
  isActive: boolean;
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

export default function CaseDetailPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const params = useParams();
  const caseId = params.id as string;

  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [caseData, setCaseData] = useState<CaseEntity | null>(null);
  const [notes, setNotes] = useState<CaseNote[]>([]);
  const [contacts, setContacts] = useState<CaseContact[]>([]);
  const [providers, setProviders] = useState<ProviderAssignment[]>([]);
  const [activeTab, setActiveTab] = useState('overview');
  const [showActionModal, setShowActionModal] = useState(false);
  const [actionType, setActionType] = useState('');
  const [actionReason, setActionReason] = useState('');

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchCaseDetails();
  }, [user, authLoading, caseId]);

  const fetchCaseDetails = async () => {
    try {
      setLoading(true);
      const [caseResponse, notesResponse, contactsResponse, providersResponse] = await Promise.all([
        apiClient.get(`/cases/${caseId}`),
        apiClient.get(`/cases/${caseId}/notes`).catch(() => ({ data: [] })),
        apiClient.get(`/cases/${caseId}/contacts`).catch(() => ({ data: [] })),
        apiClient.get(`/cases/${caseId}/providers`).catch(() => ({ data: [] }))
      ]);

      setCaseData(caseResponse.data);
      setNotes(notesResponse.data || []);
      setContacts(contactsResponse.data || []);
      setProviders(providersResponse.data || []);
    } catch (err) {
      console.error('Error fetching case details:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCaseAction = async () => {
    try {
      let endpoint = '';
      let body: any = {};

      switch (actionType) {
        case 'approve':
          endpoint = `/cases/${caseId}/approve`;
          break;
        case 'deny':
          endpoint = `/cases/${caseId}/deny`;
          body = { reason: actionReason };
          break;
        case 'terminate':
          endpoint = `/cases/${caseId}/terminate`;
          body = { reason: actionReason };
          break;
        case 'leave':
          endpoint = `/cases/${caseId}/leave`;
          body = { reason: actionReason };
          break;
        case 'withdraw':
          endpoint = `/cases/${caseId}/withdraw`;
          body = { reason: actionReason };
          break;
      }

      await apiClient.put(endpoint, body);
      setShowActionModal(false);
      setActionReason('');
      fetchCaseDetails();
    } catch (err) {
      console.error('Error performing case action:', err);
      alert('Failed to perform action');
    }
  };

  const openActionModal = (type: string) => {
    setActionType(type);
    setShowActionModal(true);
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
          <p className="text-muted mb-0">Loading Case Details...</p>
        </div>
      </div>
    );
  }

  if (!caseData) {
    return (
      <div className="container-fluid py-4">
        <div className="alert alert-danger">Case not found</div>
        <button className="btn btn-primary" onClick={() => router.push('/cases')}>
          Back to Cases
        </button>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.push('/cases')}>
            <i className="bi bi-arrow-left me-2"></i>Back to Cases
          </button>
          <h1 className="h3 mb-0">
            Case: {caseData.caseNumber}
            <span className={`badge ${getStatusBadgeClass(caseData.caseStatus)} ms-3`}>
              {caseData.caseStatus?.replace(/_/g, ' ')}
            </span>
          </h1>
        </div>
        <div className="btn-group">
          {caseData.caseStatus === 'PENDING' && (
            <>
              <button className="btn btn-success" onClick={() => openActionModal('approve')}>
                <i className="bi bi-check-lg me-2"></i>Approve
              </button>
              <button className="btn btn-danger" onClick={() => openActionModal('deny')}>
                <i className="bi bi-x-lg me-2"></i>Deny
              </button>
            </>
          )}
          {caseData.caseStatus === 'ELIGIBLE' && (
            <>
              <button className="btn btn-warning" onClick={() => openActionModal('leave')}>
                <i className="bi bi-pause-circle me-2"></i>Place on Leave
              </button>
              <button className="btn btn-danger" onClick={() => openActionModal('terminate')}>
                <i className="bi bi-stop-circle me-2"></i>Terminate
              </button>
            </>
          )}
          <button className="btn btn-outline-secondary" onClick={() => openActionModal('withdraw')}>
            <i className="bi bi-arrow-return-left me-2"></i>Withdraw
          </button>
        </div>
      </div>

      {/* Tabs */}
      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'overview' ? 'active' : ''}`}
            onClick={() => setActiveTab('overview')}
          >
            Overview
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'eligibility' ? 'active' : ''}`}
            onClick={() => setActiveTab('eligibility')}
          >
            Eligibility
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'notes' ? 'active' : ''}`}
            onClick={() => setActiveTab('notes')}
          >
            Notes ({notes.length})
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'contacts' ? 'active' : ''}`}
            onClick={() => setActiveTab('contacts')}
          >
            Contacts ({contacts.length})
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'providers' ? 'active' : ''}`}
            onClick={() => setActiveTab('providers')}
          >
            Providers ({providers.length})
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'history' ? 'active' : ''}`}
            onClick={() => setActiveTab('history')}
          >
            History
          </button>
        </li>
      </ul>

      {/* Tab Content */}
      {activeTab === 'overview' && (
        <div className="row">
          <div className="col-md-6">
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Case Information</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Case Number</th>
                      <td>{caseData.caseNumber}</td>
                    </tr>
                    <tr>
                      <th>Case Type</th>
                      <td>{caseData.caseType}</td>
                    </tr>
                    <tr>
                      <th>CIN</th>
                      <td>{caseData.cin || '-'}</td>
                    </tr>
                    <tr>
                      <th>County Code</th>
                      <td>{caseData.countyCode}</td>
                    </tr>
                    <tr>
                      <th>Case Owner</th>
                      <td>{caseData.caseOwnerId || '-'}</td>
                    </tr>
                    <tr>
                      <th>Status</th>
                      <td>
                        <span className={`badge ${getStatusBadgeClass(caseData.caseStatus)}`}>
                          {caseData.caseStatus?.replace(/_/g, ' ')}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Important Dates</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Referral Date</th>
                      <td>{caseData.referralDate || '-'}</td>
                    </tr>
                    <tr>
                      <th>Application Date</th>
                      <td>{caseData.applicationDate || '-'}</td>
                    </tr>
                    <tr>
                      <th>Eligibility Date</th>
                      <td>{caseData.eligibilityDate || '-'}</td>
                    </tr>
                    <tr>
                      <th>Authorization Start</th>
                      <td>{caseData.authorizationStartDate || '-'}</td>
                    </tr>
                    <tr>
                      <th>Authorization End</th>
                      <td>{caseData.authorizationEndDate || '-'}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Service Hours</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Monthly Hours</th>
                      <td className="fw-bold fs-5">{caseData.authorizedHoursMonthly || 0} hrs</td>
                    </tr>
                    <tr>
                      <th>Weekly Hours</th>
                      <td className="fw-bold fs-5">{caseData.authorizedHoursWeekly || 0} hrs</td>
                    </tr>
                    <tr>
                      <th>Assessment Type</th>
                      <td>{caseData.assessmentType?.replace(/_/g, ' ') || '-'}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Health Care Certification</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Status</th>
                      <td>{caseData.healthCareCertStatus || '-'}</td>
                    </tr>
                    <tr>
                      <th>Due Date</th>
                      <td className={caseData.healthCareCertDueDate && new Date(caseData.healthCareCertDueDate) < new Date() ? 'text-danger fw-bold' : ''}>
                        {caseData.healthCareCertDueDate || '-'}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Funding</h5>
              </div>
              <div className="card-body">
                <table className="table table-borderless">
                  <tbody>
                    <tr>
                      <th style={{ width: '40%' }}>Medi-Cal Aid Code</th>
                      <td>{caseData.mediCalAidCode || '-'}</td>
                    </tr>
                    <tr>
                      <th>Funding Source</th>
                      <td>{caseData.fundingSource || '-'}</td>
                    </tr>
                    <tr>
                      <th>Share of Cost</th>
                      <td>{caseData.shareOfCost ? `$${caseData.shareOfCost}` : '-'}</td>
                    </tr>
                    <tr>
                      <th>Waiver Program</th>
                      <td>{caseData.waiverProgram || '-'}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'eligibility' && (
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Service Eligibility & Assessments</h5>
            <button className="btn btn-light btn-sm" onClick={() => router.push(`/eligibility/case/${caseId}`)}>
              <i className="bi bi-plus-lg me-2"></i>New Assessment
            </button>
          </div>
          <div className="card-body">
            <p className="text-muted">Assessment history and service eligibility details will be displayed here.</p>
            <button className="btn btn-primary" onClick={() => router.push(`/eligibility/case/${caseId}`)}>
              View Eligibility Details
            </button>
          </div>
        </div>
      )}

      {activeTab === 'notes' && (
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Case Notes</h5>
            <button className="btn btn-light btn-sm" onClick={() => router.push(`/cases/${caseId}/notes/new`)}>
              <i className="bi bi-plus-lg me-2"></i>Add Note
            </button>
          </div>
          <div className="card-body">
            {notes.length === 0 ? (
              <p className="text-muted text-center py-4">No notes found</p>
            ) : (
              <div className="list-group">
                {notes.map((note) => (
                  <div key={note.id} className="list-group-item">
                    <div className="d-flex justify-content-between align-items-start">
                      <div>
                        <h6 className="mb-1">{note.subject}</h6>
                        <small className="text-muted">
                          {note.noteType} | {note.createdBy} | {note.createdDate}
                        </small>
                      </div>
                      <span className={`badge ${note.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'}`}>
                        {note.status}
                      </span>
                    </div>
                    <p className="mb-0 mt-2">{note.content}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'contacts' && (
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Case Contacts</h5>
            <button className="btn btn-light btn-sm" onClick={() => router.push(`/cases/${caseId}/contacts/new`)}>
              <i className="bi bi-plus-lg me-2"></i>Add Contact
            </button>
          </div>
          <div className="card-body">
            {contacts.length === 0 ? (
              <p className="text-muted text-center py-4">No contacts found</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Type</th>
                      <th>Relationship</th>
                      <th>Phone</th>
                      <th>Email</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {contacts.map((contact) => (
                      <tr key={contact.id}>
                        <td>{contact.firstName} {contact.lastName}</td>
                        <td>{contact.contactType}</td>
                        <td>{contact.relationship}</td>
                        <td>{contact.phoneNumber}</td>
                        <td>{contact.email || '-'}</td>
                        <td>
                          <span className={`badge ${contact.isActive ? 'bg-success' : 'bg-secondary'}`}>
                            {contact.isActive ? 'Active' : 'Inactive'}
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

      {activeTab === 'providers' && (
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Assigned Providers</h5>
            <button className="btn btn-light btn-sm" onClick={() => router.push(`/providers/assign?caseId=${caseId}`)}>
              <i className="bi bi-plus-lg me-2"></i>Assign Provider
            </button>
          </div>
          <div className="card-body">
            {providers.length === 0 ? (
              <p className="text-muted text-center py-4">No providers assigned to this case</p>
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
                        <td className="fw-bold">{provider.assignedHours} hrs/week</td>
                        <td>
                          <span className={`badge ${provider.status === 'ACTIVE' ? 'bg-success' : provider.status === 'PENDING' ? 'bg-warning text-dark' : 'bg-secondary'}`}>
                            {provider.status}
                          </span>
                        </td>
                        <td>{provider.effectiveDate}</td>
                        <td>
                          <div className="btn-group btn-group-sm">
                            <button className="btn btn-outline-primary" onClick={() => router.push(`/providers/${provider.providerId}`)}>
                              View
                            </button>
                            <button className="btn btn-outline-secondary" onClick={() => router.push(`/providers/${provider.providerId}/workweek`)}>
                              Workweek
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  <tfoot>
                    <tr className="table-primary">
                      <td colSpan={3} className="text-end fw-bold">Total Assigned Hours:</td>
                      <td className="fw-bold">{providers.reduce((sum, p) => sum + p.assignedHours, 0)} hrs/week</td>
                      <td colSpan={3}></td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'history' && (
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Case History</h5>
          </div>
          <div className="card-body">
            <div className="list-group">
              <div className="list-group-item">
                <small className="text-muted">Created</small>
                <p className="mb-0">{caseData.createdDate} by {caseData.createdBy}</p>
              </div>
              {caseData.updatedDate && (
                <div className="list-group-item">
                  <small className="text-muted">Last Updated</small>
                  <p className="mb-0">{caseData.updatedDate} by {caseData.updatedBy}</p>
                </div>
              )}
              {caseData.denialReason && (
                <div className="list-group-item">
                  <small className="text-muted">Denial Reason</small>
                  <p className="mb-0">{caseData.denialReason}</p>
                </div>
              )}
              {caseData.terminationReason && (
                <div className="list-group-item">
                  <small className="text-muted">Termination Reason</small>
                  <p className="mb-0">{caseData.terminationReason}</p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Action Modal */}
      {showActionModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  {actionType === 'approve' && 'Approve Case'}
                  {actionType === 'deny' && 'Deny Case'}
                  {actionType === 'terminate' && 'Terminate Case'}
                  {actionType === 'leave' && 'Place Case on Leave'}
                  {actionType === 'withdraw' && 'Withdraw Application'}
                </h5>
                <button type="button" className="btn-close" onClick={() => setShowActionModal(false)}></button>
              </div>
              <div className="modal-body">
                {actionType !== 'approve' && (
                  <div className="mb-3">
                    <label className="form-label">Reason</label>
                    <textarea
                      className="form-control"
                      rows={3}
                      value={actionReason}
                      onChange={(e) => setActionReason(e.target.value)}
                      placeholder="Enter reason..."
                      required
                    />
                  </div>
                )}
                {actionType === 'approve' && (
                  <p>Are you sure you want to approve this case?</p>
                )}
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowActionModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className={`btn ${actionType === 'approve' ? 'btn-success' : 'btn-danger'}`}
                  onClick={handleCaseAction}
                  disabled={actionType !== 'approve' && !actionReason}
                >
                  Confirm
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
