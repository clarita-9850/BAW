'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';

type Payment = {
  id: number;
  payPeriod: string;
  grossAmount: number;
  deductions: number;
  netAmount: number;
  status: string;
  paymentDate: string;
  paymentMethod: string;
};

export default function ProviderPaymentsPage() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [payments, setPayments] = useState<Payment[]>([]);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || authLoading) return;
    if (!user) {
      window.location.href = '/login';
      return;
    }
    // Simulated payment data - in production this would come from API
    setPayments([
      {
        id: 1,
        payPeriod: '2024-12-01 to 2024-12-15',
        grossAmount: 1200.00,
        deductions: 180.00,
        netAmount: 1020.00,
        status: 'PAID',
        paymentDate: '2024-12-20',
        paymentMethod: 'Direct Deposit'
      },
      {
        id: 2,
        payPeriod: '2024-11-16 to 2024-11-30',
        grossAmount: 1150.00,
        deductions: 172.50,
        netAmount: 977.50,
        status: 'PAID',
        paymentDate: '2024-12-05',
        paymentMethod: 'Direct Deposit'
      },
      {
        id: 3,
        payPeriod: '2024-11-01 to 2024-11-15',
        grossAmount: 1100.00,
        deductions: 165.00,
        netAmount: 935.00,
        status: 'PAID',
        paymentDate: '2024-11-20',
        paymentMethod: 'Direct Deposit'
      },
    ]);
    setLoading(false);
  }, [user, authLoading, mounted]);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
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

  const totalPaid = payments.filter(p => p.status === 'PAID').reduce((sum, p) => sum + p.netAmount, 0);

  return (
    <div>
      <div className="mb-4">
        <h1 className="h3 mb-1">Payment History</h1>
        <p className="text-muted">View your payment records and statements</p>
      </div>

      {/* Summary Cards */}
      <div className="row mb-4">
        <div className="col-md-4">
          <div className="card text-center">
            <div className="card-body">
              <h6 className="text-muted">Total Paid (Last 3 months)</h6>
              <h3 className="text-success">{formatCurrency(totalPaid)}</h3>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card text-center">
            <div className="card-body">
              <h6 className="text-muted">Payments Received</h6>
              <h3>{payments.filter(p => p.status === 'PAID').length}</h3>
            </div>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card text-center">
            <div className="card-body">
              <h6 className="text-muted">Payment Method</h6>
              <h3>Direct Deposit</h3>
            </div>
          </div>
        </div>
      </div>

      {/* Payments Table */}
      <div className="card">
        <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h5 className="mb-0" style={{ color: 'white' }}>Payment Records</h5>
        </div>
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table table-hover mb-0">
              <thead className="table-light">
                <tr>
                  <th>Pay Period</th>
                  <th>Gross</th>
                  <th>Deductions</th>
                  <th>Net</th>
                  <th>Payment Date</th>
                  <th>Method</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {payments.map((payment) => (
                  <tr key={payment.id}>
                    <td>{payment.payPeriod}</td>
                    <td>{formatCurrency(payment.grossAmount)}</td>
                    <td className="text-danger">-{formatCurrency(payment.deductions)}</td>
                    <td className="fw-bold">{formatCurrency(payment.netAmount)}</td>
                    <td>{new Date(payment.paymentDate).toLocaleDateString()}</td>
                    <td>{payment.paymentMethod}</td>
                    <td>
                      <span className={`badge ${payment.status === 'PAID' ? 'bg-success' : 'bg-warning'}`}>
                        {payment.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Help Text */}
      <div className="alert alert-info mt-4">
        <h6>Payment Information</h6>
        <ul className="mb-0 small">
          <li>Payments are processed bi-weekly</li>
          <li>Direct deposit typically takes 1-2 business days</li>
          <li>For payment issues, contact your county office</li>
          <li>Tax documents (W-2) are available in January</li>
        </ul>
      </div>
    </div>
  );
}
