'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/contexts/AuthContext';
import styles from './sw-team-workloads.module.css';

export default function SWTeamWorkloadsPage() {
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
        <Breadcrumb path={['Home']} currentPage="SW-Team & Workloads" />

        <div className={styles.content}>
          <div className={styles.header}>
            <div className={styles.headerTitle}>
              <h1>SW-Team & Workloads</h1>
              <p>Manage team workloads, assignments, and resource allocation</p>
            </div>
          </div>

          <div className={styles.mainContent}>
            <div className={styles.infoCard}>
              <h2>Team Overview</h2>
              <p>View and manage team member workloads, assignments, and capacity.</p>
            </div>

            <div className={styles.placeholder}>
              <div className={styles.placeholderIcon}>👥</div>
              <h3>Coming Soon</h3>
              <p>Team workload management features are currently under development.</p>
              <p className={styles.placeholderSubtext}>
                This section will include team member assignments, workload distribution, capacity planning, and resource allocation tools.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

