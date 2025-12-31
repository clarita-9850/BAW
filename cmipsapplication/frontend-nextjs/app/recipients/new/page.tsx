'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/api';

export default function NewReferralPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    // Personal Information
    firstName: '',
    lastName: '',
    middleName: '',
    dateOfBirth: '',
    gender: '',
    ssn: '',
    // Contact Information
    residenceAddress: '',
    residenceCity: '',
    residenceState: 'CA',
    residenceZip: '',
    mailingAddress: '',
    mailingCity: '',
    mailingState: 'CA',
    mailingZip: '',
    phoneNumber: '',
    alternatePhone: '',
    email: '',
    // Location
    countyCode: '',
    // Language & Accessibility
    primaryLanguage: 'ENGLISH',
    secondaryLanguage: '',
    interpreterNeeded: false,
    largeFont: false,
    highContrast: false,
    screenReader: false
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [sameAsResidence, setSameAsResidence] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
  }, [user, authLoading]);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.firstName.trim()) {
      newErrors.firstName = 'First name is required';
    }
    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Last name is required';
    }
    if (!formData.dateOfBirth) {
      newErrors.dateOfBirth = 'Date of birth is required';
    }
    if (!formData.countyCode) {
      newErrors.countyCode = 'County code is required';
    }
    if (formData.ssn && !/^\d{3}-?\d{2}-?\d{4}$/.test(formData.ssn)) {
      newErrors.ssn = 'Invalid SSN format';
    }
    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Invalid email format';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) return;

    try {
      setLoading(true);

      // Prepare mailing address
      const mailingData = sameAsResidence ? {
        mailingAddress: formData.residenceAddress,
        mailingCity: formData.residenceCity,
        mailingState: formData.residenceState,
        mailingZip: formData.residenceZip
      } : {
        mailingAddress: formData.mailingAddress,
        mailingCity: formData.mailingCity,
        mailingState: formData.mailingState,
        mailingZip: formData.mailingZip
      };

      const response = await apiClient.post('/recipients/referrals', {
        ...formData,
        ...mailingData,
        personType: 'OPEN_REFERRAL'
      });

      router.push(`/recipients/${response.data.id}`);
    } catch (err: any) {
      console.error('Error creating referral:', err);
      alert(err.response?.data?.message || 'Failed to create referral');
    } finally {
      setLoading(false);
    }
  };

  const handleSameAsResidence = (checked: boolean) => {
    setSameAsResidence(checked);
    if (checked) {
      setFormData(prev => ({
        ...prev,
        mailingAddress: prev.residenceAddress,
        mailingCity: prev.residenceCity,
        mailingState: prev.residenceState,
        mailingZip: prev.residenceZip
      }));
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
        <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.push('/recipients')}>
          <i className="bi bi-arrow-left me-2"></i>Back to Person Search
        </button>
        <h1 className="h3 mb-0">Create New Referral</h1>
        <p className="text-muted">Enter the person&apos;s information to create a new referral.</p>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="row">
          <div className="col-lg-8">
            {/* Personal Information */}
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Personal Information</h5>
              </div>
              <div className="card-body">
                <div className="row g-3">
                  <div className="col-md-4">
                    <label className="form-label">First Name *</label>
                    <input
                      type="text"
                      className={`form-control ${errors.firstName ? 'is-invalid' : ''}`}
                      value={formData.firstName}
                      onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    />
                    {errors.firstName && <div className="invalid-feedback">{errors.firstName}</div>}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Middle Name</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.middleName}
                      onChange={(e) => setFormData({ ...formData, middleName: e.target.value })}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Last Name *</label>
                    <input
                      type="text"
                      className={`form-control ${errors.lastName ? 'is-invalid' : ''}`}
                      value={formData.lastName}
                      onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    />
                    {errors.lastName && <div className="invalid-feedback">{errors.lastName}</div>}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Date of Birth *</label>
                    <input
                      type="date"
                      className={`form-control ${errors.dateOfBirth ? 'is-invalid' : ''}`}
                      value={formData.dateOfBirth}
                      onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                    />
                    {errors.dateOfBirth && <div className="invalid-feedback">{errors.dateOfBirth}</div>}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Gender</label>
                    <select
                      className="form-select"
                      value={formData.gender}
                      onChange={(e) => setFormData({ ...formData, gender: e.target.value })}
                    >
                      <option value="">Select Gender</option>
                      <option value="MALE">Male</option>
                      <option value="FEMALE">Female</option>
                      <option value="OTHER">Other</option>
                      <option value="UNKNOWN">Prefer not to say</option>
                    </select>
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">SSN</label>
                    <input
                      type="text"
                      className={`form-control ${errors.ssn ? 'is-invalid' : ''}`}
                      value={formData.ssn}
                      onChange={(e) => setFormData({ ...formData, ssn: e.target.value })}
                      placeholder="XXX-XX-XXXX"
                      maxLength={11}
                    />
                    {errors.ssn && <div className="invalid-feedback">{errors.ssn}</div>}
                    <small className="text-muted">Optional - will be verified later</small>
                  </div>
                </div>
              </div>
            </div>

            {/* Residence Address */}
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Residence Address</h5>
              </div>
              <div className="card-body">
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Street Address</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.residenceAddress}
                      onChange={(e) => setFormData({ ...formData, residenceAddress: e.target.value })}
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">City</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.residenceCity}
                      onChange={(e) => setFormData({ ...formData, residenceCity: e.target.value })}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">State</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.residenceState}
                      onChange={(e) => setFormData({ ...formData, residenceState: e.target.value })}
                      maxLength={2}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">ZIP Code</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formData.residenceZip}
                      onChange={(e) => setFormData({ ...formData, residenceZip: e.target.value })}
                      maxLength={10}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">County Code *</label>
                    <input
                      type="text"
                      className={`form-control ${errors.countyCode ? 'is-invalid' : ''}`}
                      value={formData.countyCode}
                      onChange={(e) => setFormData({ ...formData, countyCode: e.target.value })}
                      placeholder="e.g., 19"
                      maxLength={2}
                    />
                    {errors.countyCode && <div className="invalid-feedback">{errors.countyCode}</div>}
                  </div>
                </div>
              </div>
            </div>

            {/* Mailing Address */}
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Mailing Address</h5>
              </div>
              <div className="card-body">
                <div className="form-check mb-3">
                  <input
                    type="checkbox"
                    className="form-check-input"
                    id="sameAsResidence"
                    checked={sameAsResidence}
                    onChange={(e) => handleSameAsResidence(e.target.checked)}
                  />
                  <label className="form-check-label" htmlFor="sameAsResidence">
                    Same as residence address
                  </label>
                </div>
                {!sameAsResidence && (
                  <div className="row g-3">
                    <div className="col-md-6">
                      <label className="form-label">Street Address</label>
                      <input
                        type="text"
                        className="form-control"
                        value={formData.mailingAddress}
                        onChange={(e) => setFormData({ ...formData, mailingAddress: e.target.value })}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label">City</label>
                      <input
                        type="text"
                        className="form-control"
                        value={formData.mailingCity}
                        onChange={(e) => setFormData({ ...formData, mailingCity: e.target.value })}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label">State</label>
                      <input
                        type="text"
                        className="form-control"
                        value={formData.mailingState}
                        onChange={(e) => setFormData({ ...formData, mailingState: e.target.value })}
                        maxLength={2}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label">ZIP Code</label>
                      <input
                        type="text"
                        className="form-control"
                        value={formData.mailingZip}
                        onChange={(e) => setFormData({ ...formData, mailingZip: e.target.value })}
                        maxLength={10}
                      />
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* Contact Information */}
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Contact Information</h5>
              </div>
              <div className="card-body">
                <div className="row g-3">
                  <div className="col-md-4">
                    <label className="form-label">Primary Phone</label>
                    <input
                      type="tel"
                      className="form-control"
                      value={formData.phoneNumber}
                      onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                      placeholder="(XXX) XXX-XXXX"
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Alternate Phone</label>
                    <input
                      type="tel"
                      className="form-control"
                      value={formData.alternatePhone}
                      onChange={(e) => setFormData({ ...formData, alternatePhone: e.target.value })}
                      placeholder="(XXX) XXX-XXXX"
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Email</label>
                    <input
                      type="email"
                      className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    />
                    {errors.email && <div className="invalid-feedback">{errors.email}</div>}
                  </div>
                </div>
              </div>
            </div>

            {/* Language & Accessibility */}
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Language &amp; Accessibility</h5>
              </div>
              <div className="card-body">
                <div className="row g-3">
                  <div className="col-md-4">
                    <label className="form-label">Primary Language</label>
                    <select
                      className="form-select"
                      value={formData.primaryLanguage}
                      onChange={(e) => setFormData({ ...formData, primaryLanguage: e.target.value })}
                    >
                      <option value="ENGLISH">English</option>
                      <option value="SPANISH">Spanish</option>
                      <option value="CHINESE">Chinese</option>
                      <option value="VIETNAMESE">Vietnamese</option>
                      <option value="TAGALOG">Tagalog</option>
                      <option value="KOREAN">Korean</option>
                      <option value="ARMENIAN">Armenian</option>
                      <option value="PERSIAN">Persian</option>
                      <option value="RUSSIAN">Russian</option>
                      <option value="JAPANESE">Japanese</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Secondary Language</label>
                    <select
                      className="form-select"
                      value={formData.secondaryLanguage}
                      onChange={(e) => setFormData({ ...formData, secondaryLanguage: e.target.value })}
                    >
                      <option value="">None</option>
                      <option value="ENGLISH">English</option>
                      <option value="SPANISH">Spanish</option>
                      <option value="CHINESE">Chinese</option>
                      <option value="VIETNAMESE">Vietnamese</option>
                      <option value="TAGALOG">Tagalog</option>
                      <option value="KOREAN">Korean</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </div>
                  <div className="col-md-4">
                    <label className="form-label d-block">Interpreter Needed</label>
                    <div className="form-check form-check-inline">
                      <input
                        type="checkbox"
                        className="form-check-input"
                        id="interpreterNeeded"
                        checked={formData.interpreterNeeded}
                        onChange={(e) => setFormData({ ...formData, interpreterNeeded: e.target.checked })}
                      />
                      <label className="form-check-label" htmlFor="interpreterNeeded">Yes</label>
                    </div>
                  </div>
                </div>
                <hr />
                <h6>Accessibility Options</h6>
                <div className="row g-3">
                  <div className="col-md-4">
                    <div className="form-check">
                      <input
                        type="checkbox"
                        className="form-check-input"
                        id="largeFont"
                        checked={formData.largeFont}
                        onChange={(e) => setFormData({ ...formData, largeFont: e.target.checked })}
                      />
                      <label className="form-check-label" htmlFor="largeFont">Large Font</label>
                    </div>
                  </div>
                  <div className="col-md-4">
                    <div className="form-check">
                      <input
                        type="checkbox"
                        className="form-check-input"
                        id="highContrast"
                        checked={formData.highContrast}
                        onChange={(e) => setFormData({ ...formData, highContrast: e.target.checked })}
                      />
                      <label className="form-check-label" htmlFor="highContrast">High Contrast</label>
                    </div>
                  </div>
                  <div className="col-md-4">
                    <div className="form-check">
                      <input
                        type="checkbox"
                        className="form-check-input"
                        id="screenReader"
                        checked={formData.screenReader}
                        onChange={(e) => setFormData({ ...formData, screenReader: e.target.checked })}
                      />
                      <label className="form-check-label" htmlFor="screenReader">Screen Reader</label>
                    </div>
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
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    Creating...
                  </>
                ) : (
                  <>
                    <i className="bi bi-plus-lg me-2"></i>Create Referral
                  </>
                )}
              </button>
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => router.push('/recipients')}
              >
                Cancel
              </button>
            </div>
          </div>

          {/* Help Panel */}
          <div className="col-lg-4">
            <div className="card position-sticky" style={{ top: '1rem' }}>
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Help</h5>
              </div>
              <div className="card-body">
                <h6>Creating a Referral</h6>
                <p className="small">
                  A referral is the first step in the IHSS application process. Enter the person&apos;s basic information
                  to create an Open Referral.
                </p>

                <h6>Required Fields</h6>
                <ul className="small">
                  <li>First Name</li>
                  <li>Last Name</li>
                  <li>Date of Birth</li>
                  <li>County Code</li>
                </ul>

                <h6>What Happens Next?</h6>
                <ol className="small mb-0">
                  <li>The referral is created with status &quot;Open Referral&quot;</li>
                  <li>When a case is created, status changes to &quot;Applicant&quot;</li>
                  <li>When case is approved, status changes to &quot;Recipient&quot;</li>
                </ol>
              </div>
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}
