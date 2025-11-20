import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { apiClient, API_ENDPOINTS } from '../config/api';

interface Payment {
  id: number;
  paymentId: string;
  paymentAmount: number;
  paymentType: string;
  paymentMethod: string;
  recipientId: string;
  paymentDate: string;
  status: string;
  referenceNumber?: string;
  notes?: string;
}

const PaymentManagement: React.FC = () => {
  const { user } = useAuth();
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingPayment, setEditingPayment] = useState<Payment | null>(null);
  const [formData, setFormData] = useState({
    paymentId: '',
    paymentAmount: 0,
    paymentType: 'SERVICE',
    paymentMethod: 'CHECK',
    recipientId: '',
    paymentDate: '',
    status: 'PENDING',
    referenceNumber: '',
    notes: ''
  });

  useEffect(() => {
    fetchPayments();
  }, []);

  const fetchPayments = async () => {
    try {
      setLoading(true);
      // Mock data for now - in real implementation, this would call the backend
      const mockPayments: Payment[] = [
        {
          id: 1,
          paymentId: 'PAY-2024-001',
          paymentAmount: 1500.00,
          paymentType: 'SERVICE',
          paymentMethod: 'CHECK',
          recipientId: 'RECIPIENT-001',
          paymentDate: '2024-01-15',
          status: 'APPROVED',
          referenceNumber: 'CHK-001234',
          notes: 'Monthly service payment'
        },
        {
          id: 2,
          paymentId: 'PAY-2024-002',
          paymentAmount: 2500.00,
          paymentType: 'REIMBURSEMENT',
          paymentMethod: 'DIRECT_DEPOSIT',
          recipientId: 'RECIPIENT-002',
          paymentDate: '2024-01-16',
          status: 'PROCESSING',
          referenceNumber: 'DD-002345',
          notes: 'Travel reimbursement'
        },
        {
          id: 3,
          paymentId: 'PAY-2024-003',
          paymentAmount: 800.00,
          paymentType: 'STIPEND',
          paymentMethod: 'CHECK',
          recipientId: 'RECIPIENT-003',
          paymentDate: '2024-01-17',
          status: 'PENDING',
          referenceNumber: 'CHK-003456',
          notes: 'Monthly stipend'
        }
      ];
      setPayments(mockPayments);
    } catch (err) {
      setError('Failed to fetch payments');
      console.error('Error fetching payments:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingPayment) {
        // Update existing payment
        const updatedPayment = { ...editingPayment, ...formData };
        setPayments(payments.map(p => p.id === editingPayment.id ? updatedPayment : p));
      } else {
        // Create new payment
        const newPayment: Payment = {
          id: Date.now(),
          ...formData
        };
        setPayments([...payments, newPayment]);
      }

      setShowForm(false);
      setEditingPayment(null);
      setFormData({
        paymentId: '',
        paymentAmount: 0,
        paymentType: 'SERVICE',
        paymentMethod: 'CHECK',
        recipientId: '',
        paymentDate: '',
        status: 'PENDING',
        referenceNumber: '',
        notes: ''
      });
    } catch (err) {
      setError('Failed to save payment');
      console.error('Error saving payment:', err);
    }
  };

  const handleEdit = (payment: Payment) => {
    setEditingPayment(payment);
    setFormData({
      paymentId: payment.paymentId,
      paymentAmount: payment.paymentAmount,
      paymentType: payment.paymentType,
      paymentMethod: payment.paymentMethod,
      recipientId: payment.recipientId,
      paymentDate: payment.paymentDate,
      status: payment.status,
      referenceNumber: payment.referenceNumber || '',
      notes: payment.notes || ''
    });
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this payment?')) {
      setPayments(payments.filter(p => p.id !== id));
    }
  };

  const handleStatusChange = async (id: number, newStatus: string) => {
    setPayments(payments.map(p => 
      p.id === id ? { ...p, status: newStatus } : p
    ));
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return 'badge-warning';
      case 'APPROVED': return 'badge-success';
      case 'PROCESSING': return 'badge-primary';
      case 'COMPLETED': return 'badge-success';
      case 'CANCELLED': return 'badge-danger';
      case 'REJECTED': return 'badge-danger';
      default: return 'badge-secondary';
    }
  };

  const getPaymentTypeColor = (type: string) => {
    switch (type) {
      case 'SERVICE': return 'badge-primary';
      case 'REIMBURSEMENT': return 'badge-success';
      case 'STIPEND': return 'badge-warning';
      case 'BONUS': return 'badge-danger';
      default: return 'badge-secondary';
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-ca-highlight-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-ca-secondary-50">
      <div className="container py-6">
        <div className="px-4 py-6 sm:px-0">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold text-ca-primary-900">Payment Management</h1>
            <button
              onClick={() => setShowForm(true)}
              className="btn btn-primary"
            >
              Create New Payment
            </button>
          </div>

          {error && (
            <div className="alert alert-error mb-6">
              {error}
            </div>
          )}

          {/* Payment Form */}
          {showForm && (
            <div className="card mb-6">
              <div className="card-header">
                <h2 className="text-lg font-medium text-ca-primary-900">
                  {editingPayment ? 'Edit Payment' : 'Create New Payment'}
                </h2>
              </div>
              <div className="card-body">
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">Payment ID</label>
                      <input
                        type="text"
                        required
                        className="input mt-1"
                        value={formData.paymentId}
                        onChange={(e) => setFormData({ ...formData, paymentId: e.target.value })}
                      />
                    </div>
                    
                    <div>
                      <label className="form-label">Payment Amount</label>
                      <input
                        type="number"
                        step="0.01"
                        min="0"
                        required
                        className="input mt-1"
                        value={formData.paymentAmount}
                        onChange={(e) => setFormData({ ...formData, paymentAmount: parseFloat(e.target.value) || 0 })}
                      />
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">Payment Type</label>
                      <select
                        required
                        className="input mt-1"
                        value={formData.paymentType}
                        onChange={(e) => setFormData({ ...formData, paymentType: e.target.value })}
                      >
                        <option value="SERVICE">Service</option>
                        <option value="REIMBURSEMENT">Reimbursement</option>
                        <option value="STIPEND">Stipend</option>
                        <option value="BONUS">Bonus</option>
                      </select>
                    </div>
                    
                    <div>
                      <label className="form-label">Payment Method</label>
                      <select
                        required
                        className="input mt-1"
                        value={formData.paymentMethod}
                        onChange={(e) => setFormData({ ...formData, paymentMethod: e.target.value })}
                      >
                        <option value="CHECK">Check</option>
                        <option value="DIRECT_DEPOSIT">Direct Deposit</option>
                        <option value="WIRE_TRANSFER">Wire Transfer</option>
                        <option value="CASH">Cash</option>
                      </select>
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">Recipient ID</label>
                      <input
                        type="text"
                        required
                        className="input mt-1"
                        value={formData.recipientId}
                        onChange={(e) => setFormData({ ...formData, recipientId: e.target.value })}
                      />
                    </div>
                    
                    <div>
                      <label className="form-label">Payment Date</label>
                      <input
                        type="date"
                        required
                        className="input mt-1"
                        value={formData.paymentDate}
                        onChange={(e) => setFormData({ ...formData, paymentDate: e.target.value })}
                      />
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">Status</label>
                      <select
                        required
                        className="input mt-1"
                        value={formData.status}
                        onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                      >
                        <option value="PENDING">Pending</option>
                        <option value="APPROVED">Approved</option>
                        <option value="PROCESSING">Processing</option>
                        <option value="COMPLETED">Completed</option>
                        <option value="CANCELLED">Cancelled</option>
                        <option value="REJECTED">Rejected</option>
                      </select>
                    </div>
                    
                    <div>
                      <label className="form-label">Reference Number</label>
                      <input
                        type="text"
                        className="input mt-1"
                        value={formData.referenceNumber}
                        onChange={(e) => setFormData({ ...formData, referenceNumber: e.target.value })}
                      />
                    </div>
                  </div>
                  
                  <div>
                    <label className="form-label">Notes</label>
                    <textarea
                      className="input mt-1"
                      rows={3}
                      value={formData.notes}
                      onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                    />
                  </div>
                  
                  <div className="flex justify-end space-x-3">
                    <button
                      type="button"
                      onClick={() => {
                        setShowForm(false);
                        setEditingPayment(null);
                        setFormData({
                          paymentId: '',
                          paymentAmount: 0,
                          paymentType: 'SERVICE',
                          paymentMethod: 'CHECK',
                          recipientId: '',
                          paymentDate: '',
                          status: 'PENDING',
                          referenceNumber: '',
                          notes: ''
                        });
                      }}
                      className="btn btn-secondary"
                    >
                      Cancel
                    </button>
                    <button type="submit" className="btn btn-primary">
                      {editingPayment ? 'Update' : 'Create'} Payment
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* Payments List */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-medium text-ca-primary-900">Payments</h2>
            </div>
            <div className="card-body">
              {payments.length === 0 ? (
                <div className="text-center py-8 text-ca-primary-500">
                  No payments found. Create your first payment to get started.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Payment ID</th>
                        <th>Amount</th>
                        <th>Type</th>
                        <th>Method</th>
                        <th>Recipient</th>
                        <th>Date</th>
                        <th>Status</th>
                        <th>Reference</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {payments.map((payment) => (
                        <tr key={payment.id}>
                          <td>
                            <span className="font-medium text-ca-primary-900">{payment.paymentId}</span>
                          </td>
                          <td>
                            <span className="font-medium text-ca-primary-900">
                              {formatCurrency(payment.paymentAmount)}
                            </span>
                          </td>
                          <td>
                            <span className={`badge ${getPaymentTypeColor(payment.paymentType)}`}>
                              {payment.paymentType}
                            </span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-900">
                              {payment.paymentMethod.replace('_', ' ')}
                            </span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-900">{payment.recipientId}</span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-600">{payment.paymentDate}</span>
                          </td>
                          <td>
                            <span className={`badge ${getStatusColor(payment.status)}`}>
                              {payment.status}
                            </span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-600">
                              {payment.referenceNumber || '-'}
                            </span>
                          </td>
                          <td>
                            <div className="flex space-x-2">
                              <button
                                onClick={() => handleEdit(payment)}
                                className="text-ca-highlight-600 hover:text-ca-highlight-900 text-sm font-medium"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleDelete(payment.id)}
                                className="text-bs-danger hover:text-red-900 text-sm font-medium"
                              >
                                Delete
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentManagement;


