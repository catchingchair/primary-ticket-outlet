# Auth Feature (Login & Session Management)

This guide explains how the mock authentication flow works from the browser to the backend.

## Why this structure?
React applications often use **context** and **custom hooks** to share state, because props alone become hard to manage. Our auth feature wraps the whole app in a provider so every component can read the signed-in user. We also rely on **React Router** so that pages can redirect based on auth state.

## Frontend files
| File | Purpose |
|------|---------|
| `frontend/src/app/App.jsx` | Registers app routes. Redirects unauthenticated users to `/login` via the `ProtectedRoute` helper. |
| `frontend/src/features/auth/components/LoginPage.jsx` | UI for the mock login form. Collects email, display name, role checkboxes, and optional managed venues. Submits to the backend and then navigates to the dashboards. |
| `frontend/src/features/auth/hooks/useMockLoginForm.js` | Small hook that holds form state (`email`, `displayName`, toggles, etc.), generates the roles array, and keeps helper functions (`updateField`, `resetErrors`). |
| `frontend/src/features/auth/hooks/useAuthSession.js` | Core session hook. Stores the token/user in `localStorage`, fetches `/api/me` when a token exists, and exposes `login`, `logout`, and `setUser`. |
| `frontend/src/features/auth/context/AuthContext.jsx` | Creates the React context and wraps children with the provider from `useAuthSession`. |
| `frontend/src/features/auth/context/useAuth.js` | Convenience hook so components can do `const { token } = useAuth();`. |
| `frontend/src/features/auth/index.js` | Barrel file that re-exports `AuthProvider`, `useAuth`, and `LoginPage` for simple imports. |

## Backend files
| File | Purpose |
|------|---------|
| `backend/src/main/java/com/tickets/backend/controller/AuthController.java` | Exposes `POST /api/auth/mock`, which accepts the login payload and returns token + roles. |
| `backend/src/main/java/com/tickets/backend/service/UserService.java` | Looks up or creates users, ensures requested roles exist, and assigns manager venues. |
| `backend/src/main/java/com/tickets/backend/service/AuthTokenService.java` | Generates the signed token returned to the client. |

## End-to-end flow
1. **User opens `/login`**. `App.jsx` renders `LoginPage` because no token exists.
2. **User fills the form**. State is managed by `useMockLoginForm`. Checking "manager" and entering venue IDs shapes the payload.
3. **`LoginPage` submits**. It calls `apiRequest('/auth/mock', ...)`. On success it runs `login(response)` from `useAuth`, storing the session and clearing errors.
4. **Redirect to `/`**. `useNavigate()` sends the browser to the attendee dashboard. `ProtectedRoute` now sees a token and lets the dashboard render.
5. **Future visits**. `useAuthSession` loads `localStorage`, fetches `/api/me`, and updates context so the dashboards know who you are.
6. **Logout**. Clicking "Sign out" triggers `logout()` from the context, clearing `localStorage`, wiping user state, and returning you to `/login` (handled by the route guard).

## Testing
- `frontend/src/features/auth/components/LoginPage.test.jsx` checks validation and ensures the payload contains roles and managed venue IDs. It also verifies we redirect to `/` after login.
- `frontend/src/features/auth/context/AuthContext.test.jsx` covers login, logout, persisted sessions, and error handling when `/api/me` fails.
- Playwright E2E (`frontend/tests/e2e/login-and-role.spec.ts`) performs a full mock login and confirms dashboards load.

## React concepts recap
- **Context Provider**: allows any component to read auth data without deeply nested props.
- **Custom Hook**: `useAuthSession` encapsulates logic (fetching, localStorage) that can be reused and tested separately.
- **React Router**: `Navigate` protects routes and handles redirects on login/logout.
