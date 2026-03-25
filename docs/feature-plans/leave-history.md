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
Backend Business Rules
Source of Truth

Use leave_requests table fields:

staff_id
leave_start_date
leave_end_date
leave_type
status
requested_at
reason
Current User Resolution
identify logged-in user from JWT
map user to StaffProfile
fetch only that staff profile’s leave history
Ordering

Return history ordered by:

leave_start_date DESC
Duration Calculation

Compute:

durationDays = leave_end_date - leave_start_date + 1

Rules:

minimum duration should be 1
handle invalid stored ranges safely
do not crash on malformed data
Status Handling

Use stored status directly.
Frontend color mapping:

APPROVED → blue
DENIED → red
unknown/other → neutral
Leave Type Display

Backend can return raw enum/string and frontend formats it
or
backend can return a display label if preferred

Recommended:

backend returns raw value
frontend formats label
Repository Requirements
LeaveRequestRepository

Add or reuse query support for:

find leave history by staff id
order by leave start date descending

Suggested query:

findByStaffIdOrderByLeaveStartDateDesc(...)

If entity relationship uses StaffProfile object:

use matching derived query accordingly
Service Requirements
LeaveService / LeaveHistoryService

Responsibilities:

identify logged-in user
resolve linked staff profile
fetch leave requests for that staff
map entities to response DTOs
calculate durationDays
Suggested Method
getLeaveHistory(UUID userId)
Error Cases

Handle:

no linked staff profile found
invalid/unauthorized access
unexpected repository failure
DTO Requirements
LeaveHistoryItemResponse

Fields:

leaveRequestId
leaveType
leaveStartDate
leaveEndDate
status
durationDays
reason (optional)
requestedAt (optional)
LeaveHistoryResponse (optional)

Use only if you want wrapper structure:

items
totalCount

For first version, plain list is acceptable.

Frontend UI Requirements
Page Layout

Match the provided design closely:

light page background
top header row with avatar/logo, app name, bell icon
large page title: Leave History
short descriptive subtitle
stacked leave cards
floating plus button in bottom-right
bottom navigation dock
Header Section

Show:

avatar/logo placeholder
title: Housekeeping
bell icon placeholder

Bell is visual only for now.

Title Section

Show:

Leave History
subtitle:
View your past time off applications and their status.
Leave History List

Display cards vertically with spacing.
Each card shows:

circular icon area on left
leave type title
start date
right-aligned status
duration text below/right
Floating Action Button

Show blue rounded button with plus icon.
Behavior:

if leave apply page exists, navigate there
otherwise placeholder onClick or disabled future action
Bottom Navigation

Items:

Tasks
Attendance
Leave
Profile

Behavior:

Leave tab active/highlighted
other tabs can navigate if routes exist
otherwise keep as placeholders
Frontend State Design
Leave History State

Suggested fields:

items
loading
error
Leave History Item Type

Suggested fields:

leaveRequestId
leaveType
leaveStartDate
leaveEndDate
status
durationDays
Frontend API Integration
leave/api.ts

Implement:

getLeaveHistory()
Request Rules
use shared API client
attach bearer token automatically
rely on existing auth interceptor
Success Handling
populate leave history list state
render cards in descending order
Error Handling
show inline error or toast
provide simple retry action if desired
Empty Handling

If no leave history:

show friendly empty state
keep page layout intact
floating action button remains visible
Formatting Rules
Date Display

Format dates like:

Sep 12, 2023
Duration Display

Format:

1 Day
2 Days
3 Days
Leave Type Label Formatting

Convert raw values to readable labels
Examples:

CASUAL_LEAVE → Casual Leave
SICK_LEAVE → Sick Leave
FAMILY_CARE → Family Care
Status Display

Examples:

APPROVED
DENIED

Use color-coded text matching design.

Access and Security Rules
Backend
endpoint requires valid JWT
only STAFF users can access own leave history
do not expose other staff records
admin must not use this endpoint unless explicitly supported later
Frontend
route must be protected
admin users redirected away from this page
unauthenticated users redirected to login
Static / Placeholder Parts

Keep static for now:

bell icon behavior
leave icon graphics if exact assets not available
floating action button action if apply page not ready
non-leave bottom nav destinations if not built

Use real backend data for:

leave items
dates
status
duration
Suggested Implementation Sequence
add/update backend repository query
implement leave history service method
add leave history response DTO
expose GET /api/leave/history
add/update staff-only leave route
implement LeaveHistoryPage layout from design
implement leave history card/list components
integrate API with frontend state
add loading/error/empty states
add floating action button
add bottom nav with Leave active
test role protection and rendering
Testing
Backend
returns leave history for logged-in staff only
orders by latest first
calculates duration correctly
rejects unauthorized requests
blocks admin if staff-only restriction is applied
Frontend
staff can access route
admin cannot access route
leave cards render correctly
loading state renders while fetching
empty state renders when no data
error state renders on API failure
Leave tab shows as active
floating action button renders
Done Criteria
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
