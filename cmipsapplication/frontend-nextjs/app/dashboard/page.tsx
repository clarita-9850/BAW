'use client';

import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './dashboard.module.css';
import { useAuth } from '@/contexts/AuthContext';
import { districtCountyService } from '@/lib/services/districtCounty.service';
import { reportService, type ReportRequest } from '@/lib/services/report.service';
import Alert, { type AlertType } from '@/components/Alert';

const DashboardPage = () => {
  const router = useRouter();
  const { isAuthenticated, loading, user } = useAuth();

  const today = useMemo(() => new Date().toISOString().slice(0, 10), []);
  const lastMonth = useMemo(() => {
    const d = new Date();
    d.setMonth(d.getMonth() - 1);
    return d.toISOString().slice(0, 10);
  }, []);

  const [startDate, setStartDate] = useState(lastMonth);
  const [endDate, setEndDate] = useState(today);
  const [countyId, setCountyId] = useState('');
  const [pageSize, setPageSize] = useState(25);
  const [currentPage, setCurrentPage] = useState(0);

  const [tableRows, setTableRows] = useState<any[]>([]);
  const [columns, setColumns] = useState<string[]>([]);
  const [totalRecords, setTotalRecords] = useState(0);
  const [reportGeneratedAt, setReportGeneratedAt] = useState<string | null>(null);

  const [isFetching, setIsFetching] = useState(false);
  const [status, setStatus] = useState<{ type: 'success' | 'error' | 'info'; message: string } | null>(null);
  const [countyOptions, setCountyOptions] = useState<Array<{ value: string; label: string }>>([]);
  const [countiesLoading, setCountiesLoading] = useState(false);

  const formatCountyLabel = useCallback((value: string) => {
    if (!value) return '';
    return value
      .replace(/[_-]/g, ' ')
      .split(' ')
      .filter(Boolean)
      .map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1).toLowerCase())
      .join(' ');
  }, []);


  useEffect(() => {
    if (!user?.role) {
      console.warn('⚠️ Dashboard: User role not available:', user);
      return;
    }

    const loadCounties = async () => {
      try {
        setCountiesLoading(true);
        const userRole = user.role?.toUpperCase() || user.role;
        const userCounty = user.countyId;
        
        console.log('🔍 Dashboard: Loading counties for role:', userRole, 'User county from JWT:', userCounty);
        
        // Check if user is county-restricted (SUPERVISOR, CASE_WORKER, PROVIDER, RECIPIENT)
        const isCountyRestricted = userRole.includes('SUPERVISOR') || 
                                   userRole.includes('CASE_WORKER') || 
                                   userRole.includes('PROVIDER') || 
                                   userRole.includes('RECIPIENT');
        
        // If user is county-restricted and has a county in JWT, use that directly
        if (isCountyRestricted && userCounty) {
          console.log('🔐 Dashboard: County-restricted user, using JWT county:', userCounty);
          const countyOption = {
            value: userCounty,
            label: formatCountyLabel(userCounty),
          };
          setCountyOptions([countyOption]);
          setCountyId(userCounty);
          console.log('✅ Dashboard: Auto-selected county from JWT:', userCounty);
        } else {
          // For ADMIN/SYSTEM_SCHEDULER, load accessible counties from API
          const response = await districtCountyService.getAccessibleCounties(userRole);
          console.log('🔍 Dashboard: Received counties response:', response);
          const accessibleCounties = response?.accessibleCounties || [];
          console.log('🔍 Dashboard: Accessible counties:', accessibleCounties);
          
          const normalizedOptions = accessibleCounties.map((county) => ({
            value: county,
            label: formatCountyLabel(county),
          }));

          setCountyOptions(normalizedOptions);
          console.log('🔍 Dashboard: County options set:', normalizedOptions);

          // If user has a county in JWT, pre-select it even for ADMIN
          if (userCounty) {
            // Try exact match first
            const exactMatch = normalizedOptions.find(
              (option) => option.value === userCounty
            );
            if (exactMatch) {
              setCountyId(exactMatch.value);
              console.log('📍 Dashboard: Pre-selected user county (exact match):', exactMatch.value);
            } else {
              // Try case-insensitive match
              const normalizedUserCounty = userCounty.toLowerCase();
              const match = normalizedOptions.find(
                (option) =>
                  option.value.toLowerCase() === normalizedUserCounty ||
                  option.label.toLowerCase() === normalizedUserCounty
              );
              if (match) {
                setCountyId(match.value);
                console.log('📍 Dashboard: Pre-selected user county (fuzzy match):', match.value);
              } else {
                // If not in list but user has county, add it
                console.log('📍 Dashboard: User county not in list, adding:', userCounty);
                const userCountyOption = {
                  value: userCounty,
                  label: formatCountyLabel(userCounty),
                };
                setCountyOptions([...normalizedOptions, userCountyOption]);
                setCountyId(userCounty);
              }
            }
          }
        }
      } catch (err: any) {
        console.error('❌ Dashboard: Error loading counties:', err);
        console.error('❌ Dashboard: Error response:', err?.response);
        
        // Even if API fails, if user has county from JWT, use it
        const userCounty = user.countyId;
        if (userCounty) {
          console.log('⚠️ Dashboard: API failed but using JWT county:', userCounty);
          const countyOption = {
            value: userCounty,
            label: formatCountyLabel(userCounty),
          };
          setCountyOptions([countyOption]);
          setCountyId(userCounty);
        } else {
          const message = err?.response?.data?.message || err?.message || 'Unable to load counties';
          setStatus({ type: 'error', message });
        }
      } finally {
        setCountiesLoading(false);
      }
    };

    loadCounties();
  }, [user?.role, user?.countyId, formatCountyLabel]);

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [isAuthenticated, loading, router]);

  const showStatus = (message: string, type: 'success' | 'error' | 'info') => {
    setStatus({ message, type });
    setTimeout(() => setStatus(null), 4000);
  };

  const inferColumns = (rows: any[]) => {
    if (!rows?.length) return [];
    const keys = new Set<string>();
    rows.forEach((record) => {
      Object.keys(record).forEach((key) => keys.add(key));
    });
    return Array.from(keys);
  };

  const fetchReport = useCallback(
    async (pageOverride = 0, pageSizeOverride = pageSize, showToast = true) => {
      if (!user?.role) {
        showStatus('User role missing from session.', 'error');
        return;
      }

      if (!startDate || !endDate) {
        showStatus('Please choose a start and end date.', 'error');
        return;
      }

      setIsFetching(true);
      try {
        const response = await reportService.generateReport({
          userRole: user.role,
          reportType: 'TIMESHEET_REPORT',
          startDate,
          endDate,
          userCounty: countyId || undefined,
          page: pageOverride,
          pageSize: pageSizeOverride,
        });

        const records = response.data?.records || [];
        setTableRows(records);
        setColumns(inferColumns(records));
        setReportGeneratedAt(response.generatedAt || new Date().toISOString());
        setTotalRecords(response.data?.totalRecords ?? records.length);
        setCurrentPage(pageOverride);

        if (showToast) {
          if (!records.length) {
            showStatus('No records returned for the selected filters.', 'info');
          } else {
            showStatus(`Showing ${records.length} record(s).`, 'success');
          }
        }
      } catch (err: any) {
        const message = err?.response?.data?.message || err.message || 'Unable to fetch report';
        showStatus(message, 'error');
      } finally {
        setIsFetching(false);
      }
    },
    [countyId, endDate, pageSize, startDate, user?.role]
  );

  const handleExportCSV = () => {
    if (!tableRows.length) {
      showStatus('Generate a report before exporting.', 'error');
      return;
    }
    const activeColumns = columns.length ? columns : Object.keys(tableRows[0]);
    const header = activeColumns.join(',');
    const rows = tableRows.map((record) =>
      activeColumns
        .map((key) => {
          const value = record[key];
          if (value === null || value === undefined) return '';
          const stringValue = typeof value === 'object' ? JSON.stringify(value) : String(value);
          return stringValue.includes(',') ? `"${stringValue.replace(/"/g, '""')}"` : stringValue;
        })
        .join(',')
    );
    const csv = [header, ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `timesheet-dashboard-${Date.now()}.csv`;
    link.click();
    URL.revokeObjectURL(url);
    showStatus('CSV exported successfully.', 'success');
  };

  const handleDownloadPDF = useCallback(async () => {
    if (!tableRows.length) {
      showStatus('Generate a report before downloading PDF.', 'error');
      return;
    }

    if (!user?.role) {
      showStatus('User role missing from session.', 'error');
      return;
    }

    if (!startDate || !endDate) {
      showStatus('Please select a date range before downloading PDF.', 'error');
      return;
    }

    try {
      showStatus('Generating enhanced PDF report...', 'info');
      const request: ReportRequest = {
        userRole: user.role,
        reportType: 'TIMESHEET_REPORT',
        startDate,
        endDate,
        userCounty: countyId || undefined,
      };

      const blob = await reportService.downloadPDFReport(request);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `timesheet-report-${Date.now()}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      showStatus('Enhanced PDF report downloaded successfully!', 'success');
    } catch (err: any) {
      const message = err?.response?.data?.message || err.message || 'Failed to download PDF report';
      showStatus(message, 'error');
    }
  }, [tableRows.length, user?.role, startDate, endDate, countyId]);

  const formattedMetrics = useMemo(() => {
    const recordCount = tableRows.length;
    const totalHours = tableRows.reduce((sum, row) => sum + (Number(row.totalHours ?? row.totalhours) || 0), 0);
    const totalAmount = tableRows.reduce((sum, row) => sum + (Number(row.totalAmount ?? row.totalamount) || 0), 0);
    return [
      { label: 'Records Returned', value: recordCount.toLocaleString() },
      { label: 'Total Hours', value: totalHours.toLocaleString(undefined, { maximumFractionDigits: 1 }) },
      {
        label: 'Total Amount',
        value: totalAmount
          ? totalAmount.toLocaleString('en-US', { style: 'currency', currency: 'USD' })
          : '$0.00',
      },
      {
        label: 'Avg. Hours',
        value: recordCount ? (totalHours / recordCount).toFixed(1) : '0',
      },
      {
        label: 'Avg. Amount',
        value: recordCount
          ? (totalAmount / recordCount).toLocaleString('en-US', { style: 'currency', currency: 'USD' })
          : '$0.00',
      },
    ];
  }, [tableRows]);

  const renderTableCell = (value: any) => {
    if (value === null || value === undefined) return '—';
    if (typeof value === 'object') return JSON.stringify(value);
    return String(value);
  };

  // Check if user is county-restricted (SUPERVISOR, CASE_WORKER, PROVIDER, RECIPIENT)
  const isCountyRestricted = useMemo(() => {
    const userRole = user?.role?.toUpperCase() || '';
    return userRole.includes('SUPERVISOR') || 
           userRole.includes('CASE_WORKER') || 
           userRole.includes('PROVIDER') || 
           userRole.includes('RECIPIENT');
  }, [user?.role]);

  if (loading || !isAuthenticated) {
    return (
      <div className="container" style={{ minHeight: '60vh', display: 'flex', alignItems: 'center' }}>
        <p>Loading dashboard...</p>
      </div>
    );
  }

  return (
    <div className="container">
      <div className={styles.page}>
        <section className={styles.hero}>
          <h1 className={styles.heroTitle}>Advanced Timesheet Analytics Dashboard</h1>
          <p className={styles.heroSubtitle}>
            Welcome {user?.name || user?.username}. Generate role-aware reports with pagination and exports.
          </p>
          <div className={styles.metricsGrid}>
            {formattedMetrics.map((metric) => (
              <div className={styles.metricCard} key={metric.label}>
                <p className={styles.metricLabel}>{metric.label}</p>
                <p className={styles.metricValue}>{metric.value}</p>
              </div>
            ))}
          </div>
        </section>

        <section className={styles.controlsPanel}>
          <div className={styles.controlsGrid}>
            <div className={styles.controlGroup}>
              <label className={styles.controlLabel}>Start Date</label>
              <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
            </div>
            <div className={styles.controlGroup}>
              <label className={styles.controlLabel}>End Date</label>
              <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
            </div>
            <div className={styles.controlGroup}>
              <label className={styles.controlLabel}>
                County {isCountyRestricted && <span style={{ fontSize: '0.8em', color: '#666' }}>(from your account)</span>}
              </label>
              <select
                value={countyId}
                onChange={(e) => setCountyId(e.target.value)}
                disabled={countiesLoading || isCountyRestricted}
                title={isCountyRestricted ? `Your account is restricted to ${countyId}` : 'Select a county'}
              >
                {/* Only show "All Counties" option for non-restricted users */}
                {!isCountyRestricted && <option value="">All Counties</option>}
                {countyOptions.map((county) => (
                  <option key={county.value} value={county.value}>
                    {county.label}
                  </option>
                ))}
              </select>
              {countiesLoading && <p className={styles.hintText}>Loading counties...</p>}
              {isCountyRestricted && !countiesLoading && (
                <p className={styles.hintText}>🔐 Restricted to your assigned county</p>
              )}
            </div>
            <div className={styles.controlGroup}>
              <label className={styles.controlLabel}>Page Size</label>
              <select value={pageSize} onChange={(e) => setPageSize(Number(e.target.value))}>
                {[25, 50, 100].map((size) => (
                  <option key={size} value={size}>
                    {size} records
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className={styles.buttonRow}>
            <button
              className="btn btn-primary"
              onClick={() => fetchReport(0, pageSize, true)}
              disabled={isFetching}
              type="button"
            >
              <span className="ca-gov-icon-check-list" aria-hidden="true"></span>
              {isFetching ? 'Generating...' : 'Generate Report'}
            </button>
            <button 
              className="btn btn-secondary" 
              onClick={handleExportCSV} 
              disabled={!tableRows.length}
              type="button"
            >
              <span className="ca-gov-icon-download" aria-hidden="true"></span>
              Export CSV
            </button>
            <button
              className="btn btn-secondary"
              onClick={handleDownloadPDF}
              disabled={!tableRows.length || isFetching}
              type="button"
            >
              <span className="ca-gov-icon-pdf-text" aria-hidden="true"></span>
              Download PDF
            </button>
          </div>

          {status && (
            <Alert
              type={(status.type === 'error' ? 'danger' : status.type === 'success' ? 'success' : 'info') as AlertType}
              message={status.message}
              onClose={() => setStatus(null)}
            />
          )}
        </section>

        <section className={styles.resultsCard}>
          <div className={styles.resultsHeader}>
            <div>
              <p className={styles.resultsTitle}>Report Results</p>
              {reportGeneratedAt && (
                <p className={styles.heroSubtitle}>Generated on {new Date(reportGeneratedAt).toLocaleString()}</p>
              )}
            </div>
            <p className={styles.pageInfo}>
              Showing {tableRows.length} of {totalRecords.toLocaleString()} record(s)
            </p>
          </div>

          <div className={styles.tableWrapper}>
            {isFetching ? (
              <div className={styles.loadingBlock}>Loading report...</div>
            ) : !tableRows.length ? (
              <div className={styles.emptyState}>No report generated yet.</div>
            ) : (
              <table className={styles.dataTable}>
                <thead>
                  <tr>
                    {columns.map((col) => (
                      <th key={col}>{col}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {tableRows.map((row, idx) => (
                    <tr key={idx}>
                      {columns.map((col) => (
                        <td key={col}>{renderTableCell(row[col])}</td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {tableRows.length > 0 && (
            <div className={styles.pagination}>
              <div className={styles.paginationInfo}>
                <strong>Page {currentPage + 1}</strong> • Showing {tableRows.length} row(s) on this page
              </div>
              <div className={styles.paginationButtons}>
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => fetchReport(Math.max(currentPage - 1, 0), pageSize, false)}
                  disabled={isFetching || currentPage === 0}
                  type="button"
                >
                  <span className="ca-gov-icon-arrow-prev" aria-hidden="true"></span>
                  Previous
                </button>
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => fetchReport(currentPage + 1, pageSize, false)}
                  disabled={isFetching || tableRows.length < pageSize}
                  type="button"
                >
                  Next
                  <span className="ca-gov-icon-arrow-next" aria-hidden="true"></span>
                </button>
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  );
};

export default DashboardPage;
