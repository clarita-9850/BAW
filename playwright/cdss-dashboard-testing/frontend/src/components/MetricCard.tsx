import React from 'react';

interface MetricCardProps {
  label: string;
  value: string | number;
  testId: string;
}

export const MetricCard: React.FC<MetricCardProps> = ({ label, value, testId }) => {
  const formatValue = (val: string | number) => {
    if (typeof val === 'number') {
      return val.toLocaleString('en-US', { maximumFractionDigits: 2 });
    }
    return val;
  };

  return (
    <div className="bg-white rounded-lg shadow p-4 text-center" data-testid={testId}>
      <div className="text-3xl font-bold text-blue-600" data-testid={`${testId}-value`}>
        {formatValue(value)}
      </div>
      <div className="text-sm text-gray-600 mt-1" data-testid={`${testId}-label`}>
        {label}
      </div>
    </div>
  );
};

export default MetricCard;
