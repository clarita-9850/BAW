'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/contexts/AuthContext';
import styles from './inbox.module.css';

export default function InboxPage() {
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
        <Breadcrumb path={['Home']} currentPage="Inbox" />

        <div className={styles.content}>
          <div className={styles.header}>
            <div className={styles.headerTitle}>
              <h1>Inbox</h1>
              <p>View your messages, notifications, and communications</p>
            </div>
          </div>

          <div className={styles.mainContent}>
            <div className={styles.infoCard}>
              <h2>Messages & Notifications</h2>
              <p>Stay updated with important messages, notifications, and system alerts.</p>
            </div>

            <div className={styles.placeholder}>
              <div className={styles.placeholderIcon}>📬</div>
              <h3>Coming Soon</h3>
              <p>Inbox and messaging features are currently under development.</p>
              <p className={styles.placeholderSubtext}>
                This section will include message management, notifications, alerts, and communication tools.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

