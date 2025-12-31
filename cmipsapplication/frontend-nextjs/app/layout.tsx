import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/contexts/AuthContext";
import { CountyThemeProvider } from "@/contexts/CountyThemeContext";
import { QueryClientProvider } from "./providers/QueryClientProvider";
import I18nProvider from "./i18n-provider";
import ConditionalHeader from "@/components/structure/ConditionalHeader";
import ConditionalFooter from "@/components/structure/ConditionalFooter";
import Script from "next/script";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "CMIPS - Case Management Information and Payrolling System",
  description: "CMIPS Application",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <head>
        <link rel="stylesheet" href="/cagov/css/cagov.core.min.css" />
        {/* Default theme - will be replaced by CountyThemeProvider if county is found */}
        <link rel="stylesheet" href="/cagov/css/colortheme-oceanside.min.css" id="cagov-default-theme" />
      </head>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <QueryClientProvider>
          <I18nProvider>
            <AuthProvider>
              <CountyThemeProvider>
                <ConditionalHeader />
                <main id="main-content" className="container" style={{ minHeight: 'calc(100vh - 300px)', padding: '2rem 0' }}>
                  {children}
                </main>
                <ConditionalFooter />
              </CountyThemeProvider>
            </AuthProvider>
          </I18nProvider>
        </QueryClientProvider>
        <Script src="/cagov/js/cagov.core.min.js" strategy="afterInteractive" />
      </body>
    </html>
  );
}
