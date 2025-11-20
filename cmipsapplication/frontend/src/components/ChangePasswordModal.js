import React, { useState } from 'react';
import axios from 'axios';
import '../styles/ChangePasswordModal.css';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081';

const ChangePasswordModal = ({ isOpen, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError(''); // Clear error when user types
  };

  const validateForm = () => {
    if (!formData.currentPassword) {
      setError('Current password is required');
      return false;
    }
    
    if (!formData.newPassword) {
      setError('New password is required');
      return false;
    }
    
    if (formData.newPassword.length < 8) {
      setError('New password must be at least 8 characters long');
      return false;
    }
    
    if (formData.newPassword !== formData.confirmPassword) {
      setError('New passwords do not match');
      return false;
    }
    
    if (formData.currentPassword === formData.newPassword) {
      setError('New password must be different from current password');
      return false;
    }
    
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    if (!validateForm()) {
      return;
    }
    
    setLoading(true);
    
    try {
      // Get token from localStorage
      const token = localStorage.getItem('token');
      
      const response = await axios.post(
        `${API_BASE_URL}/api/auth/change-password`,
        {
          currentPassword: formData.currentPassword,
          newPassword: formData.newPassword,
        },
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        }
      );
      
      if (response.data.success) {
        // Clear form
        setFormData({
          currentPassword: '',
          newPassword: '',
          confirmPassword: '',
        });
        
        // Call success callback first (this will handle navigation)
        if (onSuccess) {
          onSuccess();
        }
        
        // Close modal
        if (onClose) {
          onClose();
        }
      }
    } catch (error) {
      console.error('Password change error:', error);
      if (error.response?.data?.error) {
        setError(error.response.data.error);
      } else {
        setError('Failed to change password. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="change-password-modal-overlay" onClick={onClose}>
      <div className="change-password-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="change-password-modal-header">
          <h2>Change Password Required</h2>
          <p className="change-password-modal-subtitle">
            You must change your password before continuing.
          </p>
        </div>
        
        <form onSubmit={handleSubmit} className="change-password-form">
          {error && (
            <div className="change-password-error">
              {error}
            </div>
          )}
          
          <div className="change-password-form-group">
            <label htmlFor="currentPassword">Current Password *</label>
            <input
              type="password"
              id="currentPassword"
              name="currentPassword"
              value={formData.currentPassword}
              onChange={handleChange}
              required
              autoFocus
              disabled={loading}
            />
          </div>
          
          <div className="change-password-form-group">
            <label htmlFor="newPassword">New Password *</label>
            <input
              type="password"
              id="newPassword"
              name="newPassword"
              value={formData.newPassword}
              onChange={handleChange}
              required
              disabled={loading}
              minLength={8}
            />
            <small>Must be at least 8 characters long</small>
          </div>
          
          <div className="change-password-form-group">
            <label htmlFor="confirmPassword">Confirm New Password *</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
              disabled={loading}
              minLength={8}
            />
          </div>
          
          <div className="change-password-form-actions">
            <button
              type="submit"
              className="change-password-btn-primary"
              disabled={loading}
            >
              {loading ? 'Changing Password...' : 'Change Password'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ChangePasswordModal;

