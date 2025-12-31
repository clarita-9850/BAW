'use client';

import React from 'react';
import styles from './Banner.module.css';

interface BannerProps {
  titleText: string;
  bodyText: string;
  buttonText?: string;
  buttonHref?: string;
  imageSrc?: string;
}

export default function Banner({
  titleText,
  bodyText,
  buttonText,
  buttonHref,
  imageSrc,
}: BannerProps) {
  return (
    <div className={styles.heroBanner}>
      <div className="container">
        <div className="row">
          <div className="col-lg-8">
            <div className={styles.heroContent}>
              <h1>{titleText}</h1>
              <p className={styles.heroDescription}>{bodyText}</p>
              {buttonText && buttonHref && (
                <a href={buttonHref} className="btn btn-primary">
                  {buttonText}
                </a>
              )}
            </div>
          </div>
          {imageSrc && (
            <div className="col-lg-4">
              <div className={styles.heroImage}>
                <img src={imageSrc} alt="Banner image" className="img-fluid" />
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

