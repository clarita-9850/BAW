'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/api';

type Recipient = {
  id: string;
  name: string;
  caseNumber: string;
};

export default function ProviderProfileComponent() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);
  const router = useRouter();
  const [addressForm, setAddressForm] = useState({ street: '', city: '', state: 'CA', zipCode: '' });
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [notifyRecipientId, setNotifyRecipientId] = useState('');
  const [notifyRecipientName, setNotifyRecipientName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (authLoading) return;
    if (!user || (user.role !== 'PROVIDER' && !user.roles?.includes('PROVIDER'))) {
      window.location.href = '/login';
      return;
    }
    // Load assigned recipients (to choose who to notify)
    (async () => {
      try {
        const res = await apiClient.get('/provider-recipient/my-recipients');
        const list = (res.data || []).map((r: any) => ({
          id: String(r.id || ''),
          name: r.recipientName || 'recipient1',
          caseNumber: r.caseNumber || '',
        }));
        setRecipients(list);
        if (list.length > 0) {
          setNotifyRecipientId(list[0].id);
          setNotifyRecipientName(list[0].name);
        } else {
          setNotifyRecipientId('');
          setNotifyRecipientName('recipient1');
        }
      } catch (e) {
        console.error('Error fetching recipients:', e);
        setRecipients([]);
        setNotifyRecipientId('');
        setNotifyRecipientName('recipient1');
      } finally {
        setLoading(false);
      }
    })();
  }, [authLoading, user]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    setSubmitting(true);
    try {
      const payload = {
        caseId: notifyRecipientId ? `CASE-${notifyRecipientId}` : 'CASE-PROVIDER-ADDR',
        recipientId: notifyRecipientId || undefined,
        recipientName: notifyRecipientName || undefined,
        providerId: user.username,
        newAddress: {
          line1: addressForm.street,
          city: addressForm.city,
          state: addressForm.state,
          zip: addressForm.zipCode,
        },
      };
      await apiClient.post('/cases/address-change', payload);
      alert('Your address change was submitted. Recipient will be notified (if selected) and a validation task will be created for case worker.');
      router.push('/provider/dashboard');
    } catch (err: any) {
      console.error(err);
      alert('Failed to submit address change: ' + (err?.response?.data?.error || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1e3a8a] mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading Profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-[#1e3a8a] text-white px-6 py-4 shadow-lg">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <h1 className="text-xl font-bold">My Profile</h1>
          <button
            onClick={() => router.push('/provider/dashboard')}
            className="px-4 py-2 bg-white text-[#1e3a8a] rounded hover:bg-gray-100 font-medium"
          >
            Back to Dashboard
          </button>
        </div>
      </header>

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white border border-gray-300 rounded-lg shadow-sm">
          <div className="bg-[#1e3a8a] px-6 py-4 rounded-t-lg">
            <h2 className="text-lg font-semibold text-white">My Profile</h2>
          </div>
          <div className="p-6">
            <p className="mb-2"><strong>Username:</strong> {user?.username}</p>
            <p className="mb-6"><strong>Role:</strong> Provider</p>

            <h3 className="text-md font-semibold mb-3 text-gray-900">Update My Address</h3>
            <form onSubmit={onSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">Street</label>
                <input
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#1e3a8a] focus:border-[#1e3a8a]"
                  value={addressForm.street}
                  onChange={(e) => setAddressForm({ ...addressForm, street: e.target.value })}
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">City</label>
                <input
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#1e3a8a] focus:border-[#1e3a8a]"
                  value={addressForm.city}
                  onChange={(e) => setAddressForm({ ...addressForm, city: e.target.value })}
                  required
                />
              </div>
              <div className="grid grid-cols-2 gap-4 mb-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">State</label>
                  <select
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#1e3a8a] focus:border-[#1e3a8a]"
                    value={addressForm.state}
                    onChange={(e) => setAddressForm({ ...addressForm, state: e.target.value })}
                  >
                    <option value="CA">California</option>
                    <option value="NV">Nevada</option>
                    <option value="OR">Oregon</option>
                    <option value="AZ">Arizona</option>
                    <option value="TX">Texas</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">ZIP Code</label>
                  <input
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#1e3a8a] focus:border-[#1e3a8a]"
                    value={addressForm.zipCode}
                    onChange={(e) => setAddressForm({ ...addressForm, zipCode: e.target.value })}
                    pattern="[0-9]{5}"
                    required
                  />
                </div>
              </div>

              {recipients.length > 0 ? (
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-1">Notify Recipient</label>
                  <select
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#1e3a8a] focus:border-[#1e3a8a]"
                    value={notifyRecipientId}
                    onChange={(e) => {
                      const id = e.target.value;
                      setNotifyRecipientId(id);
                      const found = recipients.find((r) => r.id === id);
                      setNotifyRecipientName(found ? found.name : 'recipient1');
                    }}
                  >
                    {recipients.map((r) => (
                      <option key={r.id} value={r.id}>
                        {r.name}
                      </option>
                    ))}
                  </select>
                </div>
              ) : (
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-1">Notify Recipient</label>
                  <input
                    type="text"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#1e3a8a] focus:border-[#1e3a8a]"
                    value={notifyRecipientName || 'recipient1'}
                    onChange={(e) => setNotifyRecipientName(e.target.value)}
                    placeholder="Enter recipient username (e.g., recipient1)"
                    required
                  />
                </div>
              )}

              <div className="flex gap-2">
                <button
                  type="button"
                  className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 font-medium"
                  onClick={() => router.back()}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-[#1e3a8a] text-white rounded hover:bg-[#1e40af] font-medium disabled:bg-gray-400 disabled:cursor-not-allowed"
                  disabled={submitting}
                >
                  {submitting ? 'Submitting...' : 'Submit My Address Change'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
