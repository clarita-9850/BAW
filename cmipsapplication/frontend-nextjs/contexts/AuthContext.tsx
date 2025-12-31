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

    console.log('🔐 [AuthContext] Initializing auth state...');
    console.log('🔐 [AuthContext] storedToken exists:', !!storedToken);
    console.log('🔐 [AuthContext] storedUser exists:', !!storedUser);
    console.log('🔐 [AuthContext] storedUser value:', storedUser);

    if (storedToken) {
      console.log('🔐 [AuthContext] Token valid:', isTokenValid(storedToken));
      try {
        const decoded = JSON.parse(atob(storedToken.split('.')[1]));
        console.log('🔐 [AuthContext] Token exp:', decoded.exp);
        console.log('🔐 [AuthContext] Current time:', Math.floor(Date.now() / 1000));
        console.log('🔐 [AuthContext] Time until expiry:', decoded.exp - Math.floor(Date.now() / 1000), 'seconds');
      } catch (e) {
        console.error('🔐 [AuthContext] Error decoding token:', e);
      }
    }

    if (storedToken && storedUser && storedUser !== 'undefined' && storedUser !== 'null' && isTokenValid(storedToken)) {
      console.log('🔐 [AuthContext] ✅ Valid auth found, setting user');
      setToken(storedToken);
      const parsedUser = JSON.parse(storedUser);
      
      // If county is not in stored user, try to extract from token
      if (!parsedUser.county) {
        try {
          const decoded = JSON.parse(atob(storedToken.split('.')[1]));
          
          // Extract county from groups first
          let county = null;
          
          // Check groups array
          if (decoded.groups && Array.isArray(decoded.groups)) {
            const countyGroup = decoded.groups.find((g: string) => 
              g.toUpperCase() === 'CTA' || 
              g.toUpperCase() === 'CTB' || 
              g.toUpperCase() === 'CTC' ||
              g.includes('/CTA') || 
              g.includes('/CTB') || 
              g.includes('/CTC')
            );
            if (countyGroup) {
              county = countyGroup.toUpperCase().replace(/^.*\/(CTA|CTB|CTC).*$/i, '$1');
              if (!['CTA', 'CTB', 'CTC'].includes(county)) {
                county = null;
              }
            }
          }
          
          // Check realm_access.groups
          if (!county && decoded.realm_access?.groups && Array.isArray(decoded.realm_access.groups)) {
            const countyGroup = decoded.realm_access.groups.find((g: string) => 
              g.toUpperCase() === 'CTA' || 
              g.toUpperCase() === 'CTB' || 
              g.toUpperCase() === 'CTC' ||
              g.includes('/CTA') || 
              g.includes('/CTB') || 
              g.includes('/CTC')
            );
            if (countyGroup) {
              county = countyGroup.toUpperCase().replace(/^.*\/(CTA|CTB|CTC).*$/i, '$1');
              if (!['CTA', 'CTB', 'CTC'].includes(county)) {
                county = null;
              }
            }
          }
          
          // Fallback to other fields
          if (!county) {
            county = decoded.county || 
                     decoded.location || 
                     decoded.userCounty || 
                     decoded.attributes?.county?.[0] ||
                     decoded.attributes?.location?.[0] ||
                     null;
          }
          
          if (county) {
            parsedUser.county = county.toUpperCase();
            localStorage.setItem('user', JSON.stringify(parsedUser));
          }
        } catch (error) {
          console.error('Error extracting county from token:', error);
        }
      }
      
      console.log('🔐 [AuthContext] User set to:', parsedUser);
      setUser(parsedUser);
    } else {
      console.log('🔐 [AuthContext] ❌ Invalid or missing auth, clearing localStorage');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('refreshToken');
    }
    console.log('🔐 [AuthContext] Setting loading to false');
    setLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    try {
      const response = await apiClient.post('/auth/login', { username, password });
      const { access_token, refresh_token } = response.data;
      if (!access_token) return { success: false, error: 'No access token received' };

      const decoded = JSON.parse(atob(access_token.split('.')[1]));
      
      // DEBUG: Log the full JWT payload to see what's available
      console.log('🔍 [AUTH] Full JWT payload:', JSON.stringify(decoded, null, 2));
      console.log('🔍 [AUTH] Available JWT fields:', Object.keys(decoded));
      console.log('🔍 [AUTH] Groups (direct):', decoded.groups);
      console.log('🔍 [AUTH] realm_access:', decoded.realm_access);
      console.log('🔍 [AUTH] realm_access.groups:', decoded.realm_access?.groups);
      
      const roles: string[] = decoded.realm_access?.roles || [];

      let primaryRole = 'USER';
      if (roles.includes('ADMIN')) primaryRole = 'ADMIN';
      else if (roles.includes('SUPERVISOR')) primaryRole = 'SUPERVISOR';
      else if (roles.includes('CASE_WORKER')) primaryRole = 'CASE_WORKER';
      else if (roles.includes('PROVIDER')) primaryRole = 'PROVIDER';
      else if (roles.includes('RECIPIENT')) primaryRole = 'RECIPIENT';

      // Extract county from JWT token
      // First check for groups (CTA, CTB, CTC are Keycloak groups)
      let county = null;
      
      // Check groups array (Keycloak groups)
      if (decoded.groups && Array.isArray(decoded.groups)) {
        console.log('🔍 [AUTH] Checking groups array:', decoded.groups);
        const countyGroup = decoded.groups.find((g: string) => 
          g.toUpperCase() === 'CTA' || 
          g.toUpperCase() === 'CTB' || 
          g.toUpperCase() === 'CTC' ||
          g.includes('/CTA') || 
          g.includes('/CTB') || 
          g.includes('/CTC')
        );
        if (countyGroup) {
          // Extract county code from group path (e.g., "/CTA" or "CTA")
          county = countyGroup.toUpperCase().replace(/^.*\/(CTA|CTB|CTC).*$/i, '$1');
          if (!['CTA', 'CTB', 'CTC'].includes(county)) {
            county = null;
          } else {
            console.log('✅ [AUTH] County found in groups array:', county);
          }
        } else {
          console.log('⚠️ [AUTH] No county group found in groups array');
        }
      } else {
        console.log('⚠️ [AUTH] No groups array found in JWT token');
      }
      
      // Check realm_access.groups
      if (!county && decoded.realm_access?.groups && Array.isArray(decoded.realm_access.groups)) {
        console.log('🔍 [AUTH] Checking realm_access.groups:', decoded.realm_access.groups);
        const countyGroup = decoded.realm_access.groups.find((g: string) => 
          g.toUpperCase() === 'CTA' || 
          g.toUpperCase() === 'CTB' || 
          g.toUpperCase() === 'CTC' ||
          g.includes('/CTA') || 
          g.includes('/CTB') || 
          g.includes('/CTC')
        );
        if (countyGroup) {
          county = countyGroup.toUpperCase().replace(/^.*\/(CTA|CTB|CTC).*$/i, '$1');
          if (!['CTA', 'CTB', 'CTC'].includes(county)) {
            county = null;
          } else {
            console.log('✅ [AUTH] County found in realm_access.groups:', county);
          }
        } else {
          console.log('⚠️ [AUTH] No county group found in realm_access.groups');
        }
      } else if (!county) {
        console.log('⚠️ [AUTH] No realm_access.groups found in JWT token');
      }
      
      // Fallback to other possible fields
      if (!county) {
        county = decoded.county || 
                 decoded.location || 
                 decoded.userCounty || 
                 decoded.attributes?.county?.[0] ||
                 decoded.attributes?.location?.[0] ||
                 null;
        if (county) {
          console.log('✅ [AUTH] County found in other fields:', county);
        } else {
          console.log('❌ [AUTH] No county found in JWT token at all');
        }
      }

      const userData = {
        username: decoded.preferred_username || username,
        roles,
        role: primaryRole,
        userId: decoded.sub,
        county: county ? county.toUpperCase() : null,
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
