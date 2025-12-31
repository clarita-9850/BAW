'use client';

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import apiClient from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";

type Timesheet = {
  id: number;
  employeeName?: string;
  payPeriodStart?: string;
  payPeriodEnd?: string;
  totalHours?: number;
  status?: string;
  submittedAt?: string;
};

type TimesheetResponse = {
  content: Timesheet[];
  totalElements: number;
  numberOfElements: number;
  allowedActions: string[];
};

export default function RecipientDashboard() {
  const router = useRouter();
  const { user, loading: authLoading } = useAuth();

  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [pendingTimesheets, setPendingTimesheets] = useState<Timesheet[]>([]);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || authLoading) return;
    if (!user || user.role !== "RECIPIENT") {
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
      const timesheets = data.content || [];
      setPendingTimesheets(
        timesheets.filter((ts) => ts.status === "SUBMITTED")
      );
    } catch (error) {
      console.error("Error fetching recipient dashboard data:", error);
      setPendingTimesheets([]);
    } finally {
      setLoading(false);
    }
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1e3a8a] mx-auto" />
          <p className="mt-4 text-gray-600 text-sm">
            Loading Recipient Dashboard...
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
        Review timesheets, track payments, and manage your IHSS providers.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <button
          onClick={() => router.push("/recipient/timesheets")}
          className="h-full w-full bg-white border rounded-lg p-4 text-left shadow-sm hover:shadow-md"
        >
          <div className="text-3xl mb-2">📋</div>
          <h2 className="font-semibold mb-1">Review Timesheets</h2>
          <p className="text-xs text-gray-600">
            View and approve provider timesheets that need your attention.
          </p>
        </button>
        <button
          onClick={() => router.push("/recipient/payments")}
          className="h-full w-full bg-white border rounded-lg p-4 text-left shadow-sm hover:shadow-md"
        >
          <div className="text-3xl mb-2">💵</div>
          <h2 className="font-semibold mb-1">Payment History</h2>
          <p className="text-xs text-gray-600">
            See payments made on your IHSS case to your providers.
          </p>
        </button>
        <button
          onClick={() => router.push("/recipient/providers")}
          className="h-full w-full bg-white border rounded-lg p-4 text-left shadow-sm hover:shadow-md"
        >
          <div className="text-3xl mb-2">👥</div>
          <h2 className="font-semibold mb-1">My Providers</h2>
          <p className="text-xs text-gray-600">
            View your current IHSS providers and their status.
          </p>
        </button>
      </div>

      <div className="bg-white border rounded-lg shadow-sm">
        <div className="border-b px-4 py-3 flex justify-between items-center">
          <h2 className="text-sm font-semibold text-gray-800">
            Timesheets Awaiting Your Review
          </h2>
          <button
            onClick={fetchDashboardData}
            className="text-xs text-[#1e3a8a] hover:underline"
          >
            Refresh
          </button>
        </div>
        <div className="p-4">
          {pendingTimesheets.length === 0 ? (
            <p className="text-sm text-gray-500 text-center py-4">
              You have no timesheets pending review.
            </p>
          ) : (
            <div className="space-y-3">
              {pendingTimesheets.map((ts) => (
                <div
                  key={ts.id}
                  className="border rounded-md px-3 py-2 flex justify-between items-center"
                >
                  <div>
                    <p className="text-sm font-semibold">
                      {ts.employeeName || "Provider"} –{" "}
                      {ts.payPeriodStart} to {ts.payPeriodEnd}
                    </p>
                    <p className="text-xs text-gray-600">
                      Total Hours: {ts.totalHours ?? 0} • Submitted:{" "}
                      {ts.submittedAt ?? "N/A"}
                    </p>
                  </div>
                  <button
                    onClick={() =>
                      router.push(`/recipient/timesheets/${ts.id}`)
                    }
                    className="text-xs px-3 py-1 rounded-md bg-[#1e3a8a] text-white font-semibold hover:bg-[#1e40af]"
                  >
                    Review
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









