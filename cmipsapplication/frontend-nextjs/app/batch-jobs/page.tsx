'use client';

import Link from 'next/link';
import React, { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/contexts/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { jobService, type JobStatus } from '@/lib/services/job.service';

export default function BatchJobsPage() {
  const { t } = useTranslation();
  const { user, isAuthenticated } = useAuth();
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [currentMonth, setCurrentMonth] = useState(() => {
    const today = new Date();
    today.setDate(1);
    return today;
  });
  const [selectedDate, setSelectedDate] = useState<string | null>(() => formatDateKey(new Date()));
  const [calendarExpanded, setCalendarExpanded] = useState(false);

  const { data: jobs, isLoading } = useQuery({
    queryKey: ['jobs', selectedStatus],
    queryFn: () => {
      if (selectedStatus === 'ALL') {
        return jobService.getAllJobs();
      }
      return jobService.getJobsByStatus(selectedStatus);
    },
    enabled: isAuthenticated,
    refetchInterval: 5000, // Poll every 5 seconds
  });

  const formatDateLabel = (dateKey: string) => {
    const date = new Date(dateKey);
    return date.toLocaleDateString('en-IN', { 
      timeZone: 'Asia/Kolkata',
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  };

  const jobsByDate = useMemo(() => {
    const map: Record<string, JobStatus[]> = {};
    (jobs || []).forEach((job) => {
      if (!job.createdAt) {
        return;
      }
      const key = formatDateKey(job.createdAt);
      if (!map[key]) {
        map[key] = [];
      }
      map[key].push(job);
    });
    return map;
  }, [jobs]);

  const filteredJobs = useMemo(() => {
    if (!jobs) return [];
    if (!selectedDate) return jobs;
    return jobsByDate[selectedDate] || [];
  }, [jobs, selectedDate, jobsByDate]);

  const calendarDays = useMemo(() => buildCalendarDays(currentMonth), [currentMonth]);

  if (!isAuthenticated) {
    return (
      <>
        <Breadcrumb path={['Home']} currentPage="Batch Jobs" />
        <div className="text-center">
          <h1>Please log in to view batch jobs</h1>
          <a href="/login" className="btn btn-primary mt-3">
            Go to Login
          </a>
        </div>
      </>
    );
  }

  return (
    <>
      <Breadcrumb path={['Home']} currentPage="Batch Jobs" />
      <div className="container">
        <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
          <div>
            <h1>{t('batchJobs.title')}</h1>
            <p>{t('batchJobs.subtitle')}</p>
          </div>
        </div>

        <div className="row mb-4">
          <div className="col-lg-6 mb-3">
            <label htmlFor="status-filter" className="form-label">Filter by Status</label>
            <select
              id="status-filter"
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="form-select"
            >
              <option value="ALL">All</option>
              <option value="QUEUED">{t('batchJobs.status.queued')}</option>
              <option value="PROCESSING">{t('batchJobs.status.processing')}</option>
              <option value="COMPLETED">{t('batchJobs.status.completed')}</option>
              <option value="FAILED">{t('batchJobs.status.failed')}</option>
              <option value="CANCELLED">{t('batchJobs.status.cancelled')}</option>
            </select>
          </div>
          <div className="col-lg-6 mb-3 d-flex align-items-end">
            <div>
              <p className="text-muted mb-1">Selected Date</p>
              <span className="fw-semibold">{selectedDate ? formatDateLabel(selectedDate) : 'All Dates'}</span>
              {selectedDate && (
                <button className="btn btn-link btn-sm ms-2 p-0 align-baseline" onClick={() => setSelectedDate(null)}>
                  Clear
                </button>
              )}
            </div>
          </div>
        </div>

        <div className="row mb-4">
          <div className="col-xl-5 col-lg-6 mb-4">
            <div className="card h-100">
              <div className="card-header d-flex justify-content-between align-items-center">
                <span className="fw-semibold">Job Calendar</span>
                {calendarExpanded ? (
                  <div className="d-flex align-items-center gap-2">
                    <div className="btn-group btn-group-sm" role="group">
                      <button
                        className="btn btn-outline-secondary"
                        onClick={() => setCurrentMonth((prev) => new Date(prev.getFullYear(), prev.getMonth() - 1, 1))}
                      >
                        ‹
                      </button>
                      <span className="btn btn-outline-secondary disabled">
                        {currentMonth.toLocaleDateString(undefined, { month: 'long', year: 'numeric' })}
                      </span>
                      <button
                        className="btn btn-outline-secondary"
                        onClick={() => setCurrentMonth((prev) => new Date(prev.getFullYear(), prev.getMonth() + 1, 1))}
                      >
                        ›
                      </button>
                    </div>
                    <button className="btn btn-sm btn-link text-decoration-none" onClick={() => setCalendarExpanded(false)}>
                      Minimize
                    </button>
                  </div>
                ) : (
                  <button className="btn btn-sm btn-outline-primary" onClick={() => setCalendarExpanded(true)}>
                    Open Calendar
                  </button>
                )}
              </div>
              {calendarExpanded ? (
                <div className="card-body">
                  <div className="d-grid" style={{ gridTemplateColumns: 'repeat(7, 1fr)', gap: '0.3rem' }}>
                    {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((day) => (
                      <div key={day} className="text-center text-muted small fw-semibold">
                        {day}
                      </div>
                    ))}
                    {calendarDays.map(({ date, isCurrentMonth }) => {
                      const dateKey = formatDateKey(date);
                      const jobsCount = jobsByDate[dateKey]?.length || 0;
                      const isSelected = selectedDate === dateKey;
                      return (
                        <button
                          key={date.toISOString()}
                          type="button"
                          onClick={() => setSelectedDate(dateKey)}
                          className={`btn btn-sm ${isSelected ? 'btn-primary text-white' : 'btn-outline-secondary'}`}
                          style={{
                            borderColor: isCurrentMonth ? '#d1d5db' : '#f3f4f6',
                            color: isCurrentMonth ? '#111827' : '#9ca3af',
                            position: 'relative',
                            minHeight: '48px',
                            fontSize: '0.8rem',
                          }}
                        >
                          <span>{date.getDate()}</span>
                          {jobsCount > 0 && (
                            <span
                              className={`badge ${isSelected ? 'bg-light text-primary' : 'bg-primary'}`}
                              style={{ position: 'absolute', bottom: '6px', right: '8px' }}
                            >
                              {jobsCount}
                            </span>
                          )}
                        </button>
                      );
                    })}
                  </div>
                </div>
              ) : (
                <div className="card-body text-center py-4">
                  <p className="text-muted mb-3 small">Calendar hidden to save space.</p>
                  <button className="btn btn-sm btn-outline-primary" onClick={() => setCalendarExpanded(true)}>
                    Open calendar to pick a date
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>

        {isLoading ? (
          <div className="text-center">
            <p>Loading jobs...</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>Job ID</th>
                  <th>Status</th>
                  <th>Progress</th>
                  <th>Report Type</th>
                  <th>User Role</th>
                  <th>Created At</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredJobs && filteredJobs.length > 0 ? (
                  filteredJobs.map((job) => (
                    <tr key={job.jobId}>
                      <td>{job.jobId.substring(0, 8)}...</td>
                      <td>
                        <span className={`badge ${
                          job.status === 'COMPLETED' ? 'bg-success' :
                          job.status === 'FAILED' ? 'bg-danger' :
                          job.status === 'PROCESSING' ? 'bg-primary' :
                          'bg-secondary'
                        }`}>
                          {job.status}
                        </span>
                      </td>
                      <td>
                        <div className="progress" style={{ width: '100px' }}>
                          <div
                            className="progress-bar"
                            role="progressbar"
                            style={{ width: `${job.progress}%` }}
                            aria-valuenow={job.progress}
                            aria-valuemin={0}
                            aria-valuemax={100}
                          >
                            {job.progress}%
                          </div>
                        </div>
                      </td>
                      <td>{job.reportType || '—'}</td>
                      <td>{job.userRole || '—'}</td>
                      <td>{job.createdAt ? new Date(job.createdAt).toLocaleString('en-IN', {
                        timeZone: 'Asia/Kolkata',
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit',
                        hour12: true
                      }) : 'N/A'}</td>
                      <td>
                        <div className="d-flex gap-2 flex-wrap">
                          {job.status === 'COMPLETED' && (
                            <button className="btn btn-sm btn-outline-primary">Download</button>
                          )}
                          <Link href={`/batch-jobs/${job.jobId}`} className="btn btn-sm btn-primary">
                            View details
                          </Link>
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={7} className="text-center">{selectedDate ? 'No jobs for this date' : 'No jobs found'}</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}

function formatDateKey(value: Date | string): string {
  const date = typeof value === 'string' ? new Date(value) : value;
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function buildCalendarDays(currentMonth: Date) {
  const startOfMonth = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), 1);
  const endOfMonth = new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 0);
  const startDate = new Date(startOfMonth);
  startDate.setDate(startDate.getDate() - startDate.getDay());
  const endDate = new Date(endOfMonth);
  endDate.setDate(endDate.getDate() + (6 - endDate.getDay()));

  const days = [];
  const date = new Date(startDate);
  while (date <= endDate) {
    days.push({
      date: new Date(date),
      isCurrentMonth: date.getMonth() === currentMonth.getMonth(),
    });
    date.setDate(date.getDate() + 1);
  }
  return days;
}

