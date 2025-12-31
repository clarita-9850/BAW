'use client';

import React, { useState, useEffect } from 'react';
import SiteHeader from './SiteHeader';
import SiteNavigation, { type NavItem } from './SiteNavigation';
import ActiveSearch from './ActiveSearch';
import LanguageSwitcher from '../LanguageSwitcher';
import { useAuth } from '@/contexts/AuthContext';
import { usePathname, useRouter } from 'next/navigation';
import styles from './Header.module.css';

export default function Header() {
  const pathname = usePathname();
  const router = useRouter();
  const { user, token, logout } = useAuth();
  const [showDashboard, setShowDashboard] = useState(false);

  // Derive isAuthenticated from user and token
  const isAuthenticated = !!user && !!token;

  const isCentralWorker = user?.role?.toUpperCase() === 'CENTRAL_WORKER';
  const isCaseWorker = user?.role?.toUpperCase() === 'CASE_WORKER';
  const isLoginPage = pathname === '/login';

  // Define supervisor/timesheet-frontend pages
  const supervisorPages = [
    '/dashboard',
    '/batch-jobs',
    '/analytics',
    '/visualization',
    '/admin/field-masking',
  ];

  // Check if current path is a supervisor page
  const isSupervisorPage = supervisorPages.some(page => pathname.startsWith(page));

  // Check for dashboard query parameter on client side
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      setShowDashboard(params.get('dashboard') === 'true');
    }
  }, [pathname]);

  // Case worker navigation (demo-screens structure)
  const caseWorkerNavigation: NavItem[] = [
    { href: '/', navHeader: 'SW-Home' },
    {
      navHeader: 'Case Management',
      navBodyArr: [
        { title: 'All Cases', href: '/cases' },
        { title: 'New Case', href: '/cases/new' },
        { title: 'My Cases', href: '/my-cases' },
        { title: 'Due for Reassessment', href: '/cases?filter=reassessment' },
      ],
    },
    {
      navHeader: 'Providers',
      navBodyArr: [
        { title: 'All Providers', href: '/providers' },
        { title: 'New Provider', href: '/providers/new' },
        { title: 'Pending Violations', href: '/providers/violations/pending' },
        { title: 'Eligible for Reinstatement', href: '/providers/pending-reinstatement' },
      ],
    },
    {
      navHeader: 'Recipients',
      navBodyArr: [
        { title: 'Person Search', href: '/recipients' },
        { title: 'New Referral', href: '/recipients/new' },
        { title: 'Open Referrals', href: '/recipients/referrals/open' },
        { title: 'Closed Referrals', href: '/recipients/referrals/closed' },
      ],
    },
    { href: '/my-workspace', navHeader: 'My Workspace' },
    { href: '/inbox', navHeader: 'Inbox' },
  ];

  // Supervisor/timesheet-frontend navigation
  // When on supervisor pages or dashboard=true, Home should maintain the dashboard view
  const supervisorNavigation: NavItem[] = [
    { href: (showDashboard || isSupervisorPage) ? '/?dashboard=true' : '/', navHeader: 'Home' },
    { href: '/dashboard', navHeader: 'Dashboard' },
    { href: '/batch-jobs', navHeader: 'Batch Jobs' },
    { href: '/analytics', navHeader: 'Analytics' },
    { href: '/visualization', navHeader: 'Visualization' },
    ...(isCentralWorker
      ? [
          {
            href: '/admin/field-masking',
            navHeader: 'Field Masking',
          } as NavItem,
        ]
      : []),
    ...(user?.role?.toUpperCase() === 'ADMIN'
      ? [
          { href: '/admin/keycloak', navHeader: 'Admin' },
        ]
      : []),
  ];

  const providerNavigation: NavItem[] = [
    { href: '/', navHeader: 'Home' },
    { href: '/provider/dashboard', navHeader: 'Dashboard' },
    {
      navHeader: 'Timesheets',
      navBodyArr: [
        { title: 'My Timesheets', href: '/provider/timesheets' },
        { title: 'Create Timesheet', href: '/provider/timesheets/new' },
      ],
    },
    { href: '/provider/evv-checkin', navHeader: 'EVV Check-In' },
    { href: '/provider/profile', navHeader: 'My Profile' },
    { href: '/provider/payments', navHeader: 'Payments' },
  ];

  const recipientNavigation: NavItem[] = [
    { href: '/', navHeader: 'Home' },
    { href: '/recipient/dashboard', navHeader: 'Dashboard' },
    { href: '/recipient/timesheets', navHeader: 'Approve Timesheets' },
    { href: '/recipient/providers', navHeader: 'My Providers' },
    { href: '/recipient/profile', navHeader: 'My Profile' },
  ];

  const supervisorNavigation2: NavItem[] = [
    { href: '/', navHeader: 'Home' },
    { href: '/supervisor/dashboard', navHeader: 'Dashboard' },
    {
      navHeader: 'Cases',
      navBodyArr: [
        { title: 'All Cases', href: '/cases' },
        { title: 'New Case', href: '/cases/new' },
        { title: 'My Team Cases', href: '/supervisor/team-cases' },
      ],
    },
    {
      navHeader: 'Providers',
      navBodyArr: [
        { title: 'All Providers', href: '/providers' },
        { title: 'New Provider', href: '/providers/new' },
        { title: 'Pending Violations', href: '/providers/violations/pending' },
      ],
    },
    {
      navHeader: 'Timesheets',
      navBodyArr: [
        { title: 'Pending Approval', href: '/supervisor/timesheets' },
        { title: 'All Timesheets', href: '/timesheets' },
      ],
    },
    { href: '/analytics', navHeader: 'Analytics' },
    { href: '/batch-jobs', navHeader: 'Batch Jobs' },
  ];

  // Select navigation based on role, dashboard parameter, and current page
  // If case worker is on supervisor pages or has dashboard=true, show supervisor navigation
  const shouldShowSupervisorNav = isCaseWorker && (showDashboard || isSupervisorPage);
  let navigationContent: NavItem[] = supervisorNavigation;

  const userRole = user?.role?.toUpperCase();

  if (userRole === 'SUPERVISOR') {
    navigationContent = supervisorNavigation2;
  } else if (shouldShowSupervisorNav) {
    navigationContent = supervisorNavigation;
  } else if (isCaseWorker) {
    navigationContent = caseWorkerNavigation;
  } else if (userRole === 'PROVIDER') {
    navigationContent = providerNavigation;
  } else if (userRole === 'RECIPIENT') {
    navigationContent = recipientNavigation;
  } else if (userRole === 'ADMIN') {
    navigationContent = supervisorNavigation2;
  }

  const handleLogout = async () => {
    await logout();
    router.replace('/login');
  };

  return (
    <header role="banner" id="header" className={styles.globalHeader}>
      <div id="skip-to-content" className={styles.skipToContent}>
        <a href="#main-content">Skip to Main Content</a>
      </div>

      {/* Utility Header */}
      <div className={styles.utilityHeader}>
        <div className="container">
          <div className={styles.flexRow}>
            <div className={styles.socialMediaLinks}>
              <div className={styles.headerCagovLogo}>
                <a href="https://www.ca.gov" className={styles.cagovLogoLink}>
                  <span className="ca-gov-logo-svg" aria-label="CA.gov"></span>
                  <span className={styles.srOnly}>CA.gov</span>
                </a>
                <p className={styles.officialTag}>
                  <span className={styles.desktopOnly}>Official website of the </span>
                  State of California
                </p>
              </div>
            </div>
            <div className={styles.settingsLinks}>
              <LanguageSwitcher />
            </div>
            {isAuthenticated && (
              <div className={styles.userActions}>
                {/* Show Dashboard/Home toggle button for case workers */}
                {user?.role?.toUpperCase() === 'CASE_WORKER' && (
                  <button
                    type="button"
                    className={styles.dashboardButton}
                    onClick={() => {
                      if (showDashboard || isSupervisorPage) {
                        // Go back to SW-Home (remove dashboard parameter)
                        router.push('/');
                      } else {
                        // Go to Dashboard view
                        router.push('/?dashboard=true');
                      }
                    }}
                  >
                    {(showDashboard || isSupervisorPage) ? 'Home' : 'Dashboard'}
                  </button>
                )}
                <span className={styles.welcomeText}>
                  {user?.name || user?.username || 'User'}
                </span>
                <button type="button" className={styles.logoutButton} onClick={handleLogout}>
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Site Header */}
      <SiteHeader
        stateText="State of California"
        departmentText={shouldShowSupervisorNav ? 'Timesheet Reporting System' : (isCaseWorker ? 'Social Work Management System' : 'CMIPS - Case Management Information and Payrolling System')}
      />

      {/* Navigation and Search */}
      {!isLoginPage && (
        <div className={styles.navigationWrapper}>
          <div className="container">
            <div className={styles.navigationSearch}>
              <div id="head-search" className={styles.searchContainer}>
                <ActiveSearch />
              </div>
              <div className={styles.navControls}>
                <SiteNavigation type="dropdown" contentArr={navigationContent} />
              </div>
            </div>
          </div>
        </div>
      )}
    </header>
  );
}

