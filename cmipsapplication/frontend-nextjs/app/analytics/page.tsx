'use client';

import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './analytics.module.css';
import { useAuth } from '@/contexts/AuthContext';
import { analyticsService } from '@/lib/services/analytics.service';

interface StatusBanner {
  type: 'success' | 'error' | 'info';
  message: string;
}

const AnalyticsPage = () => {
  const router = useRouter();
  const { isAuthenticated, loading, user } = useAuth();

  const [status, setStatus] = useState<StatusBanner | null>(null);
  const [filtersLoaded, setFiltersLoaded] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // Top Metrics
  const [metrics, setMetrics] = useState({
    individuals: 0,
    population: 0,
    perCapitaRate: '0.00',
    totalAuthorizedHours: 0,
    totalAuthorizedHours2: 0,
  });

  // Tab Selection
  const [activeTab, setActiveTab] = useState<'details' | 'pivot' | 'county'>('details');

  // Left Sidebar - Dimension Selection
  const [dimensions, setDimensions] = useState({
    dimension1: '',
    dimension2: '',
    dimension3: '',
    dimension4: '',
    dimension5: '',
    dimension6: '',
    dimension7: '',
    dimension8: '',
  });

  // Left Sidebar - Measure Selection - aligned with actual database fields
  const [measures, setMeasures] = useState<Record<string, boolean>>({
    '(All)': true,
    'ID Count': true,
    'Total Hours': true,
    'Regular Hours': false,
    'Overtime Hours': false,
    'Sick Hours': false,
    'Vacation Hours': false,
    'Holiday Hours': false,
  });

  // Right Sidebar - Filters (aligned with actual database schema)
  const [departmentFilter, setDepartmentFilter] = useState('(All)');
  const [statusFilter, setStatusFilter] = useState('(All)');
  const [availableDepartments, setAvailableDepartments] = useState<string[]>([]);
  const [availableStatuses, setAvailableStatuses] = useState<string[]>([]);

  const [countyFilter, setCountyFilter] = useState('(All)');
  const [availableCounties, setAvailableCounties] = useState<string[]>([]);

  // Demographic filters
  const [providerGenderFilter, setProviderGenderFilter] = useState('(All)');
  const [recipientGenderFilter, setRecipientGenderFilter] = useState('(All)');
  const [providerEthnicityFilter, setProviderEthnicityFilter] = useState('(All)');
  const [recipientEthnicityFilter, setRecipientEthnicityFilter] = useState('(All)');
  const [providerAgeGroupFilter, setProviderAgeGroupFilter] = useState('(All)');
  const [recipientAgeGroupFilter, setRecipientAgeGroupFilter] = useState('(All)');
  
  const [availableProviderGenders, setAvailableProviderGenders] = useState<string[]>([]);
  const [availableRecipientGenders, setAvailableRecipientGenders] = useState<string[]>([]);
  const [availableProviderEthnicities, setAvailableProviderEthnicities] = useState<string[]>([]);
  const [availableRecipientEthnicities, setAvailableRecipientEthnicities] = useState<string[]>([]);
  const [availableProviderAgeGroups, setAvailableProviderAgeGroups] = useState<string[]>([]);
  const [availableRecipientAgeGroups, setAvailableRecipientAgeGroups] = useState<string[]>([]);

  // Data Table
  const [tableData, setTableData] = useState<Array<Record<string, any>>>([]);
  const [tableColumns, setTableColumns] = useState<string[]>([]);
  const [appliedDimensions, setAppliedDimensions] = useState<Record<string, string>>({});
  const [transformedData, setTransformedData] = useState<Array<Record<string, any>>>([]);
  const [transformedColumns, setTransformedColumns] = useState<string[]>([]);

  // Available dimension options - aligned with actual database schema including demographics
  const dimensionOptions = [
    { value: '', label: 'None' },
    { value: 'location', label: 'Location (County)' },
    { value: 'department', label: 'Department' },
    { value: 'status', label: 'Status' },
    { value: 'employeeId', label: 'Employee ID' },
    { value: 'employeeName', label: 'Employee Name' },
    { value: 'userId', label: 'User ID' },
    { value: 'payPeriodStart', label: 'Pay Period Start' },
    { value: 'payPeriodEnd', label: 'Pay Period End' },
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

  // Helper function to get field value with fallback variations
  const getFieldValue = (row: Record<string, any>, fieldName: string): any => {
    // Direct match
    if (row[fieldName] !== undefined && row[fieldName] !== null) {
      return row[fieldName];
    }
    
    // Try lowercase
    if (row[fieldName.toLowerCase()] !== undefined && row[fieldName.toLowerCase()] !== null) {
      return row[fieldName.toLowerCase()];
    }
    
    // Try camelCase variations
    const camelCase = fieldName.charAt(0).toLowerCase() + fieldName.slice(1);
    if (row[camelCase] !== undefined && row[camelCase] !== null) {
      return row[camelCase];
    }
    
    // Try with common suffixes
    const variations = [
      `${fieldName}Name`,
      `${fieldName}Group`,
      `${camelCase}Name`,
      `${camelCase}Group`,
    ];
    
    for (const variation of variations) {
      if (row[variation] !== undefined && row[variation] !== null) {
        return row[variation];
      }
    }
    
    return null;
  };

  const showStatus = useCallback((message: string, type: 'success' | 'error' | 'info') => {
    setStatus({ message, type });
    setTimeout(() => setStatus(null), 5000);
  }, []);

  // Load initial filters and set user's county from JWT
  useEffect(() => {
    const loadFilters = async () => {
      try {
        const response = await analyticsService.getAdhocFilters();
        
        // Get user's county from JWT token (already extracted in AuthContext)
        const userCounty = user?.countyId;
        const userRole = user?.role?.toUpperCase() || '';
        
        // Check if user is county-restricted (SUPERVISOR, CASE_WORKER, PROVIDER, RECIPIENT)
        const isCountyRestricted = userRole.includes('SUPERVISOR') || 
                                   userRole.includes('CASE_WORKER') || 
                                   userRole.includes('PROVIDER') || 
                                   userRole.includes('RECIPIENT');
        
        // Set available counties from response
        if (response.locations && Array.isArray(response.locations)) {
          if (isCountyRestricted && userCounty) {
            // For county-restricted roles, only show user's county
            setAvailableCounties([userCounty]);
            setCountyFilter(userCounty); // Auto-select user's county
            console.log('🔐 Analytics: County-restricted user, setting county to:', userCounty);
          } else {
            // For ADMIN/SYSTEM_SCHEDULER, show all counties
            setAvailableCounties(['(All)', ...response.locations]);
            // If user has a county, pre-select it
            if (userCounty && response.locations.includes(userCounty)) {
              setCountyFilter(userCounty);
              console.log('📍 Analytics: Pre-selecting user county:', userCounty);
            }
          }
        } else {
          if (isCountyRestricted && userCounty) {
            setAvailableCounties([userCounty]);
            setCountyFilter(userCounty);
          } else {
            setAvailableCounties(['(All)']);
          }
        }
        
        // Set available departments
        if (response.departments && Array.isArray(response.departments)) {
          setAvailableDepartments(['(All)', ...response.departments]);
        } else {
          setAvailableDepartments(['(All)']);
        }
        // Set available statuses
        if (response.statuses && Array.isArray(response.statuses)) {
          setAvailableStatuses(['(All)', ...response.statuses]);
        } else {
          setAvailableStatuses(['(All)']);
        }
        // Set demographic filters
        if (response.providerGenders && Array.isArray(response.providerGenders)) {
          setAvailableProviderGenders(['(All)', ...response.providerGenders]);
        }
        if (response.recipientGenders && Array.isArray(response.recipientGenders)) {
          setAvailableRecipientGenders(['(All)', ...response.recipientGenders]);
        }
        if (response.providerEthnicities && Array.isArray(response.providerEthnicities)) {
          setAvailableProviderEthnicities(['(All)', ...response.providerEthnicities]);
        }
        if (response.recipientEthnicities && Array.isArray(response.recipientEthnicities)) {
          setAvailableRecipientEthnicities(['(All)', ...response.recipientEthnicities]);
        }
        if (response.providerAgeGroups && Array.isArray(response.providerAgeGroups)) {
          setAvailableProviderAgeGroups(['(All)', ...response.providerAgeGroups]);
        }
        if (response.recipientAgeGroups && Array.isArray(response.recipientAgeGroups)) {
          setAvailableRecipientAgeGroups(['(All)', ...response.recipientAgeGroups]);
        }
        setFiltersLoaded(true);
      } catch (err: any) {
        console.error('Error loading filters:', err);
        setStatus({ message: err?.message || 'Unable to load filters', type: 'error' });
        setFiltersLoaded(true); // Still mark as loaded to prevent infinite retries
      }
    };

    if (isAuthenticated && !filtersLoaded) {
      loadFilters();
    }
  }, [isAuthenticated, user]);

  // Load metrics
  useEffect(() => {
    if (!filtersLoaded || !isAuthenticated) return;

    const loadMetrics = async () => {
      try {
        setIsLoading(true);
        const params: Record<string, string> = {};
        
        // Build params from filters - using actual database fields
        if (countyFilter !== '(All)') params.county = countyFilter;
        if (departmentFilter !== '(All)') params.department = departmentFilter;
        if (statusFilter !== '(All)') params.status = statusFilter;
        // Demographic filters
        if (providerGenderFilter !== '(All)') params.providerGender = providerGenderFilter;
        if (recipientGenderFilter !== '(All)') params.recipientGender = recipientGenderFilter;
        if (providerEthnicityFilter !== '(All)') params.providerEthnicity = providerEthnicityFilter;
        if (recipientEthnicityFilter !== '(All)') params.recipientEthnicity = recipientEthnicityFilter;
        if (providerAgeGroupFilter !== '(All)') params.providerAgeGroup = providerAgeGroupFilter;
        if (recipientAgeGroupFilter !== '(All)') params.recipientAgeGroup = recipientAgeGroupFilter;

        const statsResponse = await analyticsService.getAdhocStats(params);
        
        if (statsResponse.status === 'SUCCESS' && statsResponse.stats) {
          setMetrics({
            individuals: statsResponse.stats.totalRecords || 0,
            population: 38814347, // Mock population data
            perCapitaRate: statsResponse.stats.totalRecords ? 
              ((statsResponse.stats.totalRecords / 38814347) * 100).toFixed(2) : '0.00',
            totalAuthorizedHours: statsResponse.stats.totalHours || 0,
            totalAuthorizedHours2: statsResponse.stats.avgHours || 0,
          });
        }

        // Load table data
        const dataResponse = await analyticsService.getAdhocData({ ...params, limit: 1000 });
        if (dataResponse.status === 'SUCCESS') {
          setTableData(dataResponse.rows || []);
          setTableColumns(dataResponse.columns || []);
        }
      } catch (err: any) {
        console.error('Error loading analytics data:', err);
        setStatus({ message: err?.message || 'Unable to load analytics data', type: 'error' });
      } finally {
        setIsLoading(false);
      }
    };

    loadMetrics();
  }, [filtersLoaded, isAuthenticated, countyFilter, departmentFilter, statusFilter, 
      providerGenderFilter, recipientGenderFilter, providerEthnicityFilter, 
      recipientEthnicityFilter, providerAgeGroupFilter, recipientAgeGroupFilter]);

  const handleMeasureToggle = (measure: string) => {
    if (measure === '(All)') {
      const allSelected = measures['(All)'];
      const newMeasures: Record<string, boolean> = {};
      Object.keys(measures).forEach((key) => {
        newMeasures[key] = !allSelected;
      });
      setMeasures(newMeasures);
    } else {
      setMeasures((prev) => ({
        ...prev,
        [measure]: !prev[measure],
        '(All)': false,
      }));
    }
  };

  // Removed hardcoded filter handlers - using actual database filters only

  // Apply dimensions to transform the data
  const handleApplyDimensions = useCallback(() => {
    if (tableData.length === 0) {
      setStatus({ message: 'No data available to apply dimensions', type: 'info' });
      return;
    }

    // Get active dimensions (non-empty)
    const activeDimensions = Object.entries(dimensions)
      .filter(([_, value]) => value && value !== '')
      .map(([key, value]) => ({ key, value }));

    if (activeDimensions.length === 0) {
      // No dimensions selected, show original data
      setTransformedData(tableData);
      setTransformedColumns(tableColumns);
      setAppliedDimensions({});
      setStatus({ message: 'No dimensions selected. Showing original data.', type: 'info' });
      return;
    }

    try {
      setIsLoading(true);

      // Group data by selected dimensions
      const groupedData = new Map<string, {
        key: string;
        values: Record<string, any>;
        count: number;
        rows: Array<Record<string, any>>;
      }>();

      tableData.forEach((row) => {
        // Create a key from the selected dimensions
        const dimensionKeys = activeDimensions.map(({ value }) => {
          const fieldValue = getFieldValue(row, value);
          return `${value}:${fieldValue || 'Unknown'}`;
        });

        const groupKey = dimensionKeys.join('|');

        if (!groupedData.has(groupKey)) {
          groupedData.set(groupKey, {
            key: groupKey,
            values: {},
            count: 0,
            rows: [],
          });
        }

        const group = groupedData.get(groupKey)!;
        group.count++;
        group.rows.push(row);

        // Store dimension values
        activeDimensions.forEach(({ value }) => {
          const fieldValue = getFieldValue(row, value);
          group.values[value] = fieldValue || 'Unknown';
        });

        // Aggregate measure values - aligned with actual database fields
        if (measures['ID Count']) {
          group.values['ID Count'] = (group.values['ID Count'] || 0) + 1;
        }
        if (measures['Total Hours']) {
          const hours = parseFloat(row.totalHours || row.total_hours || 0);
          group.values['Total Hours'] = (group.values['Total Hours'] || 0) + hours;
        }
        if (measures['Regular Hours']) {
          const hours = parseFloat(row.regularHours || row.regular_hours || 0);
          group.values['Regular Hours'] = (group.values['Regular Hours'] || 0) + hours;
        }
        if (measures['Overtime Hours']) {
          const hours = parseFloat(row.overtimeHours || row.overtime_hours || 0);
          group.values['Overtime Hours'] = (group.values['Overtime Hours'] || 0) + hours;
        }
        if (measures['Sick Hours']) {
          const hours = parseFloat(row.sickHours || row.sick_hours || 0);
          group.values['Sick Hours'] = (group.values['Sick Hours'] || 0) + hours;
        }
        if (measures['Vacation Hours']) {
          const hours = parseFloat(row.vacationHours || row.vacation_hours || 0);
          group.values['Vacation Hours'] = (group.values['Vacation Hours'] || 0) + hours;
        }
        if (measures['Holiday Hours']) {
          const hours = parseFloat(row.holidayHours || row.holiday_hours || 0);
          group.values['Holiday Hours'] = (group.values['Holiday Hours'] || 0) + hours;
        }
      });

      // Convert grouped data to array
      const transformed = Array.from(groupedData.values()).map((group) => {
        const result: Record<string, any> = { ...group.values };
        
        // Add aggregated measures - aligned with actual database fields
        if (measures['ID Count']) {
          result['ID Count'] = group.count;
        }
        if (measures['Total Hours']) {
          result['Total Hours'] = group.values['Total Hours'] || 0;
        }
        if (measures['Regular Hours']) {
          result['Regular Hours'] = group.values['Regular Hours'] || 0;
        }
        if (measures['Overtime Hours']) {
          result['Overtime Hours'] = group.values['Overtime Hours'] || 0;
        }
        if (measures['Sick Hours']) {
          result['Sick Hours'] = group.values['Sick Hours'] || 0;
        }
        if (measures['Vacation Hours']) {
          result['Vacation Hours'] = group.values['Vacation Hours'] || 0;
        }
        if (measures['Holiday Hours']) {
          result['Holiday Hours'] = group.values['Holiday Hours'] || 0;
        }

        return result;
      });

      // Create new columns based on dimensions and measures
      const newColumns: string[] = [];
      
      // Add dimension columns
      activeDimensions.forEach(({ value }) => {
        newColumns.push(value.charAt(0).toUpperCase() + value.slice(1));
      });

      // Add measure columns
      Object.entries(measures).forEach(([measure, selected]) => {
        if (selected && measure !== '(All)') {
          if (!newColumns.includes(measure)) {
            newColumns.push(measure);
          }
        }
      });

      setTransformedData(transformed);
      setTransformedColumns(newColumns);
      setAppliedDimensions(dimensions);
      setStatus({ 
        message: `Applied ${activeDimensions.length} dimension(s). Showing ${transformed.length} grouped records.`, 
        type: 'success' 
      });
    } catch (err: any) {
      console.error('Error applying dimensions:', err);
      setStatus({ message: err?.message || 'Error applying dimensions', type: 'error' });
    } finally {
      setIsLoading(false);
    }
  }, [dimensions, tableData, tableColumns, measures]);

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.push('/login');
    }
  }, [loading, isAuthenticated, router]);

  // Check if user is county-restricted (SUPERVISOR, CASE_WORKER, PROVIDER, RECIPIENT)
  const isCountyRestricted = useMemo(() => {
    const userRole = user?.role?.toUpperCase() || '';
    return userRole.includes('SUPERVISOR') || 
           userRole.includes('CASE_WORKER') || 
           userRole.includes('PROVIDER') || 
           userRole.includes('RECIPIENT');
  }, [user?.role]);

  if (loading) {
    return (
      <div className="container" style={{ minHeight: '60vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <p>Loading analytics...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null; // Will redirect via useEffect
  }

  // Get visible columns and data based on whether dimensions are applied
  const displayData = useMemo(() => {
    return Object.keys(appliedDimensions).length > 0 && transformedData.length > 0
      ? transformedData
      : tableData;
  }, [appliedDimensions, transformedData, tableData]);

  const displayColumns = useMemo(() => {
    if (Object.keys(appliedDimensions).length > 0 && transformedColumns.length > 0) {
      return transformedColumns;
    }
    
    if (measures['(All)']) {
      return tableColumns;
    }
    return tableColumns.filter((col) => {
      const colLower = col.toLowerCase();
      return Object.entries(measures)
        .filter(([_, selected]) => selected)
        .some(([measure, _]) => {
          const measureLower = measure.toLowerCase();
          return colLower.includes(measureLower) || measureLower.includes(colLower);
        });
    });
  }, [appliedDimensions, transformedColumns, measures, tableColumns]);

  // Check if dimensions have changed since last apply
  const dimensionsChanged = useMemo(() => {
    return JSON.stringify(dimensions) !== JSON.stringify(appliedDimensions);
  }, [dimensions, appliedDimensions]);

  return (
    <div style={{ 
      width: '100%', 
      maxWidth: '100%', 
      padding: '1.5rem', 
      boxSizing: 'border-box',
      margin: '0 auto',
      overflow: 'hidden',
      background: '#f5f7fa',
      minHeight: '100vh',
    }}>
      {/* Top Banner */}
      <div className={styles.topBanner}>
        <h1 className={styles.bannerTitle}>CDSS Adhoc Reporting Table</h1>
      </div>

      {/* Key Metrics */}
      <div className={styles.metricsBar}>
        <div className={styles.metricCard}>
          <div className={styles.metricValue}>{metrics.individuals.toLocaleString()}</div>
          <div className={styles.metricLabel}>Individuals</div>
        </div>
        <div className={styles.metricCard}>
          <div className={styles.metricValue}>{metrics.population.toLocaleString()}</div>
          <div className={styles.metricLabel}>Population</div>
        </div>
        <div className={styles.metricCard}>
          <div className={styles.metricValue}>{metrics.perCapitaRate}</div>
          <div className={styles.metricLabel}>Per Capita Rate</div>
        </div>
        <div className={styles.metricCard}>
          <div className={styles.metricValue}>{metrics.totalAuthorizedHours.toLocaleString()}</div>
          <div className={styles.metricLabel}>Total Authorized Hours</div>
        </div>
        <div className={styles.metricCard}>
          <div className={styles.metricValue}>{metrics.totalAuthorizedHours2.toFixed(2)}</div>
          <div className={styles.metricLabel}>Total Authorized Hours</div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className={styles.tabsContainer}>
        <button
          className={`${styles.tab} ${activeTab === 'details' ? styles.tabActive : ''}`}
          onClick={() => setActiveTab('details')}
        >
          Details Table
        </button>
        <button
          className={`${styles.tab} ${activeTab === 'pivot' ? styles.tabActive : ''}`}
          onClick={() => setActiveTab('pivot')}
        >
          Pivot Table
        </button>
        <div className={styles.tabWithDropdown}>
          <span className={styles.tabLabel}>
            {isCountyRestricted ? '🔐 Your County:' : 'Select a County:'}
          </span>
          <select
            value={countyFilter}
            onChange={(e) => setCountyFilter(e.target.value)}
            className={styles.countySelect}
            disabled={isCountyRestricted}
            title={isCountyRestricted ? `Restricted to ${countyFilter}` : 'Select a county'}
          >
            {availableCounties.map((county) => (
              <option key={county} value={county}>
                {county}
              </option>
            ))}
          </select>
          <span className={styles.countyCount}>
            {isCountyRestricted ? 'Access restricted to your county' : `${availableCounties.length} Counties Available`}
          </span>
        </div>
      </div>

      {/* Main Content Area */}
      <div className={styles.mainContent}>
        {/* Left Sidebar - Dimensions & Measures */}
        <aside className={styles.leftSidebar}>
          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Dimension Selection</h3>
            {[1, 2, 3, 4, 5, 6, 7, 8].map((num) => (
              <div key={num} className={styles.dimensionSelect}>
                <label>Dim {num}:</label>
                <select
                  value={dimensions[`dimension${num}` as keyof typeof dimensions]}
                  onChange={(e) =>
                    setDimensions((prev) => ({
                      ...prev,
                      [`dimension${num}`]: e.target.value,
                    }))
                  }
                  className={styles.select}
                >
                  {dimensionOptions.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </div>
            ))}
            <button
              onClick={handleApplyDimensions}
              className={styles.applyButton}
              disabled={isLoading || tableData.length === 0}
              style={{
                opacity: dimensionsChanged ? 1 : 0.6,
                marginTop: '1rem',
              }}
            >
              {isLoading ? 'Applying...' : dimensionsChanged ? 'Apply Dimensions' : 'Dimensions Applied'}
            </button>
            {Object.keys(appliedDimensions).length > 0 && (
              <button
                onClick={() => {
                  setAppliedDimensions({});
                  setTransformedData([]);
                  setTransformedColumns([]);
                  setStatus({ message: 'Dimensions cleared. Showing original data.', type: 'info' });
                }}
                className={styles.clearButton}
                style={{ marginTop: '0.5rem' }}
              >
                Clear Dimensions
              </button>
            )}
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Measure Selection</h3>
            <div className={styles.checkboxGroup}>
              {Object.entries(measures).map(([measure, checked]) => (
                <label key={measure} className={styles.checkboxLabel}>
                  <input
                    type="checkbox"
                    checked={checked}
                    onChange={() => handleMeasureToggle(measure)}
                  />
                  <span>{measure}</span>
                </label>
              ))}
            </div>
          </div>
        </aside>

        {/* Center Content - Data Table */}
        <main className={styles.centerContent}>
          {isLoading ? (
            <div className={styles.loadingState}>Loading data...</div>
          ) : displayData.length > 0 ? (
            <div className={styles.tableWrapper}>
              {Object.keys(appliedDimensions).length > 0 && (
                <div className={styles.dimensionInfo}>
                  <strong>Dimensions Applied:</strong>{' '}
                  {Object.entries(appliedDimensions)
                    .filter(([_, value]) => value && value !== '')
                    .map(([key, value]) => `${key}: ${value}`)
                    .join(', ')}
                </div>
              )}
              <table className={styles.dataTable}>
                <thead>
                  <tr>
                    {displayColumns.map((col) => (
                      <th key={col}>{col}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {displayData.map((row, idx) => (
                    <tr key={idx}>
                      {displayColumns.map((col) => {
                        const value = getFieldValue(row, col) || '-';
                        // Format numbers
                        if (typeof value === 'number') {
                          return <td key={col}>{value.toLocaleString()}</td>;
                        }
                        return <td key={col}>{value}</td>;
                      })}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className={styles.emptyState}>
              <p>No data available. Adjust filters and try again.</p>
            </div>
          )}
        </main>

        {/* Right Sidebar - Filters */}
        <aside className={styles.rightSidebar}>
          <div className={styles.cdssLogo}>
            <div className={styles.logoText}>CDSS</div>
            <div className={styles.logoSubtext}>CALIFORNIA Department of Social Services</div>
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Department</h3>
            <select
              value={departmentFilter}
              onChange={(e) => setDepartmentFilter(e.target.value)}
              className={styles.select}
            >
              {availableDepartments.map((dept) => (
                <option key={dept} value={dept}>
                  {dept}
                </option>
              ))}
            </select>
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Status</h3>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className={styles.select}
            >
              {availableStatuses.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </div>

          {/* Provider Demographics */}
          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Provider Gender</h3>
            <select
              value={providerGenderFilter}
              onChange={(e) => setProviderGenderFilter(e.target.value)}
              className={styles.select}
            >
              {availableProviderGenders.map((gender) => (
                <option key={gender} value={gender}>
                  {gender}
                </option>
              ))}
            </select>
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Provider Ethnicity</h3>
            <select
              value={providerEthnicityFilter}
              onChange={(e) => setProviderEthnicityFilter(e.target.value)}
              className={styles.select}
            >
              {availableProviderEthnicities.map((ethnicity) => (
                <option key={ethnicity} value={ethnicity}>
                  {ethnicity}
                </option>
              ))}
            </select>
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Provider Age Group</h3>
            <select
              value={providerAgeGroupFilter}
              onChange={(e) => setProviderAgeGroupFilter(e.target.value)}
              className={styles.select}
            >
              {availableProviderAgeGroups.map((ageGroup) => (
                <option key={ageGroup} value={ageGroup}>
                  {ageGroup}
                </option>
              ))}
            </select>
          </div>

          {/* Recipient Demographics */}
          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Recipient Gender</h3>
            <select
              value={recipientGenderFilter}
              onChange={(e) => setRecipientGenderFilter(e.target.value)}
              className={styles.select}
            >
              {availableRecipientGenders.map((gender) => (
                <option key={gender} value={gender}>
                  {gender}
                </option>
              ))}
            </select>
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Recipient Ethnicity</h3>
            <select
              value={recipientEthnicityFilter}
              onChange={(e) => setRecipientEthnicityFilter(e.target.value)}
              className={styles.select}
            >
              {availableRecipientEthnicities.map((ethnicity) => (
                <option key={ethnicity} value={ethnicity}>
                  {ethnicity}
                </option>
              ))}
            </select>
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Recipient Age Group</h3>
            <select
              value={recipientAgeGroupFilter}
              onChange={(e) => setRecipientAgeGroupFilter(e.target.value)}
              className={styles.select}
            >
              {availableRecipientAgeGroups.map((ageGroup) => (
                <option key={ageGroup} value={ageGroup}>
                  {ageGroup}
                </option>
              ))}
            </select>
          </div>

          {status && (
            <div
              className={`${styles.statusBanner} ${
                status.type === 'success' ? styles.statusSuccess : 
                status.type === 'error' ? styles.statusError : 
                styles.statusInfo
              }`}
            >
              {status.message}
            </div>
          )}
        </aside>
      </div>
    </div>
  );
};

export default AnalyticsPage;

