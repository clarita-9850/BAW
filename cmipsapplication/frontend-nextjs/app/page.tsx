'use client';

import { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';

export default function HomePage() {
  const { user, loading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || loading) return; // Wait for mount and auth to initialize

    if (!user) {
      // No user logged in, redirect to login
      window.location.href = '/login';
      return;
    }

    // User is logged in, redirect to role-specific dashboard
    const role = user.role || (user.roles?.includes('ADMIN') ? 'ADMIN' :
                              user.roles?.includes('SUPERVISOR') ? 'SUPERVISOR' :
                              user.roles?.includes('CASE_WORKER') ? 'CASE_WORKER' :
                              user.roles?.includes('PROVIDER') ? 'PROVIDER' :
                              user.roles?.includes('RECIPIENT') ? 'RECIPIENT' : 'USER');

    let url = '/login';
    if (role === 'ADMIN') url = '/admin/keycloak';
    else if (role === 'SUPERVISOR') url = '/supervisor/dashboard';
    else if (role === 'CASE_WORKER') url = '/caseworker/dashboard';
    else if (role === 'PROVIDER') url = '/provider/dashboard';
    else if (role === 'RECIPIENT') url = '/recipient/dashboard';

    window.location.href = url;
  }, [user, loading, mounted]);

  // Show loading state while checking auth
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1e3a8a] mx-auto"></div>
        <p className="mt-4 text-gray-600">Redirecting...</p>
      </div>
    </div>
  );
}
