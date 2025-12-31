'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useParams } from 'next/navigation';
import apiClient from '@/lib/api';

type Assessment = {
  id: number;
  caseId: number;
  assessmentType: string;
  assessmentStatus: string;
  assessmentDate: string;
  homeVisitDate: string;
  reassessmentDueDate: string;
  totalAssessedNeed: number;
  domesticHours: number;
  domesticHtgIndicator: string;
  personalCareHours: number;
  personalCareHtgIndicator: string;
  paramedicalHours: number;
  paramedicalHtgIndicator: string;
  protectiveSupervisionHours: number;
  protectiveSupervisionHtgIndicator: string;
  transportationHours: number;
  transportationHtgIndicator: string;
  mealPrepHours: number;
  relatedServicesHours: number;
  teachingDemoHours: number;
  shareOfCostAmount: number;
  shareOfCostMet: boolean;
  waiverProgram: string;
  advancePayEligible: boolean;
};

type HealthCert = {
  id: number;
  caseId: number;
  assessmentId: number;
  certificationMethod: string;
  status: string;
  dueDate: string;
  submittedDate: string;
  hasGoodCauseExtension: boolean;
  goodCauseReason: string;
  exceptionReason: string;
};

export default function ServiceEligibilityPage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const router = useRouter();
  const params = useParams();
  const caseId = params.caseId as string;

  const [loading, setLoading] = useState(true);
  const [assessments, setAssessments] = useState<Assessment[]>([]);
  const [healthCerts, setHealthCerts] = useState<HealthCert[]>([]);
  const [activeTab, setActiveTab] = useState('assessments');
  const [showNewAssessmentModal, setShowNewAssessmentModal] = useState(false);
  const [newAssessmentType, setNewAssessmentType] = useState('INITIAL');

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchEligibilityData();
  }, [user, authLoading, caseId]);

  const fetchEligibilityData = async () => {
    try {
      setLoading(true);
      const [assessmentsResponse, certsResponse] = await Promise.all([
        apiClient.get(`/eligibility/case/${caseId}`),
        apiClient.get(`/eligibility/case/${caseId}/health-cert`).catch(() => ({ data: [] }))
      ]);

      setAssessments(assessmentsResponse.data || []);
      setHealthCerts(certsResponse.data || []);
    } catch (err) {
      console.error('Error fetching eligibility data:', err);
    } finally {
      setLoading(false);
    }
  };

  const createAssessment = async () => {
    try {
      await apiClient.post('/eligibility', {
        caseId: parseInt(caseId),
        assessmentType: newAssessmentType
      });
      setShowNewAssessmentModal(false);
      fetchEligibilityData();
    } catch (err) {
      console.error('Error creating assessment:', err);
      alert('Failed to create assessment');
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'DRAFT': return 'bg-secondary';
      case 'IN_PROGRESS': return 'bg-warning text-dark';
      case 'COMPLETED': return 'bg-success';
      case 'SUBMITTED': return 'bg-info';
      case 'APPROVED': return 'bg-success';
      case 'OVERDUE': return 'bg-danger';
      default: return 'bg-secondary';
    }
  };

  const getHtgIndicatorDisplay = (indicator: string) => {
    switch (indicator) {
      case '+': return <span className="text-success fw-bold">+</span>;
      case '-': return <span className="text-danger fw-bold">-</span>;
      default: return <span className="text-muted">-</span>;
    }
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading Service Eligibility...</p>
        </div>
      </div>
    );
  }

  const currentAssessment = assessments.length > 0 ? assessments[0] : null;

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.push(`/cases/${caseId}`)}>
            <i className="bi bi-arrow-left me-2"></i>Back to Case
          </button>
          <h1 className="h3 mb-0">Service Eligibility - Case #{caseId}</h1>
        </div>
        <button className="btn btn-primary" onClick={() => setShowNewAssessmentModal(true)}>
          <i className="bi bi-plus-lg me-2"></i>New Assessment
        </button>
      </div>

      {/* Summary Cards */}
      {currentAssessment && (
        <div className="row mb-4">
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center h-100">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: 'var(--color-p2, #046b99)' }}>
                  {currentAssessment.totalAssessedNeed || 0}
                </div>
                <p className="text-muted small mb-0">TOTAL ASSESSED HOURS</p>
              </div>
            </div>
          </div>
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center h-100">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: currentAssessment.shareOfCostMet ? '#198754' : '#ffc107' }}>
                  ${currentAssessment.shareOfCostAmount || 0}
                </div>
                <p className="text-muted small mb-0">SHARE OF COST</p>
              </div>
            </div>
          </div>
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center h-100">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '1.5rem', color: '#0dcaf0' }}>
                  {currentAssessment.assessmentType?.replace(/_/g, ' ') || '-'}
                </div>
                <p className="text-muted small mb-0">ASSESSMENT TYPE</p>
              </div>
            </div>
          </div>
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center h-100">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '1.2rem' }}>
                  {currentAssessment.reassessmentDueDate || 'Not Set'}
                </div>
                <p className="text-muted small mb-0">REASSESSMENT DUE</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tabs */}
      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'assessments' ? 'active' : ''}`}
            onClick={() => setActiveTab('assessments')}
          >
            Assessments ({assessments.length})
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'service-hours' ? 'active' : ''}`}
            onClick={() => setActiveTab('service-hours')}
          >
            Service Hours
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'health-cert' ? 'active' : ''}`}
            onClick={() => setActiveTab('health-cert')}
          >
            Health Care Certification ({healthCerts.length})
          </button>
        </li>
      </ul>

      {/* Tab Content */}
      {activeTab === 'assessments' && (
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Assessment History</h5>
          </div>
          <div className="card-body">
            {assessments.length === 0 ? (
              <p className="text-muted text-center py-4">No assessments found. Create a new assessment to get started.</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Type</th>
                      <th>Status</th>
                      <th>Assessment Date</th>
                      <th>Home Visit Date</th>
                      <th>Reassessment Due</th>
                      <th>Total Hours</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {assessments.map((assessment) => (
                      <tr key={assessment.id}>
                        <td>{assessment.assessmentType?.replace(/_/g, ' ')}</td>
                        <td>
                          <span className={`badge ${getStatusBadgeClass(assessment.assessmentStatus)}`}>
                            {assessment.assessmentStatus}
                          </span>
                        </td>
                        <td>{assessment.assessmentDate || '-'}</td>
                        <td>{assessment.homeVisitDate || '-'}</td>
                        <td>{assessment.reassessmentDueDate || '-'}</td>
                        <td className="fw-bold">{assessment.totalAssessedNeed || 0} hrs</td>
                        <td>
                          <button
                            className="btn btn-sm btn-outline-primary"
                            onClick={() => router.push(`/eligibility/${assessment.id}`)}
                          >
                            View/Edit
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

      {activeTab === 'service-hours' && currentAssessment && (
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Service Hours Breakdown</h5>
          </div>
          <div className="card-body">
            <div className="table-responsive">
              <table className="table table-bordered">
                <thead>
                  <tr>
                    <th>Service Category</th>
                    <th className="text-center">Hours</th>
                    <th className="text-center">HTG Indicator</th>
                    <th>Description</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>Domestic Services</td>
                    <td className="text-center fw-bold">{currentAssessment.domesticHours || 0}</td>
                    <td className="text-center">{getHtgIndicatorDisplay(currentAssessment.domesticHtgIndicator)}</td>
                    <td className="small text-muted">Housework, laundry, shopping, errands</td>
                  </tr>
                  <tr>
                    <td>Personal Care</td>
                    <td className="text-center fw-bold">{currentAssessment.personalCareHours || 0}</td>
                    <td className="text-center">{getHtgIndicatorDisplay(currentAssessment.personalCareHtgIndicator)}</td>
                    <td className="small text-muted">Bathing, grooming, dressing, feeding</td>
                  </tr>
                  <tr>
                    <td>Paramedical Services</td>
                    <td className="text-center fw-bold">{currentAssessment.paramedicalHours || 0}</td>
                    <td className="text-center">{getHtgIndicatorDisplay(currentAssessment.paramedicalHtgIndicator)}</td>
                    <td className="small text-muted">Health-related tasks under medical supervision</td>
                  </tr>
                  <tr>
                    <td>Protective Supervision</td>
                    <td className="text-center fw-bold">{currentAssessment.protectiveSupervisionHours || 0}</td>
                    <td className="text-center">{getHtgIndicatorDisplay(currentAssessment.protectiveSupervisionHtgIndicator)}</td>
                    <td className="small text-muted">Supervision for safety due to mental impairment</td>
                  </tr>
                  <tr>
                    <td>Transportation</td>
                    <td className="text-center fw-bold">{currentAssessment.transportationHours || 0}</td>
                    <td className="text-center">{getHtgIndicatorDisplay(currentAssessment.transportationHtgIndicator)}</td>
                    <td className="small text-muted">Medical appointments, shopping</td>
                  </tr>
                  <tr>
                    <td>Meal Preparation</td>
                    <td className="text-center fw-bold">{currentAssessment.mealPrepHours || 0}</td>
                    <td className="text-center">-</td>
                    <td className="small text-muted">Preparing meals</td>
                  </tr>
                  <tr>
                    <td>Related Services</td>
                    <td className="text-center fw-bold">{currentAssessment.relatedServicesHours || 0}</td>
                    <td className="text-center">-</td>
                    <td className="small text-muted">Heavy cleaning, yard hazard removal</td>
                  </tr>
                  <tr>
                    <td>Teaching &amp; Demonstration</td>
                    <td className="text-center fw-bold">{currentAssessment.teachingDemoHours || 0}</td>
                    <td className="text-center">-</td>
                    <td className="small text-muted">Training to perform tasks independently</td>
                  </tr>
                  <tr className="table-primary">
                    <td className="fw-bold">TOTAL</td>
                    <td className="text-center fw-bold fs-5">{currentAssessment.totalAssessedNeed || 0}</td>
                    <td></td>
                    <td></td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div className="alert alert-info mt-3">
              <h6>HTG Indicators</h6>
              <ul className="mb-0 small">
                <li><strong className="text-success">+</strong> = Hours to be increased (Higher Than Guidelines)</li>
                <li><strong className="text-danger">-</strong> = Hours to be decreased (Lower Than Guidelines)</li>
                <li><strong className="text-muted">Blank</strong> = Within normal guidelines</li>
              </ul>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'health-cert' && (
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h5 className="mb-0">Health Care Certification (SOC 873)</h5>
            <button className="btn btn-light btn-sm" onClick={() => router.push(`/eligibility/case/${caseId}/health-cert/new`)}>
              <i className="bi bi-plus-lg me-2"></i>New Certification
            </button>
          </div>
          <div className="card-body">
            {healthCerts.length === 0 ? (
              <p className="text-muted text-center py-4">No health care certifications found.</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Method</th>
                      <th>Status</th>
                      <th>Due Date</th>
                      <th>Submitted Date</th>
                      <th>Good Cause Extension</th>
                      <th>Exception</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {healthCerts.map((cert) => (
                      <tr key={cert.id}>
                        <td>{cert.certificationMethod === 'USER_ENTERED' ? '3rd Party' : 'County Form'}</td>
                        <td>
                          <span className={`badge ${getStatusBadgeClass(cert.status)}`}>
                            {cert.status}
                          </span>
                        </td>
                        <td className={cert.dueDate && new Date(cert.dueDate) < new Date() ? 'text-danger fw-bold' : ''}>
                          {cert.dueDate || '-'}
                        </td>
                        <td>{cert.submittedDate || '-'}</td>
                        <td>
                          {cert.hasGoodCauseExtension ? (
                            <span className="badge bg-warning text-dark">Yes - {cert.goodCauseReason}</span>
                          ) : 'No'}
                        </td>
                        <td>
                          {cert.exceptionReason ? (
                            <span className="badge bg-info">{cert.exceptionReason?.replace(/_/g, ' ')}</span>
                          ) : '-'}
                        </td>
                        <td>
                          <button
                            className="btn btn-sm btn-outline-primary"
                            onClick={() => router.push(`/eligibility/health-cert/${cert.id}`)}
                          >
                            View
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div className="alert alert-info mt-3">
              <h6>Health Care Certification Requirements</h6>
              <ul className="mb-0 small">
                <li>Due within 45 days of initial authorization</li>
                <li>Good cause extension available for additional 45 days</li>
                <li>Exceptions available for hospital discharge or risk of out-of-home placement</li>
              </ul>
            </div>
          </div>
        </div>
      )}

      {/* New Assessment Modal */}
      {showNewAssessmentModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Create New Assessment</h5>
                <button type="button" className="btn-close" onClick={() => setShowNewAssessmentModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label">Assessment Type</label>
                  <select
                    className="form-select"
                    value={newAssessmentType}
                    onChange={(e) => setNewAssessmentType(e.target.value)}
                  >
                    <option value="INITIAL">Initial Assessment</option>
                    <option value="CHANGE">Change Assessment</option>
                    <option value="REASSESSMENT">Reassessment</option>
                    <option value="INTER_COUNTY_TRANSFER">Inter-County Transfer</option>
                    <option value="TELEHEALTH">Telehealth Assessment</option>
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowNewAssessmentModal(false)}>
                  Cancel
                </button>
                <button type="button" className="btn btn-primary" onClick={createAssessment}>
                  Create Assessment
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
