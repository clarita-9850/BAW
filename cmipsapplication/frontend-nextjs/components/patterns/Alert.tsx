'use client';

import React from 'react';
import styles from './Alert.module.css';

export type AlertType = 'info' | 'success' | 'warning' | 'danger';

interface AlertProps {
  type: AlertType;
  message: string;
  title?: string;
  onClose?: () => void;
  className?: string;
}

export default function Alert({ type, message, title, onClose, className }: AlertProps) {
  const iconMap = {
    info: 'ca-gov-icon-info',
    success: 'ca-gov-icon-check-mark',
    warning: 'ca-gov-icon-warning-diamond',
    danger: 'ca-gov-icon-warning-diamond',
  };

  const alertClass = `alert alert-${type === 'danger' ? 'danger' : type === 'warning' ? 'warning' : type === 'success' ? 'success' : 'info'}`;

  return (
    <div className={`${alertClass} ${className || ''}`} role="alert">
      <span className={iconMap[type]} aria-hidden="true"></span>
      <div className={styles.alertContent}>
        {title && <strong>{title}: </strong>}
        {message}
      </div>
      {onClose && (
        <button
          type="button"
          className={styles.alertClose}
          onClick={onClose}
          aria-label="Close alert"
        >
          <span className="ca-gov-icon-close-mark" aria-hidden="true"></span>
        </button>
      )}
    </div>
  );
}

