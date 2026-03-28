# Feature: Admin Portal Audit Logs + Activity Log Emission

## Goal

Implement the admin audit logs page matching the provided design, backed by an activity logs API, and add backend logging emission for core system events across task, attendance, leave, and system workflows.

---

## Design Source

- `docs/designs/admin-portal/audit-logs.png`

---

- Frontend: `/admin/logs`
- Backend API list: `/api/admin/logs`

---

## Access Rules

- admin-only feature
- authenticated `ADMIN` users can access
- `STAFF` users must not access
- unauthenticated users redirect to login / receive 401

---

## Folder Requirement

### Frontend

All new audit-logs-specific frontend files must be created under:

```text
src/features/admin-portal/audit-logs
Backend

Create audit/admin backend files inside the admin/logs feature area following current backend structure.

Recommended backend package structure:

com.ibe.housekeeping.admin.logs

Create log emission support in a dedicated backend logging/audit feature area.

Recommended package structure:

com.ibe.housekeeping.activitylog
Scope
In Scope
admin audit logs backend API
admin audit logs frontend page
activity log entity/repository/service if not already present
paginated audit logs list
category filter tabs
activity log emission from core flows
event categories:
TASK
ATTENDANCE
LEAVE
SYSTEM
event codes:
TASK_ASSIGNED
TASK_COMPLETED
TASK_RELOCATED
CLOCK_IN
CLOCK_OUT
SHORTFALL_DETECTED
ALLOCATION_RUN
loading/error/empty states
admin-only route protection
Out of Scope
editing/deleting logs
advanced multi-filter search unless already easy to add
export/download
raw JSON metadata inspector modal
cross-page deep linking from logs to entities
notification center behavior
Backend Implementation
Backend Goal
Persist structured activity logs for key system events
Expose paginated admin API to read logs
Render logs in admin portal UI
Backend Files (MANDATORY)
Create / Ensure Exists for Activity Logging
backend/src/main/java/.../activitylog/entity/ActivityLog.java
backend/src/main/java/.../activitylog/repository/ActivityLogRepository.java
backend/src/main/java/.../activitylog/service/ActivityLogService.java
backend/src/main/java/.../activitylog/dto/CreateActivityLogRequest.java (optional internal DTO/helper)
backend/src/main/java/.../common/enums/EventCategory.java
backend/src/main/java/.../common/enums/Severity.java
backend/src/main/java/.../common/enums/ActorType.java
DB migration for activity_logs table if not already present
Create for Admin Logs API
backend/src/main/java/.../admin/logs/controller/AdminAuditLogsController.java
backend/src/main/java/.../admin/logs/service/AdminAuditLogsService.java
backend/src/main/java/.../admin/logs/dto/AdminAuditLogsResponse.java
backend/src/main/java/.../admin/logs/dto/AdminAuditLogItemResponse.java
Update Existing Feature Services To Emit Logs
allocation service
relocation service
attendance service
staff workboard / task completion service
shortfall detection service if present

Only update services where the listed event codes are actually emitted.

Activity Log Table Contract

Use the given schema:

ACTIVITY_LOGS
id UUID primary key
event_code VARCHAR(50) not null
event_title VARCHAR(150) not null
event_message TEXT not null
event_category VARCHAR(30) not null
severity VARCHAR(20) not null
actor_type VARCHAR(20) not null
actor_user_id UUID null
actor_name VARCHAR(150) null
target_entity_type VARCHAR(50) null
target_entity_id UUID null
related_staff_id UUID null
related_room_id UUID null
metadata JSONB null
created_at TIMESTAMPTZ not null default now()
Enums
EventCategory

Allowed values for this feature:

TASK
ATTENDANCE
LEAVE
SYSTEM
Severity

Recommended values:

SUCCESS
INFO
WARNING
ERROR
ActorType

Recommended values:

USER
SYSTEM
Event Codes To Emit
TASK
TASK_ASSIGNED
TASK_COMPLETED
TASK_RELOCATED
ATTENDANCE
CLOCK_IN
CLOCK_OUT
SYSTEM
SHORTFALL_DETECTED
ALLOCATION_RUN
LEAVE

Leave category should exist in the system, but no mandatory leave event code was specified in this request.
Do not invent extra required codes unless already useful and consistent with current implementation.

Activity Log Emission Rules
1. TASK_ASSIGNED

Emit when task allocation successfully assigns a task.

Category
TASK
Severity
SUCCESS or INFO
Suggested fields
event_code = TASK_ASSIGNED
event_title = Task Assigned
event_message = Task <room/task> assigned to <staff>
Related fields
target_entity_type = TASK
target_entity_id = taskId
related_staff_id = assignedStaffId
related_room_id = roomId
Metadata suggestion
taskType
shiftId
estimatedMinutes
priority
2. TASK_COMPLETED

Emit when staff marks a task as completed.

Category
TASK
Severity
SUCCESS
Suggested fields
target_entity_type = TASK
target_entity_id = taskId
related_staff_id = completingStaffId
related_room_id = roomId
Metadata suggestion
completedAt
taskType
shiftId
3. TASK_RELOCATED

Emit when targeted relocation reassigns an affected task to another staff member.

Category
TASK
Severity
WARNING or INFO
Suggested fields
target_entity_type = TASK
target_entity_id = taskId
related_staff_id = newStaffId
related_room_id = roomId
Metadata suggestion
oldStaffId
newStaffId
triggerReason
shiftId
4. CLOCK_IN

Emit when staff clocks in.

Category
ATTENDANCE
Severity
SUCCESS
Suggested fields
target_entity_type = ATTENDANCE
target_entity_id = attendanceId
related_staff_id = staffId
Metadata suggestion
clockInTime
workDate
shiftId
5. CLOCK_OUT

Emit when staff clocks out.

Category
ATTENDANCE
Severity
INFO or SUCCESS
Suggested fields
target_entity_type = ATTENDANCE
target_entity_id = attendanceId
related_staff_id = staffId
Metadata suggestion
clockOutTime
workedMinutes
workDate
shiftId
6. SHORTFALL_DETECTED

Emit when shortfall calculation detects required hours exceed available hours for a shift.

Category
SYSTEM
Severity
WARNING
Suggested fields
target_entity_type = SHIFT
target_entity_id = shiftId if available
Metadata suggestion
requiredHours
availableHours
deltaHours
additionalStaffRequired
shiftLabel
date
7. ALLOCATION_RUN

Emit when allocation run completes.

Category
SYSTEM
Severity
SUCCESS
Suggested fields
target_entity_type = ALLOCATION
target_entity_id optional nullable
Metadata suggestion
taskDate
assignedCount
unassignedCount
totalTasks
Logging Service Responsibilities
ActivityLogService

Responsibilities:

provide one clean method to persist logs
hide persistence details from business services
accept structured arguments
serialize metadata safely
Suggested methods
logEvent(...)
logTaskAssigned(...)
logTaskCompleted(...)
logTaskRelocated(...)
logClockIn(...)
logClockOut(...)
logShortfallDetected(...)
logAllocationRun(...)
Recommendation

Implement:

one generic internal logEvent(...)
optional typed convenience wrappers for the event codes above
Backend API Design
1. Admin Audit Logs List API
Endpoint

GET /api/admin/logs

Access
admin-only
Query Params
page default 0
size default 10
category optional
eventCode optional
severity optional

For this page design, minimum required:

category
Allowed category filter values
ALL
TASK
ATTENDANCE
LEAVE
SYSTEM
Response

Return paginated logs list including:

id
timestamp
eventCode
eventTitle
eventMessage
eventCategory
severity
actorName
actorSubtitle optional
targetLabel
targetSubLabel optional

Plus pagination metadata:

page
size
totalElements
totalPages
Backend DTO Structure
AdminAuditLogsResponse

Fields:

List<AdminAuditLogItemResponse> items
int page
int size
long totalElements
int totalPages
AdminAuditLogItemResponse

Fields:

UUID id
OffsetDateTime createdAt
String eventCode
String eventTitle
String eventMessage
EventCategory eventCategory
Severity severity
String actorName
String actorSubtitle
String targetLabel
String targetSubLabel
Backend Data Mapping Rules
1. Timestamp

Use created_at.

Render in frontend as:

date on one line
time on next line if matching design
2. Action / Event Code

Use:

eventTitle
eventCode

Example:

title: Room Assignment Change
code: TASK_RELOCATED
3. Category

Display one compact chip from event_category.

4. Actor

Use:

actor_name
plus optional subtitle:
actor role if available
or System / Automated Service

Recommended:

if actor_type = SYSTEM, show subtitle like Automated Service
5. Target

Map using:

target_entity_type
target_entity_id
optional friendly related labels in metadata

If no friendly label exists:

show target entity type
show ID sublabel
6. Severity

Display one severity badge:

SUCCESS
WARNING
ERROR
INFO
Backend Service Responsibilities
AdminAuditLogsService

Responsibilities:

fetch paginated activity logs
apply category filter
map log rows into admin DTOs
Suggested methods
AdminAuditLogsResponse getLogs(int page, int size, String category)
Backend Repository Needs
ActivityLogRepository

Need support for:

paginated fetch ordered by newest first
optional category filter
optional severity/eventCode filters if easy to add

Suggested methods:

find all ordered by createdAt desc
find by category ordered by createdAt desc
Backend Security
admin logs controller must be admin-only
use existing JWT/role security
do not expose logs API to staff users
Frontend Implementation
Frontend Files (MANDATORY)
Create inside src/features/admin-portal/audit-logs
api.ts
types.ts
slice.ts or services.ts depending on current project pattern
pages/AdminAuditLogsPage.tsx
components/AuditLogsHeader.tsx
components/AuditLogsFilters.tsx
components/AuditLogsTable.tsx
components/AuditLogsRow.tsx
components/AuditSeverityBadge.tsx
components/AuditPagination.tsx
Update only if needed
route registration file
admin layout/sidebar file to set Logs as active
store registration file if slice must be registered
shared API client if request helpers are needed
Frontend State / API
api.ts

Implement:

getAdminAuditLogs(params)
types.ts

Mirror backend response DTOs in frontend types.

slice.ts or service state

Manage:

items
page
size
totalElements
totalPages
category
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

Logs item must be highlighted as active.

3. Page Header

Render:

title: Audit Logs
subtitle/description text
4. Filter Tabs

Render:

All Actions
Operations

Because the design shows 2 tabs, map them like this:

Suggested filter mapping
All Actions -> category = ALL
Operations -> category = SYSTEM

If you prefer, you may rename backend param values independently, but UI should match the design labels.

5. Audit Logs Table

Render table-like card with columns:

Timestamp
Action (Event Code)
Category
Actor
Target
Severity
Timestamp column

Show:

date
time underneath
Action column

Show:

event title
event code underneath
Category column

Show compact category chip

Actor column

Show:

actor name
actor subtitle
Target column

Show:

target label
target sublabel
Severity column

Show styled severity badge

6. Pagination

Render:

previous button
page buttons
next button
footer text like:
Showing 1-10 of 1,248 entries

Use backend pagination metadata.

Loading / Error / Empty States
Loading

Show placeholders for:

filter tabs
log rows
pagination
Error

Show retry-capable error state.

Empty

If no logs match the selected filter:

show safe empty state in table region
Routing
add frontend route /admin/logs
protect route for ADMIN only
reuse existing role guard pattern
Styling
use Tailwind
keep design close to screenshot
do not introduce unrelated design changes
use severity colors:
green for success
amber for warning
red for error
neutral for info
use subtle category chips
Suggested Implementation Sequence
create activity log enums if missing
create activity log entity/repository/service and migration if missing
wire event emission in existing services for required event codes
create admin logs DTOs
create admin logs service
create admin logs controller
secure backend route
create frontend types and API layer
create frontend state layer
build page shell
build filter tabs
build logs table and row components
build severity badge and pagination
register route and protect it
handle loading/error/empty states
polish styling
Testing / Verification
Backend

Verify:

activity logs are persisted for required event codes
admin logs API returns newest-first paginated results
category filter works
admin-only protection works
Frontend

Verify:

admin can access page
staff cannot access page
logs render correctly
category tabs filter results
pagination works
loading/error/empty states work
Done Criteria
activity log persistence exists and supports required event codes
required event codes are emitted from relevant services
backend admin logs API exists and is admin-only
frontend audit logs page exists and matches design closely
all audit-logs-specific frontend files are under src/features/admin-portal/audit-logs
backend and frontend are integrated end-to-end
loading/error/empty states are handled
```
