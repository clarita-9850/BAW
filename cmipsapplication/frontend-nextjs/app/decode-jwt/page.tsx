'use client';

import { useEffect, useState } from 'react';

export default function DecodeJWTPage() {
  const [token, setToken] = useState('');
  const [decoded, setDecoded] = useState<any>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    // Auto-load token from localStorage
    if (typeof window !== 'undefined') {
      const storedToken = localStorage.getItem('token');
      if (storedToken) {
        setToken(storedToken);
        decodeToken(storedToken);
      }
    }
  }, []);

  const decodeToken = (tokenToDecode: string) => {
    try {
      const parts = tokenToDecode.split('.');
      if (parts.length !== 3) {
        throw new Error('Invalid JWT token format');
      }

      const header = JSON.parse(atob(parts[0]));
      const payload = JSON.parse(atob(parts[1]));

      setDecoded({ header, payload });
      setError('');
    } catch (err: any) {
      setError(err.message);
      setDecoded(null);
    }
  };

  const handleDecode = () => {
    if (token.trim()) {
      decodeToken(token.trim());
    }
  };

  const loadFromStorage = () => {
    if (typeof window !== 'undefined') {
      const storedToken = localStorage.getItem('token');
      if (storedToken) {
        setToken(storedToken);
        decodeToken(storedToken);
      } else {
        alert('No token found in localStorage. Please login first.');
      }
    }
  };

  const analyzeCounty = () => {
    if (!decoded?.payload) return null;

    const payload = decoded.payload;
    let county = null;
    let source = '';

    // Check groups array
    if (payload.groups && Array.isArray(payload.groups)) {
      const countyGroup = payload.groups.find((g: string) =>
        g.toUpperCase() === 'CTA' ||
        g.toUpperCase() === 'CTB' ||
        g.toUpperCase() === 'CTC' ||
        g.includes('/CTA') ||
        g.includes('/CTB') ||
        g.includes('/CTC')
      );
      if (countyGroup) {
        county = countyGroup.toUpperCase().replace(/^.*\/(CTA|CTB|CTC).*$/i, '$1');
        if (['CTA', 'CTB', 'CTC'].includes(county)) {
          source = 'groups array';
        } else {
          county = null;
        }
      }
    }

    // Check realm_access.groups
    if (!county && payload.realm_access?.groups && Array.isArray(payload.realm_access.groups)) {
      const countyGroup = payload.realm_access.groups.find((g: string) =>
        g.toUpperCase() === 'CTA' ||
        g.toUpperCase() === 'CTB' ||
        g.toUpperCase() === 'CTC' ||
        g.includes('/CTA') ||
        g.includes('/CTB') ||
        g.includes('/CTC')
      );
      if (countyGroup) {
        county = countyGroup.toUpperCase().replace(/^.*\/(CTA|CTB|CTC).*$/i, '$1');
        if (['CTA', 'CTB', 'CTC'].includes(county)) {
          source = 'realm_access.groups';
        } else {
          county = null;
        }
      }
    }

    return { county, source };
  };

  const countyInfo = analyzeCounty();
  const themeMap: Record<string, string> = {
    'CTA': 'Sacramento (Dark Blue)',
    'CTB': 'Orange County (Orange/Brown)',
    'CTC': 'Shasta (Green)',
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '20px auto', padding: '20px' }}>
      <h1>🔍 JWT Token Decoder - CMIPS</h1>
      <p>This tool helps you decode and inspect JWT tokens to verify county groups are included.</p>

      <div style={{ margin: '20px 0' }}>
        <button
          onClick={loadFromStorage}
          style={{
            background: '#046b99',
            color: 'white',
            padding: '10px 20px',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            marginRight: '10px',
          }}
        >
          Load Token from LocalStorage
        </button>
        <button
          onClick={() => {
            setToken('');
            setDecoded(null);
            setError('');
          }}
          style={{
            background: '#6c757d',
            color: 'white',
            padding: '10px 20px',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
          }}
        >
          Clear
        </button>
      </div>

      <div style={{ margin: '20px 0' }}>
        <textarea
          value={token}
          onChange={(e) => setToken(e.target.value)}
          placeholder="Paste your JWT token here..."
          style={{
            width: '100%',
            minHeight: '100px',
            padding: '10px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            fontFamily: 'monospace',
            fontSize: '12px',
          }}
        />
        <button
          onClick={handleDecode}
          style={{
            background: '#046b99',
            color: 'white',
            padding: '10px 20px',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            marginTop: '10px',
          }}
        >
          Decode Token
        </button>
      </div>

      {error && (
        <div style={{ padding: '15px', background: '#f8d7da', color: '#721c24', borderRadius: '4px', margin: '20px 0' }}>
          <strong>Error:</strong> {error}
        </div>
      )}

      {decoded && (
        <div>
          <div style={{ margin: '20px 0', padding: '15px', background: '#f9f9f9', borderRadius: '4px' }}>
            <h3>📋 Header</h3>
            <pre style={{ background: '#2d2d2d', color: '#f8f8f2', padding: '15px', borderRadius: '4px', overflow: 'auto' }}>
              {JSON.stringify(decoded.header, null, 2)}
            </pre>
          </div>

          <div style={{ margin: '20px 0', padding: '15px', background: '#f9f9f9', borderRadius: '4px' }}>
            <h3>📦 Payload</h3>
            <pre style={{ background: '#2d2d2d', color: '#f8f8f2', padding: '15px', borderRadius: '4px', overflow: 'auto' }}>
              {JSON.stringify(decoded.payload, null, 2)}
            </pre>
          </div>

          <div style={{ margin: '20px 0', padding: '15px', background: '#fff', border: '1px solid #ddd', borderRadius: '4px' }}>
            <h3>🔍 County Groups Analysis</h3>
            {decoded.payload.groups && Array.isArray(decoded.payload.groups) ? (
              <p style={{ color: '#28a745', fontWeight: 'bold' }}>
                ✅ Found <code>groups</code> array: {JSON.stringify(decoded.payload.groups)}
              </p>
            ) : (
              <p style={{ color: '#dc3545', fontWeight: 'bold' }}>
                ❌ No <code>groups</code> array found in token
              </p>
            )}

            {decoded.payload.realm_access?.groups && Array.isArray(decoded.payload.realm_access.groups) ? (
              <p style={{ color: '#28a745', fontWeight: 'bold' }}>
                ✅ Found <code>realm_access.groups</code> array: {JSON.stringify(decoded.payload.realm_access.groups)}
              </p>
            ) : (
              <p style={{ color: '#ffc107', fontWeight: 'bold' }}>
                ⚠️ No <code>realm_access.groups</code> array found
              </p>
            )}

            {countyInfo?.county ? (
              <div>
                <p style={{ color: '#28a745', fontWeight: 'bold', fontSize: '18px' }}>
                  ✅ County found: <strong>{countyInfo.county}</strong> (from {countyInfo.source})
                </p>
                <p style={{ color: '#17a2b8' }}>
                  🎨 Expected theme: {themeMap[countyInfo.county] || 'Unknown'}
                </p>
              </div>
            ) : (
              <div>
                <p style={{ color: '#dc3545', fontWeight: 'bold' }}>❌ No county found in token</p>
                <div style={{ marginTop: '10px' }}>
                  <p style={{ color: '#ffc107', fontWeight: 'bold' }}>💡 Make sure:</p>
                  <ul>
                    <li>User is assigned to CTA, CTB, or CTC group in Keycloak</li>
                    <li>Groups mapper is configured in Keycloak Client Scope</li>
                    <li>You logged in AFTER assigning the user to a group</li>
                  </ul>
                </div>
              </div>
            )}
          </div>

          {decoded.payload.exp && (
            <div style={{ margin: '20px 0', padding: '15px', background: '#fff', border: '1px solid #ddd', borderRadius: '4px' }}>
              {new Date(decoded.payload.exp * 1000) < new Date() ? (
                <p style={{ color: '#dc3545', fontWeight: 'bold' }}>
                  ⚠️ Token has expired at: {new Date(decoded.payload.exp * 1000).toLocaleString()}
                </p>
              ) : (
                <p style={{ color: '#28a745', fontWeight: 'bold' }}>
                  ✅ Token is valid until: {new Date(decoded.payload.exp * 1000).toLocaleString()}
                </p>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

