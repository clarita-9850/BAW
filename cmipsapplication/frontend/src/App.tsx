import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ReactKeycloakProvider } from '@keycloak/keycloak-react';
import keycloak from './keycloak';
import TimesheetManagement from './components/TimesheetManagement';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import { KeycloakContext } from './contexts/KeycloakContext';

function App() {
  return (
    <ReactKeycloakProvider
      authClient={keycloak}
      initOptions={{
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html'
      }}
    >
      <KeycloakContext.Provider value={{ keycloak, initialized: true }}>
        <Router>
          <div className="min-h-screen bg-gray-50">
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="/" element={<Dashboard />} />
              <Route path="/timesheets" element={<TimesheetManagement />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </div>
        </Router>
      </KeycloakContext.Provider>
    </ReactKeycloakProvider>
  );
}

export default App;


