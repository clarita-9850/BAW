'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter, useParams } from 'next/navigation';
import apiClient from '@/lib/api';

type Assignment = {
  id: number;
  providerId: number;
  providerNumber: string;
  providerFirstName: string;
  providerLastName: string;
  caseId: number;
  caseNumber: string;
  recipientName: string;
  providerType: string;
  relationship: string;
  assignedHours: number;
  assignmentStatus: string;
  effectiveDate: string;
  terminationDate: string;
  terminationReason: string;
  notes: string;
  createdDate: string;
  createdBy: string;
  updatedDate: string;
  updatedBy: string;
};

const PROVIDER_TYPES: Record<string, string> = {
  'WAIVER_PERSONAL_CARE': 'Waiver Personal Care Services (WPCS)',
  'NON_WAIVER_PERSONAL_CARE': 'Non-Waiver Personal Care Services',
  'DOMESTIC': 'Domestic Services',
  'PARAMEDICAL': 'Paramedical Services',
  'PROTECTIVE_SUPERVISION': 'Protective Supervision'
};

const RELATIONSHIPS: Record<string, string> = {
  'PARENT': 'Parent',
  'SPOUSE': 'Spouse',
  'CHILD': 'Child',
  'SIBLING': 'Sibling',
  'GRANDPARENT': 'Grandparent',
  'OTHER_RELATIVE': 'Other Relative',
  'FRIEND': 'Friend',
  'NON_RELATIVE': 'Non-Relative'
};

export default function AssignmentDetailPage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const router = useRouter();
  const params = useParams();
  const assignmentId = params.id as string;

  const [loading, setLoading] = useState(true);
  const [assignment, setAssignment] = useState<Assignment | null>(null);
  const [showTerminateModal, setShowTerminateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [terminationReason, setTerminationReason] = useState('');
  const [editForm, setEditForm] = useState({
    assignedHours: 0,
    notes: ''
  });

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchAssignment();
  }, [user, authLoading, assignmentId]);

  const fetchAssignment = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(`/providers/assignments/${assignmentId}`);
      setAssignment(response.data);
      setEditForm({
        assignedHours: response.data.assignedHours,
        notes: response.data.notes || ''
      });
    } catch (err) {
      console.error('Error fetching assignment:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleTerminate = async () => {
    try {
      setLoading(true);
      await apiClient.put(`/providers/assignments/${assignmentId}/terminate`, {
        reason: terminationReason
      });
      setShowTerminateModal(false);
      fetchAssignment();
      alert('Assignment terminated successfully');
    } catch (err) {
      console.error('Error terminating assignment:', err);
      alert('Failed to terminate assignment');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async () => {
    try {
      setLoading(true);
      await apiClient.put(`/providers/assignments/${assignmentId}`, editForm);
      setShowEditModal(false);
      fetchAssignment();
      alert('Assignment updated successfully');
    } catch (err) {
      console.error('Error updating assignment:', err);
      alert('Failed to update assignment');
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'bg-success';
      case 'TERMINATED': return 'bg-danger';
      case 'SUSPENDED': return 'bg-warning text-dark';
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
          <p className="text-muted mb-0">Loading Assignment Details...</p>
        </div>
      </div>
    );
  }

  if (!assignment) {
    return (
      <div className="container-fluid py-4">
        <div className="alert alert-danger">Assignment not found</div>
        <button className="btn btn-primary" onClick={() => router.back()}>
          Go Back
        </button>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* Header */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <button className="btn btn-link text-decoration-none ps-0" onClick={() => router.back()}>
            <i className="bi bi-arrow-left me-2"></i>Back
          </button>
          <h1 className="h3 mb-0">
            Assignment Details
            <span className={`badge ${getStatusBadge(assignment.assignmentStatus)} ms-3`}>
              {assignment.assignmentStatus}
            </span>
          </h1>
          <p className="text-muted mb-0">
            {assignment.providerFirstName} {assignment.providerLastName} - {assignment.caseNumber}
          </p>
        </div>
        <div className="btn-group">
          {assignment.assignmentStatus === 'ACTIVE' && (
            <>
              <button
                className="btn btn-outline-primary"
                onClick={() => setShowEditModal(true)}
              >
                <i className="bi bi-pencil me-2"></i>Edit Hours
              </button>
              <button
                className="btn btn-outline-danger"
                onClick={() => setShowTerminateModal(true)}
              >
                <i className="bi bi-x-circle me-2"></i>Terminate
              </button>
            </>
          )}
        </div>
      </div>

      <div className="row">
        {/* Provider Info */}
        <div className="col-md-6">
          <div className="card mb-4">
            <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
              <h5 className="mb-0">Provider Information</h5>
            </div>
            <div className="card-body">
              <table className="table table-borderless">
                <tbody>
                  <tr>
                    <th style={{ width: '40%' }}>Provider Name</th>
                    <td>
                      <a
                        href="#"
                        onClick={(e) => { e.preventDefault(); router.push(`/providers/${assignment.providerId}`); }}
                        className="text-primary text-decoration-none"
                      >
                        {assignment.providerFirstName} {assignment.providerLastName}
                      </a>
                    </td>
                  </tr>
                  <tr>
                    <th>Provider Number</th>
                    <td>{assignment.providerNumber}</td>
                  </tr>
                  <tr>
                    <th>Provider Type</th>
                    <td>{PROVIDER_TYPES[assignment.providerType] || assignment.providerType}</td>
                  </tr>
                  <tr>
                    <th>Relationship</th>
                    <td>{RELATIONSHIPS[assignment.relationship] || assignment.relationship}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Case Info */}
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
                    <td>
                      <a
                        href="#"
                        onClick={(e) => { e.preventDefault(); router.push(`/cases/${assignment.caseId}`); }}
                        className="text-primary text-decoration-none"
                      >
                        {assignment.caseNumber}
                      </a>
                    </td>
                  </tr>
                  <tr>
                    <th>Recipient</th>
                    <td>{assignment.recipientName}</td>
                  </tr>
                </tbody>
              </table>
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
          <div className="row">
            <div className="col-md-6">
              <table className="table table-borderless">
                <tbody>
                  <tr>
                    <th style={{ width: '40%' }}>Status</th>
                    <td>
                      <span className={`badge ${getStatusBadge(assignment.assignmentStatus)}`}>
                        {assignment.assignmentStatus}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <th>Assigned Hours</th>
                    <td className="fs-5 fw-bold text-primary">{assignment.assignedHours} hrs/month</td>
                  </tr>
                  <tr>
                    <th>Effective Date</th>
                    <td>{assignment.effectiveDate || '-'}</td>
                  </tr>
                  {assignment.terminationDate && (
                    <>
                      <tr>
                        <th>Termination Date</th>
                        <td className="text-danger">{assignment.terminationDate}</td>
                      </tr>
                      <tr>
                        <th>Termination Reason</th>
                        <td>{assignment.terminationReason || '-'}</td>
                      </tr>
                    </>
                  )}
                </tbody>
              </table>
            </div>
            <div className="col-md-6">
              <table className="table table-borderless">
                <tbody>
                  <tr>
                    <th style={{ width: '40%' }}>Notes</th>
                    <td>{assignment.notes || 'No notes'}</td>
                  </tr>
                  <tr>
                    <th>Created</th>
                    <td>{assignment.createdDate} by {assignment.createdBy}</td>
                  </tr>
                  <tr>
                    <th>Last Updated</th>
                    <td>{assignment.updatedDate} by {assignment.updatedBy}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {/* Terminate Modal */}
      {showTerminateModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Terminate Assignment</h5>
                <button type="button" className="btn-close" onClick={() => setShowTerminateModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="alert alert-warning">
                  <i className="bi bi-exclamation-triangle me-2"></i>
                  This action will terminate the provider&apos;s assignment to this case.
                </div>
                <div className="mb-3">
                  <label className="form-label">Reason for Termination <span className="text-danger">*</span></label>
                  <textarea
                    className="form-control"
                    rows={3}
                    value={terminationReason}
                    onChange={(e) => setTerminationReason(e.target.value)}
                    placeholder="Enter reason for termination..."
                    required
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowTerminateModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-danger"
                  onClick={handleTerminate}
                  disabled={!terminationReason || loading}
                >
                  {loading ? 'Processing...' : 'Terminate Assignment'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {showEditModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Edit Assignment</h5>
                <button type="button" className="btn-close" onClick={() => setShowEditModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label">Assigned Hours</label>
                  <div className="input-group">
                    <input
                      type="number"
                      className="form-control"
                      value={editForm.assignedHours}
                      onChange={(e) => setEditForm({ ...editForm, assignedHours: parseFloat(e.target.value) || 0 })}
                      min="0"
                      step="0.5"
                    />
                    <span className="input-group-text">hrs/month</span>
                  </div>
                </div>
                <div className="mb-3">
                  <label className="form-label">Notes</label>
                  <textarea
                    className="form-control"
                    rows={3}
                    value={editForm.notes}
                    onChange={(e) => setEditForm({ ...editForm, notes: e.target.value })}
                    placeholder="Additional notes..."
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowEditModal(false)}>
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleUpdate}
                  disabled={loading}
                >
                  {loading ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
