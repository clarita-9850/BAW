'use client';

import React from 'react';
import Link from 'next/link';
import styles from './Breadcrumb.module.css';

interface BreadcrumbProps {
  path: string[];
  currentPage: string;
}

export default function Breadcrumb({ path, currentPage }: BreadcrumbProps) {
  return (
    <div className={styles.breadcrumbContainer}>
      <div className={styles.breadcrumbCards}>
        {path.map((segment, index) => (
          <React.Fragment key={index}>
            <div className={styles.breadcrumbCard}>
              <span className={styles.breadcrumbText}>{segment}</span>
            </div>
            {index < path.length - 1 && (
              <div className={styles.breadcrumbSeparator}>›</div>
            )}
          </React.Fragment>
        ))}
        {currentPage && (
          <div className={`${styles.breadcrumbCard} ${styles.current}`}>
            <span className={styles.breadcrumbText}>{currentPage}</span>
          </div>
        )}
      </div>
    </div>
  );
}

