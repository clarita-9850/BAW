'use client';

import { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';

export default function AdminKeycloakPageComponent() {
  const { user, loading: authLoading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (authLoading) return;
    if (!user || (user.role !== 'ADMIN' && !user.roles?.includes('ADMIN'))) {
      window.location.href = '/login';
      return;
    }
    console.log('Admin Keycloak page loaded');
  }, [user, authLoading]);

  if (!mounted || authLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1e3a8a] mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  if (!user || (user.role !== 'ADMIN' && !user.roles?.includes('ADMIN'))) {
    return null;
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <h1 className="text-3xl font-bold text-[#1e3a8a] mb-4">🔐 Keycloak Admin Dashboard</h1>
        <p className="text-gray-600 text-lg">Admin page is working!</p>
        <p className="text-sm text-gray-500 mt-2">Route: /admin-keycloak</p>
      </div>
    </div>
  );
}
