import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

const PaymentManagement = () => {
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (authLoading) return; // Wait for auth to initialize
    if (!user) {
      navigate('/login');
    }
  }, [user, navigate, authLoading]);

  return (
    <div className="min-h-screen bg-ca-secondary-50">
      <div className="ca-header">
        <div className="container">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-4">
              <div className="ca-logo">CA</div>
              <h1 className="text-xl font-bold text-ca-primary-900">Payment Management</h1>
            </div>
            <button onClick={() => navigate('/')} className="btn btn-outline">
              Back to Dashboard
            </button>
          </div>
        </div>
      </div>
      
      <div className="container py-8">
        <div className="card">
          <div className="card-body text-center py-12">
            <p className="text-ca-primary-600">Payment Management features coming soon...</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentManagement;
