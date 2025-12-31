'use client';

import Link from "next/link";
import { useAuth } from "@/contexts/AuthContext";
import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function HomePage() {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && user) {
      if (user.role === "PROVIDER") {
        router.push("/provider/dashboard");
      } else if (user.role === "RECIPIENT") {
        router.push("/recipient/dashboard");
      }
    }
  }, [loading, user, router]);

  return (
    <div className="w-full max-w-xl bg-white rounded-lg shadow-md p-8 mx-4">
      <h1 className="text-2xl font-bold text-gray-900 text-center mb-4">
        CMIPS Electronic Services Portal
      </h1>
      <p className="text-sm text-gray-600 text-center mb-8">
        Sign in or register to manage IHSS timesheets and payments online.
      </p>

      <div className="space-y-4">
        <Link
          href="/login"
          className="block w-full py-3 px-4 rounded-md bg-[#1e3a8a] text-sm font-semibold text-white text-center hover:bg-[#1e40af]"
        >
          Sign in to ESP
        </Link>
        <div className="pt-2 border-t border-gray-200 space-y-2">
          <p className="text-xs text-gray-500 text-center font-semibold">
            Need an ESP account?
          </p>
          <div className="flex flex-col gap-2">
            <Link
              href="/recipient/register"
              className="block w-full py-2 px-4 rounded-md border border-[#1e3a8a] text-xs font-semibold text-[#1e3a8a] text-center hover:bg-[#1e3a8a] hover:text-white"
            >
              I am a Recipient – Register
            </Link>
            <Link
              href="/provider/register"
              className="block w-full py-2 px-4 rounded-md border border-emerald-700 text-xs font-semibold text-emerald-700 text-center hover:bg-emerald-700 hover:text-white"
            >
              I am a Provider – Register
            </Link>
          </div>
        </div>

        <p className="text-xs text-gray-500 text-center mt-4">
          County staff should continue to use the internal CMIPS application.
        </p>
      </div>
    </div>
  );
}



