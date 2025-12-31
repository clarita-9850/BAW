'use client';

import { usePathname } from 'next/navigation';
import Header from './Header';

export default function ConditionalHeader() {
  const pathname = usePathname();
  const isLoginPage = pathname === '/login';
  
  // Don't show header on login page
  if (isLoginPage) {
    return null;
  }
  
  return <Header />;
}


