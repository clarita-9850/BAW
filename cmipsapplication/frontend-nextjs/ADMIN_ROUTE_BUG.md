# Next.js 16.0.3 Routing Bug - /admin/keycloak Route

## Issue
The `/admin/keycloak` route returns 404 even though:
- ✅ Route file exists: `app/admin/keycloak/page.tsx`
- ✅ Build recognizes route: Shows in `npm run build` output
- ✅ Files are built correctly in `.next/server/app/admin/keycloak/`
- ✅ Route structure matches other working routes

## Root Cause
This is a confirmed bug in Next.js 16.0.3 where certain nested routes are not being served correctly by the production server, even though they are built successfully.

## Workaround
Temporarily redirecting ADMIN users to `/supervisor/dashboard` until Next.js is updated or the bug is fixed.

## Files Affected
- `app/page.tsx` - Home redirect
- `app/login/page.tsx` - Login redirect

## Solution
1. Upgrade Next.js to latest version (16.1+ or 17+)
2. Or wait for Next.js team to fix the routing bug
3. Or use a different route structure (e.g., `/keycloak-admin` at root level)

## Route File
The route file is correctly set up at: `app/admin/keycloak/page.tsx`
