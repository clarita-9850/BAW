import React from 'react';
import { FilterOptions } from '../types/dashboard';

interface FilterPanelProps {
  filterOptions: FilterOptions;
  selectedDimensions: {
    dimension1: string;
    dimension2: string;
    dimension3: string;
    dimension4: string;
    dimension5: string;
    dimension6: string;
    dimension7: string;
    dimension8: string;
  };
  selectedMeasures: string[];
  onDimensionChange: (dimension: string, value: string) => void;
  onMeasureChange: (measures: string[]) => void;
}

export const FilterPanel: React.FC<FilterPanelProps> = ({
  filterOptions,
  selectedDimensions,
  selectedMeasures,
  onDimensionChange,
  onMeasureChange,
}) => {
  const handleMeasureToggle = (measure: string) => {
    if (measure === '(All)') {
      if (selectedMeasures.includes('(All)')) {
        onMeasureChange([]);
      } else {
        onMeasureChange(filterOptions.measures);
      }
    } else {
      if (selectedMeasures.includes(measure)) {
        onMeasureChange(selectedMeasures.filter(m => m !== measure && m !== '(All)'));
      } else {
        const newMeasures = [...selectedMeasures.filter(m => m !== '(All)'), measure];
        onMeasureChange(newMeasures);
      }
    }
  };

  return (
    <div className="bg-white rounded-lg shadow p-4 h-full overflow-y-auto" data-testid="filter-panel">
      <h3 className="font-bold text-gray-700 mb-4">Dimension Selection</h3>

      {/* Dimension Selectors */}
      {[1, 2, 3, 4, 5, 6, 7, 8].map((num) => (
        <div key={num} className="mb-3">
          <label className="block text-sm text-gray-600 mb-1">
            Select Dimension {num}
          </label>
          <select
            className="w-full border rounded px-2 py-1 text-sm"
            value={selectedDimensions[`dimension${num}` as keyof typeof selectedDimensions]}
            onChange={(e) => onDimensionChange(`dimension${num}`, e.target.value)}
            data-testid={`dimension-${num}-select`}
          >
            {filterOptions.dimensions.map((dim) => (
              <option key={dim} value={dim}>{dim}</option>
            ))}
          </select>
        </div>
      ))}

      <hr className="my-4" />

      {/* Measure Selection */}
      <h3 className="font-bold text-gray-700 mb-4">Measure Selection</h3>
      <div className="space-y-2 max-h-64 overflow-y-auto" data-testid="measure-selection">
        {filterOptions.measures.map((measure) => (
          <label key={measure} className="flex items-center text-sm cursor-pointer">
            <input
              type="checkbox"
              className="mr-2"
              checked={selectedMeasures.includes(measure)}
              onChange={() => handleMeasureToggle(measure)}
              data-testid={`measure-${measure.replace(/[^a-zA-Z0-9]/g, '-')}`}
            />
            <span className="truncate" title={measure}>{measure}</span>
          </label>
        ))}
      </div>
    </div>
  );
};

export default FilterPanel;
