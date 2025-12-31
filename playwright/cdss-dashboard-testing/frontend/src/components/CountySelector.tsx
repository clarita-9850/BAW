import React, { useState } from 'react';

interface CountySelectorProps {
  counties: string[];
  selectedCounties: string[];
  onCountyChange: (counties: string[]) => void;
  countiesIncluded: boolean;
  onCountiesIncludedChange: (included: boolean) => void;
}

export const CountySelector: React.FC<CountySelectorProps> = ({
  counties,
  selectedCounties,
  onCountyChange,
  countiesIncluded,
  onCountiesIncludedChange,
}) => {
  const [isOpen, setIsOpen] = useState(false);

  const handleCountyToggle = (county: string) => {
    if (selectedCounties.includes(county)) {
      onCountyChange(selectedCounties.filter(c => c !== county));
    } else {
      onCountyChange([...selectedCounties, county]);
    }
  };

  const handleSelectAll = () => {
    if (selectedCounties.length === counties.length) {
      onCountyChange([]);
    } else {
      onCountyChange([...counties]);
    }
  };

  return (
    <div className="flex items-center space-x-4" data-testid="county-selector">
      <div className="relative">
        <button
          onClick={() => setIsOpen(!isOpen)}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 flex items-center"
          data-testid="county-dropdown-button"
        >
          Select a County
          <svg
            className={`ml-2 w-4 h-4 transform ${isOpen ? 'rotate-180' : ''}`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </button>

        {isOpen && (
          <div className="absolute z-10 mt-1 w-64 bg-white border rounded-lg shadow-lg max-h-64 overflow-y-auto" data-testid="county-dropdown-menu">
            <div className="p-2 border-b">
              <label className="flex items-center text-sm cursor-pointer">
                <input
                  type="checkbox"
                  className="mr-2"
                  checked={selectedCounties.length === counties.length}
                  onChange={handleSelectAll}
                  data-testid="county-select-all"
                />
                Select All
              </label>
            </div>
            <div className="p-2">
              {counties.map((county) => (
                <label key={county} className="flex items-center text-sm cursor-pointer py-1">
                  <input
                    type="checkbox"
                    className="mr-2"
                    checked={selectedCounties.includes(county)}
                    onChange={() => handleCountyToggle(county)}
                    data-testid={`county-${county.toLowerCase().replace(/\s+/g, '-')}`}
                  />
                  {county}
                </label>
              ))}
            </div>
          </div>
        )}
      </div>

      {selectedCounties.length > 0 && (
        <span className="text-sm text-gray-600" data-testid="selected-counties-count">
          {selectedCounties.length} selected
        </span>
      )}

      <label className="flex items-center text-sm cursor-pointer" data-testid="counties-included-toggle">
        <input
          type="checkbox"
          className="mr-2"
          checked={countiesIncluded}
          onChange={(e) => onCountiesIncludedChange(e.target.checked)}
          data-testid="counties-included-checkbox"
        />
        Counties Included
      </label>
    </div>
  );
};

export default CountySelector;
