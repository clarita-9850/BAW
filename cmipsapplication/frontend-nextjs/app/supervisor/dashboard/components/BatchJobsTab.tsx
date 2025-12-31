'use client';

import React, { useMemo, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { jobService, type JobStatus, type BIReportRequest } from '@/lib/services/job.service';
import { useAuth } from '@/contexts/AuthContext';

export default function BatchJobsTab() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [currentMonth, setCurrentMonth] = useState(() => {
    const today = new Date();
    today.setDate(1);
    return today;
  });
  const [selectedDate, setSelectedDate] = useState<string | null>(() => formatDateKey(new Date()));
  const [calendarExpanded, setCalendarExpanded] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [formData, setFormData] = useState<BIReportRequest>({
    userRole: user?.role || 'SUPERVISOR',
    reportType: 'TIMESHEET_REPORT',
    targetSystem: 'BUSINESS_OBJECTS',
    dataFormat: 'JSON',
    chunkSize: 1000,
    priority: 5,
  });

  const { data: jobs, isLoading } = useQuery({
    queryKey: ['jobs', selectedStatus],
    queryFn: () => {
      if (selectedStatus === 'ALL') {
        return jobService.getAllJobs();
      }
      return jobService.getJobsByStatus(selectedStatus);
    },
    refetchInterval: 5000, // Poll every 5 seconds
  });

  const createJobMutation = useMutation({
    mutationFn: (request: BIReportRequest) => jobService.createJob(request),
    onSuccess: (data) => {
      alert(`Report job created successfully! Job ID: ${data.jobId}`);
      setShowCreateForm(false);
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
      // Reset form
      setFormData({
        userRole: user?.role || 'SUPERVISOR',
        reportType: 'TIMESHEET_REPORT',
        targetSystem: 'BUSINESS_OBJECTS',
        dataFormat: 'JSON',
        chunkSize: 1000,
        priority: 5,
      });
    },
    onError: (error: any) => {
      alert(`Failed to create report job: ${error.message || 'Unknown error'}`);
    },
  });

  const handleCreateJob = (e: React.FormEvent) => {
    e.preventDefault();
    createJobMutation.mutate(formData);
  };

  const formatDateLabel = (dateKey: string) => {
    const date = new Date(dateKey);
    return date.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
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

  const handleDownload = async (jobId: string) => {
    try {
      const blob = await jobService.downloadJob(jobId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report-${jobId}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error: any) {
      alert(`Failed to download: ${error.message || 'Unknown error'}`);
    }
  };

  return (
    <div>
      <div className="mb-4 d-flex justify-content-between align-items-center">
        <div>
          <h2 className="card-title mb-1">Batch Jobs</h2>
          <p className="text-muted">Manage and monitor batch report generation jobs</p>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => setShowCreateForm(!showCreateForm)}
        >
          {showCreateForm ? 'Cancel' : '+ Create Report'}
        </button>
      </div>

      {/* Create Report Form */}
      {showCreateForm && (
        <div className="card mb-4">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h3 className="card-title mb-0" style={{ color: 'white' }}>Create New Report</h3>
          </div>
          <div className="card-body">
            <form onSubmit={handleCreateJob}>
              <div className="row mb-3">
                <div className="col-md-6 mb-3">
                  <label htmlFor="reportType" className="form-label">
                    Report Type <span className="text-danger">*</span>
                  </label>
                  <select
                    id="reportType"
                    className="form-select"
                    value={formData.reportType}
                    onChange={(e) => setFormData({ ...formData, reportType: e.target.value })}
                    required
                  >
                    <option value="TIMESHEET_REPORT">Timesheet Report</option>
                    <option value="ANALYTICS_REPORT">Analytics Report</option>
                    <option value="BI_REPORT">BI Report</option>
                    <option value="SUMMARY_REPORT">Summary Report</option>
                  </select>
                </div>
                <div className="col-md-6 mb-3">
                  <label htmlFor="targetSystem" className="form-label">
                    Target System <span className="text-danger">*</span>
                  </label>
                  <select
                    id="targetSystem"
                    className="form-select"
                    value={formData.targetSystem}
                    onChange={(e) => setFormData({ ...formData, targetSystem: e.target.value })}
                    required
                  >
                    <option value="BUSINESS_OBJECTS">Business Objects</option>
                    <option value="CRYSTAL_REPORTS">Crystal Reports</option>
                    <option value="TABLEAU">Tableau</option>
                    <option value="POWER_BI">Power BI</option>
                  </select>
                </div>
              </div>

              <div className="row mb-3">
                <div className="col-md-6 mb-3">
                  <label htmlFor="dataFormat" className="form-label">
                    Data Format <span className="text-danger">*</span>
                  </label>
                  <select
                    id="dataFormat"
                    className="form-select"
                    value={formData.dataFormat}
                    onChange={(e) => setFormData({ ...formData, dataFormat: e.target.value })}
                    required
                  >
                    <option value="JSON">JSON</option>
                    <option value="CSV">CSV</option>
                    <option value="XML">XML</option>
                    <option value="EXCEL">Excel</option>
                    <option value="PDF">PDF</option>
                  </select>
                </div>
                <div className="col-md-6 mb-3">
                  <label htmlFor="userRole" className="form-label">
                    User Role
                  </label>
                  <input
                    type="text"
                    id="userRole"
                    className="form-control"
                    value={formData.userRole}
                    onChange={(e) => setFormData({ ...formData, userRole: e.target.value })}
                    readOnly
                  />
                </div>
              </div>

              <div className="row mb-3">
                <div className="col-md-6 mb-3">
                  <label htmlFor="startDate" className="form-label">
                    Start Date (Optional)
                  </label>
                  <input
                    type="date"
                    id="startDate"
                    className="form-control"
                    value={formData.startDate || ''}
                    onChange={(e) => setFormData({ ...formData, startDate: e.target.value || undefined })}
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label htmlFor="endDate" className="form-label">
                    End Date (Optional)
                  </label>
                  <input
                    type="date"
                    id="endDate"
                    className="form-control"
                    value={formData.endDate || ''}
                    onChange={(e) => setFormData({ ...formData, endDate: e.target.value || undefined })}
                  />
                </div>
              </div>

              <div className="row mb-3">
                <div className="col-md-6 mb-3">
                  <label htmlFor="countyId" className="form-label">
                    County (Optional)
                  </label>
                  <input
                    type="text"
                    id="countyId"
                    className="form-control"
                    value={formData.countyId || ''}
                    onChange={(e) => setFormData({ ...formData, countyId: e.target.value || undefined })}
                    placeholder="e.g., Los Angeles"
                  />
                </div>
                <div className="col-md-6 mb-3">
                  <label htmlFor="chunkSize" className="form-label">
                    Chunk Size
                  </label>
                  <input
                    type="number"
                    id="chunkSize"
                    className="form-control"
                    value={formData.chunkSize || 1000}
                    onChange={(e) => setFormData({ ...formData, chunkSize: parseInt(e.target.value) || 1000 })}
                    min="100"
                    max="10000"
                  />
                </div>
              </div>

              <div className="d-flex justify-content-end gap-2">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowCreateForm(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={createJobMutation.isPending}
                >
                  {createJobMutation.isPending ? 'Creating...' : 'Create Report Job'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="row mb-4">
        <div className="col-lg-6 mb-3">
          <label htmlFor="status-filter" className="form-label">
            Filter by Status
          </label>
          <select
            id="status-filter"
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            className="form-select"
          >
            <option value="ALL">All</option>
            <option value="QUEUED">Queued</option>
            <option value="PROCESSING">Processing</option>
            <option value="COMPLETED">Completed</option>
            <option value="FAILED">Failed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
        <div className="col-lg-6 mb-3 d-flex align-items-end">
          <div>
            <p className="text-muted mb-1">Selected Date</p>
            <span className="fw-semibold">
              {selectedDate ? formatDateLabel(selectedDate) : 'All Dates'}
            </span>
            {selectedDate && (
              <button
                className="btn btn-link btn-sm ms-2 p-0"
                onClick={() => setSelectedDate(null)}
              >
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
                  <button className="btn btn-link btn-sm text-decoration-none" onClick={() => setCalendarExpanded(false)}>
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
        <div className="text-center py-4">
          <p className="text-muted">Loading jobs...</p>
        </div>
      ) : (
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h3 className="card-title mb-0" style={{ color: 'white' }}>Batch Jobs</h3>
          </div>
          <div className="card-body">
            <div className="table-responsive">
              <table className="table table-striped table-hover">
                <thead>
                  <tr>
                    <th>Job ID</th>
                    <th>Status</th>
                    <th>Progress</th>
                    <th>Report Type</th>
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
                        <td>{job.reportType}</td>
                        <td>{job.createdAt ? new Date(job.createdAt).toLocaleString() : 'N/A'}</td>
                        <td>
                          {job.status === 'COMPLETED' && (
                            <button
                              className="btn btn-link btn-sm p-0"
                              onClick={() => handleDownload(job.jobId)}
                            >
                              Download
                            </button>
                          )}
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan={6} className="text-center text-muted">
                        {selectedDate ? 'No jobs for this date' : 'No jobs found'}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
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

