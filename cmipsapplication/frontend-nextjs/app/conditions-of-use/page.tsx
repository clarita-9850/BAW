'use client';

import React from 'react';

export default function ConditionsOfUsePage() {
  return (
    <div className="container" style={{ padding: '2rem 0', maxWidth: '800px' }}>
      <h1>Conditions of Use</h1>
      
      <section style={{ marginTop: '2rem' }}>
        <h2>Website Usage</h2>
        <p>
          By accessing and using this website, you accept and agree to be bound by the terms and provision of this agreement.
        </p>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Use License</h2>
        <p>
          Permission is granted to temporarily access the materials on the Timesheet Reporting System website for personal,
          non-commercial transitory viewing only. This is the grant of a license, not a transfer of title, and under this license you may not:
        </p>
        <ul>
          <li>Modify or copy the materials</li>
          <li>Use the materials for any commercial purpose or for any public display</li>
          <li>Attempt to decompile or reverse engineer any software contained on the website</li>
          <li>Remove any copyright or other proprietary notations from the materials</li>
        </ul>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Disclaimer</h2>
        <p>
          The materials on the Timesheet Reporting System website are provided on an 'as is' basis. The State of California
          makes no warranties, expressed or implied, and hereby disclaims and negates all other warranties including without
          limitation, implied warranties or conditions of merchantability, fitness for a particular purpose, or non-infringement
          of intellectual property or other violation of rights.
        </p>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Limitations</h2>
        <p>
          In no event shall the State of California or its suppliers be liable for any damages (including, without limitation,
          damages for loss of data or profit, or due to business interruption) arising out of the use or inability to use the
          materials on the Timesheet Reporting System website.
        </p>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Revisions</h2>
        <p>
          The State of California may revise these terms of service for its website at any time without notice. By using this
          website you are agreeing to be bound by the then current version of these terms of service.
        </p>
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Contact</h2>
        <p>
          If you have any questions about these Conditions of Use, please contact us through the{' '}
          <a href="/accessibility">Contact</a> page.
        </p>
      </section>
    </div>
  );
}





