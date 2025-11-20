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
  status: string;
};

function CaseWorkerDashboardComponent() {
  const { user, logout, loading: authLoading } = useAuth();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalCases: 0,
    pendingTimesheets: 0,
    evvViolations: 0,
    dueReassessments: 0
  });
  const [pendingTimesheets, setPendingTimesheets] = useState<Timesheet[]>([]);

  useEffect(() => {
    if (authLoading) return;
    if (!user || (user.role !== 'CASE_WORKER' && !user.roles?.includes('CASE_WORKER'))) {
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
        const submitted = timesheets.filter((ts: Timesheet) => ts.status === 'SUBMITTED');
        setPendingTimesheets(submitted);
        
        // Update stats
        setStats({
          totalCases: 145,
          pendingTimesheets: submitted.length,
          evvViolations: 3,
          dueReassessments: 5
        });
      } catch (err) {
        console.error('Error fetching timesheets:', err);
        setPendingTimesheets([]);
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
          <p className="mt-4 text-gray-600">Loading Case Worker Dashboard...</p>
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
              <h1 className="text-xl font-bold">CMIPS - Case Worker Portal</h1>
              <p className="text-sm text-gray-200">Sacramento County - District Office 1</p>
            </div>
          </div>
          <div className="flex items-center space-x-4">
            <NotificationCenter userId={user?.username || ''} />
            <span className="text-sm">
              <strong>{user?.username}</strong> (Case Worker)
            </span>
            <button onClick={logout} className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 font-medium">
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Statistics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <div className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center">
            <div className="text-3xl font-bold text-[#1e3a8a]">{stats.totalCases}</div>
            <p className="text-sm text-gray-600 mt-1">CASES</p>
          </div>
          
          <div className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center">
            <div className="text-3xl font-bold text-yellow-600">{stats.pendingTimesheets}</div>
            <p className="text-sm text-gray-600 mt-1">TIMESHEETS PENDING</p>
          </div>
          
          <div className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center">
            <div className="text-3xl font-bold text-red-600">{stats.evvViolations}</div>
            <p className="text-sm text-gray-600 mt-1">EVV VIOLATIONS</p>
          </div>
          
          <div className="bg-white border border-gray-300 rounded-lg shadow-sm p-6 text-center">
            <div className="text-3xl font-bold text-blue-600">{stats.dueReassessments}</div>
            <p className="text-sm text-gray-600 mt-1">DUE REASSESSMENTS</p>
          </div>
        </div>

        {/* WorkView - Tasks */}
        <div className="mb-6">
          <WorkView username={user?.username || ''} />
        </div>

        {/* Priority Actions */}
        <div className="bg-white border border-gray-300 rounded-lg shadow-sm mb-6">
          <div className="bg-[#1e3a8a] px-6 py-4 rounded-t-lg">
            <h2 className="text-lg font-semibold text-white">🚨 PRIORITY ACTIONS</h2>
          </div>
          <div className="p-6">
            <div className="space-y-2">
              <div className="p-3 bg-yellow-50 border-l-4 border-yellow-500 rounded">
                <p className="text-gray-900">• {stats.pendingTimesheets} Timesheets Pending Review</p>
              </div>
              <div className="p-3 bg-red-50 border-l-4 border-red-500 rounded">
                <p className="text-gray-900">• {stats.evvViolations} EVV Violations Need Resolution</p>
              </div>
              <div className="p-3 bg-blue-50 border-l-4 border-blue-500 rounded">
                <p className="text-gray-900">• {stats.dueReassessments} Cases Due for Reassessment</p>
              </div>
            </div>
          </div>
        </div>

        {/* Pending Timesheets */}
        <div className="bg-white border border-gray-300 rounded-lg shadow-sm">
          <div className="bg-[#1e3a8a] px-6 py-4 rounded-t-lg">
            <h2 className="text-lg font-semibold text-white">📊 PENDING TIMESHEETS</h2>
          </div>
          <div className="p-6">
            {pendingTimesheets.length === 0 ? (
              <p className="text-center text-gray-600 py-4">No timesheets pending review</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Provider</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Recipient</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Pay Period</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Hours</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {pendingTimesheets.map((timesheet) => (
                      <tr key={timesheet.id}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{timesheet.employeeName}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">-</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{timesheet.payPeriodStart} to {timesheet.payPeriodEnd}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900">{timesheet.totalHours}</td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="px-2 py-1 text-xs font-medium bg-yellow-100 text-yellow-800 rounded-full">{timesheet.status}</span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm">
                          <button
                            onClick={() => router.push(`/caseworker/timesheet/${timesheet.id}`)}
                            className="px-3 py-1 bg-[#1e3a8a] text-white rounded hover:bg-[#1e40af] font-medium text-sm"
                          >
                            Review
                          </button>
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
  );
}

export default dynamic(() => Promise.resolve(CaseWorkerDashboardComponent), { ssr: false });
