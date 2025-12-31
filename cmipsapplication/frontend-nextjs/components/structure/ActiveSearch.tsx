'use client';

import React, { useState } from 'react';
import styles from './ActiveSearch.module.css';

export default function ActiveSearch() {
  const [searchQuery, setSearchQuery] = useState('');

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    // Handle search functionality
    console.log('Searching for:', searchQuery);
  };

  return (
    <div className={styles.searchContainer}>
      <form className={styles.searchForm} onSubmit={handleSearch}>
        <div className={styles.searchInputGroup}>
          <input
            type="text"
            className={styles.searchInput}
            placeholder="Search..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            aria-label="Search"
          />
          <button type="submit" className={styles.searchButton} aria-label="Submit search">
            <i className="ca-gov-icon-search"></i>
          </button>
        </div>
      </form>
    </div>
  );
}


