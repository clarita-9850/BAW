'use client';

import { usePathname } from 'next/navigation';
import Footer from './Footer';

export default function ConditionalFooter() {
  const pathname = usePathname();
  const isLoginPage = pathname === '/login';
  
  // Don't show footer on login page
  if (isLoginPage) {
    return null;
  }
  
  return <Footer />;
}


