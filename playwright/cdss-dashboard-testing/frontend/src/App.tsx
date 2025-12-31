import React, { useState, useEffect, useCallback } from 'react';
import { dashboardApi } from './services/api';
import { DashboardResponse, DashboardFilterRequest, FilterOptions, DashboardTableRow } from './types/dashboard';
import MetricCard from './components/MetricCard';
import FilterPanel from './components/FilterPanel';
import RightFilterPanel from './components/RightFilterPanel';
import DataTable from './components/DataTable';
import CountySelector from './components/CountySelector';

const defaultFilterOptions: FilterOptions = {
  counties: [],
  ethnicities: [],
  genders: [],
  ageGroups: [],
  races: [],
  agedBlindDisabled: [],
  caseTypes: [],
  dimensions: ['None', 'Race', 'Ethnicity', 'Gender', 'Age Group', 'Aged, Blind, Disabled', 'County', 'Case Type'],
  measures: [],
};

function App() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterOptions, setFilterOptions] = useState<FilterOptions>(defaultFilterOptions);
  const [dashboardData, setDashboardData] = useState<DashboardResponse | null>(null);

  // View type
  const [viewType, setViewType] = useState<'details' | 'pivot'>('details');

  // Dimension selections
  const [selectedDimensions, setSelectedDimensions] = useState({
    dimension1: 'Age Group',
    dimension2: 'None',
    dimension3: 'Ethnicity',
    dimension4: 'Gender',
    dimension5: 'None',
    dimension6: 'None',
    dimension7: 'None',
    dimension8: 'None',
  });

  // Measure selections
  const [selectedMeasures, setSelectedMeasures] = useState<string[]>(['(All)']);

  // County selections
  const [selectedCounties, setSelectedCounties] = useState<string[]>([]);
  const [countiesIncluded, setCountiesIncluded] = useState(true);

  // Right panel filters
  const [rightFilters, setRightFilters] = useState({
    evvStatus: [] as string[],
    caseTypes: [] as string[],
    severelyImpaired: null as boolean | null,
    agedBlindDisabled: ['(All)'] as string[],
    ethnicities: ['(All)'] as string[],
    genders: ['(All)'] as string[],
    ageGroups: [] as string[],
  });

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);

      const filters: DashboardFilterRequest = {
        ...selectedDimensions,
        selectedMeasures,
        countyCodes: selectedCounties.length > 0 && countiesIncluded ? selectedCounties : undefined,
        ethnicities: rightFilters.ethnicities.filter(e => e !== '(All)').length > 0
          ? rightFilters.ethnicities.filter(e => e !== '(All)')
          : undefined,
        genders: rightFilters.genders.filter(g => g !== '(All)').length > 0
          ? rightFilters.genders.filter(g => g !== '(All)')
          : undefined,
        ageGroups: rightFilters.ageGroups.length > 0 ? rightFilters.ageGroups : undefined,
        agedBlindDisabled: rightFilters.agedBlindDisabled.filter(a => a !== '(All)').length > 0
          ? rightFilters.agedBlindDisabled.filter(a => a !== '(All)')
          : undefined,
        caseTypes: rightFilters.caseTypes.length > 0 ? rightFilters.caseTypes : undefined,
        severelyImpaired: rightFilters.severelyImpaired,
        viewType,
      };

      const response = await dashboardApi.getData(filters);
      setDashboardData(response);
      setFilterOptions(response.filterOptions);
      setError(null);
    } catch (err) {
      setError('Failed to load dashboard data. Please ensure the backend is running.');
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  }, [selectedDimensions, selectedMeasures, selectedCounties, countiesIncluded, rightFilters, viewType]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleDimensionChange = (dimension: string, value: string) => {
    setSelectedDimensions(prev => ({
      ...prev,
      [dimension]: value,
    }));
  };

  const handleRightFilterChange = (filterType: string, values: string[] | boolean | null) => {
    setRightFilters(prev => ({
      ...prev,
      [filterType]: values,
    }));
  };

  if (loading && !dashboardData) {
    return (
      <div className="min-h-screen flex items-center justify-center" data-testid="loading-spinner">
        <div className="text-xl text-gray-600">Loading dashboard...</div>
      </div>
    );
  }

  if (error && !dashboardData) {
    return (
      <div className="min-h-screen flex items-center justify-center" data-testid="error-message">
        <div className="text-xl text-red-600">{error}</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100" data-testid="dashboard-container">
      {/* Header */}
      <header className="bg-blue-800 text-white py-4 px-6" data-testid="dashboard-header">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold">CDSS Adhoc Detail Report</h1>
            <p className="text-sm text-blue-200">by CDSS Testing Team</p>
          </div>
          <div className="flex items-center space-x-4">
            <img src="/cdss-logo.png" alt="CDSS Logo" className="h-12 w-auto" onError={(e) => e.currentTarget.style.display = 'none'} />
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="p-6">
        {/* Metrics Row */}
        <div className="bg-blue-700 rounded-lg p-4 mb-6" data-testid="metrics-section">
          <h2 className="text-white text-lg font-semibold mb-4">CDSS Adhoc Reporting Table</h2>
          <div className="grid grid-cols-5 gap-4">
            <MetricCard
              label="Individuals"
              value={dashboardData?.totalIndividuals || 0}
              testId="metric-individuals"
            />
            <MetricCard
              label="Population"
              value={dashboardData?.totalPopulation || 0}
              testId="metric-population"
            />
            <MetricCard
              label="Per Capita Rate"
              value={dashboardData?.perCapitaRate || 0}
              testId="metric-per-capita"
            />
            <MetricCard
              label="Total Authorized Hours"
              value={dashboardData?.totalAuthorizedHours || 0}
              testId="metric-total-hours"
            />
            <MetricCard
              label="Avg Authorized Hours"
              value={dashboardData?.avgAuthorizedHours || 0}
              testId="metric-avg-hours"
            />
          </div>
        </div>

        {/* Controls Row */}
        <div className="bg-white rounded-lg shadow p-4 mb-6" data-testid="controls-section">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <button
                className={`px-4 py-2 rounded font-medium ${viewType === 'details' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'}`}
                onClick={() => setViewType('details')}
                data-testid="view-details-button"
              >
                Details Table
              </button>
              <button
                className={`px-4 py-2 rounded font-medium ${viewType === 'pivot' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'}`}
                onClick={() => setViewType('pivot')}
                data-testid="view-pivot-button"
              >
                Pivot Table
              </button>
            </div>

            <CountySelector
              counties={filterOptions.counties}
              selectedCounties={selectedCounties}
              onCountyChange={setSelectedCounties}
              countiesIncluded={countiesIncluded}
              onCountiesIncludedChange={setCountiesIncluded}
            />
          </div>
        </div>

        {/* Three Column Layout */}
        <div className="grid grid-cols-12 gap-6">
          {/* Left Panel - Dimension & Measure Selection */}
          <div className="col-span-2">
            <FilterPanel
              filterOptions={filterOptions}
              selectedDimensions={selectedDimensions}
              selectedMeasures={selectedMeasures}
              onDimensionChange={handleDimensionChange}
              onMeasureChange={setSelectedMeasures}
            />
          </div>

          {/* Center - Data Table */}
          <div className="col-span-8">
            {loading ? (
              <div className="bg-white rounded-lg shadow p-8 text-center" data-testid="table-loading">
                <div className="text-gray-600">Updating data...</div>
              </div>
            ) : (
              <DataTable
                data={dashboardData?.tableData || []}
                viewType={viewType}
              />
            )}
          </div>

          {/* Right Panel - Demographic Filters */}
          <div className="col-span-2">
            <RightFilterPanel
              filterOptions={filterOptions}
              selectedFilters={rightFilters}
              onFilterChange={handleRightFilterChange}
            />
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-gray-200 py-4 px-6 text-center text-sm text-gray-600" data-testid="dashboard-footer">
        <p>CDSS Dashboard Testing Application - For Testing Purposes Only</p>
      </footer>
    </div>
  );
}

export default App;
