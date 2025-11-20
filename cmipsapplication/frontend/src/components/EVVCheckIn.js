import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { apiClient } from '../config/api';

const EVVCheckIn = () => {
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const [selectedRecipient, setSelectedRecipient] = useState('');
  const [serviceType, setServiceType] = useState('');
  const [location, setLocation] = useState(null);
  const [locationError, setLocationError] = useState('');
  const [loading, setLoading] = useState(false);
  const [recipients, setRecipients] = useState([
    { id: 1, name: 'Jane Smith' },
    { id: 2, name: 'Robert Johnson' }
  ]);

  useEffect(() => {
    if (authLoading) return;
    if (!user || user.role !== 'PROVIDER') {
      navigate('/login');
      return;
    }
    getLocation();
  }, [user, navigate, authLoading]);

  const getLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLocation({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            timestamp: new Date().toISOString()
          });
          setLocationError('');
        },
        (error) => {
          setLocationError('Unable to get location. Please enable location services.');
          console.error('Geolocation error:', error);
        }
      );
    } else {
      setLocationError('Geolocation is not supported by your browser.');
    }
  };

  const handleCheckIn = async (e) => {
    e.preventDefault();
    
    if (!location) {
      setLocationError('Location is required for EVV check-in');
      return;
    }

    setLoading(true);
    try {
      const checkInData = {
        recipientId: selectedRecipient,
        serviceType: serviceType,
        checkInTime: new Date().toISOString(),
        latitude: location.latitude,
        longitude: location.longitude,
        location: `${location.latitude}, ${location.longitude}`
      };

      // await apiClient.post('/evv/check-in', checkInData);
      
      // For now, navigate to timesheet entry
      navigate('/provider/timesheets');
      
    } catch (err) {
      console.error('Error during check-in:', err);
      alert('Failed to check in. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-ca-secondary-50">
      {/* Header */}
      <div className="ca-header">
        <div className="container">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-4">
              <div className="ca-logo">CA</div>
              <h1 className="text-xl font-bold text-ca-primary-900">EVV Check-In</h1>
            </div>
            <button onClick={() => navigate('/provider/dashboard')} className="btn btn-outline">
              Back to Dashboard
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container py-8">
        <div className="max-w-2xl mx-auto">
          <div className="card">
            <div className="card-header">
              <h2 className="text-xl font-bold text-ca-primary-900">📍 ELECTRONIC VISIT VERIFICATION</h2>
            </div>
            <div className="card-body">
              <form onSubmit={handleCheckIn} className="space-y-6">
                {/* Recipient Selection */}
                <div className="form-group">
                  <label className="form-label text-lg">Select Recipient *</label>
                  <select
                    required
                    className="input"
                    style={{fontSize: '16px', padding: '12px'}}
                    value={selectedRecipient}
                    onChange={(e) => setSelectedRecipient(e.target.value)}
                  >
                    <option value="">-- Choose a recipient --</option>
                    {recipients.map((recipient) => (
                      <option key={recipient.id} value={recipient.id}>
                        {recipient.name}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Service Type */}
                <div className="form-group">
                  <label className="form-label text-lg">Service Type *</label>
                  <div className="space-y-3">
                    <label className="flex items-center p-3 border rounded hover:bg-ca-secondary-50" style={{cursor: 'pointer'}}>
                      <input
                        type="radio"
                        name="serviceType"
                        value="Personal Care"
                        checked={serviceType === 'Personal Care'}
                        onChange={(e) => setServiceType(e.target.value)}
                        className="mr-3"
                        style={{width: '20px', height: '20px'}}
                      />
                      <div>
                        <div className="font-semibold">Personal Care</div>
                        <div className="text-sm text-ca-primary-600">Bathing, grooming, dressing</div>
                      </div>
                    </label>

                    <label className="flex items-center p-3 border rounded hover:bg-ca-secondary-50" style={{cursor: 'pointer'}}>
                      <input
                        type="radio"
                        name="serviceType"
                        value="Domestic Services"
                        checked={serviceType === 'Domestic Services'}
                        onChange={(e) => setServiceType(e.target.value)}
                        className="mr-3"
                        style={{width: '20px', height: '20px'}}
                      />
                      <div>
                        <div className="font-semibold">Domestic Services</div>
                        <div className="text-sm text-ca-primary-600">Meal prep, housework, laundry</div>
                      </div>
                    </label>

                    <label className="flex items-center p-3 border rounded hover:bg-ca-secondary-50" style={{cursor: 'pointer'}}>
                      <input
                        type="radio"
                        name="serviceType"
                        value="Medical Accompaniment"
                        checked={serviceType === 'Medical Accompaniment'}
                        onChange={(e) => setServiceType(e.target.value)}
                        className="mr-3"
                        style={{width: '20px', height: '20px'}}
                      />
                      <div>
                        <div className="font-semibold">Medical Accompaniment</div>
                        <div className="text-sm text-ca-primary-600">Doctor visits, medical appointments</div>
                      </div>
                    </label>

                    <label className="flex items-center p-3 border rounded hover:bg-ca-secondary-50" style={{cursor: 'pointer'}}>
                      <input
                        type="radio"
                        name="serviceType"
                        value="Protective Supervision"
                        checked={serviceType === 'Protective Supervision'}
                        onChange={(e) => setServiceType(e.target.value)}
                        className="mr-3"
                        style={{width: '20px', height: '20px'}}
                      />
                      <div>
                        <div className="font-semibold">Protective Supervision</div>
                        <div className="text-sm text-ca-primary-600">For mentally impaired individuals</div>
                      </div>
                    </label>
                  </div>
                </div>

                {/* Location Verification */}
                <div className="form-group">
                  <label className="form-label text-lg">📍 Location Verification</label>
                  <div className="p-4 bg-ca-secondary-50 rounded">
                    {location ? (
                      <div>
                        <p className="text-green-700 font-semibold mb-2">✅ Location Verified</p>
                        <p className="text-sm text-ca-primary-600">
                          Coordinates: {location.latitude.toFixed(6)}, {location.longitude.toFixed(6)}
                        </p>
                        <p className="text-sm text-ca-primary-600">
                          Time: {new Date().toLocaleTimeString()}
                        </p>
                      </div>
                    ) : locationError ? (
                      <div>
                        <p className="text-red-700 font-semibold mb-2">❌ Location Error</p>
                        <p className="text-sm text-ca-primary-600">{locationError}</p>
                        <button 
                          type="button"
                          onClick={getLocation}
                          className="btn btn-secondary mt-2"
                        >
                          Retry Location
                        </button>
                      </div>
                    ) : (
                      <p className="text-ca-primary-600">Getting your location...</p>
                    )}
                  </div>
                </div>

                {/* Information Notice */}
                <div className="p-4 bg-blue-50 border-l-4 border-blue-500 rounded">
                  <p className="text-sm text-ca-primary-700">
                    <strong>ⓘ Important:</strong> Your location will be recorded for verification purposes as required by IHSS regulations.
                  </p>
                </div>

                {/* Submit Button */}
                <div className="text-center">
                  <button
                    type="submit"
                    disabled={loading || !location || !selectedRecipient || !serviceType}
                    className="btn btn-primary"
                    style={{fontSize: '18px', padding: '16px 48px'}}
                  >
                    {loading ? 'CHECKING IN...' : 'CHECK IN'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EVVCheckIn;


