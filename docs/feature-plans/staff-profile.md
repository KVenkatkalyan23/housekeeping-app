# Feature: Staff Profile Page

## Goal

Implement a staff-only Profile page for the web application that matches the provided design and displays the logged-in staff user’s profile information with a logout action.

---

## Scope

### In Scope

- staff-only profile route
- profile page UI matching the design
- bottom navigation with Profile tab active
- backend API to fetch logged-in staff profile
- frontend integration with backend API
- logout action from profile page
- loading, error, and empty states
- role-based access restriction

### Out of Scope

- edit profile flow
- upload/change avatar
- admin profile page
- notification bell functionality
- assigned wing management backend unless data already exists
- task/attendance/leave page changes

---

## Design Source

- `docs/designs/profile/staff-profile.png`

---

## Files (MANDATORY)

### Backend

#### staff/controller/

- `StaffProfileController.java`
  or extend existing staff controller if already present

#### staff/service/

- `StaffProfileService.java`
  or extend existing staff service if already present

#### staff/dto/

- `StaffProfileResponse.java`

#### staff/repository/

- `StaffProfileRepository.java` (reuse/update)

#### staff/entity/

- reuse existing `StaffProfile.java`

#### auth/security/

- reuse existing JWT auth / role protection

---

### Frontend

#### features/profile/

- `api.ts`
- `slice.ts` or RTK Query endpoints
- `types.ts`
- `pages/ProfilePage.tsx`
- `components/ProfileHeader.tsx`
- `components/ProfileSummaryCard.tsx`
- `components/ProfileDetailsList.tsx`
- `components/ProfileDetailCard.tsx`
- `components/LogoutButton.tsx`
- `components/BottomNav.tsx`

#### features/auth/

- reuse/update logout handling if needed

#### routes/

- update staff/profile routes
- ensure staff-only route protection

#### shared/api/

- reuse/update `client.ts` only if needed

#### app/router or app/layouts/

- update routing if needed

---

## Route Design

### Route

Recommended route:

- `/staff/profile`

### Access Rules

- authenticated `STAFF` users can access
- `ADMIN` users cannot access
- unauthenticated users redirect to `/login`

### Navigation Rules

- bottom nav Profile tab is active on this page
- Tasks / Attendance / Leave can navigate to existing routes or stay as placeholders if not ready

---

## Backend API Design

### GET `/api/staff/profile`

Purpose:

- return profile details for the logged-in staff user

### Authentication

- requires valid JWT
- current user must have role `STAFF`

### Response

Should include:

- `staffId`
- `userId`
- `fullName`
- `displayName` (optional if same as fullName)
- `email`
- `phone`
- `currentShift`
- `assignedWing` (optional / derived / placeholder if not stored directly)
- `availabilityStatus` (optional)
- `avatarUrl` (optional placeholder if you support it)

### Example Response

```json
{
  "staffId": "uuid",
  "userId": "uuid",
  "fullName": "Marcus Bennett",
  "displayName": "Marcus Bennett",
  "email": "m.bennett@luxuryhotel.com",
  "phone": "+1 (555) 092-4822",
  "currentShift": "MORNING_SHIFT",
  "assignedWing": "North Suites, Floor 4-6"
}
Backend Business Rules
Source of Truth

Use:

staff_profiles
linked users
linked shifts if current shift label is needed
Current User Resolution
identify logged-in user from JWT
map user to StaffProfile
return only that user’s profile
Assigned Wing

If schema does not currently store assignedWing:

return placeholder/static value for now
or
omit from backend and use temporary frontend mock
Recommended:
include as placeholder/static in frontend if not backed by schema yet
Shift Label

Use current shift relationship if available.
Convert to readable label like:

MORNING_SHIFT or shift code/name → Morning Shift
Repository Requirements
StaffProfileRepository

Add or reuse query support for:

fetch profile by user id
optionally join current shift

Suggested query need:

findByUserId(...)
or equivalent based on entity mapping
Service Requirements
StaffProfileService

Responsibilities:

identify logged-in user
resolve linked staff profile
map profile data into response DTO
include readable shift label
Suggested Method
getCurrentStaffProfile(UUID userId)
Error Cases

Handle:

no linked staff profile found
invalid/unauthorized access
unexpected repository failure
DTO Requirements
StaffProfileResponse

Fields:

staffId
userId
fullName
displayName
email
phone
currentShift
assignedWing
availabilityStatus (optional)
Frontend UI Requirements
Page Layout

Match the provided design closely:

light page background
top header row with avatar/logo, app name, bell icon
centered avatar/profile image section
large staff name heading
smaller subtitle/name line
blue shift badge/pill
personal details section title
stacked detail cards
logout button
bottom navigation dock
Header Section

Show:

avatar/logo placeholder
title: Housekeeping
bell icon placeholder

Bell is visual only for now.

Profile Summary Section

Show:

large profile image/avatar placeholder
primary name heading
secondary name text
current shift pill/button
Personal Details Section

Show cards for:

Work Email
Phone
Assigned Wing

Each detail card should have:

icon area on left
label
value
Logout Section

Show:

large red/pink logout button
on click: perform logout, clear auth state, clear storage, redirect to login
Bottom Navigation

Items:

Tasks
Attendance
Leave
Profile

Behavior:

Profile tab active/highlighted
Frontend State Design
Profile State

Suggested fields:

profile
loading
error
Profile Type

Suggested fields:

staffId
userId
fullName
displayName
email
phone
currentShift
assignedWing
Frontend API Integration
profile/api.ts

Implement:

getCurrentStaffProfile()
Request Rules
use shared API client
attach bearer token automatically
rely on existing auth interceptor
Success Handling
populate profile state
render all cards from API data
Error Handling
show inline error or toast
provide simple retry if desired
Empty Handling

If no profile data:

show friendly empty state
keep page layout intact
Formatting Rules
Shift Display

Format readable label:

Morning Shift
Labels

Use exact or close labels from design:

Work Email
Phone
Assigned Wing
Name Display

Use fullName for main heading and subtitle unless separate display value exists

Access and Security Rules
Backend
endpoint requires valid JWT
only STAFF users can access own profile
do not expose other staff records
admin must not use this endpoint unless explicitly supported later
Frontend
route must be protected
admin users redirected away from this page
unauthenticated users redirected to login
Static / Placeholder Parts

Keep static for now:

bell icon behavior
avatar image asset if real profile image is not available
assigned wing if not available from backend
non-profile bottom nav destinations if not built

Use real backend data for:

name
email
phone
current shift if available
Suggested Implementation Sequence
add/update backend repository query
implement profile service method
add profile response DTO
expose GET /api/staff/profile
add/update staff-only profile route
implement ProfilePage layout from design
implement profile summary and details components
integrate API with frontend state
wire logout button to auth/logout flow
add loading/error/empty states
add bottom nav with Profile active
test role protection and rendering
Testing
Backend
returns profile for logged-in staff only
rejects unauthorized requests
blocks admin if staff-only restriction is applied
Frontend
staff can access route
admin cannot access route
profile fields render correctly
loading state renders while fetching
empty state renders if no data
error state renders on API failure
Profile tab shows as active
logout clears session and redirects
Done Criteria
staff-only profile route exists
backend profile API works
frontend integrates with API correctly
page visually matches design closely
profile details render correctly
logout works from this page
Profile tab is active in bottom navigation
loading/error/empty states are handled
admin cannot access the page
implementation stays within profile/auth-related modules only
```
