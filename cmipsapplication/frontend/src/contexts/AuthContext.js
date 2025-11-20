import React, { createContext, useState, useContext, useEffect } from 'react';
import axios from 'axios';
import ChangePasswordModal from '../components/ChangePasswordModal';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const [forcePasswordChange, setForcePasswordChange] = useState(false);
  const [showChangePasswordModal, setShowChangePasswordModal] = useState(false);

  // Check if JWT token is valid (not expired) - define before useEffect
  const isTokenValid = (token) => {
    try {
      const decodedToken = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return decodedToken.exp > currentTime;
    } catch (error) {
      return false;
    }
  };

  useEffect(() => {
    const initializeAuth = async () => {
      console.log('🔵 AuthContext: Initializing authentication...');
      
      // Check if user is already logged in
      const storedToken = localStorage.getItem('token');
      const storedUser = localStorage.getItem('user');
      const refreshToken = localStorage.getItem('refreshToken');
      
      console.log('🔵 AuthContext: Has token?', !!storedToken);
      console.log('🔵 AuthContext: Has user?', !!storedUser);
      console.log('🔵 AuthContext: Has refreshToken?', !!refreshToken);
      
      if (storedToken && storedUser) {
        // Validate token before setting it
        const isValid = isTokenValid(storedToken);
        console.log('🔵 AuthContext: Token is valid?', isValid);
        
        if (isValid) {
          console.log('✅ AuthContext: Token valid, setting user');
          setToken(storedToken);
          setUser(JSON.parse(storedUser));
        } else {
          // Token is expired, try to refresh it
          console.log('⚠️ AuthContext: Token expired, attempting refresh...');
          if (refreshToken) {
            try {
              console.log('🔄 AuthContext: Calling refresh endpoint...');
              const response = await axios.post(
                'http://localhost:8081/api/auth/refresh',
                { refresh_token: refreshToken },
                { headers: { 'Content-Type': 'application/json' } }
              );
              
              const { access_token, refresh_token: newRefreshToken } = response.data;
              console.log('✅ AuthContext: Refresh successful!');
              
              localStorage.setItem('token', access_token);
              if (newRefreshToken) {
                localStorage.setItem('refreshToken', newRefreshToken);
              }
              setToken(access_token);
              setUser(JSON.parse(storedUser));
              console.log('✅ AuthContext: User set after refresh');
            } catch (error) {
              // Refresh failed, clear everything
              console.error('❌ AuthContext: Token refresh failed:', error.message);
              localStorage.removeItem('token');
              localStorage.removeItem('refreshToken');
              localStorage.removeItem('user');
            }
          } else {
            // No refresh token, clear everything
            console.log('❌ AuthContext: No refresh token, clearing storage');
            localStorage.removeItem('token');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('user');
          }
        }
      } else {
        console.log('ℹ️ AuthContext: No stored credentials found');
      }
      
      console.log('🔵 AuthContext: Setting loading to false');
      setLoading(false);
    };
    
    initializeAuth();
  }, []);

  // Refresh token if it's expired
  const refreshTokenIfNeeded = async () => {
    const storedToken = localStorage.getItem('token');
    if (!storedToken) return null;

    if (!isTokenValid(storedToken)) {
      // Token is expired, try to refresh
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          const response = await axios.post(
            'http://localhost:8081/api/auth/refresh',
            { refresh_token: refreshToken },
            { headers: { 'Content-Type': 'application/json' } }
          );
          
          const { access_token, refresh_token: newRefreshToken } = response.data;
          localStorage.setItem('token', access_token);
          if (newRefreshToken) {
            localStorage.setItem('refreshToken', newRefreshToken);
          }
          setToken(access_token);
          return access_token;
        } else {
          // No refresh token available
          logout();
          return null;
        }
      } catch (error) {
        // Refresh failed, logout user
        console.log('Token refresh failed:', error.message);
        logout();
        return null;
      }
    }
    
    return storedToken;
  };

  const login = async (username, password) => {
    try {
      console.log('🔵 AuthContext: Starting login for user:', username);
      
      // Use backend login endpoint to avoid CORS issues
      const response = await axios.post(
        'http://localhost:8081/api/auth/login',
        {
          username: username,
          password: password,
        },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );

      console.log('🔵 AuthContext: Login response received');
      console.log('🔵 AuthContext: Response data:', response.data);
      
      const { access_token, refresh_token, forcePasswordChange: requiresPasswordChange } = response.data;
      
      console.log('🔵 AuthContext: forcePasswordChange from backend:', requiresPasswordChange);
      
      if (!access_token) {
        console.error('❌ AuthContext: No access token in response');
        return { success: false, error: 'No access token received' };
      }
      
      // Decode JWT to get user info
      const decodedToken = JSON.parse(atob(access_token.split('.')[1]));
      const roles = decodedToken.realm_access?.roles || [];
      
      // Determine primary role
      let primaryRole = 'USER';
      if (roles.includes('PROVIDER')) primaryRole = 'PROVIDER';
      if (roles.includes('RECIPIENT')) primaryRole = 'RECIPIENT';
      if (roles.includes('CASE_WORKER')) primaryRole = 'CASE_WORKER';
      
      const userData = {
        username: decodedToken.preferred_username || username,
        roles: roles,
        role: primaryRole,
        userId: decodedToken.sub,
      };

      // Store tokens and user data
      localStorage.setItem('token', access_token);
      localStorage.setItem('refreshToken', refresh_token);
      localStorage.setItem('user', JSON.stringify(userData));
      
      setToken(access_token);
      setUser(userData);
      
      // Check if password change is required
      const needsPasswordChange = requiresPasswordChange === true || response.data.forcePasswordChange === true;
      
      console.log('🔵 AuthContext: needsPasswordChange:', needsPasswordChange);
      
      if (needsPasswordChange) {
        console.log('✅ AuthContext: Password change required - showing modal');
        setForcePasswordChange(true);
        setShowChangePasswordModal(true);
        return { success: true, requiresPasswordChange: true };
      }
      
      console.log('ℹ️ AuthContext: No password change required');
      setForcePasswordChange(false);
      setShowChangePasswordModal(false);
      
      return { success: true, requiresPasswordChange: false };
    } catch (error) {
      console.error('❌ AuthContext: Login error:', error);
      return { success: false, error: error.response?.data?.error || 'Login failed' };
    }
  };
  
  const handlePasswordChangeSuccess = () => {
    console.log('✅ AuthContext: Password changed successfully');
    setForcePasswordChange(false);
    setShowChangePasswordModal(false);
    
    // Force reload to refresh the app state
    window.location.href = '/';
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  const value = {
    user,
    token,
    login,
    logout,
    loading,
    refreshTokenIfNeeded,
    forcePasswordChange,
    showChangePasswordModal,
    setShowChangePasswordModal,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
      <ChangePasswordModal
        isOpen={showChangePasswordModal}
        onClose={() => {
          // Don't allow closing if password change is required
          if (forcePasswordChange) {
            console.log('⚠️ AuthContext: Modal cannot be closed - password change required');
            return; // Modal cannot be closed
          }
          setShowChangePasswordModal(false);
        }}
        onSuccess={handlePasswordChangeSuccess}
      />
    </AuthContext.Provider>
  );
};
