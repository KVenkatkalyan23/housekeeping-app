# 🧠 Project: Housekeeping Management System

## 🎯 Overview

This is a housekeeping management system.

The project is built using:

- Backend: Spring Boot, Spring Security, JWT, PostgreSQL, MapStruct,Lombok
- Frontend: React, TypeScript, RTK, Tailwind, Vite

The system manages:

- staff, rooms, bookings
- attendance tracking
- cleaning task generation
- task allocation and reassignment
- leave and relocation handling
- shortfall detection
- admin and staff dashboards

---

# ⚙️ Working Principles

## 1. Vertical Feature Development

- Always implement **one feature at a time**
- Each feature must include:
  - backend (entity, service, API)
  - frontend (UI + state + API integration)

## 2. Do Not Mix Features

- Never implement multiple unrelated features in one task
- Keep changes scoped and minimal

## 3. Incremental Progress

- Every feature must result in a **working flow**
- Avoid incomplete layers (e.g., backend-only or frontend-only)

---

# 📁 Repository Structure

## Root

- backend/ → Spring Boot application
- frontend/ → React application
- docs/feature-plans/ → feature design notes
- docs/designs/ → UI designs/screenshots

## Backend (feature-based structure)

Each feature should follow:

🧪 Definition of Done (VERY IMPORTANT)

A feature is considered complete only if:

✅ Backend:

Entity, Repository, Service, Controller implemented
API tested (Postman/manual)
Validation & error handling added

✅ Frontend:

UI implemented
API integrated
State managed properly

✅ UX:

Loading states handled
Error states handled
Empty states handled

✅ General:

No console errors
App builds and runs successfully

# ⚙️ Commands

## General Rules

- Always use scripts from the `/scripts` folder
- Always run relevant scripts to verify changes after implementation

---

## Backend

### Start Backend

```bash
./scripts/dev-backend.sh
```

## Frontend

### Start Frontend

```bash
./scripts/dev-frontend.sh
```
