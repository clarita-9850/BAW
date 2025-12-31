import React, { useState } from 'react';
import { DashboardTableRow } from '../types/dashboard';

interface DataTableProps {
  data: DashboardTableRow[];
  viewType: 'details' | 'pivot';
}

type SortField = keyof DashboardTableRow;
type SortDirection = 'asc' | 'desc';

export const DataTable: React.FC<DataTableProps> = ({ data, viewType }) => {
  const [sortField, setSortField] = useState<SortField>('count');
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc');

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('desc');
    }
  };

  const sortedData = [...data].sort((a, b) => {
    const aVal = a[sortField];
    const bVal = b[sortField];

    if (aVal === null || aVal === undefined) return 1;
    if (bVal === null || bVal === undefined) return -1;

    if (typeof aVal === 'number' && typeof bVal === 'number') {
      return sortDirection === 'asc' ? aVal - bVal : bVal - aVal;
    }

    const aStr = String(aVal);
    const bStr = String(bVal);
    return sortDirection === 'asc'
      ? aStr.localeCompare(bStr)
      : bStr.localeCompare(aStr);
  });

  const formatNumber = (num: number | undefined) => {
    if (num === undefined || num === null) return '-';
    return num.toLocaleString('en-US', { maximumFractionDigits: 2 });
  };

  const SortHeader: React.FC<{ field: SortField; label: string }> = ({ field, label }) => (
    <th
      className="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
      onClick={() => handleSort(field)}
      data-testid={`sort-${field}`}
    >
      <div className="flex items-center">
        {label}
        {sortField === field && (
          <span className="ml-1">{sortDirection === 'asc' ? '↑' : '↓'}</span>
        )}
      </div>
    </th>
  );

  if (viewType === 'pivot') {
    return (
      <div className="bg-white rounded-lg shadow overflow-hidden" data-testid="pivot-table">
        <div className="p-4 text-center text-gray-500">
          Pivot Table View (Coming Soon)
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow overflow-hidden" data-testid="data-table">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <SortHeader field="race" label="Race" />
              <SortHeader field="ethnicity" label="Ethnicity" />
              <SortHeader field="gender" label="Gender" />
              <SortHeader field="ageGroup" label="Age Group" />
              <SortHeader field="agedBlindDisabled" label="ABD Status" />
              <SortHeader field="caseType" label="Case Type" />
              <SortHeader field="count" label="# Count" />
              <SortHeader field="countyPopulation" label="County Population" />
              <SortHeader field="authorizedHours" label="Authorized Hours" />
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200" data-testid="table-body">
            {sortedData.length === 0 ? (
              <tr>
                <td colSpan={9} className="px-3 py-4 text-center text-gray-500" data-testid="no-data-message">
                  No data available for the selected filters
                </td>
              </tr>
            ) : (
              sortedData.map((row, index) => (
                <tr key={index} className="hover:bg-gray-50" data-testid={`table-row-${index}`}>
                  <td className="px-3 py-2 whitespace-nowrap text-sm text-gray-900">{row.race || '-'}</td>
                  <td className="px-3 py-2 whitespace-nowrap text-sm text-gray-900">{row.ethnicity || '-'}</td>
                  <td className="px-3 py-2 whitespace-nowrap text-sm text-gray-900">{row.gender || '-'}</td>
                  <td className="px-3 py-2 whitespace-nowrap text-sm text-gray-900">{row.ageGroup || '-'}</td>
                  <td className="px-3 py-2 whitespace-nowrap text-sm text-gray-900">{row.agedBlindDisabled || '-'}</td>
                  <td className="px-3 py-2 whitespace-nowrap text-sm text-gray-900">{row.caseType || '-'}</td>
                  <td className="px-3 py-2 whitespace-nowrap text-sm text-gray-900" data-testid={`row-${index}-count`}>
                    {formatNumber(row.count)}
                  </td>
                  <td className="px-3 py-2 whitespace-nowrap text-sm text-gray-900" data-testid={`row-${index}-population`}>
                    {formatNumber(row.countyPopulation)}
                  </td>
                  <td className="px-3 py-2 whitespace-nowrap text-sm text-gray-900" data-testid={`row-${index}-hours`}>
                    {formatNumber(row.authorizedHours)}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      <div className="px-4 py-2 bg-gray-50 text-sm text-gray-500" data-testid="table-row-count">
        Showing {sortedData.length} rows
      </div>
    </div>
  );
};

export default DataTable;
