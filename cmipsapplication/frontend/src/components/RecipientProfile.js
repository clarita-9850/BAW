import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { apiClient } from '../config/api';

const RecipientProfile = () => {
  const { recipientId } = useParams();
  const navigate = useNavigate();
  const { user, loading } = useAuth();
  const [recipient, setRecipient] = useState(null);
  const [addressForm, setAddressForm] = useState({ street: '', city: '', state: 'CA', zipCode: '' });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (loading) return;
    if (!user || user.role !== 'PROVIDER') {
      navigate('/login');
      return;
    }
    // Load recipient from provider relationships
    (async () => {
      try {
        const res = await apiClient.get('/api/provider-recipient/my-recipients');
        const found = (res.data || []).find(r => String(r.id) === String(recipientId));
        const mapped = found ? {
          id: found.id,
          name: found.recipientName || 'recipient',
          caseNumber: found.caseNumber || `CASE-${found.id}`,
          address: { street: '', city: 'Sacramento', state: 'CA', zipCode: '' },
        } : null;
        setRecipient(mapped);
        if (mapped) {
          setAddressForm({
            street: mapped.address.street,
            city: mapped.address.city,
            state: mapped.address.state,
            zipCode: mapped.address.zipCode,
          });
        }
      } catch (e) {
        setRecipient({ id: recipientId, name: 'recipient1', caseNumber: `CASE-${recipientId}`, address: { street: '', city: 'Sacramento', state: 'CA', zipCode: '' } });
      }
    })();
  }, [loading, user, recipientId, navigate]);

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!recipient) return;
    setSubmitting(true);
    try {
      const payload = {
        recipientId: recipient.id,
        recipientName: recipient.name,
        providerId: user?.username,
        caseId: recipient.caseNumber,
        newAddress: {
          line1: addressForm.street,
          city: addressForm.city,
          state: addressForm.state,
          zip: addressForm.zipCode,
        },
      };
      await apiClient.post('/cases/address-change', payload);
      alert('Address change submitted. Recipient will be notified and case worker task created.');
      navigate('/provider/dashboard');
    } catch (err) {
      console.error(err);
      alert('Failed to submit address change');
    } finally {
      setSubmitting(false);
    }
  };

  if (!recipient) {
    return <div className="container py-8">Loading profile...</div>;
  }

  return (
    <div className="container py-8">
      <div className="card mb-6">
        <div className="card-header">
          <h2 className="text-lg font-semibold text-ca-primary-900">Recipient Profile</h2>
        </div>
        <div className="card-body">
          <p className="mb-2"><strong>Name:</strong> {recipient.name}</p>
          <p className="mb-4"><strong>Case:</strong> {recipient.caseNumber}</p>

          <h3 className="text-md font-semibold mb-3">Update Address</h3>
          <form onSubmit={onSubmit}>
            <div className="form-group mb-3">
              <label className="block text-sm font-medium mb-1">Street</label>
              <input className="form-control" value={addressForm.street} onChange={e => setAddressForm({ ...addressForm, street: e.target.value })} required />
            </div>
            <div className="form-group mb-3">
              <label className="block text-sm font-medium mb-1">City</label>
              <input className="form-control" value={addressForm.city} onChange={e => setAddressForm({ ...addressForm, city: e.target.value })} required />
            </div>
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div className="form-group">
                <label className="block text-sm font-medium mb-1">State</label>
                <select className="form-control" value={addressForm.state} onChange={e => setAddressForm({ ...addressForm, state: e.target.value })}>
                  <option value="CA">California</option>
                  <option value="NV">Nevada</option>
                  <option value="OR">Oregon</option>
                  <option value="AZ">Arizona</option>
                  <option value="TX">Texas</option>
                </select>
              </div>
              <div className="form-group">
                <label className="block text-sm font-medium mb-1">ZIP Code</label>
                <input className="form-control" value={addressForm.zipCode} onChange={e => setAddressForm({ ...addressForm, zipCode: e.target.value })} pattern="[0-9]{5}" required />
              </div>
            </div>
            <div className="flex gap-2">
              <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={submitting}>{submitting ? 'Submitting...' : 'Submit Address Change'}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default RecipientProfile;

