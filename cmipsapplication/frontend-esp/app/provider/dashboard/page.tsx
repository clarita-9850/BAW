'use client';

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import apiClient from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";

type Timesheet = {
  id: number;
  payPeriodStart?: string;
  payPeriodEnd?: string;
  totalHours?: number;
  status?: string;
};

type TimesheetResponse = {
  content: Timesheet[];
  totalElements: number;
  numberOfElements: number;
  allowedActions: string[];
};

export default function ProviderDashboard() {
  const router = useRouter();
  const { user, loading: authLoading } = useAuth();

  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [recentTimesheets, setRecentTimesheets] = useState<Timesheet[]>([]);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || authLoading) return;
    if (!user || user.role !== "PROVIDER") {
      router.push("/login");
      return;
    }
    fetchDashboardData();
  }, [mounted, authLoading, user, router]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const resp = await apiClient.get<TimesheetResponse>("/timesheets");
      const data = resp.data;
      const list = data.content || [];
      setRecentTimesheets(list.slice(0, 3));
    } catch (error) {
      console.error("Error fetching provider dashboard data:", error);
      setRecentTimesheets([]);
    } finally {
      setLoading(false);
    }
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-700 mx-auto" />
          <p className="mt-4 text-gray-600 text-sm">
            Loading Provider Dashboard...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-5xl mx-auto py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-2">
        Welcome, {user.username}
      </h1>
      <p className="text-sm text-gray-600 mb-6">
        Enter time, submit timesheets, and view your payments.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <button
          onClick={() => router.push("/provider/timesheets")}
          className="h-full w-full bg-white border rounded-lg p-4 text-left shadow-sm hover:shadow-md"
        >
          <div className="text-3xl mb-2">⏱️</div>
          <h2 className="font-semibold mb-1">Enter / Review Time</h2>
          <p className="text-xs text-gray-600">
            Create and submit timesheets for your IHSS recipients.
          </p>
        </button>
        <button
          onClick={() => router.push("/provider/payments")}
          className="h-full w-full bg-white border rounded-lg p-4 text-left shadow-sm hover:shadow-md"
        >
          <div className="text-3xl mb-2">💵</div>
          <h2 className="font-semibold mb-1">Payment History</h2>
          <p className="text-xs text-gray-600">
            View recent payments and download pay information.
          </p>
        </button>
        <button
          onClick={() => router.push("/provider/recipients")}
          className="h-full w-full bg-white border rounded-lg p-4 text-left shadow-sm hover:shadow-md"
        >
          <div className="text-3xl mb-2">👥</div>
          <h2 className="font-semibold mb-1">My Recipients</h2>
          <p className="text-xs text-gray-600">
            See the recipients you currently serve.
          </p>
        </button>
      </div>

      <div className="bg-white border rounded-lg shadow-sm">
        <div className="border-b px-4 py-3 flex justify-between items-center">
          <h2 className="text-sm font-semibold text-gray-800">
            Recent Timesheets
          </h2>
          <button
            onClick={fetchDashboardData}
            className="text-xs text-emerald-700 hover:underline"
          >
            Refresh
          </button>
        </div>
        <div className="p-4">
          {recentTimesheets.length === 0 ? (
            <p className="text-sm text-gray-500 text-center py-4">
              You have no recent timesheets.
            </p>
          ) : (
            <div className="space-y-3">
              {recentTimesheets.map((ts) => (
                <div
                  key={ts.id}
                  className="border rounded-md px-3 py-2 flex justify-between items-center"
                >
                  <div>
                    <p className="text-sm font-semibold">
                      Pay Period: {ts.payPeriodStart} to {ts.payPeriodEnd}
                    </p>
                    <p className="text-xs text-gray-600">
                      Total Hours: {ts.totalHours ?? 0} • Status:{" "}
                      {ts.status ?? "N/A"}
                    </p>
                  </div>
                  <button
                    onClick={() => router.push(`/provider/timesheets`)}
                    className="text-xs px-3 py-1 rounded-md bg-emerald-700 text-white font-semibold hover:bg-emerald-800"
                  >
                    View
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}









