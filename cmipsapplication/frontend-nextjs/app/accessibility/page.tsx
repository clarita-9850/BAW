'use client';

import React from 'react';

export default function AccessibilityPage() {
  return (
    <div className="container" style={{ padding: '2rem 0', maxWidth: '800px' }}>
      <h1>Website Accessibility Certification</h1>
      
      <section style={{ marginTop: '2rem' }}>
        <h2>Accessibility Commitment</h2>
        <p>
          The State of California is committed to ensuring that our digital services are accessible to everyone,
          including individuals with disabilities. The Timesheet Reporting System is designed to comply with the
          Web Content Accessibility Guidelines (WCAG) 2.1 Level AA standards.
        </p>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Compliance Standards</h2>
        <p>
          This website complies with:
        </p>
        <ul>
          <li>
            <strong>WCAG 2.1 Level AA</strong> - Web Content Accessibility Guidelines
          </li>
          <li>
            <strong>California Government Code Section 11135</strong> - Prohibits discrimination on the basis of disability
          </li>
          <li>
            <strong>Section 508</strong> - Federal accessibility requirements
          </li>
          <li>
            <strong>California State Web Template Standards</strong> - Official state design system
          </li>
        </ul>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Accessibility Features</h2>
        <p>This website includes the following accessibility features:</p>
        <ul>
          <li>Keyboard navigation support for all interactive elements</li>
          <li>Screen reader compatibility with proper ARIA labels</li>
          <li>Sufficient color contrast ratios (WCAG 2.1 AA compliant)</li>
          <li>Alternative text for images and icons</li>
          <li>Semantic HTML structure</li>
          <li>Focus indicators for keyboard navigation</li>
          <li>Responsive design for various screen sizes</li>
        </ul>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Reporting Accessibility Issues</h2>
        <p>
          If you encounter any accessibility barriers on this website, please contact us:
        </p>
        <ul>
          <li>
            <strong>Email:</strong> accessibility@example.ca.gov
          </li>
          <li>
            <strong>Phone:</strong> (555) 123-4567
          </li>
          <li>
            <strong>Mail:</strong> State of California, Accessibility Office, [Address]
          </li>
        </ul>
        <p>
          Please include:
        </p>
        <ul>
          <li>The URL of the page where you encountered the issue</li>
          <li>A description of the accessibility barrier</li>
          <li>Your contact information</li>
        </ul>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Third-Party Content</h2>
        <p>
          Some content on this website may be provided by third parties. While we strive to ensure all content
          meets accessibility standards, we may not have direct control over third-party content. If you encounter
          accessibility issues with third-party content, please report them to us.
        </p>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Ongoing Improvements</h2>
        <p>
          We are continuously working to improve the accessibility of this website. Regular audits and user feedback
          help us identify and address accessibility issues. We welcome your feedback and suggestions.
        </p>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Certification Date</h2>
        <p>
          <strong>Last Updated:</strong> {new Date().toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
          })}
        </p>
        <p>
          This website has been tested and certified to meet WCAG 2.1 Level AA standards.
        </p>
      </section>
    </div>
  );
}





