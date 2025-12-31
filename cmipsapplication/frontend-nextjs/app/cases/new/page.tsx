'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/api';

type Recipient = {
  id: number;
  firstName: string;
  lastName: string;
  cin: string;
  personType: string;
  countyCode: string;
};

export default function NewCasePage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedRecipient, setSelectedRecipient] = useState<Recipient | null>(null);
  const [formData, setFormData] = useState({
    recipientId: '',
    caseOwnerId: '',
    countyCode: '',
    caseType: 'IHSS'
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    // Set default case owner to current user
    setFormData(prev => ({
      ...prev,
      caseOwnerId: user.username || ''
    }));
  }, [user, authLoading]);

  const searchRecipients = async () => {
    if (!searchTerm || searchTerm.length < 2) return;

    try {
      setLoading(true);
      const response = await apiClient.get(`/recipients/search?lastName=${searchTerm}`);
      setRecipients(response.data || []);
    } catch (err) {
      console.error('Error searching recipients:', err);
      setRecipients([]);
    } finally {
      setLoading(false);
    }
  };

  const handleRecipientSelect = (recipient: Recipient) => {
    setSelectedRecipient(recipient);
    setFormData(prev => ({
      ...prev,
      recipientId: recipient.id.toString(),
      countyCode: recipient.countyCode || prev.countyCode
    }));
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.recipientId) {
      newErrors.recipientId = 'Please select a recipient';
    }
    if (!formData.countyCode) {
      newErrors.countyCode = 'County code is required';
    }
    if (!formData.caseType) {
      newErrors.caseType = 'Case type is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) return;

    try {
      setLoading(true);
      const response = await apiClient.post('/cases', {
        recipientId: parseInt(formData.recipientId),
        caseOwnerId: formData.caseOwnerId || null,
        countyCode: formData.countyCode
      });

      router.push(`/cases/${response.data.id}`);
    } catch (err: any) {
      console.error('Error creating case:', err);
      alert(err.response?.data?.message || 'Failed to create case');
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
      <div className="mb-4">
        <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.push('/cases')}>
          <i className="bi bi-arrow-left me-2"></i>Back to Cases
        </button>
        <h1 className="h3 mb-0">Create New Case</h1>
      </div>

      <div className="row">
        <div className="col-lg-8">
          <form onSubmit={handleSubmit}>
            {/* Recipient Search */}
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Step 1: Select Recipient</h5>
              </div>
              <div className="card-body">
                {!selectedRecipient ? (
                  <>
                    <div className="mb-3">
                      <label className="form-label">Search Recipient by Last Name</label>
                      <div className="input-group">
                        <input
                          type="text"
                          className="form-control"
                          value={searchTerm}
                          onChange={(e) => setSearchTerm(e.target.value)}
                          placeholder="Enter last name (min 2 characters)"
                          onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), searchRecipients())}
                        />
                        <button
                          type="button"
                          className="btn btn-primary"
                          onClick={searchRecipients}
                          disabled={searchTerm.length < 2}
                        >
                          <i className="bi bi-search me-2"></i>Search
                        </button>
                      </div>
                    </div>

                    {recipients.length > 0 && (
                      <div className="table-responsive">
                        <table className="table table-striped table-hover">
                          <thead>
                            <tr>
                              <th>Name</th>
                              <th>CIN</th>
                              <th>Type</th>
                              <th>County</th>
                              <th>Action</th>
                            </tr>
                          </thead>
                          <tbody>
                            {recipients.map((recipient) => (
                              <tr key={recipient.id}>
                                <td>{recipient.firstName} {recipient.lastName}</td>
                                <td>{recipient.cin || '-'}</td>
                                <td>{recipient.personType}</td>
                                <td>{recipient.countyCode || '-'}</td>
                                <td>
                                  <button
                                    type="button"
                                    className="btn btn-sm btn-primary"
                                    onClick={() => handleRecipientSelect(recipient)}
                                  >
                                    Select
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}

                    {errors.recipientId && (
                      <div className="text-danger mt-2">{errors.recipientId}</div>
                    )}

                    <div className="mt-3">
                      <span className="text-muted">Can&apos;t find the recipient? </span>
                      <button
                        type="button"
                        className="btn btn-link p-0"
                        onClick={() => router.push('/recipients/new')}
                      >
                        Create new referral
                      </button>
                    </div>
                  </>
                ) : (
                  <div className="alert alert-success d-flex justify-content-between align-items-center mb-0">
                    <div>
                      <strong>Selected Recipient:</strong> {selectedRecipient.firstName} {selectedRecipient.lastName}
                      {selectedRecipient.cin && ` (CIN: ${selectedRecipient.cin})`}
                    </div>
                    <button
                      type="button"
                      className="btn btn-sm btn-outline-secondary"
                      onClick={() => {
                        setSelectedRecipient(null);
                        setFormData(prev => ({ ...prev, recipientId: '' }));
                      }}
                    >
                      Change
                    </button>
                  </div>
                )}
              </div>
            </div>

            {/* Case Details */}
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Step 2: Case Details</h5>
              </div>
              <div className="card-body">
                <div className="row">
                  <div className="col-md-6 mb-3">
                    <label className="form-label">Case Type *</label>
                    <select
                      className={`form-select ${errors.caseType ? 'is-invalid' : ''}`}
                      value={formData.caseType}
                      onChange={(e) => setFormData({ ...formData, caseType: e.target.value })}
                    >
                      <option value="IHSS">IHSS</option>
                      <option value="WPCS">WPCS</option>
                      <option value="IHSS_WPCS">IHSS/WPCS</option>
                    </select>
                    {errors.caseType && <div className="invalid-feedback">{errors.caseType}</div>}
                  </div>

                  <div className="col-md-6 mb-3">
                    <label className="form-label">County Code *</label>
                    <input
                      type="text"
                      className={`form-control ${errors.countyCode ? 'is-invalid' : ''}`}
                      value={formData.countyCode}
                      onChange={(e) => setFormData({ ...formData, countyCode: e.target.value })}
                      placeholder="e.g., 19 for Los Angeles"
                      maxLength={2}
                    />
                    {errors.countyCode && <div className="invalid-feedback">{errors.countyCode}</div>}
                  </div>

                  <div className="col-md-6 mb-3">
                    <label className="form-label">Case Owner (optional)</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.caseOwnerId}
                      onChange={(e) => setFormData({ ...formData, caseOwnerId: e.target.value })}
                      placeholder="Enter case owner ID"
                    />
                    <small className="text-muted">Leave blank to assign later</small>
                  </div>
                </div>
              </div>
            </div>

            {/* Actions */}
            <div className="d-flex gap-2">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={loading || !selectedRecipient}
              >
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    Creating...
                  </>
                ) : (
                  <>
                    <i className="bi bi-plus-lg me-2"></i>Create Case
                  </>
                )}
              </button>
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => router.push('/cases')}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>

        {/* Help Panel */}
        <div className="col-lg-4">
          <div className="card">
            <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
              <h5 className="mb-0">Help</h5>
            </div>
            <div className="card-body">
              <h6>Creating a New Case</h6>
              <ol className="small">
                <li className="mb-2">
                  <strong>Search for the recipient</strong> - Enter at least 2 characters of the recipient&apos;s last name
                </li>
                <li className="mb-2">
                  <strong>Select the recipient</strong> - Click &quot;Select&quot; next to the correct person
                </li>
                <li className="mb-2">
                  <strong>Enter case details</strong> - Choose the case type and verify the county code
                </li>
                <li className="mb-2">
                  <strong>Assign case owner (optional)</strong> - You can assign the case to a specific caseworker
                </li>
                <li>
                  <strong>Create the case</strong> - Click &quot;Create Case&quot; to finalize
                </li>
              </ol>

              <hr />

              <h6>Case Types</h6>
              <ul className="small mb-0">
                <li><strong>IHSS</strong> - In-Home Supportive Services</li>
                <li><strong>WPCS</strong> - Waiver Personal Care Services</li>
                <li><strong>IHSS/WPCS</strong> - Combined services</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
