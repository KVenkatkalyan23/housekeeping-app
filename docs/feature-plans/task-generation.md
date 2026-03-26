# Feature: Task Generation

## Goal

Implement task generation logic for housekeeping tasks based on room stay state for a given date.

---

## Scope

- generate cleaning tasks
- support 3 task types
- assign estimated minutes
- assign priority order
- do not assign shift during generation
- keep `shift_id` nullable at generation stage
- prevent duplicate task creation for the same room and date
- expose API to trigger task generation
- expose API to fetch generated tasks

---

## In Scope

- backend task generation service
- backend task generation API
- backend fetch tasks API

---

## Out of Scope

- staff assignment
- shift assignment
- relocation
- shortfall calculation
- task completion
- dashboard aggregation

---

## Core Business Rules

### Task Types

There are 3 types of cleaning tasks:

- `DEEP_CLEAN` → Checkout cleaning → `120` minutes
- `DAILY_CLEAN` → Daily cleaning → `30` minutes
- `VACANT_CLEAN` → Vacant cleaning → `15` minutes

### Shift Handling

- `shift_id` must remain `null` during task generation
- shift will be decided later during task allocation
- task generation only decides **what work exists**
- task allocation will decide **which shift** and **which staff** handle the task

### Task Generation Rules

#### 1. Checkout Cleaning

Generate `DEEP_CLEAN` when:

- room has a valid `RoomStay`
- `check_out_date == taskDate`

#### 2. Daily Cleaning

Generate `DAILY_CLEAN` when:

- room has a valid `RoomStay`
- `check_out_date > taskDate`

#### 3. Vacant Cleaning

Generate `VACANT_CLEAN` when:

- room has no valid `RoomStay`
- OR `RoomStay.check_out_date < taskDate`
- OR no `RoomStay` exists for the room

### Valid RoomStay

A valid `RoomStay` for generation means:

- room stay exists for the room
- and it is relevant for the selected task date

Recommended interpretation:

- if `check_out_date == taskDate` → checkout cleaning
- if `check_out_date > taskDate` → daily cleaning
- otherwise not valid for occupied cleaning logic

### Priority Order

Assign priorities during generation:

- `DEEP_CLEAN` → highest priority
- `DAILY_CLEAN` → medium priority
- `VACANT_CLEAN` → lowest priority

Recommended numeric values:

- `DEEP_CLEAN` → `1`
- `DAILY_CLEAN` → `2`
- `VACANT_CLEAN` → `3`

---

## Files (MANDATORY)

### Backend

#### task/controller/

- `CleaningTaskController.java`

#### task/service/

- `CleaningTaskService.java`

#### task/dto/

- `GenerateTasksRequest.java`
- `GenerateTasksResponse.java`
- `CleaningTaskListItemResponse.java`

#### task/repository/

- `CleaningTaskRepository.java`

#### task/entity/

- reuse existing `CleaningTask.java`
- reuse existing `TaskType.java`
- reuse existing `TaskStatus.java`

#### room/repository/

- `RoomRepository.java`

#### booking/repository/ or roomstay/repository/

- `RoomStayRepository.java`

---

## Data Model Expectations

### CleaningTask

Ensure entity supports:

- `id`
- `room`
- `taskDate`
- `shift` nullable
- `taskType`
- `priorityOrder`
- `estimatedMinutes`
- `taskStatus`
- `sourceStay` nullable
- `createdAt`
- `updatedAt`

### Important Rule

- `shift_id` must be nullable now
- task generation must persist tasks without assigning shift

---

## Backend API Design

### POST `/api/tasks/generate`

Purpose:

- generate cleaning tasks for a given date

#### Request

```json
{
  "taskDate": "2026-03-26"
}
Response

Should include:

generated task count
skipped duplicate count
optionally generated task items

Example:

{
  "generatedCount": 24,
  "skippedCount": 3
}
GET /api/tasks

Purpose:

fetch generated tasks for a given date
Query Params
taskDate required
optional taskStatus

Example:

/api/tasks?taskDate=2026-03-26
Response

Return task list including:

taskId
roomId
roomNumber
taskDate
taskType
estimatedMinutes
priorityOrder
taskStatus
shiftId
sourceStayId
Backend Service Responsibilities
CleaningTaskService

Responsibilities:

fetch all rooms
determine correct room stay status for each room
generate correct task type
assign estimated minutes
assign priority
persist task
avoid duplicates
Suggested Methods
generateTasks(LocalDate taskDate)
getTasksByDate(LocalDate taskDate)
deriveTaskType(Room room, RoomStay stay, LocalDate taskDate)
calculateEstimatedMinutes(TaskType taskType)
calculatePriority(TaskType taskType)
taskAlreadyExists(UUID roomId, LocalDate taskDate)
Repository Requirements
RoomRepository

Need:

fetch all active rooms
or
fetch all rooms considered for task generation
RoomStayRepository

Need:

fetch room stay for a room relevant to the task date
or fetch latest room stay per room
or fetch active/current stay set for all rooms efficiently

Recommended behavior:

identify whether room is:
checkout today
occupied beyond today
vacant/no valid stay
CleaningTaskRepository

Need:

check if task already exists for same room + date
fetch tasks by date
optionally fetch by date + status

Recommended duplicate rule:

do not create more than one generated task for the same room and date
Duplicate Prevention Rules
Duplicate Definition

A duplicate task means:

same room
same task date

Recommended:

only one generated cleaning task per room per date

If a task already exists for same room + date:

skip creation
increment skipped count
Backend Validation
Request Validation

For GenerateTasksRequest:

taskDate required
must be valid date
Service Validation
room must exist
generation should not fail entire batch because one room has missing stay
handle missing room stay safely
handle null values defensively
Error Handling

Handle:

invalid date input
repository/query failure
bad room stay data
duplicate generation attempt
unexpected internal errors

Recommended:

continue generation for valid rooms even if one room has inconsistent data
log failures if needed
return partial success only if your API style supports it
Frontend UI Requirements
Task Generation Page




Done Criteria
tasks can be generated for a selected date
checkout rooms generate DEEP_CLEAN
occupied rooms generate DAILY_CLEAN
vacant rooms generate VACANT_CLEAN
estimated minutes are correct
priority values are correct
shift_id remains null during generation
duplicate tasks are prevented
generated tasks can be fetched and displayed
frontend is integrated with backend
loading/error/empty states are handled
```
