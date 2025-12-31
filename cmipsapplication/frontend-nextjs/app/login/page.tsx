'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/contexts/AuthContext';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [mounted, setMounted] = useState(false);
  const { login } = useAuth();

  useEffect(() => {
    setMounted(true);
    // Version check
    const VERSION = '3.0.0';
    const storedVersion = sessionStorage.getItem('login-page-version');
    if (storedVersion && storedVersion !== VERSION) {
      sessionStorage.setItem('login-page-version', VERSION);
      window.location.reload();
    } else if (!storedVersion) {
      sessionStorage.setItem('login-page-version', VERSION);
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (!username || !password) {
        setError('Please enter both username and password');
        setLoading(false);
        return;
      }

      const result = await login(username, password);

      if (result?.success) {
        const role = result.role || result.userData?.role;
        console.log('=== LOGIN SUCCESS (v2.0.0) ===');
        console.log('Role:', role);
        console.log('Result object:', result);
        
        // Redirect based on role
        let url = '/login';
        if (role === 'ADMIN') {
          url = '/admin/keycloak';
        } else if (role === 'SUPERVISOR') {
          url = '/supervisor/dashboard';
        } else if (role === 'CASE_WORKER') {
          url = '/caseworker/dashboard';
        } else if (role === 'PROVIDER') {
          url = '/provider/dashboard';
        } else if (role === 'RECIPIENT') {
          url = '/recipient/dashboard';
        }
        
        console.log('✅ Redirecting to:', url);
        console.log('User role:', role);
        
        // Force immediate redirect with cache busting
        window.location.href = url + '?t=' + Date.now();
      } else {
        setError(result?.error || 'Login failed. Please try again.');
        setLoading(false);
      }
    } catch (err: any) {
      setError(err?.message || 'Login failed. Please try again.');
      setLoading(false);
    }
  };

  // Show loading state during SSR/hydration
  if (!mounted) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1e3a8a] mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="w-full max-w-md bg-white rounded-lg shadow-xl p-8 mx-4">
        <div className="text-center">
          <div className="inline-flex w-10 h-8 bg-[#1e3a8a] rounded-md text-white text-xs font-bold items-center justify-center mb-4">CA</div>
          <h2 className="mt-4 text-3xl font-extrabold text-gray-900">CMIPS Login</h2>
          <p className="mt-2 text-sm text-gray-500">
            Internal users, recipients, and providers can sign in here using their CMIPS / ESP credentials.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="mt-8 space-y-4" noValidate>
          <div>
            <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-1">Username</label>
            <input
              id="username"
              name="username"
              type="text"
              autoComplete="username"
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-[#1e3a8a] focus:border-[#1e3a8a]"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-[#1e3a8a] focus:border-[#1e3a8a]"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          {error && (
            <div className="bg-red-100 border border-red-200 text-red-700 text-sm rounded-md px-3 py-2">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 bg-[#1e3a8a] text-white rounded-md text-sm font-semibold hover:bg-[#1e40af] disabled:bg-gray-400"
          >
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <div className="mt-6 text-center text-xs text-gray-500 space-y-2">
          <div>
            <p className="font-semibold">Need an ESP Account?</p>
            <p className="mt-1">
              <a href="/recipient/register" className="text-[#1e3a8a] font-semibold underline">
                I am a Recipient – Register for ESP
              </a>
              <br />
              <a href="/provider/register" className="text-emerald-700 font-semibold underline">
                I am a Provider – Register for ESP
              </a>
            </p>
          </div>
          <div className="pt-2 border-t border-gray-200">
            <p className="font-semibold">Test Users</p>
            <p className="mt-1">
              admin / password123<br />
              supervisor1 / password123<br />
              caseworker1 / password123<br />
              provider1 / password123<br />
              recipient1 / password123
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
