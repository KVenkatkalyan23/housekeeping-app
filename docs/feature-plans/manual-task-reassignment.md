# Feature: Manual Task Reassignment

## Goal

Implement supervisor/admin manual task reassignment so an assigned task can be moved from one staff member to another through an admin action, with capacity revalidation, completed-task restriction, audit logging, and immediate UI update.

---

## Design Source

- `docs/designs/admin-portal/manual-task-reassignment.png`

---

## Route Context

This feature lives inside the admin task allocation view and is triggered from the task list row action.

### Frontend context

- page: `/admin/tasks`
- modal action opened from task row

### Backend API

- `POST /api/admin/tasks/reassign`

---

## Access Rules

- admin/supervisor-only feature
- authenticated `ADMIN` users can access
- `STAFF` users must not access
- unauthenticated users redirect to login / receive 401

---

## Scope

### In Scope

- manual reassignment API
- capacity revalidation
- completed-task restriction
- assignment update logic
- source flag = manual
- audit entry
- admin task list action button
- manual reassignment modal
- target staff dropdown
- validation/error messages
- updated assignment view after success

### Out of Scope

- relocation automation
- bulk reassignment
- drag-and-drop reassignment
- task splitting
- changing task type/priority during reassignment
- modifying unrelated assignments
- dedicated standalone reassignment page

---

# Backend Implementation

## Backend Goal

Allow an admin to manually move a task from one staff member to another while keeping task integrity and enforcing business constraints.

---

## Backend Files (MANDATORY)

### Create

- `backend/src/main/java/.../admin/taskreassignment/controller/AdminTaskReassignmentController.java`
- `backend/src/main/java/.../admin/taskreassignment/service/AdminTaskReassignmentService.java`
- `backend/src/main/java/.../admin/taskreassignment/dto/ManualTaskReassignmentRequest.java`
- `backend/src/main/java/.../admin/taskreassignment/dto/ManualTaskReassignmentResponse.java`
- `backend/src/main/java/.../admin/taskreassignment/dto/ReassignmentCandidateItemResponse.java` (if candidate list is returned)
- `backend/src/main/java/.../common/enums/AssignmentSource.java` (if not already present)

### Reuse / Update if Needed

- `TaskAssignmentRepository.java`
- `CleaningTaskRepository.java`
- `StaffProfileRepository.java`
- `ShiftRepository.java`
- `ActivityLogService.java` or audit logging service
- `TaskAssignment.java`
- `CleaningTask.java`

Keep backend changes focused on reassignment only.

---

## Backend API Design

## 1. Manual Reassignment API

### Endpoint

`POST /api/admin/tasks/reassign`

### Access

- admin-only

### Request

```json
{
  "taskId": "uuid",
  "targetStaffId": "uuid"
}
Response

Return:

taskId
roomId
roomNumber
previousStaffId
previousStaffName
newStaffId
newStaffName
taskStatus
assignmentSource
successMessage

Optional:

shiftId
shiftName
Optional Candidate Support

If useful for the modal dropdown, you may add:

Endpoint

GET /api/admin/tasks/reassign/candidates?taskId=<uuid>

Return:

available staff in same shift
capacity-safe candidates
display label for dropdown

This is optional.
If not implemented, frontend can reuse an existing admin staff list source if already available.

Backend Business Rules
1. Completed Task Restriction

A task cannot be manually reassigned if:

taskStatus = COMPLETED

Also reject if:

taskStatus = CANCELLED
Error message

Use clear business error such as:

Completed or cancelled tasks cannot be reassigned.
2. Assignment Must Already Exist

Manual reassignment applies to an already assigned task.

Reject if:

task has no current assignment
task does not exist
3. Same Shift Restriction

Target staff must belong to / be valid for the same shift as the task.

Do not allow reassignment across shifts.

If your system uses task shift directly:

target staff must be eligible for that task shift
4. Capacity Revalidation

Before reassigning, validate target staff remaining capacity.

Rule
current assigned minutes for target staff on the same date and shift
plus task estimated minutes
must be <= 240
If capacity fails

Reject reassignment with clear validation error.

Example:

Selected staff member does not have sufficient remaining capacity for this task.
5. Target Staff Must Be Different

Reject if:

targetStaffId is same as current assigned staff id
6. Assignment Update Logic

When reassignment succeeds:

update existing assignment to point to new staff
or
replace assignment row cleanly if that matches your current model better

Recommendation:

update the existing TaskAssignment if safe
preserve task itself
do not create duplicate active assignments
7. Source Flag

Set assignment source to:

MANUAL

If your assignment entity or related model does not yet support this:

add assignmentSource
allowed values might include:
AUTO
MANUAL
optional future values
8. Audit Entry

Create an activity log entry for manual reassignment.

Recommended event code
TASK_REASSIGNED
Category
TASK
Severity
WARNING or INFO
Suggested metadata
taskId
roomId
previousStaffId
newStaffId
previousStaffName
newStaffName
shiftId
reassignmentSource = MANUAL
Backend Service Responsibilities
AdminTaskReassignmentService

Responsibilities:

validate request
fetch task and current assignment
validate task status
validate target staff
revalidate target capacity
update assignment safely
set source flag to manual
emit audit log
return response DTO
Suggested methods
ManualTaskReassignmentResponse reassignTask(UUID taskId, UUID targetStaffId, UUID actorUserId)
validateTaskIsReassignable(CleaningTask task)
validateTargetCapacity(CleaningTask task, StaffProfile targetStaff)
updateAssignment(TaskAssignment assignment, StaffProfile targetStaff)
Backend Repository Needs
TaskAssignmentRepository

Need support for:

fetch assignment by task id
fetch current assigned minutes for staff/date/shift
save updated assignment
CleaningTaskRepository

Need support for:

fetch task by id with room and shift data
StaffProfileRepository

Need support for:

fetch target staff by id
optionally fetch same-shift candidates
ShiftRepository

Optional support if shift validation requires lookup beyond task relation

Backend Security
API must be admin-only
use existing JWT/role security
actor user id should be available for audit entry
Frontend Implementation
Frontend Goal

Allow admin to click a reassign action in the task table, open a modal, choose a target staff member, validate the action, and update the task row after success.

Frontend Files (MANDATORY)
Create inside src/features/admin-portal/task-allocation/reassignment
api.ts
types.ts
components/ManualReassignmentModal.tsx
components/ReassignmentStaffSelect.tsx
Update Existing Task Allocation Page Files If Needed
src/features/admin-portal/task-allocation/components/TaskAllocationTable.tsx
src/features/admin-portal/task-allocation/components/TaskAllocationRow.tsx
src/features/admin-portal/task-allocation/types.ts
src/features/admin-portal/task-allocation/api.ts
src/features/admin-portal/task-allocation/pages/AdminTaskAllocationPage.tsx

Keep reassignment-specific UI isolated where possible.

Frontend UI Requirements
1. Reassign Action Button

In the task table row:

add Reassign action/button for eligible tasks
Show button only when appropriate

Recommended:

show for tasks that are currently assigned
disable or hide for completed/cancelled tasks
2. Manual Reassignment Modal

Create modal matching the provided design as closely as practical.

Modal content
title: Manual Task Reassignment
close button
task details card:
room number
floor / room type if available
task type badge
target staff dropdown
validation or helper message area
informational note about audit/source tracking
footer buttons:
Cancel
Confirm Reassignment
3. Target Staff Dropdown

Dropdown should show available candidate staff.

Each option should ideally include:

full name
optional capacity hint

If candidate API is not added:

use existing staff list filtered client-side only if reliable
otherwise prefer a backend candidate endpoint
4. Validation / Error Messages

Show inline errors for:

no target staff selected
completed/cancelled task
insufficient capacity
same-staff selection
backend failure
Success behavior

On success:

close modal
update task row immediately
show success toast/message if project already uses toasts
5. Updated Assignment View

After reassignment:

assigned staff column should update without full-page confusion
if local state is used, update the affected row
if query refetch is preferred, refetch the table after success
Frontend State / API
api.ts

Implement:

reassignTask(payload)
optionally getReassignmentCandidates(taskId)
types.ts

Define:

reassignment request/response
candidate item type
modal state types
Modal state

Track:

open
selectedTask
selectedTargetStaffId
submitting
error
UX Notes
keep modal compact and admin-focused
disable confirm button while submitting
do not allow submission without a target selection
keep text clear and operational
Data Mapping Rules
Task Details in Modal

Display from selected task row:

room number
floor / room type
task type
Capacity Helper

If backend candidate response includes capacity-safe signal, show helper like:

Capacity revalidated: Selected staff member has sufficient remaining hours for this shift.

If not available before submit, show this only after successful validation or response.

Suggested Implementation Sequence
add/confirm assignmentSource enum/model support
create reassignment DTOs
create reassignment service
create reassignment controller
add/update repository methods for task assignment fetch and capacity validation
wire audit log emission
create reassignment API client/types on frontend
add task row reassign action
build modal UI
wire dropdown data source
implement submit flow
update task row after success
handle loading/error states
polish modal styling to match design
Testing / Verification
Backend

Verify:

reassignment succeeds for valid assigned task
completed task cannot be reassigned
cancelled task cannot be reassigned
target staff must have capacity
target staff must differ from current staff
same-shift restriction is enforced
source flag is set to manual
audit entry is created
Frontend

Verify:

reassign action opens modal
modal shows correct task details
dropdown selection works
confirm triggers API
success updates assignment view
validation and error messages display correctly
modal styling matches design closely
Done Criteria
manual reassignment API exists and is admin-only
capacity revalidation works
completed-task restriction works
assignment source is set to manual
audit entry is emitted
reassign action button exists in admin task allocation page
modal with target staff dropdown works
validation/error messages are shown properly
updated assignment is visible immediately after success
```
