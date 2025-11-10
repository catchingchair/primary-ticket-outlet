# Backend Architecture (Crash Course)

This section gives an approachable overview of the Spring Boot backend so readers can connect the React UI to the Java services.

## Key directories
| Path | Description |
|------|-------------|
| `backend/src/main/java/com/tickets/backend/controller` | REST controllers. Example: `AuthController` for `/api/auth/mock`, `EventController` for `/api/events`. |
| `backend/src/main/java/com/tickets/backend/service` | Business logic. Example: `UserService` handles role creation/assignment; `EventService` orchestrates purchases. |
| `backend/src/main/java/com/tickets/backend/repository` | Spring Data JPA interfaces for database access. |
| `backend/src/main/java/com/tickets/backend/model` | Entity classes (annotated with JPA + Lombok) such as `User`, `Event`, `Venue`. |
| `backend/src/main/resources/db/migration` | Flyway database migration scripts. `V1__create_tables.sql` creates schema; repeatable scripts seed sample data. |

## Request lifecycle example
1. React calls `POST /api/events/{id}/purchase` from `AttendeeDashboard.jsx`.
2. `EventController.purchaseTickets(...)` receives the request.
3. `EventService` validates availability, talks to the payment stub, and records the purchase.
4. `PurchaseRepository` persists the changes.
5. Resulting DTO is returned to the frontend (list of ticket codes).

## Why controller → service → repository?
- **Controllers** focus on HTTP details (status codes, request bodies).
- **Services** contain business rules (role assignment, ticket count checks).
- **Repositories** are database-specific; isolating them simplifies testing and future database changes.

## Testing & coverage
- Run `./gradlew test` for unit / service tests.
- JaCoCo HTML report lives at `backend/build/reports/jacoco/test/html/index.html` (current line coverage ~88%).
