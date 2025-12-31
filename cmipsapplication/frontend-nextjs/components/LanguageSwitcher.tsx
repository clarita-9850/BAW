'use client';

import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import styles from './LanguageSwitcher.module.css';

const languages = [
  { code: 'en', name: 'English', flag: '🇺🇸' },
  { code: 'es', name: 'Español', flag: '🇪🇸' },
  { code: 'zh', name: '中文', flag: '🇨🇳' },
  { code: 'hy', name: 'Հայերեն', flag: '🇦🇲' },
];

export default function LanguageSwitcher() {
  const { i18n } = useTranslation();
  const [currentLang, setCurrentLang] = useState<string>(i18n.language || 'en');

  const switchLanguage = (langCode: string) => {
    i18n.changeLanguage(langCode);
    setCurrentLang(langCode);
    
    if (typeof document !== 'undefined') {
      document.documentElement.lang = langCode;
      localStorage.setItem('preferred-language', langCode);
    }
  };

  useEffect(() => {
    // Initialize from localStorage if available
    if (typeof window !== 'undefined') {
      const savedLang = localStorage.getItem('preferred-language');
      if (savedLang && languages.some((lang) => lang.code === savedLang)) {
        switchLanguage(savedLang);
      } else {
        switchLanguage(i18n.language || 'en');
      }
    }
  }, []);

  return (
    <div className={styles.languageSwitcher}>
      <label htmlFor="language-select" className={styles.languageLabel}>
        🌐
      </label>
      <select
        id="language-select"
        value={currentLang}
        onChange={(e) => switchLanguage(e.target.value)}
        className={styles.languageSelect}
        aria-label="Select Language"
      >
        {languages.map((lang) => (
          <option key={lang.code} value={lang.code}>
            {lang.flag} {lang.name}
          </option>
        ))}
      </select>
      {currentLang && (
        <span className={styles.currentLang}>{currentLang.toUpperCase()}</span>
      )}
    </div>
  );
}





