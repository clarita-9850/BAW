'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api';
import { FieldAuthorizedValue } from '@/components/FieldAuthorizedValue';
import { isFieldVisible } from '@/hooks/useFieldAuthorization';

type RecipientProfile = {
  id: number;
  personType?: string;
  firstName?: string;
  lastName?: string;
  dateOfBirth?: string;
  gender?: string;
  cin?: string;
  primaryPhone?: string;
  email?: string;
  residenceCity?: string;
  residenceState?: string;
  countyCode?: string;
};

export default function RecipientProfilePage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [profile, setProfile] = useState<RecipientProfile | null>(null);
  const [allowedActions, setAllowedActions] = useState<string[]>([]);
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState({ primaryPhone: '', email: '' });

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchProfile();
  }, [user, authLoading, mounted]);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get('/recipients/me');
      const data = response.data;
      setProfile(data.content?.[0] || data);
      setAllowedActions(data.allowedActions || ['view', 'edit']);
      if (data.content?.[0] || data) {
        const p = data.content?.[0] || data;
        setEditForm({ primaryPhone: p.primaryPhone || '', email: p.email || '' });
      }
    } catch (err) {
      console.error('Error fetching profile:', err);
      // Demo data
      const demo = {
        id: 1,
        personType: 'RECIPIENT',
        firstName: user?.name?.split(' ')[0] || 'Jane',
        lastName: user?.name?.split(' ')[1] || 'Recipient',
        dateOfBirth: '1965-03-15',
        gender: 'Female',
        primaryPhone: '555-9876',
        email: 'jane.recipient@email.com',
        residenceCity: 'Sacramento',
        residenceState: 'CA'
      };
      setProfile(demo);
      setEditForm({ primaryPhone: demo.primaryPhone, email: demo.email });
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    try {
      await apiClient.put(`/recipients/${profile?.id}`, editForm);
      alert('Profile updated successfully!');
      setEditing(false);
      fetchProfile();
    } catch (err: any) {
      alert('Failed to update: ' + (err?.response?.data?.error || err.message));
    }
  };

  if (!mounted || loading || authLoading) {
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
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 className="h3 mb-1">My Profile</h1>
          <p className="text-muted mb-0">View and update your contact information</p>
        </div>
        {allowedActions.includes('edit') && !editing && (
          <button className="btn btn-primary" onClick={() => setEditing(true)}>
            Edit Contact Info
          </button>
        )}
      </div>

      {profile && (
        <div className="row">
          <div className="col-lg-8">
            {/* Personal Information */}
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0" style={{ color: 'white' }}>Personal Information</h5>
              </div>
              <div className="card-body">
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label text-muted">First Name</label>
                    <p className="fw-semibold mb-0">
                      <FieldAuthorizedValue data={profile} field="firstName" />
                    </p>
                  </div>
                  <div className="col-md-6">
                    <label className="form-label text-muted">Last Name</label>
                    <p className="fw-semibold mb-0">
                      <FieldAuthorizedValue data={profile} field="lastName" />
                    </p>
                  </div>
                  {isFieldVisible(profile, 'dateOfBirth') && (
                    <div className="col-md-6">
                      <label className="form-label text-muted">Date of Birth</label>
                      <p className="fw-semibold mb-0">
                        <FieldAuthorizedValue data={profile} field="dateOfBirth" type="date" />
                      </p>
                    </div>
                  )}
                  {isFieldVisible(profile, 'gender') && (
                    <div className="col-md-6">
                      <label className="form-label text-muted">Gender</label>
                      <p className="fw-semibold mb-0">
                        <FieldAuthorizedValue data={profile} field="gender" />
                      </p>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Contact Information */}
            <div className="card mb-4">
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <h5 className="mb-0" style={{ color: 'white' }}>Contact Information</h5>
              </div>
              <div className="card-body">
                {editing ? (
                  <div className="row g-3">
                    <div className="col-md-6">
                      <label className="form-label">Phone Number</label>
                      <input
                        type="tel"
                        className="form-control"
                        value={editForm.primaryPhone}
                        onChange={(e) => setEditForm({ ...editForm, primaryPhone: e.target.value })}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label">Email</label>
                      <input
                        type="email"
                        className="form-control"
                        value={editForm.email}
                        onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                      />
                    </div>
                    <div className="col-12">
                      <button className="btn btn-primary me-2" onClick={handleSave}>
                        Save Changes
                      </button>
                      <button className="btn btn-outline-secondary" onClick={() => setEditing(false)}>
                        Cancel
                      </button>
                    </div>
                  </div>
                ) : (
                  <div className="row g-3">
                    <div className="col-md-6">
                      <label className="form-label text-muted">Phone Number</label>
                      <p className="fw-semibold mb-0">
                        <FieldAuthorizedValue data={profile} field="primaryPhone" />
                      </p>
                    </div>
                    <div className="col-md-6">
                      <label className="form-label text-muted">Email</label>
                      <p className="fw-semibold mb-0">
                        <FieldAuthorizedValue data={profile} field="email" />
                      </p>
                    </div>
                    {isFieldVisible(profile, 'residenceCity') && (
                      <div className="col-md-6">
                        <label className="form-label text-muted">City</label>
                        <p className="fw-semibold mb-0">
                          <FieldAuthorizedValue data={profile} field="residenceCity" />
                        </p>
                      </div>
                    )}
                    {isFieldVisible(profile, 'residenceState') && (
                      <div className="col-md-6">
                        <label className="form-label text-muted">State</label>
                        <p className="fw-semibold mb-0">
                          <FieldAuthorizedValue data={profile} field="residenceState" />
                        </p>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Sidebar */}
          <div className="col-lg-4">
            <div className="card">
              <div className="card-header">
                <h6 className="mb-0">Need Help?</h6>
              </div>
              <div className="card-body">
                <ul className="small mb-0 ps-3">
                  <li>Contact your caseworker for major changes</li>
                  <li>Update phone/email anytime</li>
                  <li>Address changes require verification</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
