Let';s implment this

# Feature: Admin Portal Attendance Logs

## Goal

Implement the admin attendance logs page matching the provided design, backed by APIs for attendance summary metrics and a paginated detailed attendance log table with date range filtering.

---

## Design Source

- `docs/designs/admin-portal/attendance-logs.png`

---

## Routes

- Frontend: `/admin/attendance`
- Backend API summary: `/api/admin/attendance/summary`
- Backend API logs: `/api/admin/attendance/logs`

---

## Access Rules

- admin-only feature
- authenticated `ADMIN` users can access
- `STAFF` users must not access
- unauthenticated users redirect to login / receive 401

---

## Folder Requirement

### Frontend

All new attendance-logs-specific frontend files must be created under:

```text
src/features/admin-portal/attendance-logs
Backend

Create attendance admin backend files inside the admin/attendance feature area following current backend structure.

Recommended backend package structure:

com.ibe.housekeeping.admin.attendance
Scope
In Scope
admin attendance summary backend API
admin attendance logs backend API
admin attendance logs frontend page
date range filter
summary cards:
active today
average shift length
late check-ins
paginated detailed attendance table
loading/error/empty states
admin-only route protection
Out of Scope
editing attendance from this page
approving overtime from this page
exporting CSV/PDF
attendance correction workflow
detailed staff profile page navigation
notification behavior
action menu behavior beyond placeholder
Backend Implementation
Backend Goal

Provide admin-facing APIs to:

fetch attendance summary metrics for a selected date range
fetch paginated attendance log rows for a selected date range
Backend Files (MANDATORY)
Create
backend/src/main/java/.../admin/attendance/controller/AdminAttendanceController.java
backend/src/main/java/.../admin/attendance/service/AdminAttendanceService.java
backend/src/main/java/.../admin/attendance/dto/AdminAttendanceSummaryResponse.java
backend/src/main/java/.../admin/attendance/dto/AdminAttendanceLogListResponse.java
backend/src/main/java/.../admin/attendance/dto/AdminAttendanceLogItemResponse.java
Reuse / Update if Needed
AttendanceRepository.java
StaffProfileRepository.java
ShiftRepository.java
security config only if admin route protection needs update

Keep backend changes focused on read APIs for this page.

Backend API Design
1. Attendance Summary API
Endpoint

GET /api/admin/attendance/summary

Access
admin-only
Query Params
fromDate required
toDate required
Response

Return top summary values needed for cards:

activeTodayCount
activeTodayDeltaPercent
averageShiftLengthHours
averageShiftLengthLabel
lateCheckInsCount
lateCheckInsLabel
Notes
if delta vs last week is not available yet, return 0 or safe placeholder
label fields can be returned by backend or set by frontend if simpler
2. Attendance Logs API
Endpoint

GET /api/admin/attendance/logs

Access
admin-only
Query Params
fromDate required
toDate required
page default 0
size default 10

Optional:

search
Response

Return paginated list including:

attendanceId
staffId
staffName
staffRoleLabel
staffInitials
workDate
clockInTime
clockOutTime
totalWorkedHours
shiftReference
lateCheckIn
lateCheckInLabel
overtimeFlag
statusTag

Plus pagination metadata:

page
size
totalElements
totalPages
Backend DTO Structure
AdminAttendanceSummaryResponse

Fields:

LocalDate fromDate
LocalDate toDate
long activeTodayCount
double activeTodayDeltaPercent
double averageShiftLengthHours
String averageShiftLengthLabel
long lateCheckInsCount
String lateCheckInsLabel
AdminAttendanceLogListResponse

Fields:

List<AdminAttendanceLogItemResponse> items
int page
int size
long totalElements
int totalPages
AdminAttendanceLogItemResponse

Fields:

UUID attendanceId
UUID staffId
String staffName
String staffRoleLabel
String staffInitials
LocalDate workDate
String clockInTime
String clockOutTime
double totalWorkedHours
String shiftReference
boolean lateCheckIn
String lateCheckInLabel
boolean overtimeFlag
String statusTag
Backend Data Rules
1. Selected Date Range

Use:

fromDate
toDate

Validation:

both required
toDate >= fromDate
2. Active Today Count

Recommended definition:

number of attendance records on toDate

If your business wants a stricter "currently active" definition, keep it consistent across summary and UI.

3. Average Shift Length

Calculate from attendance records in selected date range.

Formula

For rows with both clock-in and clock-out:

workedMinutes = difference(clockOutTime, clockInTime)
averageShiftLengthHours = average(workedMinutes) / 60

Round reasonably for UI.

4. Late Check-Ins

A row is late if:

actual clock-in time is later than expected shift start time

This requires:

attendance linked to shift
or
shift reference available from attendance/staff

Count all late check-ins in the selected date range.

5. Detailed Attendance Rows

Each row should show:

staff identity
role label
work date
clock-in time
clock-out time
total worked hours
shift reference label
late/overtime indicators if applicable
Total Worked

Use stored worked minutes if available.
Otherwise derive from clock-in/out safely.

Shift Reference

Use:

shift code
or shift name
or a composed label already available in the system

Examples:

MORNING-SHIFT-A
LATE-ADJUST-1
OVERTIME-REQ
Backend Service Responsibilities
AdminAttendanceService

Responsibilities:

validate date range
compute summary metrics
fetch paginated attendance rows
map entities to admin DTOs
compute late/overtime flags and labels where needed
Suggested methods
AdminAttendanceSummaryResponse getAttendanceSummary(LocalDate fromDate, LocalDate toDate)
AdminAttendanceLogListResponse getAttendanceLogs(LocalDate fromDate, LocalDate toDate, int page, int size, String search)
Backend Repository Needs
AttendanceRepository

Need support for:

count attendance by date/date range
fetch paginated attendance rows in date range
compute or retrieve worked minutes
join staff and shift info
count late check-ins or fetch data needed to compute them
StaffProfileRepository

Optional support if role label or profile data is not directly accessible through attendance relationships

ShiftRepository

Optional support if late-check logic requires shift lookup beyond attendance relation

Backend Security
controller must be admin-only
use existing JWT/role security
do not expose these APIs to staff users
Frontend Implementation
Frontend Files (MANDATORY)
Create inside src/features/admin-portal/attendance-logs
api.ts
types.ts
slice.ts or services.ts
pages/AdminAttendanceLogsPage.tsx
components/AttendanceLogsHeader.tsx
components/AttendanceDateRangeFilter.tsx
components/AttendanceSummaryCards.tsx
components/AttendanceSummaryCard.tsx
components/AttendanceLogsTable.tsx
components/AttendanceLogsRow.tsx
components/AttendancePagination.tsx
Update only if needed
route registration file
admin layout/sidebar file to set Attendance as active
store registration file if slice must be registered
shared API client if request helpers are needed
Frontend State / API
api.ts

Implement:

getAdminAttendanceSummary(fromDate, toDate)
getAdminAttendanceLogs(params)
types.ts

Mirror backend response DTOs in frontend types.

slice.ts or service state

Manage:

summary data
log items
fromDate
toDate
page
size
total elements/pages
loading
error
Frontend UI Layout Requirements
1. Overall Layout
reuse admin shell style if already implemented
left sidebar
top header
content area matching design
desktop-first layout
2. Sidebar

Attendance item must be highlighted as active.

3. Page Header

Render:

title: Attendance Logs
subtitle/description text
4. Date Range Filter

Render top-right date range filter with:

from date input
to date input
Behavior
changing either date updates summary + table
validate toDate >= fromDate
default range can be current week or last 7 days
5. Summary Cards

Render 3 cards:

Active Today
Avg. Shift Length
Late Check-Ins
Card Data
Active Today
active count
delta vs last week text if available
Avg. Shift Length
average shift length in hours
optional status pill like Consistent
Late Check-Ins
late check-in count
optional pill like Requires Review
6. Detailed Attendance Log Table

Render table-like card with columns:

Staff Name
Date
Clock-In Time
Clock-Out Time
Total Worked
Shift Reference
Staff Name column

Show:

initials/avatar circle
full name
role label beneath
Clock-In Time column

Show:

time
late badge/label when late
Clock-Out Time column

Show:

time
Total Worked column

Show:

hours value like 8.5 hrs
Shift Reference column

Show compact chip/tag

Top-right action icon

Can be visual only for now

7. Pagination

Render:

previous button
page numbers
next button
footer text like:
Showing 1 - 5 of 128 entries

Use backend pagination metadata.

Loading / Error / Empty States
Loading

Show placeholders for:

summary cards
date range filter
table rows
Error

Show retry-capable error state.

Empty

If no attendance logs match the selected range:

show safe empty state in table region
Routing
add frontend route /admin/attendance
protect route for ADMIN only
reuse existing role guard pattern
Styling
use Tailwind
keep design close to screenshot
use clear visual cues:
green for normal/on-time
red for late/review
neutral chips for shift reference
Suggested Implementation Sequence
create backend DTOs
create backend service
create backend controller
add/update repository methods for summary and logs APIs
secure backend routes
create frontend types and API layer
create frontend state layer
build page shell
build date range filter
build summary cards
build attendance log table and rows
build pagination
register route and protect it
handle loading/error/empty states
polish styling to match design
Testing / Verification
Backend

Verify:

summary API returns correct metrics
logs API returns paginated results for date range
late check-in logic works
total worked hours are correct
admin-only protection works
Frontend

Verify:

admin can access page
staff cannot access page
date range filter updates summary and table
summary cards render correctly
attendance rows render correctly
pagination works
loading/error/empty states work
Done Criteria
backend summary and logs APIs exist and are admin-only
frontend attendance logs page exists and matches design closely
all attendance-logs-specific frontend files are under src/features/admin-portal/attendance-logs
date range filtering works
summary cards and table render correctly
pagination works
backend and frontend are integrated end-to-end
loading/error/empty states are handled
```
