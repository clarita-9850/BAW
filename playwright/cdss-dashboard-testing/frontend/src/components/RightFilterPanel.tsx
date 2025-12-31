import React from 'react';
import { FilterOptions } from '../types/dashboard';

interface RightFilterPanelProps {
  filterOptions: FilterOptions;
  selectedFilters: {
    evvStatus: string[];
    caseTypes: string[];
    severelyImpaired: boolean | null;
    agedBlindDisabled: string[];
    ethnicities: string[];
    genders: string[];
    ageGroups: string[];
  };
  onFilterChange: (filterType: string, values: string[] | boolean | null) => void;
}

export const RightFilterPanel: React.FC<RightFilterPanelProps> = ({
  filterOptions,
  selectedFilters,
  onFilterChange,
}) => {
  const handleMultiSelect = (filterType: string, value: string, currentValues: string[]) => {
    if (value === '(All)') {
      if (currentValues.includes('(All)')) {
        onFilterChange(filterType, []);
      } else {
        const allValues = filterType === 'ethnicities' ? filterOptions.ethnicities :
                         filterType === 'genders' ? filterOptions.genders :
                         filterType === 'ageGroups' ? filterOptions.ageGroups :
                         filterType === 'agedBlindDisabled' ? filterOptions.agedBlindDisabled :
                         filterOptions.caseTypes;
        onFilterChange(filterType, ['(All)', ...allValues]);
      }
    } else {
      if (currentValues.includes(value)) {
        onFilterChange(filterType, currentValues.filter(v => v !== value && v !== '(All)'));
      } else {
        onFilterChange(filterType, [...currentValues.filter(v => v !== '(All)'), value]);
      }
    }
  };

  return (
    <div className="bg-white rounded-lg shadow p-4 h-full overflow-y-auto" data-testid="right-filter-panel">
      {/* Electronic Visit Verification */}
      <div className="mb-4">
        <h4 className="font-bold text-sm text-gray-700 mb-2">Electronic Visit Verification</h4>
        <div className="space-y-1" data-testid="evv-filter">
          {['Verified', 'Pending'].map((status) => (
            <label key={status} className="flex items-center text-sm cursor-pointer">
              <input
                type="checkbox"
                className="mr-2"
                checked={selectedFilters.evvStatus.includes(status)}
                onChange={() => handleMultiSelect('evvStatus', status, selectedFilters.evvStatus)}
                data-testid={`evv-${status.toLowerCase()}`}
              />
              {status}
            </label>
          ))}
        </div>
      </div>

      {/* Productive Sup / Paramedical */}
      <div className="mb-4">
        <h4 className="font-bold text-sm text-gray-700 mb-2">Productive Sup / Paramedical</h4>
        <div className="space-y-1" data-testid="case-type-filter">
          {filterOptions.caseTypes.map((caseType) => (
            <label key={caseType} className="flex items-center text-sm cursor-pointer">
              <input
                type="checkbox"
                className="mr-2"
                checked={selectedFilters.caseTypes.includes(caseType)}
                onChange={() => handleMultiSelect('caseTypes', caseType, selectedFilters.caseTypes)}
                data-testid={`case-type-${caseType.toLowerCase()}`}
              />
              {caseType} Cases
            </label>
          ))}
        </div>
      </div>

      {/* Severely Impaired */}
      <div className="mb-4">
        <h4 className="font-bold text-sm text-gray-700 mb-2">Severely Impaired</h4>
        <div className="space-y-1" data-testid="severely-impaired-filter">
          <label className="flex items-center text-sm cursor-pointer">
            <input
              type="checkbox"
              className="mr-2"
              checked={selectedFilters.severelyImpaired === true}
              onChange={() => onFilterChange('severelyImpaired', selectedFilters.severelyImpaired === true ? null : true)}
              data-testid="severely-impaired-yes"
            />
            Severely Impaired Only
          </label>
        </div>
      </div>

      {/* Aged, Blind, Disabled */}
      <div className="mb-4">
        <h4 className="font-bold text-sm text-gray-700 mb-2">Aged, Blind, Disabled</h4>
        <div className="space-y-1" data-testid="abd-filter">
          <label className="flex items-center text-sm cursor-pointer">
            <input
              type="checkbox"
              className="mr-2"
              checked={selectedFilters.agedBlindDisabled.includes('(All)')}
              onChange={() => handleMultiSelect('agedBlindDisabled', '(All)', selectedFilters.agedBlindDisabled)}
              data-testid="abd-all"
            />
            (All)
          </label>
          {filterOptions.agedBlindDisabled.map((abd) => (
            <label key={abd} className="flex items-center text-sm cursor-pointer">
              <input
                type="checkbox"
                className="mr-2"
                checked={selectedFilters.agedBlindDisabled.includes(abd)}
                onChange={() => handleMultiSelect('agedBlindDisabled', abd, selectedFilters.agedBlindDisabled)}
                data-testid={`abd-${abd.toLowerCase()}`}
              />
              {abd}
            </label>
          ))}
        </div>
      </div>

      {/* Ethnicity */}
      <div className="mb-4">
        <h4 className="font-bold text-sm text-gray-700 mb-2">Ethnicity</h4>
        <div className="space-y-1" data-testid="ethnicity-filter">
          <label className="flex items-center text-sm cursor-pointer">
            <input
              type="checkbox"
              className="mr-2"
              checked={selectedFilters.ethnicities.includes('(All)')}
              onChange={() => handleMultiSelect('ethnicities', '(All)', selectedFilters.ethnicities)}
              data-testid="ethnicity-all"
            />
            (All)
          </label>
          {filterOptions.ethnicities.map((ethnicity) => (
            <label key={ethnicity} className="flex items-center text-sm cursor-pointer">
              <input
                type="checkbox"
                className="mr-2"
                checked={selectedFilters.ethnicities.includes(ethnicity)}
                onChange={() => handleMultiSelect('ethnicities', ethnicity, selectedFilters.ethnicities)}
                data-testid={`ethnicity-${ethnicity.toLowerCase().replace(/\s+/g, '-')}`}
              />
              {ethnicity}
            </label>
          ))}
        </div>
      </div>

      {/* Gender */}
      <div className="mb-4">
        <h4 className="font-bold text-sm text-gray-700 mb-2">Gender</h4>
        <div className="space-y-1" data-testid="gender-filter">
          <label className="flex items-center text-sm cursor-pointer">
            <input
              type="checkbox"
              className="mr-2"
              checked={selectedFilters.genders.includes('(All)')}
              onChange={() => handleMultiSelect('genders', '(All)', selectedFilters.genders)}
              data-testid="gender-all"
            />
            (All)
          </label>
          {filterOptions.genders.map((gender) => (
            <label key={gender} className="flex items-center text-sm cursor-pointer">
              <input
                type="checkbox"
                className="mr-2"
                checked={selectedFilters.genders.includes(gender)}
                onChange={() => handleMultiSelect('genders', gender, selectedFilters.genders)}
                data-testid={`gender-${gender.toLowerCase()}`}
              />
              {gender}
            </label>
          ))}
        </div>
      </div>

      {/* Age Group */}
      <div className="mb-4">
        <h4 className="font-bold text-sm text-gray-700 mb-2">Age Group</h4>
        <div className="space-y-1" data-testid="age-group-filter">
          {filterOptions.ageGroups.map((ageGroup) => (
            <label key={ageGroup} className="flex items-center text-sm cursor-pointer">
              <input
                type="checkbox"
                className="mr-2"
                checked={selectedFilters.ageGroups.includes(ageGroup)}
                onChange={() => handleMultiSelect('ageGroups', ageGroup, selectedFilters.ageGroups)}
                data-testid={`age-group-${ageGroup.replace(/[^a-zA-Z0-9]/g, '-')}`}
              />
              {ageGroup}
            </label>
          ))}
        </div>
      </div>
    </div>
  );
};

export default RightFilterPanel;
