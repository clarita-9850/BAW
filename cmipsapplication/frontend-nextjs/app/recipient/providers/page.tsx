'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api';
import { FieldAuthorizedValue } from '@/components/FieldAuthorizedValue';
import { isFieldVisible } from '@/hooks/useFieldAuthorization';

type Provider = {
  id: number;
  providerId?: number;
  providerName?: string;
  providerNumber?: string;
  firstName?: string;
  lastName?: string;
  relationship?: string;
  status?: string;
  authorizedHoursPerMonth?: number;
  startDate?: string;
  primaryPhone?: string;
  providerStatus?: string;
};

type ProviderResponse = {
  content?: Provider[];
  data?: Provider[];
  allowedActions?: string[];
};

export default function RecipientProvidersPage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [providers, setProviders] = useState<Provider[]>([]);
  const [allowedActions, setAllowedActions] = useState<string[]>([]);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    fetchProviders();
  }, [user, authLoading, mounted]);

  const fetchProviders = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get<ProviderResponse>('/provider-recipient/my-providers');
      const data = response.data;
      setProviders(data.content || data.data || (Array.isArray(data) ? data : []));
      setAllowedActions(data.allowedActions || ['view']);
    } catch (err) {
      console.error('Error fetching providers:', err);
      // Demo data
      setProviders([{
        id: 1,
        providerId: 1,
        providerName: 'provider1',
        firstName: 'John',
        lastName: 'Provider',
        relationship: 'Non-Relative',
        status: 'Active',
        authorizedHoursPerMonth: 40,
        startDate: '2024-01-01',
        primaryPhone: '555-1234'
      }]);
    } finally {
      setLoading(false);
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
      <div className="mb-4">
        <h1 className="h3 mb-1">My Providers</h1>
        <p className="text-muted">View your assigned care providers</p>
      </div>

      {providers.length === 0 ? (
        <div className="card">
          <div className="card-body text-center py-5">
            <p className="text-muted mb-0">No providers assigned to you</p>
          </div>
        </div>
      ) : (
        <div className="row">
          {providers.map((provider) => (
            <div key={provider.id} className="col-md-6 col-lg-4 mb-4">
              <div className="card h-100">
                <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                  <h5 className="mb-0" style={{ color: 'white' }}>
                    {provider.providerName || `${provider.firstName} ${provider.lastName}`}
                  </h5>
                </div>
                <div className="card-body">
                  <dl className="row mb-0">
                    {isFieldVisible(provider, 'relationship') && (
                      <>
                        <dt className="col-5">Relationship:</dt>
                        <dd className="col-7">
                          <FieldAuthorizedValue data={provider} field="relationship" />
                        </dd>
                      </>
                    )}
                    {isFieldVisible(provider, 'authorizedHoursPerMonth') && (
                      <>
                        <dt className="col-5">Hours/Month:</dt>
                        <dd className="col-7">
                          <FieldAuthorizedValue data={provider} field="authorizedHoursPerMonth" type="number" /> hrs
                        </dd>
                      </>
                    )}
                    {isFieldVisible(provider, 'status') && (
                      <>
                        <dt className="col-5">Status:</dt>
                        <dd className="col-7">
                          <FieldAuthorizedValue data={provider} field="status" type="badge" />
                        </dd>
                      </>
                    )}
                    {isFieldVisible(provider, 'startDate') && (
                      <>
                        <dt className="col-5">Since:</dt>
                        <dd className="col-7">
                          <FieldAuthorizedValue data={provider} field="startDate" type="date" />
                        </dd>
                      </>
                    )}
                    {isFieldVisible(provider, 'primaryPhone') && (
                      <>
                        <dt className="col-5">Phone:</dt>
                        <dd className="col-7">
                          <FieldAuthorizedValue data={provider} field="primaryPhone" />
                        </dd>
                      </>
                    )}
                  </dl>
                </div>
                <div className="card-footer bg-transparent">
                  <small className="text-muted">
                    Provider #{provider.providerNumber || provider.providerId}
                  </small>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Help Info */}
      <div className="alert alert-info mt-4">
        <h6>About Your Providers</h6>
        <ul className="mb-0 small">
          <li>These are the providers authorized to care for you</li>
          <li>Contact your caseworker to request changes</li>
          <li>You can approve timesheets submitted by your providers</li>
        </ul>
      </div>
    </div>
  );
}
