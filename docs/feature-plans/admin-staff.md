# Feature: Admin Portal Staff Directory

## Goal

Implement the admin staff directory page matching the provided design, backed by APIs for listing staff, filtering by status, searching by name, and paginated results.

---

## Design Source

- `docs/designs/admin-portal/staff-directory.png`

---

## Routes

- Frontend: `/admin/staff`
- Backend API: `/api/admin/staff`

---

## Access Rules

- admin-only feature
- authenticated `ADMIN` users can access
- `STAFF` users must not access
- unauthenticated users redirect to login / receive 401

---

## Folder Requirement

### Frontend

All new staff-directory-specific frontend files must be created under:

```text
src/features/admin-portal/staff-directory
Backend

Create staff-directory backend files inside the admin/staff feature area following current backend structure.

Recommended backend package structure:

com.ibe.housekeeping.admin.staff
Scope
In Scope
admin staff directory backend API
admin staff directory frontend page
search by staff name
filter tabs:
All Personnel
On-Duty
Off-Duty
Leave
Sick
paginated table/list
total employee count
admin-only route protection
action menu placeholder column
loading/error/empty states
Out of Scope
staff create/edit/delete flows
action dropdown functionality
bulk operations
attendance editing
staff profile details page
shift reassignment from this page
export/download
Backend Implementation
Backend Goal

Provide paginated staff directory data for admin, with search and status filtering.

Backend Files (MANDATORY)
Create
backend/src/main/java/.../admin/staff/controller/AdminStaffDirectoryController.java
backend/src/main/java/.../admin/staff/service/AdminStaffDirectoryService.java
backend/src/main/java/.../admin/staff/dto/AdminStaffDirectoryResponse.java
backend/src/main/java/.../admin/staff/dto/AdminStaffDirectoryItemResponse.java
backend/src/main/java/.../admin/staff/dto/StaffDirectorySummaryResponse.java
Reuse / Update if Needed
StaffProfileRepository.java
LeaveRequestRepository.java
AttendanceRepository.java
User / StaffProfile entity mappings if fields are missing
security config only if admin route protection needs update

Keep backend changes focused on directory listing only.

Backend API Design
Endpoint

GET /api/admin/staff

Access
admin-only
protected by JWT + role-based authorization
Query Params
page default 0
size default 10
search optional
status optional
Allowed status values
ALL
ON_DUTY
OFF_DUTY
LEAVE
SICK
Response Shape

Return:

paginated items
current page
page size
total elements
total pages
summary counts
Backend DTO Structure
AdminStaffDirectoryResponse

Fields:

List<AdminStaffDirectoryItemResponse> items
int page
int size
long totalElements
int totalPages
StaffDirectorySummaryResponse summary
AdminStaffDirectoryItemResponse

Fields:

UUID staffId
String staffCode
String fullName
String email
String phone
String status
UUID userId optional
String avatarUrl optional
StaffDirectorySummaryResponse

Fields:

long totalEmployees
long onDutyCount
long offDutyCount
long leaveCount
long sickCount
Backend Data Rules
1. Staff Base Data

Use staff profile + linked user/contact data.

Each row should provide:

full name
staff id/code
email
phone
computed current status

If some fields are missing in schema:

derive from linked User / StaffProfile
if phone is unavailable, return null or empty string safely
2. Status Calculation

Each staff member should resolve to one display status for the directory.

Recommended precedence
SICK
LEAVE
ON_DUTY
OFF_DUTY
Meaning
if the staff has an approved sick leave covering today → SICK
else if the staff has another approved leave covering today → LEAVE
else use attendance / availability to determine:
ON_DUTY
OFF_DUTY
Important

Do not show multiple statuses for one staff in this table.
Return one final resolved status.

3. Search Behavior

Search should match staff name.

Recommended:

case-insensitive
partial match on full name
if practical, also support staff code
4. Filter Behavior

Filter applies after or within staff query logic depending on implementation efficiency.

ALL
return all staff
ON_DUTY
resolved status = ON_DUTY
OFF_DUTY
resolved status = OFF_DUTY
LEAVE
resolved status = LEAVE
SICK
resolved status = SICK
5. Pagination

Use backend pagination.

Default:

page size = 10

Return metadata for pagination controls.

Backend Service Responsibilities
AdminStaffDirectoryService

Responsibilities:

fetch paginated staff data
compute resolved staff status
apply search
apply status filter
compute summary counts
map rows into response DTOs
Suggested methods
AdminStaffDirectoryResponse getStaffDirectory(int page, int size, String search, String status)
String resolveStaffStatus(StaffProfile staff, LocalDate today)
StaffDirectorySummaryResponse buildSummary(LocalDate today)
Backend Repository Needs
StaffProfileRepository

Need support for:

paginated staff fetch
optional name search
linked user/contact fetch
LeaveRequestRepository

Need support for:

checking approved leave for today
distinguishing sick leave vs planned leave
AttendanceRepository

Need support for:

determining current on-duty/off-duty state for today/current status

If attendance is not the source of truth for duty state in your project, use the existing staff availability source instead.

Backend Security
controller must be admin-only
use existing JWT/role security
do not expose staff directory API to staff users
Frontend Implementation
Frontend Files (MANDATORY)
Create inside src/features/admin-portal/staff-directory
api.ts
types.ts
slice.ts or services.ts depending on current project pattern
pages/AdminStaffDirectoryPage.tsx
components/StaffDirectoryHeader.tsx
components/StaffDirectoryFilters.tsx
components/StaffDirectoryTable.tsx
components/StaffDirectoryRow.tsx
components/StaffStatusBadge.tsx
components/StaffDirectoryPagination.tsx
Update only if needed
route registration file
admin layout file if already present
store registration file if slice must be registered
shared API client if request helpers are needed
Frontend State / API
api.ts

Implement:

getAdminStaffDirectory(params)
types.ts

Mirror backend response DTOs in frontend types.

slice.ts or service state

Manage:

items
summary
page
size
totalElements
totalPages
search
status
loading
error
Frontend UI Layout Requirements
1. Overall Layout
reuse admin portal shell style from dashboard if already implemented
left sidebar
top header
page content area
desktop-first layout matching the design
2. Page Header

Render:

page title: Staff Directory
subtitle/description text
3. Filter Bar

Render:

search input with placeholder:
Search staff by name...
status filter tabs:
All Personnel
On-Duty
Off-Duty
Leave
Sick
total employee count on the right
Behavior
changing tab refetches or re-filters data
search updates results
preserve page state sensibly
reset page to 0 when search/filter changes
4. Staff Table / List

Render a table-like card layout with columns:

Personnel
Current Status
Contact Info
Actions
Personnel column

Show:

avatar placeholder/image
full name
staff code/id line
Current Status column

Show status badge with visual styling:

ON-DUTY
OFF-DUTY
LEAVE
SICK
Contact Info column

Show:

email
phone
Actions column

Show:

kebab / three-dot button
visual only for now unless existing dropdown pattern is already ready
5. Pagination

Render bottom pagination controls:

previous button
page number buttons
next button
results per page selector (default 10)
Behavior
connect to backend pagination
update page and size correctly
Loading / Error / Empty States
Loading

Show loading placeholders for:

filter bar
table rows
pagination
Error

Show clear error state with retry action.

Empty

If no staff match the filter/search:

show safe empty state message inside the table area
Routing
add frontend route /admin/staff
protect route for ADMIN only
reuse existing role guard pattern
Styling
use Tailwind
keep design close to screenshot
do not introduce unrelated design changes
status badges should match visual hierarchy:
green for on-duty
gray/blue for off-duty
amber for leave
red for sick
Suggested Implementation Sequence
create backend DTOs
create backend service
create backend controller
add/update repository methods needed for staff listing and status resolution
secure backend route
create frontend types and API layer
create frontend state layer
build page shell
build header + filter bar
build staff table and row components
build status badge component
build pagination controls
register route and protect it
handle loading/error/empty states
polish styling
Testing / Verification
Backend

Verify:

admin staff directory API returns paginated results
search works
status filters work
summary counts are correct
role protection works
Frontend

Verify:

admin can access page
staff cannot access page
search updates results
status tabs update results
table renders correctly
pagination works
loading/error/empty states work
Done Criteria
backend staff directory API exists and is admin-only
frontend staff directory page exists and matches design closely
all staff-directory-specific frontend files are under src/features/admin-portal/staff-directory
search, filter, and pagination work
summary count is shown
loading/error/empty states are handled
backend and frontend are integrated end-to-end
```
