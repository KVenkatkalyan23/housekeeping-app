# Feature: Login UI

## Goal

Implement login page matching the provided design.

## Files

- frontend/src/features/auth/pages/LoginPage.tsx
- frontend/src/features/auth/components/LoginForm.tsx
- frontend/src/features/auth/api.ts
- frontend/src/features/auth/slice.ts
- frontend/src/shared/api/client.ts
- frontend/src/routes/... (if route update needed)

## Design Source

- docs/feature-plans/signin.png

## Behavior

- submit to `/api/auth/login`
- on success store token and user info
- redirect based on role
- show backend error on invalid credentials

## Styling

- use Tailwind
- keep UI responsive

## Constraints

- create reusable components if they are resuable
- do not modify unrelated pages
- reuse shared components if they already exist

## Done

- page visually matches design
- login works with backend
- loading and error states work

display toast for login message
