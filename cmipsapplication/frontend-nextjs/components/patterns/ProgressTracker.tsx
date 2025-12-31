'use client';

import React from 'react';
import styles from './ProgressTracker.module.css';

export interface ProgressStep {
  title: string;
  body: string;
}

interface ProgressTrackerProps {
  type?: 'horizontal' | 'vertical';
  currentStep?: number;
  contentArr: ProgressStep[];
}

export default function ProgressTracker({
  type = 'horizontal',
  currentStep = 1,
  contentArr,
}: ProgressTrackerProps) {
  const progressPercentage = ((currentStep - 1) / (contentArr.length - 1)) * 100;

  if (type === 'horizontal') {
    return (
      <div className={`${styles.progressTracker} ${styles.horizontal}`}>
        <div className={styles.progressTrackerHorizontal}>
          <div className={styles.progressLine}>
            <div
              className={styles.progressFill}
              style={{ width: `${progressPercentage}%` }}
            ></div>
          </div>
          <div className={styles.steps}>
            {contentArr.map((step, index) => {
              const stepNumber = index + 1;
              const isCompleted = stepNumber <= currentStep;
              const isCurrent = stepNumber === currentStep;

              return (
                <div
                  key={index}
                  className={`${styles.step} ${isCompleted ? styles.completed : ''} ${
                    isCurrent ? styles.current : ''
                  }`}
                >
                  <div className={styles.stepNumber}>{stepNumber}</div>
                  <div className={styles.stepContent}>
                    <h4>{step.title}</h4>
                    <p>{step.body}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    );
  }

  // Vertical layout
  return (
    <div className={`${styles.progressTracker} ${styles.vertical}`}>
      <div className={styles.progressTrackerVertical}>
        {contentArr.map((step, index) => {
          const stepNumber = index + 1;
          const isCompleted = stepNumber <= currentStep;
          const isCurrent = stepNumber === currentStep;

          return (
            <div
              key={index}
              className={`${styles.step} ${isCompleted ? styles.completed : ''} ${
                isCurrent ? styles.current : ''
              }`}
            >
              <div className={styles.stepConnector}>
                <div className={styles.stepDot}></div>
                {index < contentArr.length - 1 && (
                  <div className={styles.stepLine}></div>
                )}
              </div>
              <div className={styles.stepContent}>
                <h4>{step.title}</h4>
                <p>{step.body}</p>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

