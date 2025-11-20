import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

interface User {
  id: number;
  username: string;
  role: string;
  email: string;
  firstName: string;
  lastName: string;
  department: string;
  location: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (username: string, password: string) => Promise<boolean>;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Helper functions to get department and location based on role
const getDepartmentByRole = (role: string): string => {
  switch (role) {
    case 'ADMIN': return 'IT';
    case 'MANAGER': return 'Management';
    case 'SUPERVISOR': return 'Supervision';
    case 'CASEWORKER': return 'Case Management';
    case 'AUDITOR': return 'Audit';
    default: return 'General';
  }
};

const getLocationByRole = (role: string): string => {
  switch (role) {
    case 'ADMIN': return 'Headquarters';
    case 'MANAGER': return 'Regional Office';
    case 'SUPERVISOR': return 'County Office A';
    case 'CASEWORKER': return 'County Office A';
    case 'AUDITOR': return 'County Office B';
    default: return 'Unknown';
  }
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  // Function to decode JWT token
  const decodeJWT = (token: string) => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error decoding JWT:', error);
      return null;
    }
  };

  useEffect(() => {
    // Check for stored token on app load
    const storedToken = localStorage.getItem('jwt_token');
    if (storedToken) {
      setToken(storedToken);
      
      // Decode JWT token to get user info
      const decodedToken = decodeJWT(storedToken);
      console.log('Decoded JWT token:', decodedToken);
      
      if (decodedToken && decodedToken.sub) {
        const userRole = decodedToken.role || 'USER';
        console.log('Setting user role from JWT:', userRole);
        
        setUser({
          id: decodedToken.id || 1,
          username: decodedToken.sub,
          role: userRole,
          email: `${decodedToken.sub}@cmips.com`,
          firstName: decodedToken.sub.charAt(0).toUpperCase() + decodedToken.sub.slice(1),
          lastName: 'User',
          department: getDepartmentByRole(userRole),
          location: getLocationByRole(userRole)
        });
      } else {
        console.log('Invalid JWT token, removing from storage');
        // Fallback if token is invalid
        localStorage.removeItem('jwt_token');
      }
    }
    setLoading(false);
  }, []);

  const login = async (username: string, password: string): Promise<boolean> => {
    try {
      const response = await axios.post(API_ENDPOINTS.auth.login, {
        username,
        password
      });

      if (response.data.jwtToken) {
        const jwtToken = response.data.jwtToken;
        setToken(jwtToken);
        localStorage.setItem('jwt_token', jwtToken);
        
        // Set user info from response
        setUser({
          id: 1, // This would come from the backend in a real app
          username: response.data.username,
          role: response.data.role,
          email: `${response.data.username}@cmips.com`,
          firstName: response.data.username.charAt(0).toUpperCase() + response.data.username.slice(1),
          lastName: 'User',
          department: getDepartmentByRole(response.data.role),
          location: getLocationByRole(response.data.role)
        });
        
        return true;
      }
      return false;
    } catch (error) {
      console.error('Login failed:', error);
      return false;
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('jwt_token');
  };

  const value = {
    user,
    token,
    login,
    logout,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
