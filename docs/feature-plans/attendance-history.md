# Feature: Staff Attendance History

## Goal

Implement the staff attendance history screen shown in the design, focused on weekly overview metrics and weekly attendance logs for the logged-in `STAFF` user.

---

## Scope

### In Scope

- staff-only attendance history page
- route for attendance history screen
- bottom navigation with Attendance tab active
- weekly overview cards
- weekly logs list/timeline UI
- backend API for weekly attendance summary
- backend API for weekly attendance log list
- frontend integration with backend APIs
- role-based access control for staff only
- static notification icon and header area

### Out of Scope

- admin attendance pages
- edit/correction of attendance
- leave integration
- task integration
- biometric/fingerprint integration
- overtime approval workflow
- break tracking backend unless derived/mock
- full history page implementation behind “View Full History”

---

## Feature Summary

This feature adds a staff-facing attendance history screen that displays:

- total hours worked for the selected week
- overtime summary
- breaks summary
- weekly daily attendance cards/log entries
- bottom navigation

The page is for `STAFF` users only. It should read attendance records from the backend and compute/display weekly metrics. Since the schema currently supports attendance clock-in/out and worked minutes, hours worked can be real, while overtime and breaks can initially be derived simply or kept static/dummy depending on implementation speed.

---

## Files (MANDATORY)

### Backend

#### attendance/controller/

- `AttendanceHistoryController.java`
  or extend:
- `AttendanceController.java`

#### attendance/service/

- `AttendanceHistoryService.java`
  or extend:
- `AttendanceService.java`

#### attendance/dto/

- `WeeklyAttendanceSummaryResponse.java`
- `WeeklyAttendanceLogItemResponse.java`
- `WeeklyAttendanceHistoryResponse.java`

#### attendance/repository/

- update `AttendanceRepository.java`

#### attendance/entity/

- reuse existing `Attendance.java`

#### staff/entity/

- reuse existing `StaffProfile.java`

#### staff/repository/

- reuse existing `StaffProfileRepository.java` if needed

---

### Frontend

#### features/attendance/

- `api.ts`
- `slice.ts` or RTK Query endpoints
- `types.ts`
- `pages/AttendanceHistoryPage.tsx`
- `components/AttendanceHeader.tsx`
- `components/WeeklyOverviewCards.tsx`
- `components/WeeklyLogsTimeline.tsx`
- `components/WeeklyLogCard.tsx`
- `components/BottomNav.tsx`

#### routes/

- update attendance route definitions
- ensure staff-only route protection

#### shared/api/

- update `client.ts` only if needed

#### app/layouts/ or app/router/

- update route wiring if needed

---

## Route Design

### Recommended Route

- `/staff/attendance/history`

or if this becomes the main attendance page:

- `/staff/attendance`

Recommended:

- keep current live attendance portal at `/staff/attendance`
- add this screen at `/staff/attendance/history`

### Access Rules

- only authenticated `STAFF` users can access
- `ADMIN` users cannot access this route
- unauthenticated users redirect to `/login`

### Navigation

- Attendance tab in bottom nav should route to this page if this becomes the attendance section page
- “View Full History” can be:
  - placeholder for now
  - same route
  - future expanded page

---

## Backend API Design

### GET `/api/attendance/weekly-history`

Purpose:

- return weekly attendance summary and daily log items for the logged-in staff user

#### Query Params

- `weekStart`
  - ISO date
  - if omitted, backend derives current week

#### Response

- weekStart
- weekEnd
- totalWorkedMinutes
- overtimeMinutes
- totalBreakMinutes
- logs: array of daily items

Example response:

```json
{
  "weekStart": "2026-03-24",
  "weekEnd": "2026-03-30",
  "totalWorkedMinutes": 1950,
  "overtimeMinutes": 144,
  "totalBreakMinutes": 195,
  "logs": [
    {
      "date": "2026-03-24",
      "dayLabel": "Monday, Mar 24",
      "clockInTime": "2026-03-24T08:00:00",
      "clockOutTime": "2026-03-24T16:32:00",
      "workedMinutes": 512,
      "statusLabel": "ON TIME",
      "statusType": "ON_TIME"
    }
  ]
}

Done Criteria

staff-only attendance history route exists
backend weekly history API works
hours worked summary is displayed
weekly logs are displayed
page visually matches design closely
Attendance bottom nav tab is active
loading/error/empty states are handled
admin cannot access this page
```
