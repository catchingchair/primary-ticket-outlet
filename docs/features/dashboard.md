# Dashboard Feature (Attendee, Manager, Admin)

The dashboard feature brings together the three primary roles in the ticketing experience. This document walks through the layout, each page, and the supporting hooks in plain language.

## Why split into features and hooks?
Dashboards share lots of UI (app bar, role switcher). By collecting everything inside `frontend/src/features/dashboard`, we keep related files together. Hooks such as `useEvents` and `useManagerVenues` keep API code away from JSX so the components focus on rendering.

## Key frontend files
| File | What it does |
|------|--------------|
| `frontend/src/app/App.jsx` | Registers routes for `/`, `/manager`, `/admin` and wraps them in `DashboardLayout` inside `ProtectedRoute`. |
| `frontend/src/features/dashboard/components/DashboardLayout.jsx` | Shared shell: top app bar, role switcher (`RoleSwitcher`), and `<Outlet />` for the current dashboard. Uses React Router to change routes when a new role is selected. |
| `frontend/src/features/navigation/components/RoleSwitcher.jsx` | Toggle buttons for available roles; called from the layout. |
| `frontend/src/features/dashboard/pages/AttendeeDashboard.jsx` | Shows upcoming events, lets the attendee purchase tickets. Depends on `useEvents` and `apiRequest`. |
| `frontend/src/features/dashboard/pages/ManagerDashboard.jsx` | Manager tools: create events, generate tickets, export purchasers. Consumes `useManagerVenues`. |
| `frontend/src/features/dashboard/pages/AdminDashboard.jsx` | Admin overview: venues, ticket counts, revenue. Uses `useAdminDashboard`. |
| `frontend/src/features/dashboard/hooks/useEvents.js` | Loads `/api/events` for the attendee. Tracks `loading` and `error`. |
| `frontend/src/features/dashboard/hooks/useManagerVenues.js` | Loads events for each managed venue, exposes `generateTickets`, `downloadPurchasers`, and `refresh`. |
| `frontend/src/features/dashboard/hooks/useAdminDashboard.js` | Fetches aggregated admin data from `/api/admin/dashboard`. |

## Supporting backend files
| File | Purpose |
|------|---------|
| `backend/src/main/java/com/tickets/backend/controller/EventController.java` | Serves attendee endpoints (`/api/events`, `/api/events/{id}/purchase`). |
| `backend/src/main/java/com/tickets/backend/controller/VenueController.java` | Manager endpoints for event creation and ticket generation. |
| `backend/src/main/java/com/tickets/backend/controller/AdminController.java` | Admin dashboard data (`/api/admin/dashboard`). |

## Flow by role
### Attendee (`/`)
1. `DashboardLayout` renders `AttendeeDashboard` because the route is `/`.
2. `useEvents(token)` fetches `/api/events` and returns `events`, `loading`, `error`.
3. Each event card shows name, venue, price, and buttons. The purchase form calls `apiRequest('/events/{id}/purchase', ...)`. On success, an alert lists ticket codes.
4. Errors (network, API) show a red alert.

### Manager (`/manager`)
1. Role switcher changes the URL to `/manager`, so React Router loads `ManagerDashboard`.
2. `useManagerVenues(token, managedVenues)` fetches events grouped by venue and exposes helpers.
3. Creating a new event triggers `POST /venues/{venueId}/events`. After success, `refresh()` reloads the list.
4. “Generate Tickets” prompts for quantity and calls `/events/{id}/tickets:generate`. Success message appears; list refreshes.
5. CSV export uses `downloadCsv('/events/{id}/purchasers')` and triggers a file download.

### Admin (`/admin`)
1. Switching to admin renders `AdminDashboard`.
2. `useAdminDashboard(token)` calls `/api/admin/dashboard` and returns venue summaries.
3. The page shows each venue with ticket counts and revenue. If empty, a friendly info alert guides the user.

## Testing
- Unit/component tests cover each dashboard page (`AttendeeDashboard.test.jsx`, `ManagerDashboard.test.jsx`, `AdminDashboard.test.jsx`).
- Playwright spec `frontend/tests/e2e/login-and-role.spec.ts` logs in, switches roles, and ensures the expected controls appear.

## React concepts in use
- **Nested routes** (`<Outlet />`) let us reuse the layout while swapping out content for each role.
- **Custom hooks** keep API calls tidy and reusable.
- **Context + Router** ensure the right dashboard renders only when the user has access.

