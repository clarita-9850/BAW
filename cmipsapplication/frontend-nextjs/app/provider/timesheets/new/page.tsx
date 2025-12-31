'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/api';

type Recipient = {
  id: number;
  recipientName: string;
  authorizedHoursPerMonth: number;
  caseNumber: string;
  status: string;
};

export default function NewTimesheetPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const [form, setForm] = useState({
    recipientId: '',
    payPeriodStart: '',
    payPeriodEnd: '',
    regularHours: 0,
    overtimeHours: 0,
    comments: '',
  });

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchRecipients();
  }, [user, authLoading, mounted]);

  const fetchRecipients = async () => {
    try {
      const response = await apiClient.get('/provider-recipient/my-recipients');
      setRecipients(response.data || []);
    } catch (err) {
      console.error('Error fetching recipients:', err);
      // Use default recipient for demo
      setRecipients([{
        id: 1,
        recipientName: 'recipient1',
        authorizedHoursPerMonth: 40,
        caseNumber: 'CASE-001',
        status: 'Active'
      }]);
    }
  };

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!form.payPeriodStart) newErrors.payPeriodStart = 'Pay period start is required';
    if (!form.payPeriodEnd) newErrors.payPeriodEnd = 'Pay period end is required';
    if (form.regularHours < 0) newErrors.regularHours = 'Regular hours cannot be negative';
    if (form.overtimeHours < 0) newErrors.overtimeHours = 'Overtime hours cannot be negative';
    if (form.regularHours + form.overtimeHours <= 0) {
      newErrors.regularHours = 'Total hours must be greater than 0';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent, isDraft: boolean = true) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      setLoading(true);
      const payload = {
        ...form,
        recipientId: form.recipientId ? parseInt(form.recipientId) : null,
        employeeId: user?.username,
        employeeName: user?.name || user?.username,
        status: isDraft ? 'DRAFT' : 'SUBMITTED',
      };

      const response = await apiClient.post('/timesheets', payload);

      if (isDraft) {
        alert('Timesheet saved as draft');
      } else {
        alert('Timesheet submitted successfully!');
      }

      router.push('/provider/timesheets');
    } catch (err: any) {
      alert('Failed to create timesheet: ' + (err?.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };

  const totalHours = form.regularHours + form.overtimeHours;

  if (!mounted || authLoading) {
    return (
      <div className="d-flex align-items-center justify-content-center" style={{ minHeight: '400px' }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-4">
        <button className="btn btn-link ps-0" onClick={() => router.back()}>
          &larr; Back
        </button>
        <h1 className="h3 mb-1">Create New Timesheet</h1>
        <p className="text-muted">Enter your hours for the pay period</p>
      </div>

      <form onSubmit={(e) => handleSubmit(e, true)}>
        <div className="row">
          <div className="col-lg-8">
            {/* Pay Period */}
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0" style={{ color: 'white' }}>Pay Period Information</h5>
              </div>
              <div className="card-body">
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Pay Period Start <span className="text-danger">*</span></label>
                    <input
                      type="date"
                      className={`form-control ${errors.payPeriodStart ? 'is-invalid' : ''}`}
                      value={form.payPeriodStart}
                      onChange={(e) => setForm({ ...form, payPeriodStart: e.target.value })}
                    />
                    {errors.payPeriodStart && <div className="invalid-feedback">{errors.payPeriodStart}</div>}
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Pay Period End <span className="text-danger">*</span></label>
                    <input
                      type="date"
                      className={`form-control ${errors.payPeriodEnd ? 'is-invalid' : ''}`}
                      value={form.payPeriodEnd}
                      onChange={(e) => setForm({ ...form, payPeriodEnd: e.target.value })}
                    />
                    {errors.payPeriodEnd && <div className="invalid-feedback">{errors.payPeriodEnd}</div>}
                  </div>
                </div>
              </div>
            </div>

            {/* Recipient Selection */}
            {recipients.length > 0 && (
              <div className="card mb-4">
                <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                  <h5 className="mb-0" style={{ color: 'white' }}>Recipient (Optional)</h5>
                </div>
                <div className="card-body">
                  <label className="form-label">Select Recipient</label>
                  <select
                    className="form-select"
                    value={form.recipientId}
                    onChange={(e) => setForm({ ...form, recipientId: e.target.value })}
                  >
                    <option value="">-- All Recipients --</option>
                    {recipients.map((r) => (
                      <option key={r.id} value={r.id}>
                        {r.recipientName} - {r.caseNumber} ({r.authorizedHoursPerMonth} hrs/month)
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            )}

            {/* Hours Entry */}
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0" style={{ color: 'white' }}>Hours Worked</h5>
              </div>
              <div className="card-body">
                <div className="row g-3">
                  <div className="col-md-4">
                    <label className="form-label">Regular Hours <span className="text-danger">*</span></label>
                    <input
                      type="number"
                      step="0.5"
                      min="0"
                      className={`form-control ${errors.regularHours ? 'is-invalid' : ''}`}
                      value={form.regularHours}
                      onChange={(e) => setForm({ ...form, regularHours: parseFloat(e.target.value) || 0 })}
                    />
                    {errors.regularHours && <div className="invalid-feedback">{errors.regularHours}</div>}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Overtime Hours</label>
                    <input
                      type="number"
                      step="0.5"
                      min="0"
                      className={`form-control ${errors.overtimeHours ? 'is-invalid' : ''}`}
                      value={form.overtimeHours}
                      onChange={(e) => setForm({ ...form, overtimeHours: parseFloat(e.target.value) || 0 })}
                    />
                    {errors.overtimeHours && <div className="invalid-feedback">{errors.overtimeHours}</div>}
                  </div>
                  <div className="col-md-4">
                    <label className="form-label">Total Hours</label>
                    <input
                      type="text"
                      className="form-control fw-bold"
                      value={totalHours}
                      disabled
                    />
                  </div>
                </div>

                <div className="mt-3">
                  <label className="form-label">Comments</label>
                  <textarea
                    className="form-control"
                    rows={3}
                    value={form.comments}
                    onChange={(e) => setForm({ ...form, comments: e.target.value })}
                    placeholder="Add any notes about this timesheet..."
                  />
                </div>
              </div>
            </div>

            {/* Actions */}
            <div className="d-flex gap-2">
              <button
                type="submit"
                className="btn btn-outline-primary"
                disabled={loading}
              >
                {loading ? 'Saving...' : 'Save as Draft'}
              </button>
              <button
                type="button"
                className="btn btn-primary"
                disabled={loading}
                onClick={(e) => handleSubmit(e, false)}
              >
                {loading ? 'Submitting...' : 'Submit Timesheet'}
              </button>
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => router.back()}
              >
                Cancel
              </button>
            </div>
          </div>

          {/* Summary Sidebar */}
          <div className="col-lg-4">
            <div className="card">
              <div className="card-header">
                <h5 className="mb-0">Summary</h5>
              </div>
              <div className="card-body">
                <dl className="row mb-0">
                  <dt className="col-6">Provider:</dt>
                  <dd className="col-6">{user?.name || user?.username}</dd>

                  <dt className="col-6">Pay Period:</dt>
                  <dd className="col-6">
                    {form.payPeriodStart && form.payPeriodEnd
                      ? `${new Date(form.payPeriodStart).toLocaleDateString()} - ${new Date(form.payPeriodEnd).toLocaleDateString()}`
                      : '-'}
                  </dd>

                  <dt className="col-6">Regular:</dt>
                  <dd className="col-6">{form.regularHours} hrs</dd>

                  <dt className="col-6">Overtime:</dt>
                  <dd className="col-6">{form.overtimeHours} hrs</dd>

                  <dt className="col-6 fw-bold">Total:</dt>
                  <dd className="col-6 fw-bold">{totalHours} hrs</dd>
                </dl>
              </div>
            </div>

            <div className="card mt-3">
              <div className="card-header">
                <h6 className="mb-0">Tips</h6>
              </div>
              <div className="card-body">
                <ul className="small mb-0 ps-3">
                  <li>Regular hours are capped at 40 per week</li>
                  <li>Overtime is calculated after 40 hours</li>
                  <li>Save as draft to continue later</li>
                  <li>Submit when ready for approval</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}
