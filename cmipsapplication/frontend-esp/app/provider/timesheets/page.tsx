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

type TimesheetPageResponse = {
  content: Timesheet[];
  totalElements: number;
  number: number;
  size: number;
  numberOfElements: number;
};

export default function ProviderTimesheetsPage() {
  const router = useRouter();
  const { user, loading: authLoading } = useAuth();

  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [timesheets, setTimesheets] = useState<Timesheet[]>([]);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || authLoading) return;
    if (!user || user.role !== "PROVIDER") {
      router.push("/login");
      return;
    }
    fetchTimesheets();
  }, [mounted, authLoading, user, router]);

  const fetchTimesheets = async () => {
    try {
      setLoading(true);
      const resp = await apiClient.get<TimesheetPageResponse>("/timesheets");
      const data = resp.data;
      setTimesheets(data.content || []);
    } catch (error) {
      console.error("Error fetching timesheets:", error);
      setTimesheets([]);
    } finally {
      setLoading(false);
    }
  };

  if (!mounted || loading || authLoading || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-700 mx-auto" />
          <p className="mt-4 text-gray-600 text-sm">Loading timesheets...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-5xl mx-auto py-8">
      <h1 className="text-xl font-bold text-gray-900 mb-4">
        My Timesheets – Provider View
      </h1>
      <p className="text-sm text-gray-600 mb-4">
        These are timesheets you have created. You can review, edit (if
        allowed), and submit timesheets for your recipients.
      </p>

      <div className="bg-white border rounded-lg shadow-sm overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-2 text-left font-semibold text-gray-700">
                Pay Period
              </th>
              <th className="px-4 py-2 text-left font-semibold text-gray-700">
                Hours
              </th>
              <th className="px-4 py-2 text-left font-semibold text-gray-700">
                Status
              </th>
              <th className="px-4 py-2 text-right font-semibold text-gray-700">
                Action
              </th>
            </tr>
          </thead>
          <tbody>
            {timesheets.length === 0 ? (
              <tr>
                <td
                  colSpan={4}
                  className="px-4 py-4 text-center text-gray-500"
                >
                  No timesheets found.
                </td>
              </tr>
            ) : (
              timesheets.map((ts) => (
                <tr key={ts.id} className="border-t">
                  <td className="px-4 py-2">
                    {ts.payPeriodStart} – {ts.payPeriodEnd}
                  </td>
                  <td className="px-4 py-2">{ts.totalHours ?? 0}</td>
                  <td className="px-4 py-2">{ts.status}</td>
                  <td className="px-4 py-2 text-right">
                    <button
                      onClick={() =>
                        router.push(`/provider/timesheets`)
                      }
                      className="text-xs px-3 py-1 rounded-md bg-emerald-700 text-white font-semibold hover:bg-emerald-800"
                    >
                      View
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}









