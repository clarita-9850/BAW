import type { Metadata } from "next";
import "./globals.css";
import { AuthProvider } from "@/contexts/AuthContext";

export const metadata: Metadata = {
  title: "CMIPS ESP Portal",
  description: "Electronic Services Portal for IHSS Recipients and Providers",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased bg-gray-50">
        <AuthProvider>
          <main className="min-h-screen flex items-center justify-center">
            {children}
          </main>
        </AuthProvider>
      </body>
    </html>
  );
}



