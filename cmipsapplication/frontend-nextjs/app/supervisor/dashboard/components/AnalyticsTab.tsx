'use client';

import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { analyticsService } from '@/lib/services/analytics.service';
import styles from './analytics.module.css';

interface StatusBanner {
  type: 'success' | 'error' | 'info';
  message: string;
}

export default function AnalyticsTab() {
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

  // Left Sidebar - Measure Selection
  const [measures, setMeasures] = useState<Record<string, boolean>>({
    '(All)': true,
    'ID Count': true,
    'County Population': true,
    'Authorized Hours': true,
    'Total Hours': false,
    'Total Amount': false,
    'Active Error Rate': false,
  });

  // Right Sidebar - Filters
  const [evvFilters, setEvvFilters] = useState({
    all: true,
    etsAuthorizedCases: true,
    etsProviders: true,
  });

  const [pmFilters, setPmFilters] = useState({
    all: true,
    pmCases: true,
    psCases: true,
  });

  const [severelyImpaired, setSeverelyImpaired] = useState('(All)');
  const [agedBlindDisabled, setAgedBlindDisabled] = useState({
    all: true,
    aged: true,
    blind: true,
    disabled: true,
  });

  const [genderFilter, setGenderFilter] = useState('(All)');
  const [ageGroupFilters, setAgeGroupFilters] = useState({
    all: true,
    '0-17': true,
    '18-44': true,
    '45-64': true,
    '65-74': true,
    '75-84': true,
    '85+': true,
  });

  const [countyFilter, setCountyFilter] = useState('(All)');
  const [availableCounties, setAvailableCounties] = useState<string[]>([]);

  // Data Table
  const [tableData, setTableData] = useState<Array<Record<string, any>>>([]);
  const [tableColumns, setTableColumns] = useState<string[]>([]);
  const [appliedDimensions, setAppliedDimensions] = useState<Record<string, string>>({});
  const [transformedData, setTransformedData] = useState<Array<Record<string, any>>>([]);
  const [transformedColumns, setTransformedColumns] = useState<string[]>([]);

  // Available dimension options
  const dimensionOptions = [
    { value: '', label: 'None' },
    { value: 'providerEthnicity', label: 'Provider Ethnicity' },
    { value: 'recipientEthnicity', label: 'Recipient Ethnicity' },
    { value: 'providerAgeGroup', label: 'Provider Age Group' },
    { value: 'recipientAgeGroup', label: 'Recipient Age Group' },
    { value: 'providerGender', label: 'Provider Gender' },
    { value: 'recipientGender', label: 'Recipient Gender' },
    { value: 'providerCounty', label: 'Provider County' },
    { value: 'recipientCounty', label: 'Recipient County' },
    { value: 'projectCounty', label: 'Project County' },
    { value: 'districtName', label: 'District' },
    { value: 'districtId', label: 'District ID' },
    { value: 'serviceType', label: 'Service Type' },
    { value: 'serviceCategory', label: 'Service Category' },
    { value: 'status', label: 'Status' },
    { value: 'priorityLevel', label: 'Priority Level' },
  ];

  // Helper function to get field value with fallback variations
  const getFieldValue = (row: Record<string, any>, fieldName: string): any => {
    if (row[fieldName] !== undefined && row[fieldName] !== null) {
      return row[fieldName];
    }
    if (row[fieldName.toLowerCase()] !== undefined && row[fieldName.toLowerCase()] !== null) {
      return row[fieldName.toLowerCase()];
    }
    const camelCase = fieldName.charAt(0).toLowerCase() + fieldName.slice(1);
    if (row[camelCase] !== undefined && row[camelCase] !== null) {
      return row[camelCase];
    }
    return null;
  };

  const showStatus = useCallback((message: string, type: 'success' | 'error' | 'info') => {
    setStatus({ message, type });
    setTimeout(() => setStatus(null), 5000);
  }, []);

  // Load initial filters
  useEffect(() => {
    const loadFilters = async () => {
      try {
        const response = await analyticsService.getAdhocFilters();
        setAvailableCounties(['(All)', 'Los Angeles', 'Orange', 'San Diego']);
        setFiltersLoaded(true);
      } catch (err: any) {
        console.error('Error loading filters:', err);
        setStatus({ message: err?.message || 'Unable to load filters', type: 'error' });
      }
    };
    loadFilters();
  }, []);

  // Load metrics
  useEffect(() => {
    if (!filtersLoaded) return;

    const loadMetrics = async () => {
      try {
        setIsLoading(true);
        const params: Record<string, string> = {};
        
        if (countyFilter !== '(All)') params.county = countyFilter;
        if (genderFilter !== '(All)') params.gender = genderFilter;
        if (severelyImpaired !== '(All)') params.severelyImpaired = severelyImpaired;

        const statsResponse = await analyticsService.getAdhocStats(params);
        
        if (statsResponse.status === 'SUCCESS' && statsResponse.stats) {
          setMetrics({
            individuals: statsResponse.stats.totalRecords || 0,
            population: 38814347,
            perCapitaRate: statsResponse.stats.totalRecords ? 
              ((statsResponse.stats.totalRecords / 38814347) * 100).toFixed(2) : '0.00',
            totalAuthorizedHours: statsResponse.stats.totalHours || 0,
            totalAuthorizedHours2: statsResponse.stats.avgHours || 0,
          });
        }

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
  }, [filtersLoaded, countyFilter, genderFilter, severelyImpaired]);

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

  const handleEvvToggle = (key: string) => {
    if (key === 'all') {
      const allSelected = evvFilters.all;
      setEvvFilters({
        all: !allSelected,
        etsAuthorizedCases: !allSelected,
        etsProviders: !allSelected,
      });
    } else {
      setEvvFilters((prev) => ({
        ...prev,
        [key]: !prev[key as keyof typeof prev],
        all: false,
      }));
    }
  };

  const handlePmToggle = (key: string) => {
    if (key === 'all') {
      const allSelected = pmFilters.all;
      setPmFilters({
        all: !allSelected,
        pmCases: !allSelected,
        psCases: !allSelected,
      });
    } else {
      setPmFilters((prev) => ({
        ...prev,
        [key]: !prev[key as keyof typeof prev],
        all: false,
      }));
    }
  };

  const handleAgedBlindDisabledToggle = (key: string) => {
    if (key === 'all') {
      const allSelected = agedBlindDisabled.all;
      setAgedBlindDisabled({
        all: !allSelected,
        aged: !allSelected,
        blind: !allSelected,
        disabled: !allSelected,
      });
    } else {
      setAgedBlindDisabled((prev) => ({
        ...prev,
        [key]: !prev[key as keyof typeof prev],
        all: false,
      }));
    }
  };

  const handleAgeGroupToggle = (key: string) => {
    if (key === 'all') {
      const allSelected = ageGroupFilters.all;
      setAgeGroupFilters({
        all: !allSelected,
        '0-17': !allSelected,
        '18-44': !allSelected,
        '45-64': !allSelected,
        '65-74': !allSelected,
        '75-84': !allSelected,
        '85+': !allSelected,
      });
    } else {
      setAgeGroupFilters((prev) => ({
        ...prev,
        [key]: !prev[key as keyof typeof prev],
        all: false,
      }));
    }
  };

  // Apply dimensions to transform the data
  const handleApplyDimensions = useCallback(() => {
    if (tableData.length === 0) {
      setStatus({ message: 'No data available to apply dimensions', type: 'info' });
      return;
    }

    const activeDimensions = Object.entries(dimensions)
      .filter(([_, value]) => value && value !== '')
      .map(([key, value]) => ({ key, value }));

    if (activeDimensions.length === 0) {
      setTransformedData(tableData);
      setTransformedColumns(tableColumns);
      setAppliedDimensions({});
      setStatus({ message: 'No dimensions selected. Showing original data.', type: 'info' });
      return;
    }

    try {
      setIsLoading(true);

      const groupedData = new Map<string, {
        key: string;
        values: Record<string, any>;
        count: number;
        rows: Array<Record<string, any>>;
      }>();

      tableData.forEach((row) => {
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

        activeDimensions.forEach(({ value }) => {
          const fieldValue = getFieldValue(row, value);
          group.values[value] = fieldValue || 'Unknown';
        });

        if (measures['ID Count']) {
          group.values['ID Count'] = (group.values['ID Count'] || 0) + 1;
        }
        if (measures['Total Hours'] || measures['Authorized Hours']) {
          const hours = parseFloat(row.totalHours || row.hours || row.authorizedHours || 0);
          group.values['Total Hours'] = (group.values['Total Hours'] || 0) + hours;
        }
        if (measures['Total Amount']) {
          const amount = parseFloat(row.totalAmount || row.amount || 0);
          group.values['Total Amount'] = (group.values['Total Amount'] || 0) + amount;
        }
      });

      const transformed = Array.from(groupedData.values()).map((group) => {
        const result: Record<string, any> = { ...group.values };
        
        if (measures['ID Count']) {
          result['ID Count'] = group.count;
        }
        if (measures['Total Hours'] || measures['Authorized Hours']) {
          result['Authorized Hours'] = group.values['Total Hours'] || 0;
        }
        if (measures['Total Amount']) {
          result['Total Amount'] = group.values['Total Amount'] || 0;
        }
        if (measures['County Population']) {
          const firstRow = group.rows[0];
          result['County Population'] = firstRow.countyPopulation || firstRow.population || 0;
        }

        return result;
      });

      const newColumns: string[] = [];
      
      activeDimensions.forEach(({ value }) => {
        newColumns.push(value.charAt(0).toUpperCase() + value.slice(1));
      });

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
      background: 'rgba(255, 255, 255, 0.95)',
      backdropFilter: 'blur(10px)',
      borderRadius: '20px',
      boxShadow: '0 20px 40px rgba(0, 0, 0, 0.1)',
      minHeight: 'auto',
    }}>
      {/* Top Banner */}
      <div style={{ 
        background: 'linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%)',
        color: 'white',
        padding: '1.5rem 2rem',
        borderRadius: '12px',
        marginBottom: '1.5rem',
        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
      }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, margin: 0 }}>CDSS Adhoc Reporting Table</h1>
      </div>

      {/* Key Metrics */}
      <div style={{ 
        display: 'grid',
        gridTemplateColumns: 'repeat(5, 1fr)',
        gap: '1rem',
        marginBottom: '1.5rem',
      }}>
        {[
          { value: metrics.individuals.toLocaleString(), label: 'Individuals' },
          { value: metrics.population.toLocaleString(), label: 'Population' },
          { value: metrics.perCapitaRate, label: 'Per Capita Rate' },
          { value: metrics.totalAuthorizedHours.toLocaleString(), label: 'Total Authorized Hours' },
          { value: metrics.totalAuthorizedHours2.toFixed(2), label: 'Total Authorized Hours' },
        ].map((metric, idx) => (
          <div key={idx} style={{
            background: idx === 0 ? 'linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%)' :
                         idx === 1 ? 'linear-gradient(135deg, #2563eb 0%, #1e40af 100%)' :
                         idx === 2 ? 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)' :
                         idx === 3 ? 'linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%)' :
                         'linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%)',
            color: 'white',
            padding: '1.5rem',
            borderRadius: '12px',
            textAlign: 'center',
            boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
          }}>
            <div style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '0.5rem' }}>{metric.value}</div>
            <div style={{ fontSize: '0.9rem', opacity: 0.9, textTransform: 'uppercase', letterSpacing: '0.05em' }}>{metric.label}</div>
          </div>
        ))}
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
          <span className={styles.tabLabel}>Select a County:</span>
          <select
            value={countyFilter}
            onChange={(e) => setCountyFilter(e.target.value)}
            className={styles.countySelect}
          >
            {availableCounties.map((county) => (
              <option key={county} value={county}>
                {county}
              </option>
            ))}
          </select>
          <span className={styles.countyCount}>58 Counties Included</span>
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
            <h3 className={styles.sidebarTitle}>Electronic Visit Verification</h3>
            <div className={styles.checkboxGroup}>
              {Object.entries(evvFilters).map(([key, checked]) => (
                <label key={key} className={styles.checkboxLabel}>
                  <input
                    type="checkbox"
                    checked={checked}
                    onChange={() => handleEvvToggle(key)}
                  />
                  <span>
                    {key === 'all' ? '(All)' : key === 'etsAuthorizedCases' ? 'ETS Authorized Cases' : 'ETS Providers'}
                  </span>
                </label>
              ))}
            </div>
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Protective Sup / Paramedical</h3>
            <div className={styles.checkboxGroup}>
              {Object.entries(pmFilters).map(([key, checked]) => (
                <label key={key} className={styles.checkboxLabel}>
                  <input
                    type="checkbox"
                    checked={checked}
                    onChange={() => handlePmToggle(key)}
                  />
                  <span>
                    {key === 'all' ? '(All)' : key === 'pmCases' ? 'PM Cases' : 'PS Cases'}
                  </span>
                </label>
              ))}
            </div>
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Severely Impaired</h3>
            <select
              value={severelyImpaired}
              onChange={(e) => setSeverelyImpaired(e.target.value)}
              className={styles.select}
            >
              <option value="(All)">(All)</option>
              <option value="NSI">NSI</option>
              <option value="SI">SI</option>
            </select>
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Aged, Blind, Disabled</h3>
            <div className={styles.checkboxGroup}>
              {Object.entries(agedBlindDisabled).map(([key, checked]) => (
                <label key={key} className={styles.checkboxLabel}>
                  <input
                    type="checkbox"
                    checked={checked}
                    onChange={() => handleAgedBlindDisabledToggle(key)}
                  />
                  <span>
                    {key === 'all' ? '(All)' : key.charAt(0).toUpperCase() + key.slice(1)}
                  </span>
                </label>
              ))}
            </div>
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Gender</h3>
            <select
              value={genderFilter}
              onChange={(e) => setGenderFilter(e.target.value)}
              className={styles.select}
            >
              <option value="(All)">(All)</option>
              <option value="Male">Male</option>
              <option value="Female">Female</option>
              <option value="Other">Other</option>
            </select>
          </div>

          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Age Group</h3>
            <div className={styles.checkboxGroup}>
              {Object.entries(ageGroupFilters).map(([key, checked]) => (
                <label key={key} className={styles.checkboxLabel}>
                  <input
                    type="checkbox"
                    checked={checked}
                    onChange={() => handleAgeGroupToggle(key)}
                  />
                  <span>{key === 'all' ? '(All)' : key}</span>
                </label>
              ))}
            </div>
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
}

