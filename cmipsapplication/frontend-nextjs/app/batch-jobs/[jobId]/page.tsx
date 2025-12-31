'use client';

import React, { useMemo, useState } from 'react';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/contexts/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { jobService, type JobStatus } from '@/lib/services/job.service';

export default function JobDetailsPage() {
  const router = useRouter();
  const params = useParams();
  const jobId = Array.isArray(params?.jobId) ? params.jobId[0] : params?.jobId;
  const { isAuthenticated } = useAuth();

  const {
    data: jobDetails,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ['job-status', jobId],
    queryFn: () => jobService.getJobStatus(jobId!),
    enabled: isAuthenticated && !!jobId,
    refetchInterval: 5000,
  });

  const { data: allJobs } = useQuery({
    queryKey: ['job-history'],
    queryFn: jobService.getAllJobs,
    enabled: isAuthenticated,
    refetchInterval: 15000,
  });

  const history = useMemo(() => {
    if (!jobDetails || !allJobs) return [];
    return allJobs
      .filter((job) => job.reportType === jobDetails.reportType)
      .sort((a, b) => {
        const aTime = a.createdAt ? new Date(a.createdAt).getTime() : 0;
        const bTime = b.createdAt ? new Date(b.createdAt).getTime() : 0;
        return bTime - aTime;
      });
  }, [jobDetails, allJobs]);

  if (!isAuthenticated) {
    return (
      <>
        <Breadcrumb path={['Home', 'Batch Jobs']} currentPage="Job Detail" />
        <div className="text-center">
          <h1>Please log in to view job details</h1>
          <a href="/login" className="btn btn-primary mt-3">
            Go to Login
          </a>
        </div>
      </>
    );
  }

  if (!jobId) {
    return router.replace('/batch-jobs');
  }

  return (
    <>
      <Breadcrumb path={['Home', 'Batch Jobs']} currentPage={jobId} />
      <div className="container py-4">
        <div className="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
          <div>
            <h1 className="h3 mb-1">Job {jobId}</h1>
            <p className="text-muted mb-0">Detailed status and history for this report run.</p>
          </div>
          <Link href="/batch-jobs" className="btn btn-outline-secondary">
            ← Back to Batch Jobs
          </Link>
        </div>

        {isLoading ? (
          <div className="text-center py-5">
            <p>Loading job details...</p>
          </div>
        ) : isError || !jobDetails ? (
          <div className="alert alert-danger">
            Unable to load job details. The job may not exist or you may not have access.
          </div>
        ) : (
          <div className="row">
            <div className="col-lg-8 mb-4">
              <div className="card mb-4">
                <div className="card-header">
                  <span className="fw-semibold">Job Summary</span>
                </div>
                <div className="card-body">
                  <div className="row">
                    <InfoColumn label="Status">
                      <span className={`badge ${
                        jobDetails.status === 'COMPLETED' ? 'bg-success' :
                        jobDetails.status === 'FAILED' ? 'bg-danger' :
                        jobDetails.status === 'PROCESSING' ? 'bg-primary' :
                        'bg-secondary'
                      }`}>
                        {jobDetails.status}
                      </span>
                    </InfoColumn>
                    <InfoColumn label="Progress">
                      <div className="progress" style={{ maxWidth: '220px' }}>
                        <div
                          className="progress-bar"
                          role="progressbar"
                          style={{ width: `${jobDetails.progress}%` }}
                          aria-valuenow={jobDetails.progress}
                          aria-valuemin={0}
                          aria-valuemax={100}
                        >
                          {jobDetails.progress}%
                        </div>
                      </div>
                    </InfoColumn>
                    <InfoColumn label="Report Type">{jobDetails.reportType || '—'}</InfoColumn>
                    <InfoColumn label="User Role">{jobDetails.userRole || '—'}</InfoColumn>
                    <InfoColumn label="Created At">{formatDate(jobDetails.createdAt)}</InfoColumn>
                    <InfoColumn label="Started At">{formatDate(jobDetails.startedAt)}</InfoColumn>
                    <InfoColumn label="Completed At">{formatDate(jobDetails.completedAt)}</InfoColumn>
                  </div>
                  {jobDetails.errorMessage && (
                    <div className="alert alert-danger mt-3">
                      <strong>Error:</strong> {jobDetails.errorMessage}
                    </div>
                  )}
                </div>
              </div>

              <div className="card">
                <div className="card-header d-flex justify-content-between align-items-center">
                  <span className="fw-semibold">Actions</span>
                  <small className="text-muted">Download or cancel this job</small>
                </div>
                <div className="card-body d-flex gap-2 flex-wrap">
                  <button
                    className="btn btn-outline-primary"
                    onClick={async () => {
                      try {
                        const blob = await jobService.downloadReport(jobId);
                        const url = window.URL.createObjectURL(blob);
                        const a = document.createElement('a');
                        a.href = url;
                        a.download = `report-${jobId}.json`;
                        document.body.appendChild(a);
                        a.click();
                        document.body.removeChild(a);
                        window.URL.revokeObjectURL(url);
                      } catch (error: any) {
                        alert('Failed to download report: ' + (error?.message || 'Unknown error'));
                      }
                    }}
                    disabled={jobDetails.status !== 'COMPLETED'}
                  >
                    Download Output
                  </button>
                  {jobDetails.status === 'PROCESSING' && (
                    <button
                      className="btn btn-outline-danger"
                      onClick={async () => {
                        await jobService.cancelJob(jobId);
                      }}
                    >
                      Cancel Job
                    </button>
                  )}
                </div>
              </div>
            </div>

            <div className="col-lg-4 mb-4">
              <div className="card h-100">
                <div className="card-header">
                  <span className="fw-semibold">Run History ({jobDetails.reportType})</span>
                </div>
                <div className="card-body" style={{ maxHeight: '480px', overflowY: 'auto' }}>
                  {!history.length && <div className="text-muted small">No past executions recorded yet.</div>}
                  <ol className="list-group list-group-numbered list-group-flush">
                    {history.map((run) => (
                      <li key={run.jobId} className="list-group-item d-flex justify-content-between align-items-start">
                        <div>
                          <div className="fw-semibold">{formatDate(run.createdAt)}</div>
                          <div className="text-muted small">{run.jobId}</div>
                        </div>
                        <span className={`badge ${
                          run.status === 'COMPLETED' ? 'bg-success' :
                          run.status === 'FAILED' ? 'bg-danger' :
                          run.status === 'PROCESSING' ? 'bg-primary' :
                          'bg-secondary'
                        }`}>
                          {run.status}
                        </span>
                      </li>
                    ))}
                  </ol>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

const InfoColumn = ({ label, children }: { label: string; children: React.ReactNode }) => (
  <div className="col-md-6 col-xl-4 mb-3">
    <p className="text-muted mb-1 small text-uppercase">{label}</p>
    <div className="fw-semibold">{children || '—'}</div>
  </div>
);

function formatDate(value?: string) {
  if (!value) return '—';
  return new Date(value).toLocaleString('en-IN', {
    timeZone: 'Asia/Kolkata',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: true
  });
}

