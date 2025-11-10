# Ticket Purchasing Flow

This walkthrough shows how a ticket purchase travels from the browser to the backend and back.

## Step-by-step (frontend)
1. **User view**: On `/` the `AttendeeDashboard.jsx` component renders cards for each event using data from `useEvents.js`.
2. **Form submission**: When the "Purchase" button is clicked, `AttendeeDashboard` gathers quantity + payment token and calls `apiRequest('/events/{eventId}/purchase', ...)`.
3. **Success handling**: If the request succeeds, the component displays a success alert with returned ticket codes (`state.lastResult.ticketCodes`). Errors show a red alert.
4. **React concepts used**:
   - `useMemo` sorts events by start date.
   - `useState` stores per-event purchase state (quantity, token, error).
   - `crypto.randomUUID()` generates the idempotency key header to prevent duplicate purchases.

## Step-by-step (backend)
1. **Controller**: `EventController.purchaseTickets(...)` receives the POST request.
2. **Service**: `EventService.purchaseTickets(...)` checks seat availability, contacts the payment stub, and generates ticket codes.
3. **Repository**: `TicketRepository` and `PurchaseRepository` persist the new records.
4. **Response**: A DTO returns to the frontend with the ticket codes and updated counts.

## Files involved
- Frontend: `frontend/src/features/dashboard/pages/AttendeeDashboard.jsx`, `frontend/src/features/dashboard/hooks/useEvents.js`, `frontend/src/api/client.js`.
- Backend: `backend/src/main/java/com/tickets/backend/controller/EventController.java`, `backend/src/main/java/com/tickets/backend/service/EventService.java`, `backend/src/main/java/com/tickets/backend/repository/TicketRepository.java`.

## Testing
- Unit tests: `AttendeeDashboard.test.jsx` mocks the API and verifies success/error UI.
- E2E tests: `tests/e2e/login-and-role.spec.ts` purchases tickets via the UI.
- Backend tests: look at the service layer tests in `backend/src/test/java/com/tickets/backend/service` for purchase logic.
