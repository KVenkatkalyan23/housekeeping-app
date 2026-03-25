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

- `weekStart` (optional)
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
Backend Data Rules
Source of Truth

Use attendance table:

staff_id
work_date
clock_in_time
clock_out_time
worked_minutes
Summary Metrics
Hours Worked
sum worked_minutes across the week
frontend displays formatted hours like 32.5
Overtime

Initial recommended rule:

overtime = total minutes worked above expected weekly baseline

You can choose one of these approaches:

Option A (recommended for now)
static/dummy overtime until business rule finalized
Option B
derive daily overtime using shift duration
if attendance row belongs to a shift and worked_minutes > shift.duration_minutes
overtime = extra minutes beyond shift duration
weekly overtime = sum of daily overtime

Recommended if you want real backend value:

use shift duration from linked attendance shift
Breaks

Since schema has no explicit break table:

use static/dummy value for now
or
derive as:
scheduled shift minutes - worked minutes if positive
This is usually inaccurate as “breaks,” so safest approach:
keep breaks static/dummy for now unless product rules say otherwise
Log Status Labels

Initial display can be simple and derived:

Possible values:

ON TIME
LATE IN
OVERTIME

Recommended initial rules:

if clockInTime is later than shift start → LATE IN
else if workedMinutes > shift.duration_minutes → OVERTIME
else → ON TIME

If shift data is unavailable or unreliable:

backend can return simple placeholder status
or frontend can display static status for first version
Repository Requirements
AttendanceRepository

Add query support for:

all attendance rows for a staff member in a date range
ordered by work_date ASC

Suggested needs:

fetch by staff + week range
include shift relationship if used for overtime/status calculation
Service Requirements
AttendanceHistoryService

Responsibilities:

identify logged-in user
load linked staff profile
resolve requested/current week
fetch attendance records for that week
compute weekly metrics
map rows into UI-friendly response DTOs
Suggested Methods
getWeeklyHistory(UUID userId, LocalDate weekStart)
helper: resolveWeekStart(LocalDate input)
helper: formatStatus(...)
Frontend Screen Design

Design source:

docs/designs/attendance/attendance-history.png
Header

Include:

avatar/logo placeholder
page title: Attendance
subtitle: HOUSEKEEPING DEPT.
notification bell icon placeholder
Weekly Overview Section

Cards:

Hours Worked
Overtime
Breaks

Behavior:

fetch values from API when available
if overtime/breaks are static for now, still structure data so later integration is easy
Weekly Logs Section

Include:

section title
“View Full History” link/button
vertical timeline styling
one card per day

Each log card should show:

day label
clock in / clock out time range
total worked hours/minutes
attendance status label
Bottom Navigation

Tabs:

Tasks
Attendance
Leave
Profile

For now:

Attendance should be active
other tabs can route if already available, otherwise placeholder links
Frontend State Design
Attendance History State

Suggested fields:

weekStart
weekEnd
totalWorkedMinutes
overtimeMinutes
totalBreakMinutes
logs
loading
error
Weekly Log Item Type

Suggested fields:

date
dayLabel
clockInTime
clockOutTime
workedMinutes
statusLabel
statusType
API Integration Rules
attendance/api.ts

Add:

getWeeklyAttendanceHistory(weekStart?)
Loading State

Show loading skeleton or placeholder for:

overview cards
weekly log list
Error State

Show:

inline error message
or
toast + retry option
Empty State

If no attendance exists for the week:

show zero metrics
show “No attendance records for this week”
Formatting Rules
Hours Display

Convert minutes to display formats:

32.5
8h 32m
3h 15m

Use consistent formatter helpers.

Dates

Display like:

Monday, Oct 23
Time Range

Display like:

08:00 AM — 04:32 PM
Role and Access Rules
Backend
endpoint requires valid JWT
only STAFF should access weekly self-history endpoint
admin should not use this endpoint for self/staff history unless explicitly added later
Frontend
only STAFF can open attendance history page
redirect admin away from this route
UI Behavior Notes
Static vs Real Data

Real from backend:

total worked minutes
daily attendance log rows
time ranges
hours worked

Static/dummy for first version if needed:

breaks
some status labels if business rule not finalized

Recommended priority:

real weekly logs
real hours worked
simple derived overtime
static breaks if needed
Suggested Implementation Sequence
create backend weekly history DTOs
add repository query for weekly attendance
implement weekly history service
expose weekly history API
create attendance history route
implement page layout from design
connect overview cards to API
connect weekly logs list to API
implement bottom navigation active state
handle loading, error, and empty states
test staff-only access
Testing
Backend

Test:

weekly history returns rows for logged-in staff
empty week returns zero/empty response
unauthorized request returns 401
admin access blocked if restricted
totals are calculated correctly from attendance rows
Frontend

Test:

staff can access page
admin cannot access page
overview cards render
weekly logs render in correct order
empty state renders when no data
loading state renders during fetch
bottom navigation shows Attendance active
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
