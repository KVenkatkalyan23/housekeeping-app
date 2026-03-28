## Goal

Implement the admin dashboard page matching the provided design, with a left sidebar, top header, critical shortfall alert banner, KPI cards, live capacity vs workload chart, and resource discrepancy analysis section.

---

## Design Source

- `docs/designs/admin-portal/dashboard.png`

---

## Route

- `/admin/dashboard`

---

## Access Rules

- admin-only page
- authenticated `ADMIN` users can access
- `STAFF` users must not access
- unauthenticated users redirect to login

---

## Folder Requirement

All new dashboard-specific frontend files must be created under:

```text
src/features/admin-portal/dashboard

Use this structure.

Files (MANDATORY)
Create inside src/features/admin-portal/dashboard
api.ts
types.ts
slice.ts or services.ts depending on current project pattern
pages/AdminDashboardPage.tsx
components/DashboardSidebar.tsx
components/DashboardHeader.tsx
components/ShortfallAlertBanner.tsx
components/KpiCardsSection.tsx
components/KpiCard.tsx
components/CapacityWorkloadChart.tsx
components/ResourceDiscrepancySection.tsx
Update only if needed
route registration file
admin layout file if one already exists
store registration file if dashboard slice must be registered
shared API client if new request helpers are needed
Scope
In Scope
full admin dashboard page UI
sidebar navigation UI
top header UI
shortfall alert banner
KPI cards section
live capacity vs workload chart
resource discrepancy analysis section
static or mocked data first if backend API is not ready
clean responsive layout for desktop
API integration structure prepared
Out of Scope
implementing backend dashboard APIs unless explicitly added later
real notifications behavior
help center functionality
logout business logic changes beyond wiring existing logout action
full admin portal for rooms/staff/tasks/attendance/logs/leave pages
support request workflow behind button
UI Layout Requirements
1. Overall Layout

The page should have:

fixed or stable left sidebar
top header across main content area
content area with vertical spacing between sections
desktop-first layout matching the design
Main structure
sidebar on left
main content on right
dashboard content inside padded container
2. Sidebar

Implement a sidebar similar to the design.

Sidebar content

Top:

product/app title
subtitle/tagline

Navigation items:

Dashboard
Rooms
Staff
Tasks
Attendance
Logs
Leave

Bottom:

Help Center
Logout
Sidebar behavior
highlight active item: Dashboard
non-dashboard items can be placeholders if routes are not ready
use existing route paths if already present
if routes do not exist, keep buttons visually present but safe
3. Top Header

Implement a compact top header.

Header content

Right side should include:

notification bell icon
admin name
admin role/subtitle
profile avatar
Behavior
notification icon is visual only for now
profile area is visual only unless existing profile behavior already exists
4. Critical Shortfall Alert Banner

Large red alert banner at top of content area.

Content
small badge/label like CRITICAL ALERT
large headline showing staffing shortfall percentage
descriptive text showing current shift shortfall
CTA button: Request Support
Behavior
use dashboard data source
if no shortfall exists, the banner may:
be hidden
or render a neutral/healthy state if desired
for first version, keep visible if mocked data is used
Data needed
shortfallPercent
shortfallMessage
optional additionalStaffRequired
5. KPI Cards Section

Render 3 cards in a row on desktop.

Card 1: Inventory Status

Display:

total registered rooms
occupied count
vacant count
occupancy rate
Card 2: Workforce Efficiency

Display:

utilization percentage
progress bar
supporting description text
Card 3: SLA Performance

Display:

task completion rate
delta vs yesterday
supporting text
Layout rules
each card should be visually distinct
keep card spacing and proportions close to design
cards must be reusable through a generic KpiCard component where practical
6. Live Capacity vs Workload Chart

Implement a chart section below KPI cards.

Section content
title: Live Capacity vs. Workload
subtitle text
legend for:
Available
Required
bar chart showing time labels and two values
Data points in design

Example labels:

6AM
9AM
12PM
3PM (PEAK)
6PM
9PM
Chart behavior
use a single chart component
use current chart library already present in project if one exists
if no chart library is used yet, prefer a lightweight approach aligned with current stack
visually match the design, but exact pixel perfection is not required
include critical threshold marker / overload label if practical
Data needed

For each time slot:

label
availableHours
requiredHours
7. Resource Discrepancy Analysis Section

Implement a lower section with two parts.

Left side

Display:

section title
cleaning volume required
current roster availability
horizontal progress bars / comparison bars
Right side

Display:

warning icon area
Action Required
short explanatory message
Data needed
requiredHours
availableHours
deltaHours
optional estimated operational impact message
Data Model / Types
DashboardPageData

Define a main dashboard response/type model that includes:

Shortfall
shortfallPercent
shortfallMessage
additionalStaffRequired
isCritical
Inventory
totalRooms
occupiedRooms
vacantRooms
occupancyRate
Workforce
utilizationPercent
description
SLA
completionRate
deltaVsYesterday
completedTasks
totalAssignedTasks
Capacity vs Workload
timeSeries: array of
label
availableHours
requiredHours
Discrepancy
requiredHours
availableHours
deltaHours
impactMessage
Current Admin
displayName
roleLabel
avatarUrl optional
Data Source Strategy
Preferred approach

Build the page so it can work with:

mocked data first
real API integration later without refactor
api.ts

Create functions for:

getAdminDashboardData()
types.ts

Create all dashboard-specific types

slice.ts or service state

Manage:

loading
error
data
Mock Data Requirement

If backend API is not ready, provide local mocked dashboard data inside this feature and wire the page through the same state shape that real API will use later.

Do not hardcode random values directly inside JSX.
Keep mock data centralized.

Frontend State Requirements

State should include:

data
loading
error

Optional:

lastUpdatedAt

On page load:

fetch dashboard data
render loading state first
then render dashboard
Loading / Error / Empty States
Loading
show skeletons or simple loading placeholders for:
banner
cards
chart
discrepancy section
Error
show clear error state with retry button or retry action
Empty
if API returns no data, show safe empty state instead of broken layout
Component Responsibilities
pages/AdminDashboardPage.tsx
assemble the page
fetch dashboard state
render sidebar + header + all sections
components/DashboardSidebar.tsx
render left navigation
active dashboard state
help + logout section
components/DashboardHeader.tsx
render top-right admin header content
components/ShortfallAlertBanner.tsx
render critical alert banner and CTA
components/KpiCardsSection.tsx
render 3 KPI cards in responsive grid
components/KpiCard.tsx
reusable card component for KPI presentation
components/CapacityWorkloadChart.tsx
render chart area, legend, labels, threshold marker if supported
components/ResourceDiscrepancySection.tsx
render lower comparison + action required section
Styling Requirements
use Tailwind
keep styling close to design
desktop-first layout
rounded cards and sections
clear spacing hierarchy
use subtle shadows/borders where needed
preserve the red critical alert emphasis
keep typography consistent and clean

Do not introduce unrelated design systems or large styling refactors.

Routing Requirements
add admin dashboard route if not already present
ensure it is protected for ADMIN
reuse existing role-based route guard if the project already has one
Reuse Rules
reuse shared UI patterns/components only if they already fit
do not move dashboard files outside src/features/admin-portal/dashboard
do not refactor unrelated admin modules
Suggested Implementation Sequence
create dashboard folder structure
create dashboard types
create API/mock data layer
create state management for dashboard fetch
build page shell with sidebar + header
build shortfall banner
build KPI cards section
build capacity vs workload chart
build discrepancy section
wire route protection
add loading/error/empty states
polish styling to match design
Testing / Verification

Verify:

admin can access page
staff cannot access page
sidebar renders correctly
active dashboard nav item is highlighted
header renders correctly
shortfall banner renders
KPI cards render correct values
chart renders without layout issues
discrepancy section renders correct values
loading state works
error state works
page uses files only inside src/features/admin-portal/dashboard for dashboard-specific logic
Done Criteria
admin dashboard page exists and matches design closely
dashboard-specific files are created under src/features/admin-portal/dashboard
admin-only route works
sidebar + header + all main sections render correctly
mock/API-backed data flow is clean
loading/error/empty states are handled
implementation is modular and maintainable
```
