# Foundation Implementation Plan

## Goal

Set up the minimum shared frontend and backend foundation needed before starting feature modules.

This slice must only include:

- frontend app routing foundation
- global toast setup
- route-level error handling
- Redux Toolkit store + RTK Query base API
- backend global exception handling
- backend common API response wrapper

---

## Scope

### Frontend

1. Configure global toast using `react-toastify`
2. Add route-level error handling using `createBrowserRouter`
3. Create a reusable `ErrorPage`
4. Set up Redux Toolkit store
5. Set up RTK Query `baseApi`

### Backend

1. Create a reusable API response wrapper
2. Implement a global exception handler

---

## Frontend Plan

### 1. Toast Configuration

#### Objective

Enable app-wide toast support for future success and error notifications.

#### Tasks

- Install `react-toastify`
- Add `ToastContainer` at app level
- Keep default configuration simple and reusable

#### Expected Result

- Any page or component can use toast notifications later without additional setup

---

### 2. Router Setup with `createBrowserRouter`

#### Objective

Use modern React Router configuration as the app routing foundation.

#### Tasks

- Create router configuration using `createBrowserRouter`
- Add only one route for now:
    - `/` → simple placeholder home page
- Render the router through `RouterProvider`

#### Expected Result

- App starts with `/`
- Router structure is ready for future protected and public routes

---

### 3. Route-Level Error Handling

#### Objective

Provide a modern fallback UI for route and render errors.

#### Tasks

- Use `errorElement: <ErrorPage />` in router config
- Create `ErrorPage` component
- Read error details using React Router error helpers
- Show:
    - user-friendly title
    - readable error message
    - reload page button
    - go home button

#### Expected Result

- Broken routes or rendering errors show a proper fallback page instead of a blank screen

---

### 4. Redux Toolkit Store Setup

#### Objective

Initialize the global application store for future slices and API integration.

#### Tasks

- Create `store.ts`
- Configure Redux Toolkit store with no feature slices yet
- Register RTK Query reducer and middleware
- Export store types for future use

#### Expected Result

- App is wrapped with Redux `Provider`
- Store is ready for future features

---

### 5. RTK Query Base API Setup

#### Objective

Create a shared base API configuration for future backend integration.

#### Tasks

- Create `baseApi.ts`
- Use `createApi`
- Use `fetchBaseQuery`
- Configure a base URL placeholder from environment or shared config
- Register `baseApi.reducer`
- Register `baseApi.middleware`

#### Expected Result

- API layer is initialized
- Future endpoints can be injected cleanly by feature modules

---

## Frontend Files to Create or Update

### Toast Setup

- **Update** `frontend/src/App.tsx`

### Router Setup

- **Create** `frontend/src/app/routes/router.tsx`

### Error Page

- **Create** `frontend/src/app/routes/ErrorPage.tsx`

### Root Page

- **Create** `frontend/src/features/dashboard/pages/HomePage.tsx`

### Store Setup

- **Create** `frontend/src/app/store/store.ts`

### Base API

- **Create** `frontend/src/shared/api/baseApi.ts`

### App Entry Wiring

- **Update** `frontend/src/main.tsx`
- **Update** `frontend/src/App.tsx`

---

## Backend Plan

### 1. API Response Wrapper

#### Objective

Create a standard response structure for all future APIs.

#### Tasks

- Create `ApiResponse.java`
- Create `ErrorResponse.java`
- Keep the structure generic and reusable across all modules

#### Structure

- `ApiResponse<T>` → `success`, `data`, `error`
- `ErrorResponse` → `message`, `status`, `timestamp`, `details`

#### Expected Result

All APIs can return a consistent structure for success and failure responses.

---

### 2. Global Exception Handler

#### Objective

Centralize exception handling and return consistent error responses.

#### Tasks

- Create `GlobalExceptionHandler.java`
- Annotate it with `@RestControllerAdvice`
- Handle:
    - `Exception`
    - `RuntimeException`
    - validation exceptions, if available
- Return `ApiResponse.failure(...)`

#### Expected Result

- Controllers do not need repetitive try/catch blocks
- Errors are returned in a consistent JSON structure
- Backend error handling is centralized and reusable

---

## Backend Files to Create

### API Response Wrapper

- **Create** `backend/src/main/java/com/ibe/housekeeping/common/api/ApiResponse.java`
- **Create** `backend/src/main/java/com/ibe/housekeeping/common/api/ErrorResponse.java`

### Global Exception Handler

- **Create** `backend/src/main/java/com/ibe/housekeeping/common/exception/GlobalExceptionHandler.java`

---

## Acceptance Criteria

### Frontend

- `createBrowserRouter` is used
- `/` route renders successfully
- `errorElement: <ErrorPage />` is configured
- `ErrorPage` shows readable error details
- `ErrorPage` has reload and go home actions
- `ToastContainer` is configured globally
- Redux store is configured
- `baseApi` is registered in the store

### Backend

- Reusable response wrapper is created
- Global exception handler is implemented
- Errors return a consistent JSON structure
- Generic, runtime, and validation exceptions are centrally handled

---

## Out of Scope

Do not implement:

- authentication
- protected routes
- feature modules
- business logic
- feature-specific APIs
- dashboard widgets
- slices or endpoints beyond foundation

---

## Notes

- Keep code minimal and production-ready
- Follow the existing folder structure strictly
- Do not touch unrelated feature folders
