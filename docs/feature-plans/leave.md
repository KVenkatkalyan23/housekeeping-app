# Feature: Leave Management

## Goal

Implement leave management as a single feature covering leave application, personal leave history, and admin leave overview with matching backend and frontend validations.

---

## Scope

### Backend

- `LeaveRequest` entity
- apply leave API
- my leave list API
- DTO validation
- service-level business validation
- leave types: `PLANNED`, `SICK`
- auto-approve on creation
- pagination for list APIs

### Frontend

- apply leave form
- my leaves page
- date pickers
- leave type dropdown
- optional reason input
- frontend validation errors
- API integration
- loading/error/empty states

---

## Out of Scope

- leave edit/update/delete
- leave overlap conflict handling unless already required
- approval workflow beyond auto-approve
- notifications
- calendar view

---

## Files (MANDATORY)

### Backend

- `leave/entity/LeaveRequest.java`
- `leave/repository/LeaveRequestRepository.java`
- `leave/service/LeaveService.java`
- `leave/controller/LeaveController.java`
- `leave/dto/ApplyLeaveRequest.java`
- `leave/dto/MyLeavesResponse.java`
- `common/enums/LeaveType.java`
- `common/enums/LeaveStatus.java`

### Frontend

- `src/features/staff-portal/leave/api.ts`
- `src/features/staff-portal/leave/types.ts`
- `src/features/staff-portal/leave/slice.ts`
- src/features/staff-portal/attendance/components/leaveSection.tsx - you have to integrate the api in this and a all the fields mentioned above. and design is attached in the path /docs/designs/leave-card.png

---

remeber status of the leave should be approved by default.

## Data Model

### LeaveRequest

Required fields:

- `id`
- `staff`
- `leaveStartDate`
- `leaveEndDate`
- `leaveType`
- `reason`
- `status`
- `requestedAt`
- `updatedAt`

### LeaveType

Allowed values:

- `PLANNED`
- `SICK`

### LeaveStatus

Use existing status enum if present, otherwise create:

- `APPROVED`

For this feature:

- new leave requests should be auto-approved

---

## Backend API Design

## 1. Apply Leave API

### Endpoint

`POST /api/leave/apply`

### Request payload

```json
{
  "userId": "uuid",
  "fromDate": "2026-03-26",
  "toDate": "2026-03-28",
  "leaveType": "PLANNED",
  "reason": "optional"
}
Flow

DTO -> validation -> controller -> service -> repository

DTO Validation
userId required
fromDate required
toDate required
leaveType required
reason optional
Service Validation
userId must be a valid UUID and user must exist
fromDate must be today or future
toDate must be today or future
toDate must be on or after fromDate
leaveType must be SICK or PLANNED
Business Rules
SICK leave
no prior notice required
max leave duration = 7 days
PLANNED leave
must be applied at least 2 days before fromDate
max leave duration = 7 days
Duration calculation
durationDays = toDate - fromDate + 1
Auto-approve logic
on successful creation, set:
status = APPROVED
Response

Return created leave item including:

leave id
from date
to date
leave type
status
duration days
reason
2. My Leaves API
Endpoint

GET /api/leave/my

Query Params
userId
page
size
Rules
paginated response
return only leaves for the given user
order latest first
Response item fields
leaveId
fromDate
toDate
leaveType
status
durationDays
reason

Frontend Requirements
1. Apply Leave Form
Fields
fromDate date picker
toDate date picker
leaveType dropdown with:
SICK
PLANNED
reason text input (optional)
Frontend Validation
Common
fromDate required
toDate required
toDate >= fromDate
SICK
max duration = 7 days
PLANNED
must be at least 2 days from today
max duration = 7 days
Behavior
submit calls apply leave API
show backend validation errors clearly
show success message on success
2. My Leaves Page
fetch paginated leave list for current user
display leave cards/list with:
from date
to date
leave type
status
duration
reason if present
support loading/error/empty states

Testing
Backend
apply sick leave success
apply planned leave success
planned leave rejected if less than 2 days prior
leave rejected if duration > 7 days
leave rejected if dates invalid
my leaves returns paginated user-specific data
admin leaves returns paginated all-user data

Frontend
apply form validation works
backend errors are shown
my leaves page renders paginated data
admin page renders paginated data
loading/error/empty states work

Done Criteria
leave request can be created successfully
SICK and PLANNED rules are enforced
leave is auto-approved on creation
my leaves API works with pagination
admin leaves API works with pagination
apply leave form works end-to-end
my leaves page works
admin leave overview page works
frontend and backend validations match
```
