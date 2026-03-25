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
