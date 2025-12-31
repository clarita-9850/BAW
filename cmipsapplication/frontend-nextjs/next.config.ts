import type { NextConfig } from "next";

// Always use localhost:8090 for local development (browser cannot resolve Docker hostnames)
// For production/Docker, use NEXT_PUBLIC_API_URL environment variable
const API_PROXY_TARGET = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8090';

const nextConfig: NextConfig = {
  reactStrictMode: true,
  // Only use standalone for production builds
  ...(process.env.NODE_ENV === 'production' ? { output: 'standalone' } : {}),

  // Proxy API requests to API Gateway (running on localhost:8090)
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${API_PROXY_TARGET}/api/:path*`,
      },
    ];
  },

  // Allow images from external sources if needed
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: '**',
      },
      {
        protocol: 'http',
        hostname: 'localhost',
      },
    ],
  },

  // Enable file watching in Docker (only when using webpack)
  ...(process.env.NODE_ENV === 'development' ? {
    webpack: (config, { dev, isServer }) => {
      if (dev && !isServer) {
        // Enable polling for file changes in Docker
        config.watchOptions = {
          poll: 1000, // Check for changes every second
          aggregateTimeout: 300, // Delay before rebuilding
        };
      }
      return config;
    },
  } : {}),
};

export default nextConfig;
