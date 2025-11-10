# Navigation Feature (Role Switching)

This note explains how the role switcher works and why we placed it in a dedicated feature.

## Overview
- **Goal**: allow a signed-in user to switch between attendee, manager, and admin dashboards without refreshing the page.
- **Location**: `frontend/src/features/navigation`.

## Files
| File | Role |
|------|------|
| `components/RoleSwitcher.jsx` | Renders the toggle button group and chip summary. Receives `roles`, `activeRole`, `onRoleChange`. |
| `index.js` | Barrel export for easy imports (`import { RoleSwitcher } from 'features/navigation';`). |
| `frontend/src/features/dashboard/components/DashboardLayout.jsx` | Consumes `RoleSwitcher` and maps roles to routes. |

## How it works
1. `DashboardLayout` passes available roles from auth context to `RoleSwitcher`.
2. When a user clicks another role, `onRoleChange` triggers `navigate()` (React Router). The URL changes (for example `/manager`), and the router loads the new dashboard.
3. The chip row under the switcher reminds the user which roles are currently active for the session.

## Testing
- `components/RoleSwitcher.test.jsx` confirms the component hides when no roles exist, shows buttons/chips for each role, and calls `onRoleChange` appropriately.
- Playwright also interacts with the switcher when verifying manager/admin flows.

## Why this pattern?
Separating navigation UI makes the layout cleaner and lets future work (for example, top-level nav menus) re-use it. The component is stateless and controlled via props, which is simple to test and reuse.
