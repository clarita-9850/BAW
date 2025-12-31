'use client';

import React, { Suspense, useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useSearchParams } from 'next/navigation';
import apiClient from '@/lib/api';

type Provider = {
  id: number;
  providerNumber: string;
  firstName: string;
  lastName: string;
  providerStatus: string;
  eligible: string;
};

type Case = {
  id: number;
  caseNumber: string;
  recipientName: string;
  caseStatus: string;
  authorizedHours: number;
  assignedHours: number;
  availableHours: number;
};

type AssignmentForm = {
  providerId: string;
  caseId: string;
  providerType: string;
  relationship: string;
  assignedHours: number;
  effectiveDate: string;
  notes: string;
};

const PROVIDER_TYPES = [
  { value: 'WAIVER_PERSONAL_CARE', label: 'Waiver Personal Care Services (WPCS)' },
  { value: 'NON_WAIVER_PERSONAL_CARE', label: 'Non-Waiver Personal Care Services' },
  { value: 'DOMESTIC', label: 'Domestic Services' },
  { value: 'PARAMEDICAL', label: 'Paramedical Services' },
  { value: 'PROTECTIVE_SUPERVISION', label: 'Protective Supervision' }
];

const RELATIONSHIPS = [
  { value: 'PARENT', label: 'Parent' },
  { value: 'SPOUSE', label: 'Spouse' },
  { value: 'CHILD', label: 'Child' },
  { value: 'SIBLING', label: 'Sibling' },
  { value: 'GRANDPARENT', label: 'Grandparent' },
  { value: 'OTHER_RELATIVE', label: 'Other Relative' },
  { value: 'FRIEND', label: 'Friend' },
  { value: 'NON_RELATIVE', label: 'Non-Relative' }
];

function NewAssignmentPageContent() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const router = useRouter();
  const searchParams = useSearchParams();
  const preSelectedProviderId = searchParams.get('providerId') || '';
  const preSelectedCaseId = searchParams.get('caseId') || '';

  const [loading, setLoading] = useState(false);
  const [providers, setProviders] = useState<Provider[]>([]);
  const [cases, setCases] = useState<Case[]>([]);
  const [selectedCase, setSelectedCase] = useState<Case | null>(null);
  const [selectedProvider, setSelectedProvider] = useState<Provider | null>(null);
  const [errors, setErrors] = useState<{[key: string]: string}>({});

  const [form, setForm] = useState<AssignmentForm>({
    providerId: preSelectedProviderId,
    caseId: preSelectedCaseId,
    providerType: 'NON_WAIVER_PERSONAL_CARE',
    relationship: '',
    assignedHours: 0,
    effectiveDate: new Date().toISOString().split('T')[0],
    notes: ''
  });

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchProvidersAndCases();
  }, [user, authLoading]);

  const fetchProvidersAndCases = async () => {
    try {
      const [providersResponse, casesResponse] = await Promise.all([
        apiClient.get('/providers?status=ACTIVE'),
        apiClient.get('/cases?status=ACTIVE')
      ]);
      setProviders(providersResponse.data || []);
      setCases(casesResponse.data || []);

      // If pre-selected, load details
      if (preSelectedProviderId) {
        const provider = (providersResponse.data || []).find((p: Provider) => p.id.toString() === preSelectedProviderId);
        if (provider) setSelectedProvider(provider);
      }
      if (preSelectedCaseId) {
        const caseItem = (casesResponse.data || []).find((c: Case) => c.id.toString() === preSelectedCaseId);
        if (caseItem) setSelectedCase(caseItem);
      }
    } catch (err) {
      console.error('Error fetching data:', err);
    }
  };

  const handleProviderChange = (providerId: string) => {
    setForm({ ...form, providerId });
    const provider = providers.find(p => p.id.toString() === providerId);
    setSelectedProvider(provider || null);
  };

  const handleCaseChange = (caseId: string) => {
    setForm({ ...form, caseId });
    const caseItem = cases.find(c => c.id.toString() === caseId);
    setSelectedCase(caseItem || null);
  };

  const validateForm = (): boolean => {
    const newErrors: {[key: string]: string} = {};

    if (!form.providerId) newErrors.providerId = 'Provider is required';
    if (!form.caseId) newErrors.caseId = 'Case is required';
    if (!form.providerType) newErrors.providerType = 'Provider type is required';
    if (!form.relationship) newErrors.relationship = 'Relationship is required';
    if (!form.effectiveDate) newErrors.effectiveDate = 'Effective date is required';
    if (form.assignedHours <= 0) newErrors.assignedHours = 'Assigned hours must be greater than 0';

    if (selectedCase && form.assignedHours > selectedCase.availableHours) {
      newErrors.assignedHours = `Cannot exceed available hours (${selectedCase.availableHours} hrs)`;
    }

    if (selectedProvider && selectedProvider.eligible !== 'YES') {
      newErrors.providerId = 'Provider must be eligible to receive assignments';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) return;

    try {
      setLoading(true);
      await apiClient.post('/providers/assignments', {
        providerId: parseInt(form.providerId),
        caseId: parseInt(form.caseId),
        providerType: form.providerType,
        relationship: form.relationship,
        assignedHours: form.assignedHours,
        effectiveDate: form.effectiveDate,
        notes: form.notes
      });

      alert('Assignment created successfully!');

      // Navigate back to provider or case detail
      if (preSelectedProviderId) {
        router.push(`/providers/${preSelectedProviderId}`);
      } else if (preSelectedCaseId) {
        router.push(`/cases/${preSelectedCaseId}`);
      } else {
        router.push('/providers');
      }
    } catch (err: any) {
      console.error('Error creating assignment:', err);
      alert(err.response?.data?.message || 'Failed to create assignment');
    } finally {
      setLoading(false);
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
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <button
            className="btn btn-link text-decoration-none ps-0"
            onClick={() => router.back()}
          >
            <i className="bi bi-arrow-left me-2"></i>Back
          </button>
          <h1 className="h3 mb-0">New Provider Assignment</h1>
          <p className="text-muted mb-0">Assign a provider to a case</p>
        </div>
      </div>

      {/* Business Rules Info */}
      <div className="card mb-4">
        <div className="card-header bg-info text-white">
          <h5 className="mb-0"><i className="bi bi-info-circle me-2"></i>Assignment Rules</h5>
        </div>
        <div className="card-body">
          <div className="row">
            <div className="col-md-4">
              <h6>Provider Requirements</h6>
              <ul className="small mb-0">
                <li>Provider must be ACTIVE and ELIGIBLE</li>
                <li>Background check must be cleared</li>
                <li>Workweek agreement must be signed</li>
              </ul>
            </div>
            <div className="col-md-4">
              <h6>Assignment Limits</h6>
              <ul className="small mb-0">
                <li>Cannot exceed authorized case hours</li>
                <li>Weekly OT limit: 66 hours (all recipients)</li>
                <li>Travel time max: 7 hours/week</li>
              </ul>
            </div>
            <div className="col-md-4">
              <h6>Relationship Rules</h6>
              <ul className="small mb-0">
                <li>Parent providers need approval</li>
                <li>Live-in providers have restrictions</li>
                <li>WPCS requires Medi-Cal eligibility</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="row">
          {/* Provider Selection */}
          <div className="col-md-6">
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Provider Information</h5>
              </div>
              <div className="card-body">
                <div className="mb-3">
                  <label className="form-label">Select Provider <span className="text-danger">*</span></label>
                  <select
                    className={`form-select ${errors.providerId ? 'is-invalid' : ''}`}
                    value={form.providerId}
                    onChange={(e) => handleProviderChange(e.target.value)}
                    disabled={!!preSelectedProviderId}
                  >
                    <option value="">-- Select a Provider --</option>
                    {providers.map(provider => (
                      <option key={provider.id} value={provider.id}>
                        {provider.lastName}, {provider.firstName} ({provider.providerNumber}) - {provider.providerStatus}
                      </option>
                    ))}
                  </select>
                  {errors.providerId && <div className="invalid-feedback">{errors.providerId}</div>}
                </div>

                {selectedProvider && (
                  <div className="alert alert-light">
                    <h6 className="mb-2">Provider Details</h6>
                    <table className="table table-sm table-borderless mb-0">
                      <tbody>
                        <tr>
                          <th style={{ width: '40%' }}>Name</th>
                          <td>{selectedProvider.firstName} {selectedProvider.lastName}</td>
                        </tr>
                        <tr>
                          <th>Provider #</th>
                          <td>{selectedProvider.providerNumber}</td>
                        </tr>
                        <tr>
                          <th>Status</th>
                          <td>
                            <span className={`badge ${selectedProvider.providerStatus === 'ACTIVE' ? 'bg-success' : 'bg-secondary'}`}>
                              {selectedProvider.providerStatus}
                            </span>
                          </td>
                        </tr>
                        <tr>
                          <th>Eligible</th>
                          <td>
                            <span className={`badge ${selectedProvider.eligible === 'YES' ? 'bg-success' : 'bg-danger'}`}>
                              {selectedProvider.eligible}
                            </span>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Case Selection */}
          <div className="col-md-6">
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Case Information</h5>
              </div>
              <div className="card-body">
                <div className="mb-3">
                  <label className="form-label">Select Case <span className="text-danger">*</span></label>
                  <select
                    className={`form-select ${errors.caseId ? 'is-invalid' : ''}`}
                    value={form.caseId}
                    onChange={(e) => handleCaseChange(e.target.value)}
                    disabled={!!preSelectedCaseId}
                  >
                    <option value="">-- Select a Case --</option>
                    {cases.map(caseItem => (
                      <option key={caseItem.id} value={caseItem.id}>
                        {caseItem.caseNumber} - {caseItem.recipientName} ({caseItem.availableHours} hrs available)
                      </option>
                    ))}
                  </select>
                  {errors.caseId && <div className="invalid-feedback">{errors.caseId}</div>}
                </div>

                {selectedCase && (
                  <div className="alert alert-light">
                    <h6 className="mb-2">Case Details</h6>
                    <table className="table table-sm table-borderless mb-0">
                      <tbody>
                        <tr>
                          <th style={{ width: '50%' }}>Case Number</th>
                          <td>{selectedCase.caseNumber}</td>
                        </tr>
                        <tr>
                          <th>Recipient</th>
                          <td>{selectedCase.recipientName}</td>
                        </tr>
                        <tr>
                          <th>Authorized Hours</th>
                          <td>{selectedCase.authorizedHours} hrs/month</td>
                        </tr>
                        <tr>
                          <th>Already Assigned</th>
                          <td>{selectedCase.assignedHours} hrs</td>
                        </tr>
                        <tr>
                          <th>Available Hours</th>
                          <td className="text-success fw-bold">{selectedCase.availableHours} hrs</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Assignment Details */}
        <div className="card mb-4">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Assignment Details</h5>
          </div>
          <div className="card-body">
            <div className="row g-3">
              <div className="col-md-4">
                <label className="form-label">Provider Type <span className="text-danger">*</span></label>
                <select
                  className={`form-select ${errors.providerType ? 'is-invalid' : ''}`}
                  value={form.providerType}
                  onChange={(e) => setForm({ ...form, providerType: e.target.value })}
                >
                  {PROVIDER_TYPES.map(type => (
                    <option key={type.value} value={type.value}>{type.label}</option>
                  ))}
                </select>
                {errors.providerType && <div className="invalid-feedback">{errors.providerType}</div>}
              </div>

              <div className="col-md-4">
                <label className="form-label">Relationship to Recipient <span className="text-danger">*</span></label>
                <select
                  className={`form-select ${errors.relationship ? 'is-invalid' : ''}`}
                  value={form.relationship}
                  onChange={(e) => setForm({ ...form, relationship: e.target.value })}
                >
                  <option value="">-- Select Relationship --</option>
                  {RELATIONSHIPS.map(rel => (
                    <option key={rel.value} value={rel.value}>{rel.label}</option>
                  ))}
                </select>
                {errors.relationship && <div className="invalid-feedback">{errors.relationship}</div>}
              </div>

              <div className="col-md-4">
                <label className="form-label">Assigned Hours <span className="text-danger">*</span></label>
                <div className="input-group">
                  <input
                    type="number"
                    className={`form-control ${errors.assignedHours ? 'is-invalid' : ''}`}
                    value={form.assignedHours}
                    onChange={(e) => setForm({ ...form, assignedHours: parseFloat(e.target.value) || 0 })}
                    min="0"
                    max={selectedCase?.availableHours || 999}
                    step="0.5"
                  />
                  <span className="input-group-text">hrs/month</span>
                  {errors.assignedHours && <div className="invalid-feedback">{errors.assignedHours}</div>}
                </div>
                {selectedCase && (
                  <small className="text-muted">Max available: {selectedCase.availableHours} hrs</small>
                )}
              </div>

              <div className="col-md-4">
                <label className="form-label">Effective Date <span className="text-danger">*</span></label>
                <input
                  type="date"
                  className={`form-control ${errors.effectiveDate ? 'is-invalid' : ''}`}
                  value={form.effectiveDate}
                  onChange={(e) => setForm({ ...form, effectiveDate: e.target.value })}
                />
                {errors.effectiveDate && <div className="invalid-feedback">{errors.effectiveDate}</div>}
              </div>

              <div className="col-md-8">
                <label className="form-label">Notes</label>
                <textarea
                  className="form-control"
                  rows={2}
                  value={form.notes}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
                  placeholder="Additional notes about this assignment..."
                />
              </div>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="d-flex gap-2">
          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2"></span>
                Creating Assignment...
              </>
            ) : (
              <>
                <i className="bi bi-check-lg me-2"></i>
                Create Assignment
              </>
            )}
          </button>
          <button
            type="button"
            className="btn btn-outline-secondary"
            onClick={() => router.back()}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}

export default function NewAssignmentPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading...</p>
        </div>
      </div>
    }>
      <NewAssignmentPageContent />
    </Suspense>
  );
}
