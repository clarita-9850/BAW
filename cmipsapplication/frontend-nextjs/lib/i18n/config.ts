import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import en from './locales/en.json';
import es from './locales/es.json';
import zh from './locales/zh.json';
import hy from './locales/hy.json';

// Initialize i18n only on client side
if (typeof window !== 'undefined') {
  if (!i18n.isInitialized) {
    i18n
      .use(LanguageDetector)
      .use(initReactI18next)
      .init({
        resources: {
          en: { translation: en },
          es: { translation: es },
          zh: { translation: zh },
          hy: { translation: hy },
        },
        fallbackLng: 'en',
        defaultNS: 'translation',
        interpolation: {
          escapeValue: false,
        },
        detection: {
          order: ['localStorage', 'navigator'],
          caches: ['localStorage'],
          lookupLocalStorage: 'preferred-language',
        },
      });
  }
} else {
  // Server-side: create a minimal i18n instance
  if (!i18n.isInitialized) {
    i18n.init({
      resources: {
        en: { translation: en },
        es: { translation: es },
        zh: { translation: zh },
        hy: { translation: hy },
      },
      fallbackLng: 'en',
      defaultNS: 'translation',
      interpolation: {
        escapeValue: false,
      },
      lng: 'en', // Default language for server-side
    });
  }
}

export default i18n;

