'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useAuth } from './AuthContext';

// CA.gov Template Theme Files Available
export const CA_GOV_THEMES = {
  oceanside: 'colortheme-oceanside',
  delta: 'colortheme-delta',
  eureka: 'colortheme-eureka',
  mono: 'colortheme-mono',
  orangecounty: 'colortheme-orangecounty',
  pasorobles: 'colortheme-pasorobles',
  sacramento: 'colortheme-sacramento',
  santabarbara: 'colortheme-santabarbara',
  santacruz: 'colortheme-santacruz',
  shasta: 'colortheme-shasta',
  sierra: 'colortheme-sierra',
  trinity: 'colortheme-trinity',
} as const;

// Map CMIPS counties to California State Web Template themes
// Based on user requirements:
// CTA → Sacramento theme (Dark Blue: #153554)
// CTB → Orange County theme (Orange & Brown: #A15801)
// CTC → Shasta theme (Green: #336c39)
const COUNTY_THEME_MAP: Record<string, keyof typeof CA_GOV_THEMES> = {
  'CTA': 'sacramento',      // CTA → Sacramento theme (Dark Blue)
  'CTB': 'orangecounty',    // CTB → Orange County theme (Orange & Brown)
  'CTC': 'shasta',          // CTC → Shasta theme (Green)
};

// Default theme if county is not found
const DEFAULT_THEME: keyof typeof CA_GOV_THEMES = 'oceanside';

interface CountyThemeContextType {
  currentCounty: string | null;
  currentTheme: keyof typeof CA_GOV_THEMES | null;
  themeLoaded: boolean;
}

const CountyThemeContext = createContext<CountyThemeContextType | undefined>(undefined);

export function CountyThemeProvider({ children }: { children: ReactNode }) {
  const { user, token } = useAuth();
  const [currentCounty, setCurrentCounty] = useState<string | null>(null);
  const [currentTheme, setCurrentTheme] = useState<keyof typeof CA_GOV_THEMES | null>(null);
  const [themeLoaded, setThemeLoaded] = useState(false);

  // Extract county from JWT token or user object
  const extractCounty = (): string | null => {
    if (typeof window === 'undefined') return null;

    // DEBUG: Check for manual override in localStorage (for testing)
    const manualCounty = localStorage.getItem('manualCounty');
    if (manualCounty) {
      console.log('🔧 Using manual county override:', manualCounty);
      return manualCounty.toUpperCase();
    }

    // Try to get county from user object first
    if (user?.county) {
      console.log('✅ County found in user object:', user.county);
      return user.county.toUpperCase();
    }

    // Try to get county from JWT token
    if (token) {
      try {
        const decoded = JSON.parse(atob(token.split('.')[1]));
        console.log('🔍 JWT decoded, checking for county. Available fields:', Object.keys(decoded));
        console.log('🔍 Full JWT payload:', JSON.stringify(decoded, null, 2));
        
        // Method 1: Check for groups array (Keycloak groups)
        // Groups can be in: groups, realm_access.groups, or as group paths
        let county = null;
        
        // Check direct 'groups' claim (array of group names/paths)
        if (decoded.groups && Array.isArray(decoded.groups)) {
          console.log('🔍 Found groups array:', decoded.groups);
          // Look for CTA, CTB, or CTC in group names
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
            if (county && ['CTA', 'CTB', 'CTC'].includes(county)) {
              console.log('✅ County found in groups array:', county);
              return county;
            }
          }
        }
        
        // Check realm_access.groups
        if (decoded.realm_access?.groups && Array.isArray(decoded.realm_access.groups)) {
          console.log('🔍 Found realm_access.groups:', decoded.realm_access.groups);
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
            if (county && ['CTA', 'CTB', 'CTC'].includes(county)) {
              console.log('✅ County found in realm_access.groups:', county);
              return county;
            }
          }
        }
        
        // Method 2: Check other possible fields for county
        county = decoded.county || 
                 decoded.location || 
                 decoded.userCounty || 
                 decoded.attributes?.county?.[0] ||
                 decoded.attributes?.location?.[0];
        
        if (county) {
          console.log('✅ County found in JWT token attributes:', county);
          return county.toUpperCase();
        } else {
          console.log('⚠️ No county found in JWT token.');
          console.log('⚠️ Available groups:', decoded.groups);
          console.log('⚠️ Available realm_access:', decoded.realm_access);
          console.log('⚠️ Available attributes:', decoded.attributes);
        }
      } catch (error) {
        console.error('❌ Error decoding JWT token:', error);
      }
    } else {
      console.log('⚠️ No JWT token available');
    }

    // Try to get from localStorage as fallback
    const storedCounty = localStorage.getItem('userCounty');
    if (storedCounty) {
      console.log('✅ County found in localStorage:', storedCounty);
      return storedCounty.toUpperCase();
    }

    console.log('❌ No county found in any source');
    return null;
  };

  // Function to load/unload theme CSS dynamically
  const loadTheme = (themeFile: keyof typeof CA_GOV_THEMES) => {
    if (typeof document === 'undefined') return;

    const themeFileName = `${CA_GOV_THEMES[themeFile]}.min.css`;
    const themeId = 'cagov-dynamic-theme';
    const defaultThemeId = 'cagov-default-theme';
    
    // Remove existing dynamic theme if any
    const existingTheme = document.getElementById(themeId);
    if (existingTheme) {
      existingTheme.remove();
    }

    // Also remove/hide default theme
    const defaultTheme = document.getElementById(defaultThemeId);
    if (defaultTheme) {
      defaultTheme.remove();
    }

    // Create new link element for the theme
    const link = document.createElement('link');
    link.id = themeId;
    link.rel = 'stylesheet';
    link.href = `/cagov/css/${themeFileName}`;
    link.onload = () => {
      setCurrentTheme(themeFile);
      setThemeLoaded(true);
      console.log(`✅ County theme loaded: ${themeFile} for county: ${currentCounty}`);
      console.log(`📄 Theme file loaded: /cagov/css/${themeFileName}`);
      
      // Verify CSS variables are available
      const rootStyles = getComputedStyle(document.documentElement);
      const p2Color = rootStyles.getPropertyValue('--color-p2').trim();
      console.log(`🎨 Current --color-p2 value: ${p2Color || 'NOT SET'}`);
    };
    link.onerror = () => {
      console.error(`❌ Failed to load theme: ${themeFile}`);
      // Fallback to default theme
      loadTheme(DEFAULT_THEME);
    };
    
    // Append to head
    document.head.appendChild(link);
  };

  // Update theme when user or county changes
  useEffect(() => {
    if (typeof window === 'undefined') return;

    // Check if user is admin - admins should use default theme
    const isAdmin = user?.role === 'ADMIN' || user?.roles?.includes('ADMIN');
    
    if (isAdmin) {
      // Admin users always use default theme (Oceanside)
      console.log('Admin user detected - using default theme:', DEFAULT_THEME);
      loadTheme(DEFAULT_THEME);
      setCurrentCounty(null);
      setCurrentTheme(DEFAULT_THEME);
      setThemeLoaded(true);
      return;
    }

    const county = extractCounty();
    
    if (county) {
      setCurrentCounty(county);
      
      // Map county to theme
      const theme = COUNTY_THEME_MAP[county] || DEFAULT_THEME;
      
      console.log(`🗺️ County mapping: ${county} → ${theme}`);
      console.log(`📋 Available county mappings:`, Object.keys(COUNTY_THEME_MAP));
      
      // Store county in localStorage for persistence
      localStorage.setItem('userCounty', county);
      
      // Load the theme
      loadTheme(theme);
    } else {
      // No county found, use default theme
      console.log('⚠️ No county found, using default theme:', DEFAULT_THEME);
      console.log('💡 TIP: To test county themes, set localStorage.setItem("manualCounty", "CTA") or "CTB" or "CTC"');
      loadTheme(DEFAULT_THEME);
      setCurrentCounty(null);
      setCurrentTheme(DEFAULT_THEME);
      setThemeLoaded(true);
    }
  }, [user, token]);

  return (
    <CountyThemeContext.Provider
      value={{
        currentCounty,
        currentTheme,
        themeLoaded,
      }}
    >
      {children}
    </CountyThemeContext.Provider>
  );
}

export function useCountyTheme() {
  const context = useContext(CountyThemeContext);
  if (context === undefined) {
    throw new Error('useCountyTheme must be used within a CountyThemeProvider');
  }
  return context;
}


