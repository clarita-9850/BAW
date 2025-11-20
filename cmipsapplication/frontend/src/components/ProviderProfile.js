import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { apiClient } from '../config/api';

const ProviderProfile = () => {
  const navigate = useNavigate();
  const { user, loading } = useAuth();

  const [addressForm, setAddressForm] = useState({ street: '', city: '', state: 'CA', zipCode: '' });
  const [recipients, setRecipients] = useState([]);
  const [notifyRecipientId, setNotifyRecipientId] = useState('');
  const [notifyRecipientName, setNotifyRecipientName] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (loading) return;
    if (!user || user.role !== 'PROVIDER') {
      navigate('/login');
      return;
    }
    // Load assigned recipients (to choose who to notify)
    (async () => {
      try {
        const res = await apiClient.get('/api/provider-recipient/my-recipients');
        const list = (res.data || []).map(r => ({ id: String(r.id || ''), name: r.recipientName || 'recipient1', caseNumber: r.caseNumber || '' }));
        setRecipients(list);
        if (list.length > 0) {
          setNotifyRecipientId(list[0].id);
          setNotifyRecipientName(list[0].name);
        } else {
          // Default to recipient1 if no recipients found
          setNotifyRecipientId('');
          setNotifyRecipientName('recipient1');
        }
      } catch (e) {
        // Fallback to default recipient1
        console.error('Error fetching recipients:', e);
        setRecipients([]);
        setNotifyRecipientId('');
        setNotifyRecipientName('recipient1');
      }
    })();
  }, [loading, user, navigate]);

  const onSubmit = async (e) => {
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
      navigate('/provider/dashboard');
    } catch (err) {
      console.error(err);
      alert('Failed to submit address change');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container py-8">
      <div className="card mb-6">
        <div className="card-header">
          <h2 className="text-lg font-semibold text-ca-primary-900">My Profile</h2>
        </div>
        <div className="card-body">
          <p className="mb-2"><strong>Username:</strong> {user?.username}</p>
          <p className="mb-4"><strong>Role:</strong> Provider</p>

          <h3 className="text-md font-semibold mb-3">Update My Address</h3>
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

            {recipients.length > 0 ? (
              <div className="form-group mb-4">
                <label className="block text-sm font-medium mb-1">Notify Recipient</label>
                <select
                  className="form-control"
                  value={notifyRecipientId}
                  onChange={(e) => {
                    const id = e.target.value;
                    setNotifyRecipientId(id);
                    const found = recipients.find(r => r.id === id);
                    setNotifyRecipientName(found ? found.name : 'recipient1');
                  }}
                >
                  {recipients.map(r => (
                    <option key={r.id} value={r.id}>{r.name}</option>
                  ))}
                </select>
              </div>
            ) : (
              <div className="form-group mb-4">
                <label className="block text-sm font-medium mb-1">Notify Recipient</label>
                <input
                  type="text"
                  className="form-control"
                  value={notifyRecipientName || 'recipient1'}
                  onChange={(e) => setNotifyRecipientName(e.target.value)}
                  placeholder="Enter recipient username (e.g., recipient1)"
                  required
                />
              </div>
            )}

            <div className="flex gap-2">
              <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={submitting}>{submitting ? 'Submitting...' : 'Submit My Address Change'}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ProviderProfile;

