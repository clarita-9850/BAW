import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const result = await login(username, password);
      
      // Handle new response format (object with success and requiresPasswordChange)
      if (result && typeof result === 'object') {
        if (result.success) {
          // If password change is required, modal will be shown automatically
          // Don't navigate yet - user needs to change password first
          if (!result.requiresPasswordChange) {
            navigate('/');
          }
          // If requiresPasswordChange is true, the modal is already shown
          // User will navigate after password change
        } else {
          setError(result.error || 'Invalid credentials. Please try again.');
        }
      } else {
        // Fallback for old boolean return (shouldn't happen, but just in case)
        if (result) {
          navigate('/');
        } else {
          setError('Invalid credentials. Please try again.');
        }
      }
    } catch (err) {
      setError('Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-ca-highlight-50 to-ca-secondary-100">
      <div className="max-w-md w-full space-y-8 p-8">
        <div className="text-center">
          <div className="ca-logo mx-auto mb-4">CA</div>
          <h2 className="mt-6 text-3xl font-extrabold text-ca-primary-900">
            CMIPS Login
          </h2>
          <p className="mt-2 text-sm text-ca-primary-600">
            Case Management Information and Payrolling System
          </p>
        </div>
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="space-y-4">
            <div>
              <label htmlFor="username" className="form-label">
                Username
              </label>
              <input
                id="username"
                name="username"
                type="text"
                required
                className="input mt-1"
                placeholder="Enter username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
            </div>
            
            <div>
              <label htmlFor="password" className="form-label">
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                required
                className="input mt-1"
                placeholder="Enter password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          {error && (
            <div className="alert alert-error">
              {error}
            </div>
          )}

          <div>
            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary w-full"
            >
              {loading ? 'Signing in...' : 'Sign in'}
            </button>
          </div>
        </form>

        <div className="mt-8 text-center">
          <p className="text-sm text-ca-primary-600">
            <strong>Test Users:</strong>
          </p>
          <p className="text-xs text-ca-primary-500 mt-1">
            provider1 / password123<br/>
            recipient1 / password123<br/>
            caseworker1 / password123
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
