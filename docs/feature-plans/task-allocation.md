# Feature: Task Allocation

## Goal

Implement task allocation so generated cleaning tasks are assigned fairly and correctly while allowing staff to split their 4-hour workday across morning and afternoon windows. Tasks are atomic: once a staff member starts a task, that task must be completed by the same staff member.

---

## Scope

- allocate generated cleaning tasks to staff
- allow staff to work across morning and afternoon windows on the same day
- keep tasks atomic (no task splitting across staff)
- respect total daily capacity per staff = 240 minutes
- decide task shift during allocation
- create task assignments
- prefer assigning staff into their preferred shift when possible
- allow overriding preferred shift when shortage exists
- expose allocation API
- expose API to fetch allocation result

---

## In Scope

- backend allocation service
- backend allocation API
- backend fetch assignments API
- backend fairness logic
- backend time-window-aware allocation logic
- preferred-shift-aware candidate selection
- frontend screen to trigger allocation
- frontend allocation result view
- loading/error/empty states

---

## Out of Scope

- manual reassignment
- relocation
- leave approval flow
- dashboard analytics
- payroll
- overtime approvals

---

## Core Clarification

- staff are not restricted to exactly one full shift block
- staff can split their 4-hour daily capacity across morning and afternoon windows
- tasks are atomic
- if a staff member starts a task, assume they complete it
- no task can be split between multiple staff

---

## Required Data Model Change

### Staff Profile Field Change

Change:

- `current_shift_id`

To:

- `preferred_shift_id`

### Meaning

- `preferred_shift_id` is a soft preference, not a hard allocation rule
- system should first try to allocate staff into work that matches their preferred shift
- system may override the preferred shift when needed to avoid shortage/unassigned tasks

### Files Affected by This Change

#### backend/staff/entity/

- `StaffProfile.java`

#### backend/staff/repository/

- `StaffProfileRepository.java`

#### backend resources / migrations

- add migration to rename or replace `current_shift_id` with `preferred_shift_id`

#### any DTOs using current shift

- update to preferred shift naming if applicable

---

## Core Business Rules

### Cleaning Task Types

- `DEEP_CLEAN` = 120 minutes
- `DAILY_CLEAN` = 30 minutes
- `VACANT_CLEAN` = 15 minutes

### Hotel Timing Rules

- checkout time = `10:00`
- check-in time = `15:00`

### Working Windows

- Morning window: `08:00 - 12:00`
- Afternoon window: `13:00 - 17:00`

### Deep Clean Allowed Windows

- Morning deep-clean window: `10:00 - 12:00`
- Afternoon deep-clean window: `13:00 - 15:00`

### Remaining Task Windows

- Morning general window: `08:00 - 10:00`
- Afternoon general window: `15:00 - 17:00`

### Total Staff Capacity

- each staff member has total daily capacity = `240 minutes`

### Atomic Task Rule

- one task must be assigned to one staff member only
- no task splitting across staff

### Preferred Shift Rule

- staff should be allocated to work matching their `preferred_shift_id` whenever possible
- preferred shift is a soft preference
- if demand cannot be met using preferred-shift-aligned staff, system may allocate staff into the other shift/window

### Shortage Override Rule

Preferred shift may be overridden when:

- there is no feasible preferred-shift candidate for a task
- required work would otherwise remain unassigned
- there is insufficient capacity in the preferred shift pool

### Fairness Rule

When multiple staff are eligible, choose in this order:

1. preferred-shift match first, if feasible
2. lower currently allocated minutes for the day
3. lower historical total minutes worked
4. fewer assigned tasks today
5. stable tie-breaker

---

## Allocation Model

## High-Level Model

For a given `taskDate`:

1. fetch all generated unassigned tasks
2. fetch all eligible staff
3. allocate deep clean tasks first
4. allocate remaining daily/vacant tasks next
5. prefer candidates whose preferred shift matches the selected window
6. if shortage exists, allow cross-preference allocation
7. assign each task to one staff member and a valid window
8. update task with `shift_id`
9. create `TaskAssignment`
10. leave tasks unassigned if no feasible placement exists

---

## Important Feasibility Rules

### Deep Clean Feasibility

A deep clean takes 120 minutes.
It can fit in:

- morning deep-clean window: `10:00 - 12:00`
- afternoon deep-clean window: `13:00 - 15:00`

### Daily / Vacant Feasibility

These can be assigned into:

- `08:00 - 10:00`
- `15:00 - 17:00`

### Daily Capacity Rule

For each staff member:

- total assigned minutes across all windows for that day must be `<= 240`

---

## Internal Time Buckets

### Bucket A

- `08:00 - 10:00`
- duration = `120`
- allowed task types:
  - `DAILY_CLEAN`
  - `VACANT_CLEAN`
- shift = Morning

### Bucket B

- `10:00 - 12:00`
- duration = `120`
- allowed task types:
  - `DEEP_CLEAN`
- shift = Morning

### Bucket C

- `13:00 - 15:00`
- duration = `120`
- allowed task types:
  - `DEEP_CLEAN`
- shift = Afternoon

### Bucket D

- `15:00 - 17:00`
- duration = `120`
- allowed task types:
  - `DAILY_CLEAN`
  - `VACANT_CLEAN`
- shift = Afternoon

---

## Files (MANDATORY)

### Backend

#### allocation/controller/

- `TaskAllocationController.java`

#### allocation/service/

- `TaskAllocationService.java`

#### allocation/dto/

- `RunAllocationRequest.java`
- `RunAllocationResponse.java`
- `TaskAssignmentItemResponse.java`
- `UnassignedTaskItemResponse.java`

#### allocation/repository/

- `TaskAssignmentRepository.java`

#### allocation/entity/

- reuse existing `TaskAssignment.java`

#### task/repository/

- `CleaningTaskRepository.java`

#### task/entity/

- reuse existing `CleaningTask.java`
- reuse existing `TaskType.java`
- reuse existing `TaskStatus.java`

#### staff/repository/

- `StaffProfileRepository.java`

#### staff/entity/

- reuse existing `StaffProfile.java`
- update field from `currentShift` to `preferredShift`
- reuse fairness field:
  - `totalMinutesWorked`

#### shift/repository/

- `ShiftRepository.java`

#### leave/repository/

- `LeaveRequestRepository.java` if excluding staff on leave for selected date

#### migrations/

- migration to replace `current_shift_id` with `preferred_shift_id`

---

### Frontend

#### features/allocation/

- `api.ts`
- `slice.ts` or RTK Query endpoints
- `types.ts`
- `pages/TaskAllocationPage.tsx`
- `components/AllocationForm.tsx`
- `components/AllocationSummary.tsx`
- `components/AssignmentList.tsx`
- `components/UnassignedTasksList.tsx`

#### routes/

- update admin allocation routes

#### shared/api/

- reuse/update `client.ts` only if needed

---

## Data Model Expectations

### CleaningTask

Must support:

- `id`
- `room`
- `taskDate`
- `shift` nullable before allocation
- `taskType`
- `priorityOrder`
- `estimatedMinutes`
- `taskStatus`
- `sourceStay`
- `createdAt`
- `updatedAt`

### TaskAssignment

Must support:

- `id`
- `cleaningTask`
- `staff`
- `createdAt`
- `updatedAt`

### StaffProfile

Must support:

- `id`
- `availabilityStatus`
- `preferredShift`
- `totalMinutesWorked`

### Important Rule

During allocation:

- update `cleaning_tasks.shift_id`
- create corresponding `task_assignments` row

---

## Backend API Design

### POST `/api/allocation/run`

Purpose:

- run allocation for a selected date

#### Request

```json
{
  "taskDate": "2026-03-26"
}
Response

Should include:

taskDate
totalTasks
assignedTasks
unassignedTasks
assignments
unassigned
GET /api/allocation

Purpose:

fetch allocation results for a selected date
Query Params
taskDate required
Task Ordering Rules
Phase 1: Deep Clean

Allocate all DEEP_CLEAN tasks first.

Phase 2: Daily/Vacant

Allocate remaining:

DAILY_CLEAN
then VACANT_CLEAN
Staff Eligibility Rules

A staff member is eligible if:

active staff profile exists
available for work
not on leave for selected date
has enough remaining total daily capacity
has enough capacity in a valid bucket for the task type
Staff Load Tracking

Track in memory during allocation:

Per Staff
totalAllocatedMinutesForDay
allocatedTaskCountForDay
bucketACapacityUsed
bucketBCapacityUsed
bucketCCapacityUsed
bucketDCapacityUsed
Bucket Capacity Limits
A max = 120
B max = 120
C max = 120
D max = 120
Total Daily Limit
total across all buckets must not exceed 240
Preferred Shift Aware Placement Rules
Preferred Shift Matching

For each feasible candidate, determine whether the chosen bucket matches preferred shift:

Morning preferred shift matches:
bucket A
bucket B
Afternoon preferred shift matches:
bucket C
bucket D
Selection Strategy

For each task:

build all feasible candidate + bucket combinations
split them into:
preferred-shift-matching candidates
non-matching candidates
if preferred-shift-matching candidates exist, choose from them first
if none exist and task would otherwise remain unassigned, use non-matching candidates
Why

This ensures:

staff preference is respected where possible
system can still override preference to avoid shortage
Candidate Selection Rules

For each task:

generate feasible candidate staff + bucket combinations
discard any candidate that breaks bucket capacity or daily capacity
prefer candidates whose preferred shift matches the chosen bucket
then sort by:
lowest total allocated minutes today
lowest historical total minutes worked
fewest tasks assigned today
stable tie-breaker
choose best candidate
Unassigned Task Rules

Leave a task unassigned if:

no eligible staff can fit it in a valid bucket
no valid bucket remains
all staff are at 240 daily minutes

Recommended:

do not set shift_id if task remains unassigned
Repository Requirements
CleaningTaskRepository

Need:

fetch unassigned tasks by date
fetch allocated tasks by date
StaffProfileRepository

Need:

fetch eligible staff
include preferred_shift_id
include fairness field
optionally exclude leave/sick/off-duty
TaskAssignmentRepository

Need:

save assignments
fetch assignments by date
ShiftRepository

Need:

resolve morning shift id
resolve afternoon shift id
LeaveRequestRepository

Need:

exclude staff on leave for selected date if leave feature exists
Backend Validation
taskDate required
task estimated minutes must be valid
task type must be known
staff must exist
no staff may exceed 240 total minutes
preferred shift is soft, not mandatory
Error Handling

Handle:

invalid date
missing shift records
bad task data
repository failure
duplicate assignment attempt

Recommended:

continue allocation for valid tasks when one task is invalid
return assigned/unassigned summary
Frontend UI Requirements
Allocation Page

Implement:

page title
date picker
run allocation button
allocation summary
assigned tasks list
unassigned tasks list
Assignment List

Show:

room number
task type
staff name
estimated minutes
assigned shift
whether preferred shift was respected (optional badge)
UI States

Handle:

loading during allocation
loading during fetch
empty state
error state
success feedback
Frontend API Integration
allocation/api.ts

Implement:

runAllocation(taskDate)
getAllocation(taskDate)
Suggested Implementation Sequence
add migration to rename/replace current_shift_id with preferred_shift_id
update StaffProfile entity and repository
confirm nullable shift_id on generated tasks
add/confirm fairness field on staff profile
resolve morning and afternoon shift ids
implement staff bucket model in allocation service
implement preferred-shift-aware candidate selection
implement shortage override logic
implement deep-clean allocation first pass
implement daily/vacant allocation second pass
set shift_id based on chosen bucket
create TaskAssignment rows
expose POST allocation API
expose GET allocation result API
implement frontend allocation page
integrate and test
Edge Cases
Preferred Shift Enough
matching preferred-shift candidates exist
use them first
Preferred Shift Shortage
no feasible preferred-shift candidate exists
use non-matching candidate to avoid unassigned task
Exact Fit
staff has exactly enough room in bucket/daily capacity
assign successfully
Deep Clean Overflow
more deep cleans than valid deep-clean capacity
leave overflow unassigned
Daily/Vacant Overflow
remaining general bucket capacity exhausted
leave overflow unassigned
Mixed Day
one staff can take morning + afternoon tasks
total must still be <= 240
No Staff
all tasks unassigned
Leave Conflict
exclude leave staff
Duplicate Assignment
same task cannot get multiple assignments
Testing
Backend

Test:

preferred-shift-matching staff are chosen first
preferred shift is overridden when needed to avoid unassigned tasks
deep clean assigned only in B or C window
daily/vacant assigned only in A or D window
one task is assigned to exactly one staff
total staff daily minutes never exceed 240
fairness uses historical total minutes as tie-breaker
shift_id is set according to chosen bucket
unassigned tasks remain visible
Frontend

Test:

run allocation triggers API
assigned list renders
unassigned list renders
loading/error/empty states work
Done Criteria
current_shift is replaced by preferred_shift
preferred shift is respected when possible
preferred shift can be overridden when shortage exists
allocation runs for selected date
staff can work across morning and afternoon windows
tasks remain atomic
no staff exceeds 240 minutes total per day
shift_id is assigned during allocation
assignments are created correctly
unassigned tasks remain visible
```
