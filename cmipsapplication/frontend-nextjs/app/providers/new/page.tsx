'use client';

import React, { Suspense, useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useSearchParams } from 'next/navigation';
import apiClient from '@/lib/api';

type EnrollmentStep = {
  id: string;
  title: string;
  description: string;
  status: 'pending' | 'in_progress' | 'completed' | 'skipped';
  required: boolean;
  completedDate?: string;
  notes?: string;
};

const ENROLLMENT_STEPS: EnrollmentStep[] = [
  {
    id: 'soc426',
    title: 'SOC 426 - Provider Enrollment Agreement',
    description: 'Provider must complete and sign the SOC 426 form to begin enrollment.',
    status: 'pending',
    required: true
  },
  {
    id: 'orientation',
    title: 'Provider Orientation',
    description: 'Provider must complete orientation training covering IHSS program rules.',
    status: 'pending',
    required: true
  },
  {
    id: 'background_check',
    title: 'DOJ Background Check',
    description: 'Submit fingerprints for DOJ background check and await clearance.',
    status: 'pending',
    required: true
  },
  {
    id: 'soc846',
    title: 'SOC 846 - Provider Agreement',
    description: 'Provider must sign the SOC 846 acknowledgment form.',
    status: 'pending',
    required: true
  },
  {
    id: 'workweek',
    title: 'Workweek Agreement',
    description: 'Provider must establish workweek agreement defining overtime calculations.',
    status: 'pending',
    required: true
  },
  {
    id: 'direct_deposit',
    title: 'Direct Deposit Setup (Optional)',
    description: 'Provider may set up direct deposit for payment.',
    status: 'pending',
    required: false
  }
];

type ProviderForm = {
  // Personal Information
  firstName: string;
  lastName: string;
  middleName: string;
  dateOfBirth: string;
  ssn: string;
  gender: string;

  // Contact Information
  residenceAddress: string;
  residenceCity: string;
  residenceState: string;
  residenceZip: string;
  phoneNumber: string;
  email: string;

  // Enrollment Information
  dojCountyCode: string;
  enrollmentType: string; // NEW, RE_ENROLLMENT, REINSTATEMENT

  // Related Recipient (if any)
  recipientId: string;
};

function ProviderEnrollmentPageContent() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();

  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [activeStep, setActiveStep] = useState(0);
  const [enrollmentSteps, setEnrollmentSteps] = useState<EnrollmentStep[]>(ENROLLMENT_STEPS);
  const [providerId, setProviderId] = useState<number | null>(null);
  const [recipients, setRecipients] = useState<any[]>([]);

  const [providerForm, setProviderForm] = useState<ProviderForm>({
    firstName: '',
    lastName: '',
    middleName: '',
    dateOfBirth: '',
    ssn: '',
    gender: '',
    residenceAddress: '',
    residenceCity: '',
    residenceState: 'CA',
    residenceZip: '',
    phoneNumber: '',
    email: '',
    dojCountyCode: '',
    enrollmentType: 'NEW',
    recipientId: searchParams.get('recipientId') || ''
  });

  const [stepData, setStepData] = useState<{[key: string]: any}>({
    soc426: { signedDate: '', witnessName: '' },
    orientation: { completedDate: '', location: '' },
    background_check: { submissionDate: '', agencyCode: '' },
    soc846: { signedDate: '' },
    workweek: { startDay: 'SUNDAY', signedDate: '' },
    direct_deposit: { bankName: '', routingNumber: '', accountNumber: '' }
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
      const response = await apiClient.get('/recipients?personType=RECIPIENT');
      setRecipients(response.data || []);
    } catch (err) {
      console.error('Error fetching recipients:', err);
    }
  };

  const handleCreateProvider = async () => {
    try {
      setLoading(true);
      const response = await apiClient.post('/providers/enroll', providerForm);
      setProviderId(response.data.id);
      updateStepStatus('soc426', 'in_progress');
      setActiveStep(1);
    } catch (err) {
      console.error('Error creating provider:', err);
      alert('Failed to initiate provider enrollment');
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteStep = async (stepId: string) => {
    try {
      setLoading(true);
      await apiClient.put(`/providers/${providerId}/enrollment/${stepId}`, stepData[stepId]);
      updateStepStatus(stepId, 'completed');

      // Move to next step
      const currentIndex = enrollmentSteps.findIndex(s => s.id === stepId);
      if (currentIndex < enrollmentSteps.length - 1) {
        const nextStep = enrollmentSteps[currentIndex + 1];
        updateStepStatus(nextStep.id, 'in_progress');
        setActiveStep(currentIndex + 2);
      }
    } catch (err) {
      console.error('Error completing step:', err);
      alert('Failed to complete enrollment step');
    } finally {
      setLoading(false);
    }
  };

  const handleSkipStep = (stepId: string) => {
    const step = enrollmentSteps.find(s => s.id === stepId);
    if (step?.required) {
      alert('This step is required and cannot be skipped');
      return;
    }
    updateStepStatus(stepId, 'skipped');
    const currentIndex = enrollmentSteps.findIndex(s => s.id === stepId);
    if (currentIndex < enrollmentSteps.length - 1) {
      const nextStep = enrollmentSteps[currentIndex + 1];
      updateStepStatus(nextStep.id, 'in_progress');
      setActiveStep(currentIndex + 2);
    }
  };

  const updateStepStatus = (stepId: string, status: EnrollmentStep['status']) => {
    setEnrollmentSteps(prev => prev.map(step =>
      step.id === stepId ? { ...step, status } : step
    ));
  };

  const handleFinalizeEnrollment = async () => {
    try {
      setLoading(true);
      await apiClient.put(`/providers/${providerId}/finalize-enrollment`);
      alert('Provider enrollment completed successfully!');
      router.push(`/providers/${providerId}`);
    } catch (err) {
      console.error('Error finalizing enrollment:', err);
      alert('Failed to finalize enrollment');
    } finally {
      setLoading(false);
    }
  };

  const getStepIcon = (status: string) => {
    switch (status) {
      case 'completed': return <i className="bi bi-check-circle-fill text-success"></i>;
      case 'in_progress': return <i className="bi bi-circle-fill text-primary"></i>;
      case 'skipped': return <i className="bi bi-dash-circle-fill text-secondary"></i>;
      default: return <i className="bi bi-circle text-secondary"></i>;
    }
  };

  const allRequiredStepsCompleted = enrollmentSteps
    .filter(s => s.required)
    .every(s => s.status === 'completed');

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
          <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.push('/providers')}>
            <i className="bi bi-arrow-left me-2"></i>Back to Providers
          </button>
          <h1 className="h3 mb-0">Provider Enrollment</h1>
          <p className="text-muted mb-0">Complete all required steps to enroll a new provider</p>
        </div>
      </div>

      {/* Business Rules Info */}
      <div className="card mb-4">
        <div className="card-header bg-info text-white">
          <h5 className="mb-0"><i className="bi bi-info-circle me-2"></i>Enrollment Process (BR PM 1-22)</h5>
        </div>
        <div className="card-body">
          <div className="row">
            <div className="col-md-3">
              <h6>Requirements</h6>
              <ul className="small mb-0">
                <li>Must be 18 years or older</li>
                <li>Valid SSN required</li>
                <li>California resident</li>
                <li>Pass DOJ background check</li>
              </ul>
            </div>
            <div className="col-md-3">
              <h6>Forms Required</h6>
              <ul className="small mb-0">
                <li>SOC 426 - Enrollment Agreement</li>
                <li>SOC 846 - Provider Agreement</li>
                <li>Workweek Agreement</li>
                <li>W-4 Tax Form</li>
              </ul>
            </div>
            <div className="col-md-3">
              <h6>Background Check</h6>
              <ul className="small mb-0">
                <li>Fingerprints required</li>
                <li>DOJ/FBI check performed</li>
                <li>Tier 1 = Ineligible</li>
                <li>Tier 2 = Conditional</li>
              </ul>
            </div>
            <div className="col-md-3">
              <h6>Timeline</h6>
              <ul className="small mb-0">
                <li>Orientation within 30 days</li>
                <li>Background ~2-4 weeks</li>
                <li>Complete within 60 days</li>
                <li>Can work pending clearance*</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div className="row">
        {/* Progress Sidebar */}
        <div className="col-md-3">
          <div className="card">
            <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
              <h5 className="mb-0">Enrollment Progress</h5>
            </div>
            <div className="card-body p-0">
              <div className="list-group list-group-flush">
                <div
                  className={`list-group-item ${activeStep === 0 ? 'active' : ''}`}
                  style={{ cursor: 'pointer' }}
                  onClick={() => !providerId && setActiveStep(0)}
                >
                  {providerId ? (
                    <i className="bi bi-check-circle-fill text-success me-2"></i>
                  ) : (
                    <i className="bi bi-circle-fill text-primary me-2"></i>
                  )}
                  Provider Information
                </div>
                {enrollmentSteps.map((step, index) => (
                  <div
                    key={step.id}
                    className={`list-group-item ${activeStep === index + 1 ? 'active' : ''}`}
                    style={{ cursor: providerId ? 'pointer' : 'not-allowed', opacity: providerId ? 1 : 0.5 }}
                    onClick={() => providerId && setActiveStep(index + 1)}
                  >
                    {getStepIcon(step.status)}
                    <span className="ms-2">
                      {step.title}
                      {step.required && <span className="text-danger">*</span>}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Main Content */}
        <div className="col-md-9">
          {/* Step 0: Provider Information */}
          {activeStep === 0 && !providerId && (
            <div className="card">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0">Provider Information</h5>
              </div>
              <div className="card-body">
                <div className="row g-3">
                  <div className="col-md-4">
                    <label className="form-label">First Name <span className="text-danger">*</span></label>
                    <input
                      type="text"
                      className="form-control"
                      value={providerForm.firstName}
                      onChange={(e) => setProviderForm({ ...providerForm, firstName: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Middle Name</label>
                    <input
                      type="text"
                      className="form-control"
                      value={providerForm.middleName}
                      onChange={(e) => setProviderForm({ ...providerForm, middleName: e.target.value })}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Last Name <span className="text-danger">*</span></label>
                    <input
                      type="text"
                      className="form-control"
                      value={providerForm.lastName}
                      onChange={(e) => setProviderForm({ ...providerForm, lastName: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Date of Birth <span className="text-danger">*</span></label>
                    <input
                      type="date"
                      className="form-control"
                      value={providerForm.dateOfBirth}
                      onChange={(e) => setProviderForm({ ...providerForm, dateOfBirth: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">SSN <span className="text-danger">*</span></label>
                    <input
                      type="text"
                      className="form-control"
                      value={providerForm.ssn}
                      onChange={(e) => setProviderForm({ ...providerForm, ssn: e.target.value })}
                      placeholder="XXX-XX-XXXX"
                      maxLength={11}
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Gender</label>
                    <select
                      className="form-select"
                      value={providerForm.gender}
                      onChange={(e) => setProviderForm({ ...providerForm, gender: e.target.value })}
                    >
                      <option value="">Select...</option>
                      <option value="M">Male</option>
                      <option value="F">Female</option>
                      <option value="X">Non-Binary</option>
                    </select>
                  </div>
                </div>

                <hr className="my-4" />
                <h6>Contact Information</h6>

                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Street Address <span className="text-danger">*</span></label>
                    <input
                      type="text"
                      className="form-control"
                      value={providerForm.residenceAddress}
                      onChange={(e) => setProviderForm({ ...providerForm, residenceAddress: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label">City <span className="text-danger">*</span></label>
                    <input
                      type="text"
                      className="form-control"
                      value={providerForm.residenceCity}
                      onChange={(e) => setProviderForm({ ...providerForm, residenceCity: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-1">
                    <label className="form-label">State</label>
                    <input
                      type="text"
                      className="form-control"
                      value={providerForm.residenceState}
                      disabled
                    />
                  </div>
                  <div className="col-md-2">
                    <label className="form-label">ZIP <span className="text-danger">*</span></label>
                    <input
                      type="text"
                      className="form-control"
                      value={providerForm.residenceZip}
                      onChange={(e) => setProviderForm({ ...providerForm, residenceZip: e.target.value })}
                      maxLength={10}
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Phone Number <span className="text-danger">*</span></label>
                    <input
                      type="tel"
                      className="form-control"
                      value={providerForm.phoneNumber}
                      onChange={(e) => setProviderForm({ ...providerForm, phoneNumber: e.target.value })}
                      placeholder="(XXX) XXX-XXXX"
                      required
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Email</label>
                    <input
                      type="email"
                      className="form-control"
                      value={providerForm.email}
                      onChange={(e) => setProviderForm({ ...providerForm, email: e.target.value })}
                    />
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">DOJ County Code <span className="text-danger">*</span></label>
                    <input
                      type="text"
                      className="form-control"
                      value={providerForm.dojCountyCode}
                      onChange={(e) => setProviderForm({ ...providerForm, dojCountyCode: e.target.value })}
                      placeholder="e.g., 19 for Los Angeles"
                      required
                    />
                  </div>
                </div>

                <hr className="my-4" />
                <h6>Enrollment Type</h6>

                <div className="row g-3">
                  <div className="col-md-4">
                    <label className="form-label">Enrollment Type <span className="text-danger">*</span></label>
                    <select
                      className="form-select"
                      value={providerForm.enrollmentType}
                      onChange={(e) => setProviderForm({ ...providerForm, enrollmentType: e.target.value })}
                    >
                      <option value="NEW">New Enrollment</option>
                      <option value="RE_ENROLLMENT">Re-Enrollment</option>
                      <option value="REINSTATEMENT">Reinstatement</option>
                    </select>
                  </div>
                  <div className="col-md-8">
                    <label className="form-label">Link to Recipient (Optional)</label>
                    <select
                      className="form-select"
                      value={providerForm.recipientId}
                      onChange={(e) => setProviderForm({ ...providerForm, recipientId: e.target.value })}
                    >
                      <option value="">No specific recipient</option>
                      {recipients.map(r => (
                        <option key={r.id} value={r.id}>
                          {r.lastName}, {r.firstName} (CIN: {r.cin})
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="mt-4">
                  <button
                    className="btn btn-primary"
                    onClick={handleCreateProvider}
                    disabled={loading || !providerForm.firstName || !providerForm.lastName ||
                             !providerForm.dateOfBirth || !providerForm.ssn || !providerForm.residenceAddress ||
                             !providerForm.residenceCity || !providerForm.residenceZip || !providerForm.phoneNumber ||
                             !providerForm.dojCountyCode}
                  >
                    {loading ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2"></span>
                        Processing...
                      </>
                    ) : (
                      <>
                        <i className="bi bi-arrow-right me-2"></i>
                        Begin Enrollment Process
                      </>
                    )}
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Provider Info Summary (after creation) */}
          {activeStep === 0 && providerId && (
            <div className="card">
              <div className="card-header bg-success text-white">
                <h5 className="mb-0"><i className="bi bi-check-circle me-2"></i>Provider Record Created</h5>
              </div>
              <div className="card-body">
                <p>Provider ID: <strong>{providerId}</strong></p>
                <p>Name: <strong>{providerForm.firstName} {providerForm.lastName}</strong></p>
                <button className="btn btn-primary" onClick={() => setActiveStep(1)}>
                  Continue to SOC 426 <i className="bi bi-arrow-right ms-2"></i>
                </button>
              </div>
            </div>
          )}

          {/* Enrollment Steps */}
          {activeStep > 0 && providerId && enrollmentSteps.map((step, index) => (
            activeStep === index + 1 && (
              <div key={step.id} className="card">
                <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                  <h5 className="mb-0">
                    Step {index + 1}: {step.title}
                    {step.required && <span className="badge bg-danger ms-2">Required</span>}
                  </h5>
                </div>
                <div className="card-body">
                  <p className="text-muted">{step.description}</p>

                  {step.status === 'completed' ? (
                    <div className="alert alert-success">
                      <i className="bi bi-check-circle me-2"></i>
                      This step has been completed.
                    </div>
                  ) : (
                    <>
                      {/* SOC 426 Form */}
                      {step.id === 'soc426' && (
                        <div className="row g-3">
                          <div className="col-md-6">
                            <label className="form-label">Date Signed</label>
                            <input
                              type="date"
                              className="form-control"
                              value={stepData.soc426.signedDate}
                              onChange={(e) => setStepData({
                                ...stepData,
                                soc426: { ...stepData.soc426, signedDate: e.target.value }
                              })}
                              max={new Date().toISOString().split('T')[0]}
                            />
                          </div>
                          <div className="col-md-6">
                            <label className="form-label">Witness Name</label>
                            <input
                              type="text"
                              className="form-control"
                              value={stepData.soc426.witnessName}
                              onChange={(e) => setStepData({
                                ...stepData,
                                soc426: { ...stepData.soc426, witnessName: e.target.value }
                              })}
                            />
                          </div>
                        </div>
                      )}

                      {/* Orientation */}
                      {step.id === 'orientation' && (
                        <div className="row g-3">
                          <div className="col-md-6">
                            <label className="form-label">Date Completed</label>
                            <input
                              type="date"
                              className="form-control"
                              value={stepData.orientation.completedDate}
                              onChange={(e) => setStepData({
                                ...stepData,
                                orientation: { ...stepData.orientation, completedDate: e.target.value }
                              })}
                              max={new Date().toISOString().split('T')[0]}
                            />
                          </div>
                          <div className="col-md-6">
                            <label className="form-label">Location</label>
                            <input
                              type="text"
                              className="form-control"
                              value={stepData.orientation.location}
                              onChange={(e) => setStepData({
                                ...stepData,
                                orientation: { ...stepData.orientation, location: e.target.value }
                              })}
                            />
                          </div>
                        </div>
                      )}

                      {/* Background Check */}
                      {step.id === 'background_check' && (
                        <div className="row g-3">
                          <div className="col-md-6">
                            <label className="form-label">Fingerprint Submission Date</label>
                            <input
                              type="date"
                              className="form-control"
                              value={stepData.background_check.submissionDate}
                              onChange={(e) => setStepData({
                                ...stepData,
                                background_check: { ...stepData.background_check, submissionDate: e.target.value }
                              })}
                              max={new Date().toISOString().split('T')[0]}
                            />
                          </div>
                          <div className="col-md-6">
                            <label className="form-label">Agency Code</label>
                            <input
                              type="text"
                              className="form-control"
                              value={stepData.background_check.agencyCode}
                              onChange={(e) => setStepData({
                                ...stepData,
                                background_check: { ...stepData.background_check, agencyCode: e.target.value }
                              })}
                            />
                          </div>
                          <div className="col-12">
                            <div className="alert alert-warning">
                              <i className="bi bi-exclamation-triangle me-2"></i>
                              <strong>Note:</strong> Background check results typically take 2-4 weeks.
                              Provider may begin working pending clearance if all other requirements are met.
                            </div>
                          </div>
                        </div>
                      )}

                      {/* SOC 846 */}
                      {step.id === 'soc846' && (
                        <div className="row g-3">
                          <div className="col-md-6">
                            <label className="form-label">Date Signed</label>
                            <input
                              type="date"
                              className="form-control"
                              value={stepData.soc846.signedDate}
                              onChange={(e) => setStepData({
                                ...stepData,
                                soc846: { ...stepData.soc846, signedDate: e.target.value }
                              })}
                              max={new Date().toISOString().split('T')[0]}
                            />
                          </div>
                        </div>
                      )}

                      {/* Workweek Agreement */}
                      {step.id === 'workweek' && (
                        <div className="row g-3">
                          <div className="col-md-6">
                            <label className="form-label">Workweek Start Day</label>
                            <select
                              className="form-select"
                              value={stepData.workweek.startDay}
                              onChange={(e) => setStepData({
                                ...stepData,
                                workweek: { ...stepData.workweek, startDay: e.target.value }
                              })}
                            >
                              <option value="SUNDAY">Sunday</option>
                              <option value="MONDAY">Monday</option>
                              <option value="TUESDAY">Tuesday</option>
                              <option value="WEDNESDAY">Wednesday</option>
                              <option value="THURSDAY">Thursday</option>
                              <option value="FRIDAY">Friday</option>
                              <option value="SATURDAY">Saturday</option>
                            </select>
                          </div>
                          <div className="col-md-6">
                            <label className="form-label">Date Signed</label>
                            <input
                              type="date"
                              className="form-control"
                              value={stepData.workweek.signedDate}
                              onChange={(e) => setStepData({
                                ...stepData,
                                workweek: { ...stepData.workweek, signedDate: e.target.value }
                              })}
                              max={new Date().toISOString().split('T')[0]}
                            />
                          </div>
                        </div>
                      )}

                      {/* Direct Deposit */}
                      {step.id === 'direct_deposit' && (
                        <div className="row g-3">
                          <div className="col-md-4">
                            <label className="form-label">Bank Name</label>
                            <input
                              type="text"
                              className="form-control"
                              value={stepData.direct_deposit.bankName}
                              onChange={(e) => setStepData({
                                ...stepData,
                                direct_deposit: { ...stepData.direct_deposit, bankName: e.target.value }
                              })}
                            />
                          </div>
                          <div className="col-md-4">
                            <label className="form-label">Routing Number</label>
                            <input
                              type="text"
                              className="form-control"
                              value={stepData.direct_deposit.routingNumber}
                              onChange={(e) => setStepData({
                                ...stepData,
                                direct_deposit: { ...stepData.direct_deposit, routingNumber: e.target.value }
                              })}
                              maxLength={9}
                            />
                          </div>
                          <div className="col-md-4">
                            <label className="form-label">Account Number</label>
                            <input
                              type="text"
                              className="form-control"
                              value={stepData.direct_deposit.accountNumber}
                              onChange={(e) => setStepData({
                                ...stepData,
                                direct_deposit: { ...stepData.direct_deposit, accountNumber: e.target.value }
                              })}
                            />
                          </div>
                        </div>
                      )}

                      <div className="mt-4 d-flex gap-2">
                        <button
                          className="btn btn-success"
                          onClick={() => handleCompleteStep(step.id)}
                          disabled={loading}
                        >
                          {loading ? 'Processing...' : 'Complete Step'}
                        </button>
                        {!step.required && (
                          <button
                            className="btn btn-outline-secondary"
                            onClick={() => handleSkipStep(step.id)}
                            disabled={loading}
                          >
                            Skip This Step
                          </button>
                        )}
                      </div>
                    </>
                  )}
                </div>
              </div>
            )
          ))}

          {/* Finalize Enrollment */}
          {providerId && allRequiredStepsCompleted && (
            <div className="card mt-4 border-success">
              <div className="card-header bg-success text-white">
                <h5 className="mb-0"><i className="bi bi-check-circle me-2"></i>Ready to Finalize Enrollment</h5>
              </div>
              <div className="card-body">
                <p>All required steps have been completed. You can now finalize the provider enrollment.</p>
                <button
                  className="btn btn-success btn-lg"
                  onClick={handleFinalizeEnrollment}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2"></span>
                      Finalizing...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-check-lg me-2"></i>
                      Finalize Provider Enrollment
                    </>
                  )}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default function ProviderEnrollmentPage() {
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
      <ProviderEnrollmentPageContent />
    </Suspense>
  );
}
