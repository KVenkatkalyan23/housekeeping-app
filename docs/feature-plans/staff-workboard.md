# Feature: Staff Workboard

## Goal

Implement the staff workboard so a logged-in `STAFF` user can view assigned tasks for today, see workload summary, track task status/progress, and mark tasks as completed.

---

## Scope

### In Scope

- staff-only workboard route/page
- backend API to fetch today’s assigned tasks for logged-in staff
- backend API to fetch current workload summary for logged-in staff
- backend API to mark an assigned task as completed
- update task status flow
- set completed timestamp when task is completed
- frontend workboard UI
- assigned minutes summary
- completed vs pending display
- progress/status view
- role-based access restriction

### Out of Scope

- supervisor reassignment
- task editing
- staff profile changes
- leave integration on this page
- attendance history
- dashboard analytics beyond this page
- partial task progress percentages beyond status-based progress
- admin workboard view

---

## Design / UX Intent

This page is the main daily workboard for a staff user.  
It should answer:

- what tasks are assigned to me today?
- which are pending vs completed?
- how many minutes are assigned today?
- can I mark a task complete?

The page should be simple, staff-focused, and action-oriented.

---

## Files (MANDATORY)

### Backend

#### staffworkboard/controller/

- `StaffWorkboardController.java`

#### staffworkboard/service/

- `StaffWorkboardService.java`

#### staffworkboard/dto/

- `MyAssignedTaskItemResponse.java`
- `MyAssignedTasksResponse.java`
- `WorkloadSummaryResponse.java`
- `MarkTaskCompleteResponse.java`

#### allocation/repository/

- reuse/update `TaskAssignmentRepository.java`

#### task/repository/

- reuse/update `CleaningTaskRepository.java`

#### task/entity/

- reuse existing `CleaningTask.java`
- ensure it supports:
  - `taskStatus`
  - `completedAt` or equivalent completion timestamp field

#### allocation/entity/

- reuse existing `TaskAssignment.java`

#### staff/repository/

- reuse `StaffProfileRepository.java`

#### auth/security/

- reuse existing JWT / role protection

#### migrations/

- add migration for completion timestamp if not already present on `cleaning_tasks`

---

### Frontend

#### features/staff-workboard/

- `api.ts`
- `slice.ts` or RTK Query endpoints
- `types.ts`
- `pages/StaffWorkboardPage.tsx`
- `components/WorkboardHeader.tsx`
- `components/WorkloadSummaryCard.tsx`
- `components/AssignedTasksList.tsx`
- `components/AssignedTaskCard.tsx`
- `components/TaskStatusBadge.tsx`

#### routes/

- add/update staff-only workboard route

#### shared/api/

- reuse/update `client.ts` only if needed

#### features/auth/

- reuse existing auth role-based route handling

---

## Route Design

### Recommended Route

- `/staff/tasks`
  or
- `/staff/workboard`

Recommended:

- `/staff/tasks`

### Access Rules

- only authenticated `STAFF` users can access
- `ADMIN` users cannot access this page
- unauthenticated users redirect to `/login`

### Navigation

- this page can be the staff landing page after login if desired
- or remain reachable via bottom navigation / route menu

Recommended:

- if attendance portal is the first page after login, this should still be easily reachable from navigation

---

## Backend API Design

### GET `/api/staff/tasks/today`

Purpose:

- return today’s assigned tasks for the logged-in staff user

#### Authentication

- requires valid JWT
- current user must have role `STAFF`

#### Response

Should include:

- date
- tasks list

Each task item should include:

- `taskId`
- `roomId`
- `roomNumber`
- `taskType`
- `taskStatus`
- `estimatedMinutes`
- `priorityOrder`
- `shiftId`
- `shiftCode`
- `shiftName`
- `completedAt` (nullable)

Optional:

- `sourceStayId`

---

### GET `/api/staff/tasks/workload`

Purpose:

- return workload summary for the logged-in staff user for today

#### Response

Should include:

- `assignedMinutes`
- `completedMinutes`
- `pendingMinutes`
- `totalTaskCount`
- `completedTaskCount`
- `pendingTaskCount`

Optional:

- `completionPercentage`

---

### POST `/api/staff/tasks/{taskId}/complete`

Purpose:

- mark a task assigned to the logged-in staff user as completed

#### Rules

- only assigned staff can complete their own task
- completed tasks cannot be completed again
- cancelled tasks cannot be completed

#### Response

Should include:

- `taskId`
- `taskStatus`
- `completedAt`
- optional success message

---

## Backend Business Rules

### Assigned Tasks Rule

A staff user should see only tasks:

- assigned to them
- for today
- that are relevant to the workboard

### Workload Rule

Workload summary should be calculated from today's assigned tasks for the logged-in staff:

- assigned minutes = sum of all today’s assigned task minutes
- completed minutes = sum of completed task minutes
- pending minutes = sum of non-completed assigned task minutes

### Mark Complete Rule

When marking a task complete:

- verify task exists
- verify task is assigned to logged-in staff
- verify task is not already completed
- update `taskStatus = COMPLETED`
- set `completedAt = now`

### Status Transition Rules

Recommended:

- `ASSIGNED` → `COMPLETED`
- optionally allow `IN_PROGRESS` → `COMPLETED` if used in your system

For MVP:

- if current task status is `ASSIGNED`, allow completion
- if already `COMPLETED`, reject
- if `CANCELLED`, reject

### Completed Timestamp

When task is completed:

- store exact completion timestamp in the task entity
- recommended field:
  - `completedAt`

---

## Data Model Expectations

### CleaningTask

Ensure entity supports:

- `id`
- `room`
- `taskDate`
- `shift`
- `taskType`
- `priorityOrder`
- `estimatedMinutes`
- `taskStatus`
- `completedAt`
- `createdAt`
- `updatedAt`

### TaskAssignment

Ensure entity supports:

- `id`
- `cleaningTask`
- `staff`
- `createdAt`
- `updatedAt`

### StaffProfile

Must map logged-in user to staff identity.

---

## Repository Requirements

### TaskAssignmentRepository

Need:

- fetch task assignments for a staff member for a given date
- fetch assignment by task id + staff id
- optionally fetch joined task data to avoid N+1 query issues

Suggested capabilities:

- find all by staff id and task date
- find by cleaning task id and staff id

### CleaningTaskRepository

Need:

- fetch task by id
- save updated task on completion
- optionally support summary queries if you want aggregation in DB

### StaffProfileRepository

Need:

- resolve staff profile by user id

---

## Service Requirements

### StaffWorkboardService

Responsibilities:

- resolve logged-in user to `StaffProfile`
- fetch assigned tasks for today
- compute workload summary
- validate and mark task complete
- map entities into response DTOs

### Suggested Methods

- `getMyAssignedTasks(UUID userId, LocalDate taskDate)`
- `getMyWorkload(UUID userId, LocalDate taskDate)`
- `markTaskComplete(UUID userId, UUID taskId)`

---

## Frontend UI Requirements

### Staff Workboard Page

Display:

- page header
- assigned minutes summary
- completed vs pending summary
- list of today’s assigned tasks
- mark complete action for pending tasks

### Header

Show:

- page title (e.g. `My Tasks` or `Today’s Tasks`)
- subtitle with today’s date or short helper text

### Workload Summary

Show:

- assigned minutes
- completed minutes
- pending minutes
- completed vs pending count

Optional:

- progress bar or simple completion percentage

### Assigned Task List

Each card/item should show:

- room number
- task type label
- estimated minutes
- shift
- task status badge
- completed timestamp if completed
- action button if pending

### Mark Complete Action

Pending tasks should show:

- `Mark Complete` button

Completed tasks should show:

- completed status
- completed timestamp
- no action button

---

## Frontend State Design

### Assigned Tasks State

Suggested fields:

- `items`
- `loading`
- `error`

### Workload Summary State

Suggested fields:

- `assignedMinutes`
- `completedMinutes`
- `pendingMinutes`
- `totalTaskCount`
- `completedTaskCount`
- `pendingTaskCount`
- `loading`
- `error`

### Completion Action State

Suggested fields:

- `markCompleteLoadingTaskId`
- `actionError`
- `actionSuccess`

---

## API Integration Rules

### staff-workboard/api.ts

Implement:

- `getMyAssignedTasks()`
- `getMyWorkload()`
- `markTaskComplete(taskId)`

### Behavior

On page load:

- fetch assigned tasks
- fetch workload summary

On mark complete success:

- update the affected task in the UI
- refresh or recompute workload summary
- show success feedback if needed

### Error Handling

Show:

- inline error or toast for failed fetch
- inline action error for failed completion

### Empty State

If no tasks are assigned for today:

- show friendly message like:
  - `No tasks assigned for today`

---

## Formatting Rules

### Task Type Labels

Display:

- `DEEP_CLEAN` → `Checkout Cleaning`
- `DAILY_CLEAN` → `Daily Cleaning`
- `VACANT_CLEAN` → `Vacant Cleaning`

### Duration Display

Display:

- `120 min`
- `30 min`
- `15 min`

### Status Labels

Display:

- `ASSIGNED`
- `IN_PROGRESS`
- `COMPLETED`

Use status badge styling.

### Completed Timestamp

Display readable format like:

- `Completed at 2:15 PM`
  or
- `2:15 PM`

---

## Role and Security Rules

### Backend

- all workboard endpoints require valid JWT
- only `STAFF` users can access them
- staff can only view and complete their own assigned tasks

### Frontend

- route must be staff-only
- admin must not access this page
- redirect unauthenticated users to login

---

## Edge Cases

### No Staff Profile

- logged-in user does not map to a staff profile
- return safe error

### No Assigned Tasks

- return empty list and zero summary

### Task Assigned to Someone Else

- reject mark complete
- return forbidden or business error

### Task Already Completed

- reject second completion attempt
- return clear message

### Task Cancelled

- reject completion

### Missing Task

- return not found

### Stale UI

- if frontend tries to complete a task already completed elsewhere, backend must reject and frontend should refresh state

---

## Suggested Implementation Sequence

1. add completion timestamp field to `CleaningTask` if missing
2. add/update repository methods for assigned tasks queries
3. implement workboard service
4. implement GET assigned tasks API
5. implement GET workload summary API
6. implement POST mark task complete API
7. implement staff-only route
8. build workboard page UI
9. integrate frontend with APIs
10. wire mark complete action
11. update summary after completion
12. handle loading/error/empty states
13. test role protection and completion flow

---

## Testing

### Backend

Test:

- fetch assigned tasks for logged-in staff
- workload summary is calculated correctly
- mark complete works for assigned task
- mark complete rejects task assigned to another staff
- mark complete rejects already completed task
- completedAt is set when task is completed

### Frontend

Test:

- staff can access workboard page
- admin cannot access page
- assigned tasks render correctly
- workload summary renders correctly
- mark complete updates task state
- summary updates after completion
- loading/error/empty states work

---

## Done Criteria

- staff-only workboard route exists
- assigned tasks API works
- workload summary API works
- mark complete API works
- completed timestamp is stored
- staff can see today’s assigned tasks
- staff can mark their tasks complete
- workload summary reflects completed vs pending work
- UI handles loading/error/empty states
- admin cannot access this page
