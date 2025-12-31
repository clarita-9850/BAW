'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/contexts/AuthContext';
import styles from './my-cases.module.css';

export default function MyCasesPage() {
  const { t } = useTranslation();
  const router = useRouter();
  const { isAuthenticated, loading: authLoading } = useAuth();

  React.useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  if (authLoading || !isAuthenticated) {
    return (
      <div className="container py-5 text-center">
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className={styles.pageContainer}>
      <div className="container">
        <Breadcrumb path={['Home']} currentPage="My Cases" />

        <div className={styles.content}>
          <div className={styles.header}>
            <div className={styles.headerTitle}>
              <h1>My Cases</h1>
              <p>View and manage your assigned cases and client information</p>
            </div>
          </div>

          <div className={styles.mainContent}>
            <div className={styles.infoCard}>
              <h2>Case Management</h2>
              <p>Access your assigned cases, update case information, and track case progress.</p>
            </div>

            <div className={styles.placeholder}>
              <div className={styles.placeholderIcon}>📋</div>
              <h3>Coming Soon</h3>
              <p>Case management features are currently under development.</p>
              <p className={styles.placeholderSubtext}>
                This section will include case listings, case details, client information, case history, and case status tracking.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

