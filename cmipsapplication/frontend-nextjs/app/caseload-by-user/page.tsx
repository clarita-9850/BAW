'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/contexts/AuthContext';
import styles from './caseload-by-user.module.css';

export default function CaseloadByUserPage() {
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
        <Breadcrumb path={['Home']} currentPage="Caseload by User" />

        <div className={styles.content}>
          <div className={styles.header}>
            <div className={styles.headerTitle}>
              <h1>Caseload by User</h1>
              <p>View caseload distribution, analytics, and reporting across team members</p>
            </div>
          </div>

          <div className={styles.mainContent}>
            <div className={styles.infoCard}>
              <h2>Caseload Analytics</h2>
              <p>Monitor caseload distribution, workload balance, and performance metrics by user.</p>
            </div>

            <div className={styles.placeholder}>
              <div className={styles.placeholderIcon}>📊</div>
              <h3>Coming Soon</h3>
              <p>Caseload analytics and reporting features are currently under development.</p>
              <p className={styles.placeholderSubtext}>
                This section will include caseload distribution charts, workload analytics, user performance metrics, and reporting tools.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

