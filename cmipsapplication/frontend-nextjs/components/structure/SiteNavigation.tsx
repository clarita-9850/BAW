'use client';

import React, { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import styles from './SiteNavigation.module.css';

export interface NavItem {
  href?: string;
  navHeader: string;
  navBodyArr?: Array<{
    title: string;
    href: string;
  }>;
}

interface SiteNavigationProps {
  type?: 'dropdown';
  contentArr: NavItem[];
  currentPath?: string;
}

export default function SiteNavigation({
  type = 'dropdown',
  contentArr,
  currentPath: propCurrentPath,
}: SiteNavigationProps) {
  const pathname = usePathname();
  const currentPath = propCurrentPath || pathname;
  const [activeDropdown, setActiveDropdown] = useState<number | null>(null);
  const dropdownRefs = useRef<(HTMLDivElement | null)[]>([]);

  const toggleDropdown = (index: number) => {
    setActiveDropdown(activeDropdown === index ? null : index);
  };

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        activeDropdown !== null &&
        dropdownRefs.current[activeDropdown] &&
        !dropdownRefs.current[activeDropdown]?.contains(event.target as Node)
      ) {
        setActiveDropdown(null);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [activeDropdown]);

  return (
    <nav className={styles.siteNavigation} role="navigation" aria-label="Main navigation">
      <div className={styles.navContainer}>
        <ul className={styles.navMenu}>
          {contentArr.map((item, index) => {
            const isActive = item.href && item.href !== '#' && currentPath === item.href;
            const isDropdownActive = activeDropdown === index;

            return (
              <li
                key={index}
                className={`${styles.navItem} ${isDropdownActive ? styles.active : ''} ${
                  isActive ? styles.currentPage : ''
                }`}
              >
                {item.href && !item.navBodyArr ? (
                  <Link href={item.href} className={`${styles.navLink} ${isActive ? styles.active : ''}`}>
                    {item.navHeader}
                  </Link>
                ) : (
                  <>
                    <button
                      className={`${styles.navLink} ${styles.dropdownToggle} ${isActive ? styles.active : ''}`}
                      onClick={() => toggleDropdown(index)}
                      aria-expanded={isDropdownActive}
                      aria-haspopup="true"
                    >
                      {item.navHeader}
                      <span className={styles.dropdownArrow}>▼</span>
                    </button>
                    {item.navBodyArr && (
                      <div
                        ref={(el) => {
                          dropdownRefs.current[index] = el;
                        }}
                        className={`${styles.dropdownMenu} ${isDropdownActive ? styles.show : ''}`}
                      >
                        <ul>
                          {item.navBodyArr.map((subItem, subIndex) => (
                            <li key={subIndex} className={styles.dropdownItem}>
                              <Link href={subItem.href} className={styles.dropdownLink}>
                                {subItem.title}
                              </Link>
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </>
                )}
              </li>
            );
          })}
        </ul>
      </div>
    </nav>
  );
}


