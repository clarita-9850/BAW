'use client';

import React, { createContext, useState, useContext, useEffect } from 'react';
import apiClient from '@/lib/api';

const AuthContext = createContext<any>(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<any>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const isTokenValid = (token: string) => {
    try {
      const decoded = JSON.parse(atob(token.split('.')[1]));
      return decoded.exp > Math.floor(Date.now() / 1000);
    } catch {
      return false;
    }
  };

  useEffect(() => {
    if (typeof window === 'undefined') return;

    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');

    if (storedToken && storedUser && storedUser !== 'undefined' && storedUser !== 'null' && isTokenValid(storedToken)) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    } else {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('refreshToken');
    }
    setLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    try {
      const response = await apiClient.post('/auth/login', { username, password });
      const { access_token, refresh_token } = response.data;
      if (!access_token) return { success: false, error: 'No access token received' };

      const decoded = JSON.parse(atob(access_token.split('.')[1]));
      const roles: string[] = decoded.realm_access?.roles || [];

      let primaryRole = 'USER';
      if (roles.includes('ADMIN')) primaryRole = 'ADMIN';
      else if (roles.includes('SUPERVISOR')) primaryRole = 'SUPERVISOR';
      else if (roles.includes('CASE_WORKER')) primaryRole = 'CASE_WORKER';
      else if (roles.includes('PROVIDER')) primaryRole = 'PROVIDER';
      else if (roles.includes('RECIPIENT')) primaryRole = 'RECIPIENT';

      const userData = {
        username: decoded.preferred_username || username,
        roles,
        role: primaryRole,
        userId: decoded.sub,
      };

      localStorage.setItem('token', access_token);
      if (refresh_token) localStorage.setItem('refreshToken', refresh_token);
      localStorage.setItem('user', JSON.stringify(userData));

      setToken(access_token);
      setUser(userData);

      return { success: true, requiresPasswordChange: false, role: primaryRole, userData };
    } catch (error: any) {
      const msg =
        error?.response?.data?.error ||
        error?.response?.data?.message ||
        error?.message ||
        'Login failed. Please try again.';
      return { success: false, error: msg };
    }
  };

  const logout = () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
    }
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
