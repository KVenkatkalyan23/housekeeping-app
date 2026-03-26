# Feature: Task Allocation

## Goal

Implement task allocation logic that assigns generated cleaning tasks to staff fairly and correctly, while deciding the shift during allocation, respecting time windows, staff capacity, and workload balancing rules.

---

## Scope

- allocate generated tasks to staff
- assign `shift_id` during allocation
- create task assignments
- support morning and afternoon shift allocation
- enforce deep-clean time window rules
- enforce 4-hour max work per staff per shift
- distribute work evenly
- avoid repeatedly assigning more work to the same people
- support unassigned tasks when capacity is insufficient
- expose allocation API
- expose API to fetch allocation result

---

## In Scope

- backend allocation service
- backend allocation API
- backend fetch assignments API
- backend fairness logic
- backend shift/time-window allocation logic
- frontend screen to trigger allocation
- frontend allocation result view
- loading/error/empty states

---

## Out of Scope

- relocation
- manual reassignment
- shortfall feature implementation
- attendance history
- dashboard aggregation
- auto shift rotation engine
- multi-shift staff split in a single day

---

## Core Business Rules

### Shift Windows

- Morning shift: `08:00 - 12:00`
- Afternoon shift: `13:00 - 17:00`

### Hotel Timing Rules

- Checkout time: `10:00`
- Check-in time: `15:00`

### Deep Clean Allowed Windows

Deep cleaning can only happen after checkout and before next check-in.

- Morning deep clean window: `10:00 - 12:00`
- Afternoon deep clean window: `13:00 - 15:00`

### Remaining Task Windows

Other tasks can use the remaining shift hours.

- Morning remaining window: `08:00 - 10:00`
- Afternoon remaining window: `15:00 - 17:00`

### Task Types and Duration

- `DEEP_CLEAN` → `120` minutes
- `DAILY_CLEAN` → `30` minutes
- `VACANT_CLEAN` → `15` minutes

### Task Priority

- `DEEP_CLEAN` → priority `1`
- `DAILY_CLEAN` → priority `2`
- `VACANT_CLEAN` → priority `3`

### Staff Work Rules

- one staff works in only one shift for allocation
- no partial split across morning and afternoon
- max workload per staff per shift = `240` minutes
- allocation must be done separately per shift

### Fairness Rules

To divide work evenly:

1. prefer staff with lower currently allocated minutes in that shift
2. if tie, prefer staff with lower historical total minutes/hours worked
3. if still tie, use stable tie-breaker (e.g. `staffId`)

### Historical Workload

Introduce and use a field in `staff_profiles`:

- `total_minutes_worked`
  or
- `total_hours_worked`

Recommended:

- `total_minutes_worked`

This field is used only for fairness tie-breaking, not for daily shift capacity.

### Shift Assignment Rule

- tasks are generated with `shift_id = null`
- during allocation, selected shift is assigned to the task
- allocation input must include:
  - `taskDate`
  - `shiftId`

---

## Allocation Strategy

### High-Level Strategy

Run allocation per shift.

For a given `taskDate` and `shiftId`:

1. fetch unassigned tasks for the date
2. fetch eligible staff for the selected shift
3. separate tasks by type
4. allocate `DEEP_CLEAN` tasks first
5. allocate remaining `DAILY_CLEAN` and `VACANT_CLEAN` tasks next
6. persist assignments
7. leave overflow tasks unassigned

### Recommended Algorithm

Use a greedy, capacity-aware, priority-first allocator.

For each task:

- find eligible staff who can still fit the task into remaining capacity
- sort candidates by:
  1. lowest allocated minutes in current shift
  2. lowest historical total minutes worked
  3. stable tie-breaker
- assign task to first valid candidate

### Why This Algorithm

- simple to implement
- deterministic
- easy to debug
- fair enough for sprint scope
- works with current business rules

---

## Deep Clean Allocation Logic

### Rule

Allocate deep clean tasks first because:

- they are highest priority
- they have strict allowed time windows
- they consume full 120 minutes

### Practical Implication

Within a deep-clean window:

- one staff can typically take only one deep clean task in that window
- if there are more deep clean tasks than available staff capacity, some tasks remain unassigned

### Morning Example

- deep clean window = `10:00 - 12:00`
- one deep clean = `120 min`
- one staff can handle one deep clean in this window

### Afternoon Example

- deep clean window = `13:00 - 15:00`
- one deep clean = `120 min`
- one staff can handle one deep clean in this window

### Rule to Enforce

Do not assign a deep clean to a staff member if:

- they already have another deep clean in the same shift
  or
- their remaining capacity is less than 120 minutes

---

## Daily and Vacant Allocation Logic

### Rule

After deep clean allocation:

- allocate `DAILY_CLEAN`
- allocate `VACANT_CLEAN`

### Allowed Time Use

- Morning: `08:00 - 10:00`
- Afternoon: `15:00 - 17:00`

### Practical Rule

These tasks should fill remaining shift capacity after deep clean priority has been handled.

### Ordering

Recommended order after deep clean:

1. `DAILY_CLEAN`
2. `VACANT_CLEAN`

Within same type:

- assign to least-loaded eligible staff first

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
- `AllocationResultSummaryResponse.java`

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
- reuse existing `AvailabilityStatus.java`

#### shift/repository/

- `ShiftRepository.java`

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

- update admin/allocation routes

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
- `user`
- `currentShift`
- `availabilityStatus`
- `totalMinutesWorked` or equivalent fairness field

### Important Rule

During allocation:

- update `cleaning_tasks.shift_id`
- create corresponding `task_assignments` row

---

## Backend API Design

### POST `/api/allocation/run`

Purpose:

- run allocation for a selected date and shift

#### Request

```json
{
  "taskDate": "2026-03-26",
  "shiftId": "uuid"
}
Response

Should include:

total task count
assigned task count
unassigned task count
assignment list
unassigned task list
selected shift info

Example:

{
  "taskDate": "2026-03-26",
  "shiftId": "uuid",
  "totalTasks": 20,
  "assignedTasks": 16,
  "unassignedTasks": 4,
  "assignments": [],
  "unassigned": []
}
GET /api/allocation


Done Criteria
allocation runs for selected date and shift
deep clean tasks are assigned first
shift is assigned during allocation
one staff works in only one shift
no staff exceeds 240 minutes
work is distributed evenly
historical total minutes worked is considered for fairness
tasks are assigned to staff correctly
unassigned tasks remain visible
allocation results can be fetched and displayed
loading/error/empty states are handled
```
