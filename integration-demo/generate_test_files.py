#!/usr/bin/env python3
"""
Python script to generate test CSV files for the Integration Hub Framework Demo.

This script creates two test files:
1. employees_test1.csv - First set of employee data
2. employees_test2.csv - Second set of employee data (for merging)

These files follow the Employee model schema:
- id (Long)
- name (String)
- department (String)
- salary (BigDecimal)
"""

import csv
import os
from pathlib import Path

# Get the directory where this script is located
script_dir = Path(__file__).parent
data_input_dir = script_dir / "data" / "input"

# Create directory if it doesn't exist
data_input_dir.mkdir(parents=True, exist_ok=True)

# Test data for first file
employees_test1 = [
    {"id": 101, "name": "Alice Johnson", "department": "Engineering", "salary": 85000},
    {"id": 102, "name": "Bob Smith", "department": "Sales", "salary": 75000},
    {"id": 103, "name": "Carol Williams", "department": "HR", "salary": 65000},
    {"id": 104, "name": "David Brown", "department": "Engineering", "salary": 92000},
    {"id": 105, "name": "Eve Davis", "department": "Marketing", "salary": 68000},
    {"id": 106, "name": "Frank Miller", "department": "Sales", "salary": 77000},
    {"id": 107, "name": "Grace Lee", "department": "Engineering", "salary": 88000},
]

# Test data for second file (with some overlaps for merge testing)
employees_test2 = [
    {"id": 108, "name": "Henry Wilson", "department": "Engineering", "salary": 95000},
    {"id": 109, "name": "Ivy Chen", "department": "HR", "salary": 70000},
    {"id": 102, "name": "Bob Smith", "department": "Sales", "salary": 75000},  # Duplicate ID
    {"id": 110, "name": "Jack Taylor", "department": "Marketing", "salary": 72000},
    {"id": 111, "name": "Karen Martinez", "department": "Engineering", "salary": 91000},
    {"id": 112, "name": "Liam Anderson", "department": "Sales", "salary": 76000},
    {"id": 113, "name": "Mia Thompson", "department": "HR", "salary": 67000},
]

def write_csv_file(filename, data):
    """Write employee data to a CSV file."""
    filepath = data_input_dir / filename
    
    with open(filepath, 'w', newline='', encoding='utf-8') as csvfile:
        fieldnames = ['id', 'name', 'department', 'salary']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        
        writer.writeheader()
        for employee in data:
            writer.writerow(employee)
    
    print(f"✅ Created: {filepath}")
    print(f"   Records: {len(data)}")
    return filepath

def main():
    print("=" * 60)
    print("Generating Test CSV Files for Integration Hub Framework Demo")
    print("=" * 60)
    print()
    
    # Generate first test file
    file1 = write_csv_file("employees_test1.csv", employees_test1)
    print()
    
    # Generate second test file
    file2 = write_csv_file("employees_test2.csv", employees_test2)
    print()
    
    print("=" * 60)
    print("Test files generated successfully!")
    print("=" * 60)
    print()
    print("Files created:")
    print(f"  - {file1}")
    print(f"  - {file2}")
    print()
    print("Usage in tests:")
    print(f"  File 1: ./data/input/employees_test1.csv")
    print(f"  File 2: ./data/input/employees_test2.csv")
    print()
    print("These files can be used for:")
    print("  - Merge operations (with duplicate ID 102)")
    print("  - Split operations (by department)")
    print("  - Convert operations (CSV to JSON/XML)")
    print("  - Filter and sort operations")
    print()

if __name__ == "__main__":
    main()



