'use client';

import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './visualization.module.css';
import { useAuth } from '@/contexts/AuthContext';
import { analyticsService } from '@/lib/services/analytics.service';
import Alert, { AlertType } from '@/components/Alert';
import {
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';

interface StatusBanner {
  type: 'success' | 'error' | 'info';
  message: string;
}

const VisualizationPage = () => {
  const router = useRouter();
  const { isAuthenticated, loading, user } = useAuth();

  const [filtersLoaded, setFiltersLoaded] = useState(false);
  const [status, setStatus] = useState<StatusBanner | null>(null);

  const [departmentFilter, setDepartmentFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const [departments, setDepartments] = useState<string[]>([]);
  const [statuses, setStatuses] = useState<string[]>([]);

  // Demographic filters
  const [providerGenderFilter, setProviderGenderFilter] = useState('');
  const [recipientGenderFilter, setRecipientGenderFilter] = useState('');
  const [providerEthnicityFilter, setProviderEthnicityFilter] = useState('');
  const [recipientEthnicityFilter, setRecipientEthnicityFilter] = useState('');
  const [providerAgeGroupFilter, setProviderAgeGroupFilter] = useState('');
  const [recipientAgeGroupFilter, setRecipientAgeGroupFilter] = useState('');

  const [availableProviderGenders, setAvailableProviderGenders] = useState<string[]>([]);
  const [availableRecipientGenders, setAvailableRecipientGenders] = useState<string[]>([]);
  const [availableProviderEthnicities, setAvailableProviderEthnicities] = useState<string[]>([]);
  const [availableRecipientEthnicities, setAvailableRecipientEthnicities] = useState<string[]>([]);
  const [availableProviderAgeGroups, setAvailableProviderAgeGroups] = useState<string[]>([]);
  const [availableRecipientAgeGroups, setAvailableRecipientAgeGroups] = useState<string[]>([]);

  const [summaryStats, setSummaryStats] = useState({
    totalRecords: 0,
    totalHours: 0,
    totalAmount: 0,
    avgHours: 0,
    avgAmount: 0,
  });

  const [dataset, setDataset] = useState<{ columns: string[]; rows: Array<Record<string, any>> }>({
    columns: [],
    rows: [],
  });
  const [selectedColumns, setSelectedColumns] = useState<string[]>([]);
  const [resultLimit, setResultLimit] = useState(200);
  const [lastGeneratedAt, setLastGeneratedAt] = useState<string | null>(null);

  const [isGenerating, setIsGenerating] = useState(false);
  const [reportGenerated, setReportGenerated] = useState(false);

  // Chart visualization state
  const [chartType, setChartType] = useState<'bar' | 'treemap' | 'pie'>('bar');
  const [selectedDimension, setSelectedDimension] = useState<string>('status');
  const [countyFilter, setCountyFilter] = useState<string>('(All)');

  // Get available dimensions for chart - aligned with actual database schema including demographics
  const availableDimensions = useMemo(() => {
    if (!dataset.rows.length) return [];
    const dimensions = [
      { value: 'status', label: 'Status' },
      { value: 'location', label: 'Location (County)' },
      { value: 'department', label: 'Department' },
      { value: 'employeeId', label: 'Employee ID' },
      { value: 'employeeName', label: 'Employee Name' },
      { value: 'userId', label: 'User ID' },
      { value: 'submittedBy', label: 'Submitted By' },
      { value: 'approvedBy', label: 'Approved By' },
      // Provider demographics
      { value: 'providerGender', label: 'Provider Gender' },
      { value: 'providerEthnicity', label: 'Provider Ethnicity' },
      { value: 'providerAgeGroup', label: 'Provider Age Group' },
      // Recipient demographics
      { value: 'recipientGender', label: 'Recipient Gender' },
      { value: 'recipientEthnicity', label: 'Recipient Ethnicity' },
      { value: 'recipientAgeGroup', label: 'Recipient Age Group' },
    ];
    // Filter to only show dimensions that exist in the data
    return dimensions.filter((dim) => 
      dataset.rows.some((row) => row[dim.value] || row[dim.value.toLowerCase()])
    );
  }, [dataset.rows]);

  // Prepare chart data based on selected dimension and county filter
  const chartData = useMemo(() => {
    if (!dataset.rows.length || !selectedDimension) return [];

    let filteredRows = dataset.rows;
    
    // Apply county filter if not "(All)" - using actual database field 'location'
    if (countyFilter !== '(All)') {
      filteredRows = dataset.rows.filter((row: any) => {
        const county = row.location || row.Location || row.county || row.County;
        return county === countyFilter;
      });
    }

    const dimensionMap: Record<string, number> = {};
    
    filteredRows.forEach((row: any) => {
      const value = row[selectedDimension] || row[selectedDimension.toLowerCase()] || 'Unknown';
      if (value && value !== 'null' && value !== 'undefined') {
        dimensionMap[value] = (dimensionMap[value] || 0) + 1;
      }
    });

    return Object.entries(dimensionMap)
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 20); // Limit to top 20 for performance
  }, [dataset.rows, selectedDimension, countyFilter]);

  // Color palette for charts
  const CHART_COLORS = [
    '#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6',
    '#06b6d4', '#ec4899', '#84cc16', '#f97316', '#6366f1',
  ];

  const showStatus = (message: string, type: StatusBanner['type']) => {
    setStatus({ message, type });
    setTimeout(() => setStatus(null), 4000);
  };

  const loadSummaryStats = useCallback(
    async (params: Record<string, string>) => {
      try {
        const response = await analyticsService.getAdhocStats(params);
        if (response.status === 'SUCCESS' && response.stats) {
          setSummaryStats(response.stats);
        } else {
          setSummaryStats({
            totalRecords: 0,
            totalHours: 0,
            totalAmount: 0,
            avgHours: 0,
            avgAmount: 0,
          });
        }
      } catch (err: any) {
        showStatus(err?.message || 'Unable to load analytics KPIs', 'error');
      }
    },
    []
  );

  useEffect(() => {
    if (!isAuthenticated) return;
    loadSummaryStats({});
  }, [isAuthenticated, loadSummaryStats]);

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [isAuthenticated, loading, router]);

  useEffect(() => {
    const loadFilterOptions = async () => {
      try {
        const response = await analyticsService.getAdhocFilters();
        setDepartments(response.departments || []);
        setStatuses(response.statuses || []);
        
        // Get user's county from JWT token (already extracted in AuthContext)
        const userCounty = user?.countyId;
        const userRole = user?.role?.toUpperCase() || '';
        
        // Check if user is county-restricted (SUPERVISOR, CASE_WORKER, PROVIDER, RECIPIENT)
        const isCountyRestricted = userRole.includes('SUPERVISOR') || 
                                   userRole.includes('CASE_WORKER') || 
                                   userRole.includes('PROVIDER') || 
                                   userRole.includes('RECIPIENT');
        
        // Set county filter based on user's JWT county
        if (isCountyRestricted && userCounty) {
          // For county-restricted roles, auto-select and lock to user's county
          setCountyFilter(userCounty);
          console.log('🔐 Visualization: County-restricted user, setting county to:', userCounty);
        } else if (userCounty) {
          // For ADMIN/SYSTEM_SCHEDULER with county, pre-select it but allow change
          setCountyFilter(userCounty);
          console.log('📍 Visualization: Pre-selecting user county:', userCounty);
        }
        
        // Load demographic filters
        if (response.providerGenders && Array.isArray(response.providerGenders)) {
          setAvailableProviderGenders(['', ...response.providerGenders]);
        }
        if (response.recipientGenders && Array.isArray(response.recipientGenders)) {
          setAvailableRecipientGenders(['', ...response.recipientGenders]);
        }
        if (response.providerEthnicities && Array.isArray(response.providerEthnicities)) {
          setAvailableProviderEthnicities(['', ...response.providerEthnicities]);
        }
        if (response.recipientEthnicities && Array.isArray(response.recipientEthnicities)) {
          setAvailableRecipientEthnicities(['', ...response.recipientEthnicities]);
        }
        if (response.providerAgeGroups && Array.isArray(response.providerAgeGroups)) {
          setAvailableProviderAgeGroups(['', ...response.providerAgeGroups]);
        }
        if (response.recipientAgeGroups && Array.isArray(response.recipientAgeGroups)) {
          setAvailableRecipientAgeGroups(['', ...response.recipientAgeGroups]);
        }
        setFiltersLoaded(true);
      } catch (err: any) {
        setFiltersLoaded(true);
        showStatus(err?.message || 'Unable to load filter options', 'error');
      }
    };

    if (isAuthenticated) {
      loadFilterOptions();
    }
  }, [isAuthenticated, user]);

  const buildFilterParams = useCallback(() => {
    const params: Record<string, string> = {};
    if (departmentFilter) params.department = departmentFilter;
    if (statusFilter) params.status = statusFilter;
    if (countyFilter && countyFilter !== '(All)') params.county = countyFilter;
    // Demographic filters
    if (providerGenderFilter) params.providerGender = providerGenderFilter;
    if (recipientGenderFilter) params.recipientGender = recipientGenderFilter;
    if (providerEthnicityFilter) params.providerEthnicity = providerEthnicityFilter;
    if (recipientEthnicityFilter) params.recipientEthnicity = recipientEthnicityFilter;
    if (providerAgeGroupFilter) params.providerAgeGroup = providerAgeGroupFilter;
    if (recipientAgeGroupFilter) params.recipientAgeGroup = recipientAgeGroupFilter;
    return params;
  }, [departmentFilter, statusFilter, countyFilter, providerGenderFilter, recipientGenderFilter, 
      providerEthnicityFilter, recipientEthnicityFilter, providerAgeGroupFilter, recipientAgeGroupFilter]);

  const handleGenerateReport = useCallback(async () => {
    const params = {
      ...buildFilterParams(),
      limit: resultLimit.toString(),
    };

    setIsGenerating(true);
    try {
      const [statsResponse, dataResponse] = await Promise.all([analyticsService.getAdhocStats(params), analyticsService.getAdhocData(params)]);

      if (statsResponse.status === 'SUCCESS' && statsResponse.stats) {
        setSummaryStats(statsResponse.stats);
      } else {
        setSummaryStats({
          totalRecords: 0,
          totalHours: 0,
          totalAmount: 0,
          avgHours: 0,
          avgAmount: 0,
        });
      }

      if (dataResponse.status === 'SUCCESS') {
        setDataset({ columns: dataResponse.columns || [], rows: dataResponse.rows || [] });
        setSelectedColumns((prev) => {
          if (!prev.length) return dataResponse.columns || [];
          const available = new Set(dataResponse.columns);
          const next = prev.filter((col) => available.has(col));
          return next.length ? next : dataResponse.columns || [];
        });
        setLastGeneratedAt(new Date().toISOString());
        setReportGenerated(true);
        showStatus(`Loaded ${dataResponse.rows?.length || 0} records`, 'success');
      } else {
        showStatus('Analytics dataset did not return results.', 'info');
      }
    } catch (err: any) {
      showStatus(err?.message || 'Unable to generate report', 'error');
    } finally {
      setIsGenerating(false);
    }
  }, [buildFilterParams, resultLimit]);

  const clearFilters = () => {
    setDepartmentFilter('');
    setStatusFilter('');
    setProviderGenderFilter('');
    setRecipientGenderFilter('');
    setProviderEthnicityFilter('');
    setRecipientEthnicityFilter('');
    setProviderAgeGroupFilter('');
    setRecipientAgeGroupFilter('');
    setCountyFilter('(All)');
  };

  const handleFiltersSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    handleGenerateReport();
  };

  const activeFilters = useMemo(() => {
    const filters: Array<{ label: string; value: string; clear: () => void }> = [];
    if (departmentFilter) filters.push({ label: `Department: ${departmentFilter}`, value: departmentFilter, clear: () => setDepartmentFilter('') });
    if (statusFilter) filters.push({ label: `Status: ${statusFilter}`, value: statusFilter, clear: () => setStatusFilter('') });
    if (providerGenderFilter) filters.push({ label: `Provider Gender: ${providerGenderFilter}`, value: providerGenderFilter, clear: () => setProviderGenderFilter('') });
    if (recipientGenderFilter) filters.push({ label: `Recipient Gender: ${recipientGenderFilter}`, value: recipientGenderFilter, clear: () => setRecipientGenderFilter('') });
    if (providerEthnicityFilter) filters.push({ label: `Provider Ethnicity: ${providerEthnicityFilter}`, value: providerEthnicityFilter, clear: () => setProviderEthnicityFilter('') });
    if (recipientEthnicityFilter) filters.push({ label: `Recipient Ethnicity: ${recipientEthnicityFilter}`, value: recipientEthnicityFilter, clear: () => setRecipientEthnicityFilter('') });
    if (providerAgeGroupFilter) filters.push({ label: `Provider Age Group: ${providerAgeGroupFilter}`, value: providerAgeGroupFilter, clear: () => setProviderAgeGroupFilter('') });
    if (recipientAgeGroupFilter) filters.push({ label: `Recipient Age Group: ${recipientAgeGroupFilter}`, value: recipientAgeGroupFilter, clear: () => setRecipientAgeGroupFilter('') });
    if (countyFilter && countyFilter !== '(All)') filters.push({ label: `County: ${countyFilter}`, value: countyFilter, clear: () => setCountyFilter('(All)') });
    return filters;
  }, [departmentFilter, statusFilter, countyFilter]);

  const displayedColumns = useMemo(() => {
    if (selectedColumns.length) {
      return selectedColumns;
    }
    return dataset.columns;
  }, [dataset.columns, selectedColumns]);

  const handleColumnToggle = useCallback(
    (column: string) => {
      setSelectedColumns((prev) => {
        const exists = prev.includes(column);
        if (exists) {
          const filtered = prev.filter((col) => col !== column);
          return filtered.length ? filtered : prev;
        }
        return [...prev, column];
      });
    },
    []
  );

  const formatColumnLabel = (label: string) =>
    label
      .replace(/_/g, ' ')
      .replace(/\b\w/g, (char) => char.toUpperCase());

  const formatCellValue = (value: any, column: string) => {
    if (value === null || value === undefined) return '—';
    if (typeof value === 'number') {
      if (column.toLowerCase().includes('amount')) {
        return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
      }
      return value.toLocaleString();
    }
    if (typeof value === 'string') {
      const lower = column.toLowerCase();
      const isDateLike = lower.includes('date') || lower.includes('at');
      if (isDateLike && !Number.isNaN(Date.parse(value))) {
        return new Date(value).toLocaleDateString();
      }
      return value;
    }
    return value;
  };

  // Calculate executive summary metrics from dataset
  const executiveMetrics = useMemo(() => {
    if (!dataset.rows.length) {
      return {
        totalRecords: 0,
        totalHours: 0,
        totalAmount: 0,
        approvalRate: 0,
        statusDistribution: {} as Record<string, number>,
        topProviders: [] as Array<{ name: string; hours: number; amount: number }>,
      };
    }

    const statusCounts: Record<string, number> = {};
    const providerHours: Record<string, number> = {};
    const providerAmounts: Record<string, number> = {};
    let totalHours = 0;
    let totalAmount = 0;
    let approvedCount = 0;

    dataset.rows.forEach((row: any) => {
      // Status distribution
      const status = row.status || row.STATUS || 'UNKNOWN';
      statusCounts[status] = (statusCounts[status] || 0) + 1;
      if (status === 'APPROVED') approvedCount++;

      // Hours and amounts
      const hours = Number(row.total_hours || row.totalHours || 0);
      const amount = Number(row.total_amount || row.totalAmount || 0);
      totalHours += hours;
      totalAmount += amount;

      // Provider performance
      const providerName = row.provider_name || row.providerName || 'Unknown';
      providerHours[providerName] = (providerHours[providerName] || 0) + hours;
      providerAmounts[providerName] = (providerAmounts[providerName] || 0) + amount;
    });

    // Top 5 providers by hours
    const topProviders = Object.entries(providerHours)
      .sort(([, a], [, b]) => b - a)
      .slice(0, 5)
      .map(([name, hours]) => ({
        name,
        hours,
        amount: providerAmounts[name] || 0,
      }));

    const approvalRate = dataset.rows.length > 0 ? (approvedCount * 100) / dataset.rows.length : 0;

    return {
      totalRecords: dataset.rows.length,
      totalHours,
      totalAmount,
      approvalRate,
      statusDistribution: statusCounts,
      topProviders,
    };
  }, [dataset.rows]);

  // Calculate demographic breakdowns
  const demographicBreakdowns = useMemo(() => {
    if (!dataset.rows.length) {
      return {
        gender: {} as Record<string, number>,
        ethnicity: {} as Record<string, number>,
      };
    }

    const genderCounts: Record<string, number> = {};
    const ethnicityCounts: Record<string, number> = {};

    dataset.rows.forEach((row: any) => {
      const providerGender = row.provider_gender || row.providerGender;
      const recipientGender = row.recipient_gender || row.recipientGender;
      const providerEthnicity = row.provider_ethnicity || row.providerEthnicity;
      const recipientEthnicity = row.recipient_ethnicity || row.recipientEthnicity;

      if (providerGender) genderCounts[providerGender] = (genderCounts[providerGender] || 0) + 1;
      if (recipientGender) genderCounts[recipientGender] = (genderCounts[recipientGender] || 0) + 1;
      if (providerEthnicity) ethnicityCounts[providerEthnicity] = (ethnicityCounts[providerEthnicity] || 0) + 1;
      if (recipientEthnicity) ethnicityCounts[recipientEthnicity] = (ethnicityCounts[recipientEthnicity] || 0) + 1;
    });

    return { gender: genderCounts, ethnicity: ethnicityCounts };
  }, [dataset.rows]);

  // Calculate geographic analysis
  const geographicAnalysis = useMemo(() => {
    if (!dataset.rows.length) {
      return {
        county: {} as Record<string, number>,
        district: {} as Record<string, number>,
      };
    }

    const countyCounts: Record<string, number> = {};
    const districtCounts: Record<string, number> = {};

    dataset.rows.forEach((row: any) => {
      const county = row.provider_county || row.providerCounty || row.project_county || row.projectCounty;
      const district = row.district_name || row.districtName;
      if (county) countyCounts[county] = (countyCounts[county] || 0) + 1;
      if (district) districtCounts[district] = (districtCounts[district] || 0) + 1;
    });

    return { county: countyCounts, district: districtCounts };
  }, [dataset.rows]);

  // Calculate service type analysis
  const serviceTypeAnalysis = useMemo(() => {
    if (!dataset.rows.length) {
      return {} as Record<string, { count: number; hours: number; amount: number }>;
    }

    const serviceData: Record<string, { count: number; hours: number; amount: number }> = {};

    dataset.rows.forEach((row: any) => {
      const serviceType = row.service_type || row.serviceType || 'Unknown';
      const hours = Number(row.total_hours || row.totalHours || 0);
      const amount = Number(row.total_amount || row.totalAmount || 0);

      if (!serviceData[serviceType]) {
        serviceData[serviceType] = { count: 0, hours: 0, amount: 0 };
      }
      serviceData[serviceType].count++;
      serviceData[serviceType].hours += hours;
      serviceData[serviceType].amount += amount;
    });

    return serviceData;
  }, [dataset.rows]);

  // Check if user is county-restricted (SUPERVISOR, CASE_WORKER, PROVIDER, RECIPIENT)
  const isCountyRestricted = useMemo(() => {
    const userRole = user?.role?.toUpperCase() || '';
    return userRole.includes('SUPERVISOR') || 
           userRole.includes('CASE_WORKER') || 
           userRole.includes('PROVIDER') || 
           userRole.includes('RECIPIENT');
  }, [user?.role]);

  // Handle PDF download - Note: PDF generation requires date range, so we'll show a message
  const handleDownloadPDF = useCallback(async () => {
    showStatus('PDF download is available on the Dashboard page with date range filters', 'info');
  }, []);

  if (loading || !isAuthenticated) {
    return (
      <div className="container" style={{ minHeight: '60vh', display: 'flex', alignItems: 'center' }}>
        <p>Loading visualization...</p>
      </div>
    );
  }

  const metricCards = [
    { label: 'Total Records', value: summaryStats.totalRecords.toLocaleString() },
    { label: 'Total Hours', value: summaryStats.totalHours.toLocaleString(undefined, { minimumFractionDigits: 0 }) },
    { label: 'Total Amount', value: `$${summaryStats.totalAmount.toLocaleString(undefined, { minimumFractionDigits: 2 })}` },
    { label: 'Avg Hours', value: summaryStats.avgHours.toLocaleString(undefined, { minimumFractionDigits: 1 }) },
    { label: 'Avg Amount', value: `$${summaryStats.avgAmount.toLocaleString(undefined, { minimumFractionDigits: 2 })}` },
  ];

  return (
    <div className="container py-4">
      <div className={styles.layout}>
        <aside className={`card ${styles.filterCard}`} aria-label="Visualization filters">
          <div className="card-header">
            <p className="h4 mb-1">Ad-hoc Visualization</p>
            <p className={styles.filterDescription}>
              Interactive data visualization with dynamic filters and real-time chart updates.
            </p>
          </div>
          <div className="card-body">
            <form onSubmit={handleFiltersSubmit}>
              <div className={styles.filterSection}>
                <p className={styles.sectionHeading}>Data filters</p>
                <div className="form-group">
                  <label htmlFor="departmentFilter">Department</label>
                  <select
                    id="departmentFilter"
                    className="form-control"
                    value={departmentFilter}
                    onChange={(e) => setDepartmentFilter(e.target.value)}
                    disabled={!filtersLoaded}
                  >
                    <option value="">All departments</option>
                    {departments.map((value) => (
                      <option key={value} value={value}>
                        {value}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label htmlFor="statusFilter">Status</label>
                  <select
                    id="statusFilter"
                    className="form-control"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    disabled={!filtersLoaded}
                  >
                    <option value="">All statuses</option>
                    {statuses.map((value) => (
                      <option key={value} value={value}>
                        {value}
                      </option>
                    ))}
                  </select>
                </div>
                
                {/* Provider Demographics */}
                <div className="form-group">
                  <label htmlFor="providerGenderFilter">Provider Gender</label>
                  <select
                    id="providerGenderFilter"
                    className="form-control"
                    value={providerGenderFilter}
                    onChange={(e) => setProviderGenderFilter(e.target.value)}
                    disabled={!filtersLoaded}
                  >
                    <option value="">All Provider Genders</option>
                    {availableProviderGenders.map((gender) => (
                      <option key={gender} value={gender}>
                        {gender}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label htmlFor="providerEthnicityFilter">Provider Ethnicity</label>
                  <select
                    id="providerEthnicityFilter"
                    className="form-control"
                    value={providerEthnicityFilter}
                    onChange={(e) => setProviderEthnicityFilter(e.target.value)}
                    disabled={!filtersLoaded}
                  >
                    <option value="">All Provider Ethnicities</option>
                    {availableProviderEthnicities.map((ethnicity) => (
                      <option key={ethnicity} value={ethnicity}>
                        {ethnicity}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label htmlFor="providerAgeGroupFilter">Provider Age Group</label>
                  <select
                    id="providerAgeGroupFilter"
                    className="form-control"
                    value={providerAgeGroupFilter}
                    onChange={(e) => setProviderAgeGroupFilter(e.target.value)}
                    disabled={!filtersLoaded}
                  >
                    <option value="">All Provider Age Groups</option>
                    {availableProviderAgeGroups.map((ageGroup) => (
                      <option key={ageGroup} value={ageGroup}>
                        {ageGroup}
                      </option>
                    ))}
                  </select>
                </div>
                
                {/* Recipient Demographics */}
                <div className="form-group">
                  <label htmlFor="recipientGenderFilter">Recipient Gender</label>
                  <select
                    id="recipientGenderFilter"
                    className="form-control"
                    value={recipientGenderFilter}
                    onChange={(e) => setRecipientGenderFilter(e.target.value)}
                    disabled={!filtersLoaded}
                  >
                    <option value="">All Recipient Genders</option>
                    {availableRecipientGenders.map((gender) => (
                      <option key={gender} value={gender}>
                        {gender}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label htmlFor="recipientEthnicityFilter">Recipient Ethnicity</label>
                  <select
                    id="recipientEthnicityFilter"
                    className="form-control"
                    value={recipientEthnicityFilter}
                    onChange={(e) => setRecipientEthnicityFilter(e.target.value)}
                    disabled={!filtersLoaded}
                  >
                    <option value="">All Recipient Ethnicities</option>
                    {availableRecipientEthnicities.map((ethnicity) => (
                      <option key={ethnicity} value={ethnicity}>
                        {ethnicity}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label htmlFor="recipientAgeGroupFilter">Recipient Age Group</label>
                  <select
                    id="recipientAgeGroupFilter"
                    className="form-control"
                    value={recipientAgeGroupFilter}
                    onChange={(e) => setRecipientAgeGroupFilter(e.target.value)}
                    disabled={!filtersLoaded}
                  >
                    <option value="">All Recipient Age Groups</option>
                    {availableRecipientAgeGroups.map((ageGroup) => (
                      <option key={ageGroup} value={ageGroup}>
                        {ageGroup}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className={styles.filterSection}>
                <p className={styles.sectionHeading}>Result size</p>
                <div className="form-group">
                  <label htmlFor="resultLimit">Rows per fetch</label>
                  <input
                    id="resultLimit"
                    type="number"
                    min={50}
                    max={1000}
                    step={50}
                    className="form-control"
                    value={resultLimit}
                    onChange={(e) => {
                      const nextValue = Number(e.target.value);
                      if (Number.isNaN(nextValue)) return;
                      setResultLimit(Math.min(1000, Math.max(50, nextValue)));
                    }}
                  />
                  <small className={styles.hintText}>Limit between 50 and 1000 rows per fetch.</small>
                </div>
              </div>

              {!!activeFilters.length && (
                <div className={styles.filterSection}>
                  <p className={styles.sectionHeading}>Active filters</p>
                  <div className={styles.activeFilters}>
                    {activeFilters.map((filter) => (
                      <span className={`badge badge-pill badge-primary ${styles.filterChip}`} key={filter.label}>
                        {filter.label}
                        <button
                          type="button"
                          className={styles.filterChipButton}
                          onClick={filter.clear}
                          aria-label={`Remove ${filter.label}`}
                        >
                          <span className="ca-gov-icon-close-mark" aria-hidden="true"></span>
                        </button>
                      </span>
                    ))}
                  </div>
                </div>
              )}

              <div className={styles.formActions}>
                <button type="submit" className="btn btn-primary" disabled={isGenerating}>
                  {isGenerating ? 'Generating...' : 'Generate Report'}
                </button>
                <button type="button" className="btn btn-outline-primary" onClick={clearFilters}>
                  Clear Filters
                </button>
                {reportGenerated && dataset.rows.length > 0 && (
                  <button type="button" className="btn btn-secondary" onClick={handleDownloadPDF}>
                    <span className={`ca-gov-icon-pdf-text ${styles.buttonIcon}`} aria-hidden="true"></span>
                    Download PDF
                  </button>
                )}
              </div>
            </form>

            {status && (
              <Alert
                className="mt-3"
                type={(status.type === 'error' ? 'danger' : status.type) as AlertType}
                message={status.message}
                onClose={() => setStatus(null)}
              />
            )}
          </div>
        </aside>

        <main className={styles.content}>
          <section className={styles.heroCard}>
            <div className={styles.heroTitle}>📊 Ad-hoc Visualization Dashboard</div>
            <p className={styles.heroSubtitle}>
              Interactive data visualization with dynamic filters and real-time insights
            </p>
            <div className={styles.metricsRow}>
              {metricCards.map((metric) => (
                <div className={styles.metricCard} key={metric.label}>
                  <div className={styles.metricLabel}>{metric.label}</div>
                  <div className={styles.metricValue}>{metric.value}</div>
                </div>
              ))}
            </div>
          </section>

          {reportGenerated && dataset.rows.length > 0 && (
            <>
              {/* Interactive Visualization Section */}
              <section className={styles.reportCard}>
                <div className={styles.reportHeader}>
                  <div>
                    <div className={styles.reportTitle}>Data Visualization</div>
                    <div className={styles.reportMeta}>
                      {chartData.length > 0 
                        ? `${chartData.length} categories shown for ${selectedDimension}`
                        : 'Select a dimension and generate report to view visualizations'}
                    </div>
                  </div>
                </div>

                {/* Chart Type Selector */}
                <div className={styles.chartControls}>
                  <div className={`${styles.chartTypeGroup} btn-group`} role="group" aria-label="Chart type selector">
                    <button
                      type="button"
                      className={`btn btn-sm ${chartType === 'bar' ? 'btn-primary' : 'btn-outline-primary'}`}
                      onClick={() => setChartType('bar')}
                    >
                      Bar Chart
                    </button>
                    <button
                      type="button"
                      className={`btn btn-sm ${chartType === 'treemap' ? 'btn-primary' : 'btn-outline-primary'}`}
                      onClick={() => setChartType('treemap')}
                    >
                      Treemap
                    </button>
                    <button
                      type="button"
                      className={`btn btn-sm ${chartType === 'pie' ? 'btn-primary' : 'btn-outline-primary'}`}
                      onClick={() => setChartType('pie')}
                    >
                      Pie Chart
                    </button>
                  </div>

                  {availableDimensions.length > 0 && (
                    <div className={styles.chartSelectGroup}>
                      <label className="form-label mb-0" htmlFor="chartDimension">
                        Select dimension
                      </label>
                      <select
                        id="chartDimension"
                        className={`form-control ${styles.chartSelect}`}
                        value={selectedDimension}
                        onChange={(e) => setSelectedDimension(e.target.value)}
                      >
                        {availableDimensions.map((dim) => (
                          <option key={dim.value} value={dim.value}>
                            {dim.label}
                          </option>
                        ))}
                      </select>
                    </div>
                  )}

                  {Object.keys(geographicAnalysis.county).length > 0 && (
                    <div className={styles.chartSelectGroup}>
                      <label className="form-label mb-0" htmlFor="chartCounty">
                        County
                      </label>
                      <select
                        id="chartCounty"
                        className={`form-control ${styles.chartSelect}`}
                        value={countyFilter}
                        onChange={(e) => setCountyFilter(e.target.value)}
                      >
                        <option value="(All)">(All)</option>
                        {Object.keys(geographicAnalysis.county)
                          .sort()
                          .map((county) => (
                            <option key={county} value={county}>
                              {county}
                            </option>
                          ))}
                      </select>
                    </div>
                  )}
                </div>

                {/* Chart Visualization */}
                {chartData.length > 0 ? (
                  <div style={{ width: '100%', height: '500px', marginTop: '1rem' }}>
                    {chartType === 'bar' && (
                      <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 60 }}>
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis 
                            dataKey="name" 
                            angle={-45} 
                            textAnchor="end" 
                            height={100}
                            interval={0}
                            style={{ fontSize: '12px' }}
                          />
                          <YAxis />
                          <Tooltip />
                          <Legend />
                          <Bar dataKey="value" fill="#3b82f6" name="Count" />
                        </BarChart>
                      </ResponsiveContainer>
                    )}

                    {chartType === 'treemap' && (
                      <div style={{ 
                        width: '100%', 
                        height: '100%', 
                        display: 'flex',
                        flexWrap: 'wrap',
                        gap: '4px',
                        padding: '1rem',
                        background: '#f9fafb',
                        borderRadius: '0.5rem',
                      }}>
                        {chartData.map((entry, index) => {
                          const total = chartData.reduce((sum, item) => sum + item.value, 0);
                          const percentage = (entry.value / total) * 100;
                          const size = Math.max(15, percentage * 2); // Minimum size for visibility
                          return (
                            <div
                              key={entry.name}
                              style={{
                                flex: `1 1 ${size}%`,
                                minWidth: '120px',
                                background: CHART_COLORS[index % CHART_COLORS.length],
                                color: 'white',
                                padding: '1rem',
                                borderRadius: '0.5rem',
                                display: 'flex',
                                flexDirection: 'column',
                                justifyContent: 'center',
                                alignItems: 'center',
                                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                                border: '2px solid white',
                              }}
                            >
                              <div style={{ fontWeight: 'bold', fontSize: '1.1rem', marginBottom: '0.5rem' }}>
                                {entry.name}
                              </div>
                              <div style={{ fontSize: '1.5rem', fontWeight: '700' }}>
                                {entry.value.toLocaleString()}
                              </div>
                              <div style={{ fontSize: '0.85rem', opacity: 0.9, marginTop: '0.25rem' }}>
                                {percentage.toFixed(1)}%
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    )}

                    {chartType === 'pie' && (
                      <ResponsiveContainer width="100%" height="100%">
                        <PieChart>
                          <Pie
                            data={chartData}
                            cx="50%"
                            cy="50%"
                            labelLine={false}
                            label={({ name, percent }) => percent ? `${name}: ${(percent * 100).toFixed(0)}%` : name}
                            outerRadius={150}
                            fill="#8884d8"
                            dataKey="value"
                          >
                            {chartData.map((entry, index) => (
                              <Cell key={`cell-${index}`} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                            ))}
                          </Pie>
                          <Tooltip />
                          <Legend />
                        </PieChart>
                      </ResponsiveContainer>
                    )}
                  </div>
                ) : (
                  <div className={styles.chartEmptyState}>
                    <p className={styles.chartEmptyTitle}>No data available for visualization</p>
                    <p className={styles.chartEmptySubtitle}>Generate a report first to see interactive charts</p>
                  </div>
                )}
              </section>
            </>
          )}

          {reportGenerated && dataset.rows.length > 0 && (
            <>
              {/* Executive Summary Section */}
              <section className={styles.reportCard}>
                <div className={styles.reportHeader}>
                  <div>
                    <div className={styles.reportTitle}>Executive Summary</div>
                    <div className={styles.reportMeta}>Key performance indicators and insights</div>
                  </div>
                </div>

                <div className={styles.metricsRow} style={{ marginBottom: '1.5rem' }}>
                  <div className={styles.metricCard} style={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)', color: 'white' }}>
                    <div className={styles.metricLabel} style={{ color: 'rgba(255,255,255,0.9)' }}>Total Records</div>
                    <div className={styles.metricValue} style={{ color: 'white' }}>{executiveMetrics.totalRecords.toLocaleString()}</div>
                  </div>
                  <div className={styles.metricCard} style={{ background: 'linear-gradient(135deg, #10b981, #059669)', color: 'white' }}>
                    <div className={styles.metricLabel} style={{ color: 'rgba(255,255,255,0.9)' }}>Total Hours</div>
                    <div className={styles.metricValue} style={{ color: 'white' }}>{executiveMetrics.totalHours.toFixed(2)}</div>
                  </div>
                  <div className={styles.metricCard} style={{ background: 'linear-gradient(135deg, #f59e0b, #d97706)', color: 'white' }}>
                    <div className={styles.metricLabel} style={{ color: 'rgba(255,255,255,0.9)' }}>Total Amount</div>
                    <div className={styles.metricValue} style={{ color: 'white' }}>${executiveMetrics.totalAmount.toLocaleString()}</div>
                  </div>
                  <div className={styles.metricCard} style={{ background: 'linear-gradient(135deg, #ef4444, #dc2626)', color: 'white' }}>
                    <div className={styles.metricLabel} style={{ color: 'rgba(255,255,255,0.9)' }}>Approval Rate</div>
                    <div className={styles.metricValue} style={{ color: 'white' }}>{executiveMetrics.approvalRate.toFixed(1)}%</div>
                  </div>
                </div>

                {/* Status Distribution */}
                {Object.keys(executiveMetrics.statusDistribution).length > 0 && (
                  <div style={{ marginBottom: '1.5rem' }}>
                    <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem', color: '#111827' }}>Status Distribution</h3>
                    <div className={styles.tableWrapper}>
                      <table className={styles.summaryTable}>
                        <thead>
                          <tr>
                            <th>Status</th>
                            <th>Count</th>
                            <th>Percentage</th>
                          </tr>
                        </thead>
                        <tbody>
                          {Object.entries(executiveMetrics.statusDistribution)
                            .sort(([, a], [, b]) => b - a)
                            .map(([status, count]) => {
                              const percentage = (count / executiveMetrics.totalRecords) * 100;
                              return (
                                <tr key={status}>
                                  <td>{status}</td>
                                  <td>{count.toLocaleString()}</td>
                                  <td>{percentage.toFixed(1)}%</td>
                                </tr>
                              );
                            })}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}

                {/* Top Providers */}
                {executiveMetrics.topProviders.length > 0 && (
                  <div>
                    <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem', color: '#111827' }}>Top 5 Providers by Hours</h3>
                    <div className={styles.tableWrapper}>
                      <table className={styles.summaryTable}>
                        <thead>
                          <tr>
                            <th>Rank</th>
                            <th>Provider Name</th>
                            <th>Total Hours</th>
                            <th>Total Amount</th>
                          </tr>
                        </thead>
                        <tbody>
                          {executiveMetrics.topProviders.map((provider, index) => (
                            <tr key={provider.name}>
                              <td>{index + 1}</td>
                              <td>{provider.name}</td>
                              <td>{provider.hours.toFixed(2)}</td>
                              <td>${provider.amount.toLocaleString()}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
              </section>

              {/* Demographic Breakdowns */}
              {(Object.keys(demographicBreakdowns.gender).length > 0 || Object.keys(demographicBreakdowns.ethnicity).length > 0) && (
                <section className={styles.reportCard}>
                  <div className={styles.reportHeader}>
                    <div>
                      <div className={styles.reportTitle}>Demographic Breakdowns</div>
                      <div className={styles.reportMeta}>Distribution by gender and ethnicity</div>
                    </div>
                  </div>

                  {Object.keys(demographicBreakdowns.gender).length > 0 && (
                    <div style={{ marginBottom: '1.5rem' }}>
                      <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem', color: '#111827' }}>Gender Distribution</h3>
                      <div className={styles.tableWrapper}>
                        <table className={styles.summaryTable}>
                          <thead>
                            <tr>
                              <th>Gender</th>
                              <th>Count</th>
                              <th>Percentage</th>
                            </tr>
                          </thead>
                          <tbody>
                            {Object.entries(demographicBreakdowns.gender)
                              .sort(([, a], [, b]) => b - a)
                              .map(([gender, count]) => {
                                const total = Object.values(demographicBreakdowns.gender).reduce((a, b) => a + b, 0);
                                const percentage = total > 0 ? (count / total) * 100 : 0;
                                return (
                                  <tr key={gender}>
                                    <td>{gender}</td>
                                    <td>{count.toLocaleString()}</td>
                                    <td>{percentage.toFixed(1)}%</td>
                                  </tr>
                                );
                              })}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  )}

                  {Object.keys(demographicBreakdowns.ethnicity).length > 0 && (
                    <div>
                      <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem', color: '#111827' }}>Ethnicity Distribution</h3>
                      <div className={styles.tableWrapper}>
                        <table className={styles.summaryTable}>
                          <thead>
                            <tr>
                              <th>Ethnicity</th>
                              <th>Count</th>
                              <th>Percentage</th>
                            </tr>
                          </thead>
                          <tbody>
                            {Object.entries(demographicBreakdowns.ethnicity)
                              .sort(([, a], [, b]) => b - a)
                              .map(([ethnicity, count]) => {
                                const total = Object.values(demographicBreakdowns.ethnicity).reduce((a, b) => a + b, 0);
                                const percentage = total > 0 ? (count / total) * 100 : 0;
                                return (
                                  <tr key={ethnicity}>
                                    <td>{ethnicity}</td>
                                    <td>{count.toLocaleString()}</td>
                                    <td>{percentage.toFixed(1)}%</td>
                                  </tr>
                                );
                              })}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  )}
                </section>
              )}

              {/* Geographic Analysis */}
              {(Object.keys(geographicAnalysis.county).length > 0 || Object.keys(geographicAnalysis.district).length > 0) && (
                <section className={styles.reportCard}>
                  <div className={styles.reportHeader}>
                    <div>
                      <div className={styles.reportTitle}>Geographic Analysis</div>
                      <div className={styles.reportMeta}>Distribution by county and district</div>
                    </div>
                  </div>

                  {Object.keys(geographicAnalysis.county).length > 0 && (
                    <div style={{ marginBottom: '1.5rem' }}>
                      <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem', color: '#111827' }}>County Distribution</h3>
                      <div className={styles.tableWrapper}>
                        <table className={styles.summaryTable}>
                          <thead>
                            <tr>
                              <th>County</th>
                              <th>Count</th>
                              <th>Percentage</th>
                            </tr>
                          </thead>
                          <tbody>
                            {Object.entries(geographicAnalysis.county)
                              .sort(([, a], [, b]) => b - a)
                              .map(([county, count]) => {
                                const total = Object.values(geographicAnalysis.county).reduce((a, b) => a + b, 0);
                                const percentage = total > 0 ? (count / total) * 100 : 0;
                                return (
                                  <tr key={county}>
                                    <td>{county}</td>
                                    <td>{count.toLocaleString()}</td>
                                    <td>{percentage.toFixed(1)}%</td>
                                  </tr>
                                );
                              })}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  )}

                  {Object.keys(geographicAnalysis.district).length > 0 && (
                    <div>
                      <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem', color: '#111827' }}>District Distribution</h3>
                      <div className={styles.tableWrapper}>
                        <table className={styles.summaryTable}>
                          <thead>
                            <tr>
                              <th>District</th>
                              <th>Count</th>
                              <th>Percentage</th>
                            </tr>
                          </thead>
                          <tbody>
                            {Object.entries(geographicAnalysis.district)
                              .sort(([, a], [, b]) => b - a)
                              .map(([district, count]) => {
                                const total = Object.values(geographicAnalysis.district).reduce((a, b) => a + b, 0);
                                const percentage = total > 0 ? (count / total) * 100 : 0;
                                return (
                                  <tr key={district}>
                                    <td>{district}</td>
                                    <td>{count.toLocaleString()}</td>
                                    <td>{percentage.toFixed(1)}%</td>
                                  </tr>
                                );
                              })}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  )}
                </section>
              )}

              {/* Service Type Analysis */}
              {Object.keys(serviceTypeAnalysis).length > 0 && (
                <section className={styles.reportCard}>
                  <div className={styles.reportHeader}>
                    <div>
                      <div className={styles.reportTitle}>Service Type Analysis</div>
                      <div className={styles.reportMeta}>Breakdown by service type with hours and amounts</div>
                    </div>
                  </div>

                  <div className={styles.tableWrapper}>
                    <table className={styles.summaryTable}>
                      <thead>
                        <tr>
                          <th>Service Type</th>
                          <th>Count</th>
                          <th>Total Hours</th>
                          <th>Total Amount</th>
                        </tr>
                      </thead>
                      <tbody>
                        {Object.entries(serviceTypeAnalysis)
                          .sort(([, a], [, b]) => b.count - a.count)
                          .map(([serviceType, data]) => (
                            <tr key={serviceType}>
                              <td>{serviceType}</td>
                              <td>{data.count.toLocaleString()}</td>
                              <td>{data.hours.toFixed(2)}</td>
                              <td>${data.amount.toLocaleString()}</td>
                            </tr>
                          ))}
                      </tbody>
                    </table>
                  </div>
                </section>
              )}
            </>
          )}

          <section className={styles.reportCard}>
            <div className={styles.reportHeader}>
              <div>
                <div className={styles.reportTitle}>Summary Snapshot</div>
                <div className={styles.reportMeta}>
                  {reportGenerated ? 'Report up to date with your filters' : 'Select filters and run "Generate Report" to update results'}
                </div>
              </div>
              <div className={styles.reportMeta}>{new Date().toLocaleString()}</div>
            </div>

            <div className={styles.tableWrapper}>
              <table className={styles.summaryTable}>
                <thead>
                  <tr>
                    <th>Metric</th>
                    <th>Value</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>Total Records</td>
                    <td>{summaryStats.totalRecords.toLocaleString()}</td>
                  </tr>
                  <tr>
                    <td>Total Hours</td>
                    <td>{summaryStats.totalHours.toLocaleString()}</td>
                  </tr>
                  <tr>
                    <td>Total Amount</td>
                    <td>${summaryStats.totalAmount.toLocaleString()}</td>
                  </tr>
                  <tr>
                    <td>Average Hours</td>
                    <td>{summaryStats.avgHours.toLocaleString(undefined, { minimumFractionDigits: 1 })}</td>
                  </tr>
                  <tr>
                    <td>Average Amount</td>
                    <td>${summaryStats.avgAmount.toLocaleString(undefined, { minimumFractionDigits: 2 })}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section className={styles.reportCard}>
            <div className={styles.reportHeader}>
              <div>
                <div className={styles.reportTitle}>Visualization Dataset</div>
                <div className={styles.reportMeta}>
                  {reportGenerated
                    ? `Showing ${dataset.rows.length} of ${resultLimit.toLocaleString()} requested rows`
                    : 'Run the report to populate the dataset'}
                </div>
              </div>
              <div className={styles.reportMeta}>
                {lastGeneratedAt ? `Updated ${new Date(lastGeneratedAt).toLocaleString()}` : `Limit ${resultLimit}`}
              </div>
            </div>

            {!dataset.columns.length ? (
              <div className={styles.tableEmptyState}>
                {isGenerating ? 'Generating dataset...' : 'Use the filters on the left and click Generate Report to see row-level results.'}
              </div>
            ) : (
              <>
                <div className={styles.columnControls}>
                  <div>
                    <p className={styles.columnTitle}>Columns</p>
                    <p className={styles.columnHint}>Toggle which fields should appear in the visualization table.</p>
                  </div>
                  <div className={styles.columnChips}>
                    {dataset.columns.map((column) => {
                      const checked = displayedColumns.includes(column);
                      return (
                        <label key={column} className={styles.columnChip}>
                          <input
                            type="checkbox"
                            checked={checked}
                            onChange={() => handleColumnToggle(column)}
                          />
                          {formatColumnLabel(column)}
                        </label>
                      );
                    })}
                  </div>
                </div>

                <div className={styles.tableWrapper}>
                  <table className={styles.dataTable}>
                    <thead>
                      <tr>
                        {displayedColumns.map((column) => (
                          <th key={column}>{formatColumnLabel(column)}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {dataset.rows.length ? (
                        dataset.rows.map((row, index) => (
                          <tr key={`${row.timesheet_id ?? index}-${index}`}>
                            {displayedColumns.map((column) => (
                              <td key={`${column}-${index}`}>{formatCellValue(row[column], column)}</td>
                            ))}
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan={displayedColumns.length} className={styles.tableEmptyState}>
                            No rows matched your filters.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </>
            )}
          </section>
        </main>
      </div>
    </div>
  );
};

export default VisualizationPage;

