'use client';

import React from 'react';
import styles from './Footer.module.css';

export default function Footer() {
  return (
    <footer className="site-footer" role="contentinfo">
      <div className="container">
        <div className="row">
          <div className="col-md-12">
            <div className={styles.footerContent}>
              <div className={styles.footerLogo}>
                <a href="https://www.ca.gov">
                  <span className="ca-gov-logo-svg" aria-label="CA.gov"></span>
                  <span className="sr-only">CA.gov</span>
                </a>
              </div>
              <nav>
                <ul className={styles.footerLinks}>
                  <li><a href="https://www.ca.gov">CA.gov</a></li>
                  <li><a href="/conditions-of-use">Conditions of Use</a></li>
                  <li><a href="https://ca.gov/privacy">Privacy Policy</a></li>
                  <li><a href="/accessibility">Website Accessibility Certification</a></li>
                  <li><a href="https://ca.gov/contact">Contact</a></li>
                </ul>
              </nav>
              <div className={styles.footerCopyright}>
                <p>&copy; State of California</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}


