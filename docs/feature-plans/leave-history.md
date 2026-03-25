# Feature: Staff Leave History

## Goal

Implement a staff-only Leave History page for the web application that matches the provided design and shows the logged-in staff user’s past leave requests with status and duration.

---

## Scope

### In Scope

- staff-only leave history route
- leave history page UI matching the design
- bottom navigation with Leave tab active
- floating action button for future leave application flow
- backend API to fetch leave history for logged-in staff user
- frontend integration with backend API
- loading, error, and empty states
- role-based access restriction

### Out of Scope

- leave apply form flow
- leave edit/delete
- admin leave management page
- approval workflow changes
- notifications/bell functionality
- profile page implementation
- task page implementation
- attendance page changes

---

## Design Source

- `docs/designs/leave/leave-history.png`

---

## Files (MANDATORY)

### Backend

#### leave/controller/

- `LeaveController.java` (extend if already present)
  or
- `LeaveHistoryController.java`

#### leave/service/

- `LeaveService.java` (extend if already present)
  or
- `LeaveHistoryService.java`

#### leave/dto/

- `LeaveHistoryItemResponse.java`
- `LeaveHistoryResponse.java` (optional wrapper if needed)

#### leave/repository/

- `LeaveRequestRepository.java` (reuse/update)

#### leave/entity/

- reuse existing `LeaveRequest.java`

#### staff/repository/

- `StaffProfileRepository.java` (reuse if needed)

#### auth/security/

- reuse existing JWT auth / role protection

---

### Frontend

#### features/leave/

- `api.ts`
- `slice.ts` or RTK Query endpoints
- `types.ts`
- `pages/LeaveHistoryPage.tsx`
- `components/LeaveHistoryHeader.tsx`
- `components/LeaveHistoryList.tsx`
- `components/LeaveHistoryCard.tsx`
- `components/LeaveFab.tsx`
- `components/BottomNav.tsx`

#### routes/

- update leave/staff routes
- ensure staff-only protection

#### shared/api/

- reuse/update `client.ts` only if needed

#### app/router or app/layouts/

- update routing if needed

---

## Route Design

### Route

Recommended route:

- `/staff/leave`

### Access Rules

- authenticated `STAFF` users can access
- `ADMIN` users cannot access
- unauthenticated users redirect to `/login`

### Navigation Rules

- bottom nav Leave tab is active on this page
- Tasks / Attendance / Profile can navigate to existing routes or stay as placeholders if not ready
- floating action button can route to future leave apply page or remain placeholder

---

## Backend API Design

### GET `/api/leave/history`

Purpose:

- return leave history for the logged-in staff user

### Authentication

- requires valid JWT
- current user must have role `STAFF`

### Query Params

Optional:

- `limit`
- `status`
- `fromDate`
- `toDate`

For first version:

- no params required
- return all records for logged-in staff
- order newest first

### Response Shape

Each item should include:

- `leaveRequestId`
- `leaveType`
- `leaveStartDate`
- `leaveEndDate`
- `status`
- `durationDays`
- `reason` (optional)
- `requestedAt` (optional)

### Example Response

```json
[
  {
    "leaveRequestId": "uuid",
    "leaveType": "CASUAL_LEAVE",
    "leaveStartDate": "2023-09-12",
    "leaveEndDate": "2023-09-12",
    "status": "APPROVED",
    "durationDays": 1
  }
]

Done Criteria:

staff-only leave history route exists
backend leave history API works
frontend integrates with API correctly
leave cards display type, date, status, and duration
page visually matches design closely
Leave tab is active in bottom navigation
loading/error/empty states are handled
admin cannot access the page
implementation stays within leave-related modules only
```
