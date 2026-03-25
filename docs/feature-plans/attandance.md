# Feature: Staff Attendance Portal

## Goal

Implement the staff-only attendance portal screen and backend attendance flow so that a logged-in `STAFF` user is redirected to the staff portal, can clock in and clock out, and the system updates attendance records and staff availability status correctly.

---

## Scope

### In Scope

- staff-only route for attendance portal
- redirect staff users to attendance portal immediately after login
- prevent admins from accessing the staff attendance portal
- bottom navigation menu for staff web view
- clock-in API
- clock-out API
- attendance record creation/update
- update `staff_profiles.availability_status`
- logout warning/confirmation for staff
- leave section UI on the same page
- static/dummy data for:
  - shift progress
  - today’s tasks

### Out of Scope

- task APIs
- shift progress API
- leave API integration
- admin attendance screen changes
- relocation trigger on clock-out
- dashboard aggregation
- notifications
- mobile app specific implementation

---

## Feature Summary

This feature delivers the main staff-facing portal page shown after login for staff users. The page includes:

- shift status card
- clock-in / clock-out controls
- static task cards for today
- leave request section UI
- bottom navigation

Backend attendance logic must:

- create a new attendance row on clock-in
- prevent invalid repeated clock-ins
- update the attendance row on clock-out
- set `staff_profiles.availability_status = ON_DUTY` on clock-in
- set `staff_profiles.availability_status = OFF_DUTY` on clock-out

Frontend auth/routing logic must:

- redirect `STAFF` users to the staff attendance portal after login
- block `ADMIN` users from opening this page
- show logout confirmation before leaving the session

---

## Files (MANDATORY)

### Backend

#### attendance/controller/

- `AttendanceController.java`

#### attendance/service/

- `AttendanceService.java`

#### attendance/dto/

- `ClockInRequest.java`
- `ClockInResponse.java`
- `ClockOutRequest.java`
- `ClockOutResponse.java`
- `AttendancePortalResponse.java` (optional, only if you want one endpoint for current status)
- `AttendanceStatusResponse.java` (optional)

#### attendance/repository/

- `AttendanceRepository.java`

#### attendance/entity/

- reuse existing `Attendance.java`

#### staff/repository/

- reuse/update `StaffProfileRepository.java`

#### staff/entity/

- reuse existing `StaffProfile.java`
- reuse existing `AvailabilityStatus.java`

#### common/exception/ or attendance/exception/

- attendance-specific exception class if needed

---

### Frontend

#### features/attendance/

- `api.ts`
- `slice.ts` or RTK Query endpoints
- `types.ts`
- `pages/StaffAttendancePage.tsx`
- `components/ShiftStatusCard.tsx`
- `components/ClockControls.tsx`
- `components/TodayTasksSection.tsx`
- `components/LeaveSection.tsx`
- `components/BottomNav.tsx`
- `components/LogoutConfirmModal.tsx`

#### features/auth/

- update login success handling in `slice.ts` or auth flow
- update auth types if needed

#### routes/

- update staff route definitions
- add staff-only attendance portal route
- add route protection / role restriction

#### shared/api/

- update `client.ts` if request helpers/interceptors need small changes

#### app/layouts/ or app/router/

- update role-based redirect logic if needed

---

## Route Design

### Staff Attendance Portal Route

Create a staff-only route such as:

- `/staff/attendance`
  or
- `/staff/portal`

Pick one route and use it consistently. Recommended:

- `/staff/attendance`

### Route Rules

- authenticated `STAFF` users can access it
- `ADMIN` users must be denied access
- unauthenticated users must be redirected to `/login`

### Login Redirect Rules

After successful login:

- if role = `STAFF` → redirect to `/staff/attendance`
- if role = `ADMIN` → redirect to admin landing page

### Access Restriction

If an admin manually enters the staff route:

- redirect to unauthorized page or admin page
- do not render the staff portal

---

## Backend API Design

### POST `/api/attendance/clock-in`

Purpose:

- start staff duty for current date/shift
- insert a row into `attendance`
- mark staff as `ON_DUTY`

#### Request

- no body if shift is derived from current profile
  or
- optional shiftId if your system needs it

Recommended:

- keep request minimal and derive staff from authenticated user

Example:

```json
{}
Response
attendanceId
staffId
shiftId
workDate
clockInTime
workedMinutes
availabilityStatus
POST /api/attendance/clock-out

Purpose:

end staff duty for current active attendance
update clock_out_time
calculate worked_minutes
mark staff as OFF_DUTY
Request
no body required in simplest version

Example:

{}
Response
attendanceId
staffId
shiftId
workDate
clockInTime
clockOutTime
workedMinutes
availabilityStatus
Optional GET /api/attendance/current

Purpose:

fetch current attendance state for logged-in staff
useful for page reload / hydration
Response
isOnDuty
currentAttendanceId
currentShift
clockInTime
workedMinutes
availabilityStatus


Backend Business Rules
Clock In Rules

On clock-in:

identify logged-in user
load linked StaffProfile
validate staff profile exists
validate staff is not already on duty with an open attendance record
determine current shift
insert attendance record
set staff_profiles.availability_status = ON_DUTY
Clock Out Rules

On clock-out:

identify logged-in user
load linked StaffProfile
find active attendance record for current date with clock_out_time IS NULL
reject if no active clock-in exists
set clock_out_time
calculate worked_minutes
set staff_profiles.availability_status = OFF_DUTY
Edge Cases (MANDATORY)
Clock-in without clock-out

Interpretation:

user tries to clock in again while already clocked in

Behavior:

reject request
return clear error:
"You are already clocked in. Please clock out before clocking in again."
Clock-out without clock-in

Behavior:

reject request
return clear error:
"No active clock-in found for today."
Missing staff profile

Behavior:

reject request with business error
Missing current shift

If your data model allows null current shift:

reject clock-in
return clear message:
"No shift assigned to this staff profile."
Multiple open attendance rows

Behavior:

treat as invalid data state
return safe error
do not create new row
Double click / repeated button click

Behavior:

frontend disables button while request is in progress
backend remains idempotent enough to reject duplicates safely
Logout while on duty

Behavior:

show confirmation modal/alert before logout
message should warn the user they are still on duty

Recommended message:

"You are currently on duty. Are you sure you want to log out?"

Note:

for now, logout warning does not have to auto clock-out unless you explicitly want that behavior later
Database Behavior
On Clock In

Insert into attendance:

staff_id
shift_id
work_date
clock_in_time
worked_minutes = 0
created_at
updated_at

Update in staff_profiles:

availability_status = ON_DUTY
updated_at
On Clock Out

Update attendance row:

clock_out_time
worked_minutes
updated_at

Update staff profile:

availability_status = OFF_DUTY
updated_at
Security and Authorization


## Done Criteria
staff login redirects to staff attendance page
admin cannot access staff attendance page
clock-in API inserts attendance row
clock-in updates staff_profiles.availability_status to ON_DUTY
clock-out updates attendance row correctly
clock-out updates staff_profiles.availability_status to OFF_DUTY
duplicate clock-in is blocked
clock-out without clock-in is blocked
logout warning appears for on-duty staff
shift progress and tasks are present with static data
leave section UI is implemented
bottom navigation is implemented
feature works end-to-end for staff flow

```
