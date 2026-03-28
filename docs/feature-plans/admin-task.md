# Feature: Admin Portal Task Allocation Page

## Goal

Implement the admin task allocation page matching the provided design, backed by APIs for task allocation summary metrics and paginated allocated task listing with filters.

---

## Design Source

- `docs/designs/admin-portal/admin-task.png`

---

## Routes

- Frontend: `/admin/tasks`
- Backend API summary: `/api/admin/tasks/allocation/summary`
- Backend API list: `/api/admin/tasks/allocation`
- Backend API trigger allocation (reuse existing if already implemented): `/api/allocation/run`

---

## Access Rules

- admin-only feature
- authenticated `ADMIN` users can access
- `STAFF` users must not access
- unauthenticated users redirect to login / receive 401

---

## Folder Requirement

### Frontend

All new task-allocation-page-specific frontend files must be created under:

```text
src/features/admin-portal/task-allocation
Backend

Create task-allocation admin backend files inside the admin/tasks feature area following current backend structure.

Recommended backend package structure:

com.ibe.housekeeping.admin.taskallocation
Scope
In Scope
admin task allocation page backend APIs
admin task allocation frontend page
allocation summary cards
searchable/filterable paginated task table
filter by task type
filter by status
search by assigned staff name
pagination
integration with existing allocation result data
reuse existing allocation run API if already available
admin-only route protection
loading/error/empty states
Out of Scope
implementing a new allocation algorithm
manual reassignment from this page
relocation from this page
task edit flow
task completion from admin view
bulk actions
export/download
detailed task drawer/modal behavior
Backend Implementation
Backend Goal

Provide admin-facing APIs to:

fetch allocation summary metrics
fetch paginated allocated tasks with filters
optionally reuse existing allocation trigger endpoint
Backend Files (MANDATORY)
Create
backend/src/main/java/.../admin/taskallocation/controller/AdminTaskAllocationController.java
backend/src/main/java/.../admin/taskallocation/service/AdminTaskAllocationService.java
backend/src/main/java/.../admin/taskallocation/dto/AdminTaskAllocationSummaryResponse.java
backend/src/main/java/.../admin/taskallocation/dto/AdminAllocatedTaskListResponse.java
backend/src/main/java/.../admin/taskallocation/dto/AdminAllocatedTaskItemResponse.java
Reuse / Update if Needed
CleaningTaskRepository.java
TaskAssignmentRepository.java
StaffProfileRepository.java
RoomRepository.java
ShiftRepository.java
security config only if admin route protection needs update

Keep backend changes focused on read APIs for this page.

Backend API Design
1. Allocation Summary API
Endpoint

GET /api/admin/tasks/allocation/summary

Access
admin-only
Query Params
taskDate optional
if omitted, use current date
Response

Return top summary values needed for cards:

totalActiveTasks
inProgressCount
checkoutTaskCount
checkoutAssignedCount
checkoutPendingCount
dailyTaskCount
dailyAssignedCount
dailyPendingCount
vacantTaskCount
vacantAssignedCount
vacantPendingCount
Notes
“active tasks” means tasks not completed/cancelled for the selected date
“pending” means not completed; use project’s actual status semantics
“assigned” means task has an assignment row or assigned staff depending on your model
2. Allocation Task List API
Endpoint

GET /api/admin/tasks/allocation

Access
admin-only
Query Params
taskDate optional
page default 0
size default 10
search optional
taskType optional
status optional
Filter behavior
search filters by assigned staff name
taskType filters task type
status filters by task status
Response

Return paginated list including:

taskId
roomId
roomNumber
floorLabel optional
roomTypeLabel optional
taskType
assignedStaffId nullable
assignedStaffName nullable
status
priorityLabel
estimatedMinutes
shiftName optional

Plus pagination metadata:

page
size
totalElements
totalPages
Backend DTO Structure
AdminTaskAllocationSummaryResponse

Fields:

LocalDate taskDate
long totalActiveTasks
long inProgressCount
long checkoutTaskCount
long checkoutAssignedCount
long checkoutPendingCount
long dailyTaskCount
long dailyAssignedCount
long dailyPendingCount
long vacantTaskCount
long vacantAssignedCount
long vacantPendingCount
AdminAllocatedTaskListResponse

Fields:

List<AdminAllocatedTaskItemResponse> items
int page
int size
long totalElements
int totalPages
AdminAllocatedTaskItemResponse

Fields:

UUID taskId
UUID roomId
String roomNumber
String floorLabel
String roomTypeLabel
TaskType taskType
UUID assignedStaffId
String assignedStaffName
TaskStatus status
String priorityLabel
Integer estimatedMinutes
String shiftName
Backend Data Rules
1. Selected Date

Use taskDate if provided, otherwise default to today.

2. Summary Calculations
Total Active Tasks

Count tasks for selected date where status is not:

COMPLETED
CANCELLED
In Progress Count

Count tasks with status:

IN_PROGRESS
or your equivalent status if used
Type-wise Counts

Compute separately for:

DEEP_CLEAN as checkout
DAILY_CLEAN as daily
VACANT_CLEAN as vacant
Assigned Count

A task is considered assigned if:

it has a TaskAssignment
or
its status is in an assigned state depending on your current model
Pending Count

For this page:

pending means not completed
if you distinguish ASSIGNED vs PENDING, reflect the real status correctly
3. Task List Mapping
Room Info

Show:

room number
optional floor label
optional room type label

If those fields are not directly stored:

derive from room entity
safely return null/empty if unavailable
Assigned Staff

Show assigned staff name if assignment exists.
If not assigned:

return null or placeholder-safe values
Priority Label

Convert internal priority/order into UI-friendly label:

1 -> HIGH
2 -> MEDIUM
3 -> LOW

Use current project priority mapping consistently.

Backend Service Responsibilities
AdminTaskAllocationService

Responsibilities:

fetch summary data for selected date
fetch paginated task allocation table data
apply search/filter logic
map entities to admin DTOs
Suggested methods
AdminTaskAllocationSummaryResponse getAllocationSummary(LocalDate taskDate)
AdminAllocatedTaskListResponse getAllocatedTasks(LocalDate taskDate, int page, int size, String search, String taskType, String status)
Backend Repository Needs
CleaningTaskRepository

Need support for:

count tasks by date/status/type
fetch paginated tasks by date with optional filters
join room info if needed
TaskAssignmentRepository

Need support for:

join assigned staff name for task list
count assigned tasks
fetch tasks by assigned staff search if practical
StaffProfileRepository

Optional support for staff-name-based filtering if needed

RoomRepository

Optional support if room metadata is not already available through task relation

Backend Security
controller must be admin-only
use existing JWT/role security
do not expose these APIs to staff users
Frontend Implementation
Frontend Files (MANDATORY)
Create inside src/features/admin-portal/task-allocation
api.ts
types.ts
slice.ts or services.ts depending on current project pattern
pages/AdminTaskAllocationPage.tsx
components/TaskAllocationHeader.tsx
components/TaskAllocationSummaryCards.tsx
components/TaskAllocationSummaryCard.tsx
components/TaskAllocationFilters.tsx
components/TaskAllocationTable.tsx
components/TaskAllocationRow.tsx
components/TaskPriorityBadge.tsx
components/TaskStatusBadge.tsx
components/TaskAllocationPagination.tsx
Update only if needed
route registration file
admin layout/sidebar file to set Tasks as active
store registration file if slice must be registered
shared API client if request helpers are needed
Frontend State / API
api.ts

Implement:

getAdminTaskAllocationSummary(taskDate?)
getAdminTaskAllocationList(params)
types.ts

Mirror backend response DTOs in frontend types.

slice.ts or service state

Manage:

summary data
list items
page
size
total elements/pages
current filters
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

Tasks item must be highlighted as active.

Other items can reuse existing admin portal shell if already available.

3. Page Header

Render:

title: Task Allocation
subtitle/description text
4. Summary Cards

Render 4 cards:

Total Active
Checkout Tasks
Daily Cleaning
Vacant Cleaning
Card Data
Total Active
total active task count
secondary line: in progress count
Checkout Tasks
total checkout task count
assigned + pending breakdown
Daily Cleaning
total daily task count
assigned + pending breakdown
Vacant Cleaning
total vacant task count
assigned + pending breakdown
5. Filter Bar

Render:

search input with placeholder:
Search assigned staff...
task type dropdown
status dropdown
reset action
Behavior
search filters by assigned staff name
dropdowns filter task list
reset clears search + filters and reloads first page
page resets to 0 when filters change
6. Task Table

Render table-like card with columns:

Room Number
Task Type
Assigned Staff
Status
Priority
Action
Room Number column

Show:

room number
optional floor and room type line
Task Type column

Show a small pill/badge like:

Checkout
Daily
Vacant
Assigned Staff column

Show avatar placeholder/image if available and assigned staff name

Status column

Show status icon/text badge:

Pending
In Progress
Completed
Priority column

Show:

HIGH
MEDIUM
LOW
Action column

Show placeholder action icon/button only if needed for matching design.
It can be non-functional for now.

7. Pagination

Render:

previous button
next button
optional page text
footer text:
Showing X of Y tasks

Use backend pagination metadata.

Loading / Error / Empty States
Loading

Show placeholders for:

summary cards
filter bar
table rows
Error

Show retry-capable error state.

Empty

If no tasks match:

render safe empty state in table region
Routing
add frontend route /admin/tasks
protect route for ADMIN only
reuse existing role guard pattern
Styling
use Tailwind
keep design close to screenshot
do not introduce unrelated design changes
maintain clean spacing and card/table hierarchy
use subtle badges for status and priority
Suggested Implementation Sequence
create backend DTOs
create backend service
create backend controller
add/update repository methods for summary and list APIs
secure backend routes
create frontend types and API layer
create frontend state layer
build page shell
build summary cards
build filter bar
build task table and row components
build pagination
register route and protect it
handle loading/error/empty states
polish styling to match design
Testing / Verification
Backend

Verify:

summary API returns correct counts
list API returns paginated filtered results
search by assigned staff works
task type filter works
status filter works
admin-only protection works
Frontend

Verify:

admin can access page
staff cannot access page
summary cards render correct values
filters update list correctly
reset works
table renders correctly
pagination works
loading/error/empty states work
Done Criteria
backend summary and list APIs exist and are admin-only
frontend task allocation page exists and matches design closely
all task-allocation-page-specific frontend files are under src/features/admin-portal/task-allocation
summary cards, filters, table, and pagination work
backend and frontend are integrated end-to-end
loading/error/empty states are handled
```
