'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import dynamic from 'next/dynamic';
import WorkView from '@/components/WorkView';
import NotificationCenter from '@/components/NotificationCenter';
import apiClient from '@/lib/api';

type Recipient = {
  id: number;
  name: string;
  status: string;
  authorizedHours: number;
  caseNumber: string;
};

function ProviderDashboardComponent() {
  const { user, logout, loading: authLoading } = useAuth();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [pendingActions, setPendingActions] = useState<Array<{ type: string; message: string; priority: string }>>([]);

  useEffect(() => {
    if (authLoading) return;
    if (!user || (user.role !== 'PROVIDER' && !user.roles?.includes('PROVIDER'))) {
      window.location.href = '/login';
      return;
    }
    fetchDashboardData();
  }, [user, authLoading]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      // Fetch assigned recipients from backend
      try {
        const recipientsData = await apiClient.get('/provider-recipient/my-recipients');
        if (recipientsData.data && recipientsData.data.length > 0) {
          const mappedRecipients = recipientsData.data.map((rel: any) => ({
            id: rel.id || 1,
            name: rel.recipientName || 'recipient1',
            status: rel.status || 'Active',
            authorizedHours: rel.authorizedHoursPerMonth || 40,
            caseNumber: rel.caseNumber || 'CASE-001',
          }));
          setRecipients(mappedRecipients);
        } else {
          setRecipients([{
            id: 1,
            name: 'recipient1',
            status: 'Active',
            authorizedHours: 40,
            caseNumber: 'CASE-001',
          }]);
        }
      } catch (apiError) {
        console.error('Error fetching recipients from API, using default:', apiError);
        setRecipients([{
          id: 1,
          name: 'recipient1',
          status: 'Active',
          authorizedHours: 40,
          caseNumber: 'CASE-001',
        }]);
      }
      
      setPendingActions([
        { type: 'timesheet', message: 'Submit timesheet for Sep 15-30 (Due: Oct 5)', priority: 'high' },
        { type: 'review', message: 'Review rejected timesheet for Aug 2025', priority: 'medium' }
      ]);
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading || authLoading || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1e3a8a] mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading Provider Dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-[#1e3a8a] text-white px-6 py-4 shadow-lg">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <div className="flex items-center space-x-4">
            <div className="inline-flex w-10 h-8 bg-white rounded-md text-[#1e3a8a] text-xs font-bold items-center justify-center">CA</div>
            <div>
              <h1 className="text-xl font-bold">IHSS Electronic Services Portal</h1>
              <p className="text-sm text-gray-200">In-Home Supportive Services</p>
            </div>
          </div>
          <div className="flex items-center space-x-4">
            <NotificationCenter userId={user?.username || ''} />
            <span className="text-sm">
              Welcome, <strong>{user?.username}</strong> (Provider)
            </span>
            <button onClick={logout} className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 font-medium">
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div
            className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center cursor-pointer hover:shadow-md transition-shadow"
            onClick={() => router.push('/provider/evv-checkin')}
          >
            <div className="text-4xl mb-2">📍</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">EVV CHECK-IN</h3>
            <p className="text-sm text-gray-600">Start your service visit</p>
          </div>

          <div
            className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center cursor-pointer hover:shadow-md transition-shadow"
            onClick={() => router.push('/provider/timesheets')}
          >
            <div className="text-4xl mb-2">📋</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">TIMESHEETS</h3>
            <p className="text-sm text-gray-600">Submit & view timesheets</p>
          </div>

          <div
            className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center cursor-pointer hover:shadow-md transition-shadow"
            onClick={() => router.push('/provider/payments')}
          >
            <div className="text-4xl mb-2">💰</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">PAYMENT HISTORY</h3>
            <p className="text-sm text-gray-600">View your payments</p>
          </div>

          <div
            className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center cursor-pointer hover:shadow-md transition-shadow"
            onClick={() => router.push('/provider/profile')}
          >
            <div className="text-4xl mb-2">👤</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">MY PROFILE</h3>
            <p className="text-sm text-gray-600">Update my address & details</p>
          </div>
        </div>

        {/* WorkView - Tasks */}
        <div className="mb-6">
          <WorkView username={user?.username || ''} />
        </div>

        {/* My Recipients */}
        <div className="bg-white border border-gray-300 rounded-lg shadow-sm mb-6">
          <div className="bg-[#1e3a8a] px-6 py-4 rounded-t-lg">
            <h2 className="text-lg font-semibold text-white">📋 MY RECIPIENTS</h2>
          </div>
          <div className="p-6">
            {recipients.length === 0 ? (
              <p className="text-center text-gray-600 py-4">No recipients assigned</p>
            ) : (
              <div className="space-y-3">
                {recipients.map((recipient) => (
                  <div key={recipient.id} className="flex justify-between items-center p-4 bg-gray-50 rounded border border-gray-200">
                    <div>
                      <h3 className="font-semibold text-gray-900">{recipient.name}</h3>
                      <p className="text-sm text-gray-600">
                        Status: <span className="px-2 py-1 bg-green-100 text-green-800 rounded text-xs font-medium">{recipient.status}</span>
                        {' • '} Authorized: {recipient.authorizedHours} hours/month
                      </p>
                    </div>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => router.push(`/provider/timesheet/new/${recipient.id}`)}
                        className="px-4 py-2 bg-[#1e3a8a] text-white rounded hover:bg-[#1e40af] font-medium"
                      >
                        Submit Timesheet
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Pending Actions */}
        <div className="bg-white border border-gray-300 rounded-lg shadow-sm">
          <div className="bg-[#1e3a8a] px-6 py-4 rounded-t-lg">
            <h2 className="text-lg font-semibold text-white">⏰ PENDING ACTIONS</h2>
          </div>
          <div className="p-6">
            {pendingActions.length === 0 ? (
              <p className="text-center text-gray-600 py-4">No pending actions</p>
            ) : (
              <div className="space-y-3">
                {pendingActions.map((action, index) => (
                  <div
                    key={index}
                    className={`p-4 rounded border-l-4 ${
                      action.priority === 'high'
                        ? 'bg-red-50 border-red-500'
                        : 'bg-yellow-50 border-yellow-500'
                    }`}
                  >
                    <p className="text-gray-900">{action.message}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default dynamic(() => Promise.resolve(ProviderDashboardComponent), { ssr: false });

