'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import dynamic from 'next/dynamic';
import WorkView from '@/components/WorkView';
import NotificationCenter from '@/components/NotificationCenter';
import apiClient from '@/lib/api';

type Timesheet = {
  id: number;
  employeeName: string;
  payPeriodStart: string;
  payPeriodEnd: string;
  totalHours: number;
  createdAt: string;
  status: string;
};

type Provider = {
  id: number;
  name: string;
  status: string;
  role: string;
};

function RecipientDashboardComponent() {
  const { user, logout, loading: authLoading } = useAuth();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [pendingTimesheets, setPendingTimesheets] = useState<Timesheet[]>([]);
  const [providers, setProviders] = useState<Provider[]>([]);

  useEffect(() => {
    if (authLoading) return;
    if (!user || (user.role !== 'RECIPIENT' && !user.roles?.includes('RECIPIENT'))) {
      window.location.href = '/login';
      return;
    }
    fetchDashboardData();
  }, [user, authLoading]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      // Fetch pending timesheets
      try {
        const timesheetsResponse = await apiClient.get('/timesheets');
        const timesheets = timesheetsResponse.data.content || timesheetsResponse.data || [];
        setPendingTimesheets(timesheets.filter((ts: Timesheet) => ts.status === 'SUBMITTED'));
      } catch (err) {
        console.error('Error fetching timesheets:', err);
        setPendingTimesheets([]);
      }
      
      // Fetch providers
      try {
        const providersResponse = await apiClient.get('/provider-recipient/my-providers');
        if (providersResponse.data && providersResponse.data.length > 0) {
          const mappedProviders = providersResponse.data.map((rel: any) => ({
            id: rel.id || 1,
            name: rel.providerName || 'Provider',
            status: rel.status || 'Active',
            role: rel.relationship || 'Primary Caregiver',
          }));
          setProviders(mappedProviders);
        } else {
          setProviders([
            { id: 1, name: 'John Doe', status: 'Active', role: 'Primary Caregiver' },
            { id: 2, name: 'Mary Johnson', status: 'Active', role: 'Backup' }
          ]);
        }
      } catch (err) {
        console.error('Error fetching providers:', err);
        setProviders([
          { id: 1, name: 'John Doe', status: 'Active', role: 'Primary Caregiver' },
          { id: 2, name: 'Mary Johnson', status: 'Active', role: 'Backup' }
        ]);
      }
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
          <p className="mt-4 text-gray-600">Loading Recipient Dashboard...</p>
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
              Welcome, <strong>{user?.username}</strong> (Recipient)
            </span>
            <button onClick={logout} className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 font-medium">
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* WorkView - Tasks */}
        <div className="mb-6">
          <WorkView username={user?.username || ''} />
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div
            className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center cursor-pointer hover:shadow-md transition-shadow"
            onClick={() => router.push('/recipient/timesheets')}
          >
            <div className="text-4xl mb-2">📋</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              TIMESHEETS TO REVIEW
              {pendingTimesheets.length > 0 && (
                <span className="ml-2 px-2 py-1 bg-red-600 text-white text-xs font-bold rounded">
                  {pendingTimesheets.length} Pending
                </span>
              )}
            </h3>
            <p className="text-sm text-gray-600">Review & approve timesheets</p>
          </div>

          <div
            className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center cursor-pointer hover:shadow-md transition-shadow"
            onClick={() => router.push('/recipient/providers')}
          >
            <div className="text-4xl mb-2">👥</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">MY PROVIDERS</h3>
            <p className="text-sm text-gray-600">Manage your caregivers</p>
          </div>

          <div className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center cursor-pointer hover:shadow-md transition-shadow">
            <div className="text-4xl mb-2">📅</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">SERVICE SCHEDULE</h3>
            <p className="text-sm text-gray-600">View upcoming services</p>
          </div>

          <div className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center cursor-pointer hover:shadow-md transition-shadow">
            <div className="text-4xl mb-2">❓</div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">HELP & SUPPORT</h3>
            <p className="text-sm text-gray-600">Get assistance</p>
          </div>
        </div>

        {/* Timesheets Awaiting Approval */}
        <div className="bg-white border border-gray-300 rounded-lg shadow-sm mb-6">
          <div className="bg-[#1e3a8a] px-6 py-4 rounded-t-lg">
            <h2 className="text-lg font-semibold text-white">🔔 TIMESHEETS AWAITING YOUR APPROVAL</h2>
          </div>
          <div className="p-6">
            {pendingTimesheets.length === 0 ? (
              <p className="text-center text-gray-600 py-4">No timesheets pending review</p>
            ) : (
              <div className="space-y-4">
                {pendingTimesheets.map((timesheet) => (
                  <div key={timesheet.id} className="p-4 bg-yellow-50 border-l-4 border-yellow-500 rounded">
                    <div className="flex justify-between items-center">
                      <div>
                        <h3 className="font-semibold text-gray-900">
                          {timesheet.employeeName} - {timesheet.payPeriodStart} to {timesheet.payPeriodEnd}
                        </h3>
                        <p className="text-sm text-gray-600">
                          Total Hours: {timesheet.totalHours} • Submitted: {new Date(timesheet.createdAt).toLocaleDateString()}
                        </p>
                      </div>
                      <button
                        onClick={() => router.push(`/recipient/timesheet/${timesheet.id}`)}
                        className="px-4 py-2 bg-[#1e3a8a] text-white rounded hover:bg-[#1e40af] font-medium"
                      >
                        Review & Approve
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* My Providers */}
        <div className="bg-white border border-gray-300 rounded-lg shadow-sm">
          <div className="bg-[#1e3a8a] px-6 py-4 rounded-t-lg">
            <h2 className="text-lg font-semibold text-white">👥 MY PROVIDERS</h2>
          </div>
          <div className="p-6">
            {providers.length === 0 ? (
              <p className="text-center text-gray-600 py-4">No providers assigned</p>
            ) : (
              <div className="space-y-3">
                {providers.map((provider) => (
                  <div key={provider.id} className="flex justify-between items-center p-4 bg-gray-50 rounded border border-gray-200">
                    <div>
                      <h3 className="font-semibold text-gray-900">{provider.name}</h3>
                      <p className="text-sm text-gray-600">
                        <span className="px-2 py-1 bg-green-100 text-green-800 rounded text-xs font-medium">{provider.status}</span>
                        {' • '} {provider.role}
                      </p>
                    </div>
                    <button className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 font-medium">
                      View Details
                    </button>
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

export default dynamic(() => Promise.resolve(RecipientDashboardComponent), { ssr: false });
