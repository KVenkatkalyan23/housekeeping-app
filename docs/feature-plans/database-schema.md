# Database Schema

## Tables

### users

- id: UUID (PK)
- username: VARCHAR(100) NOT NULL UNIQUE
- password: VARCHAR(255) NOT NULL
- role: ENUM NOT NULL
- created_at: TIMESTAMP NOT NULL
- updated_at: TIMESTAMP NOT NULL

Indexes:

- username UNIQUE
- idx_users_role (role)

---

### shifts

- id: UUID (PK)
- shift_code: VARCHAR(30) NOT NULL UNIQUE
- shift_name: VARCHAR(50) NOT NULL
- start_time: TIME NOT NULL
- end_time: TIME NOT NULL
- duration_minutes: INT NOT NULL (> 0)
- created_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- updated_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

Indexes:

- shift_code UNIQUE

---

### staff_profiles

- id: UUID (PK)
- user_id: UUID NOT NULL UNIQUE (FK → users.id)
- full_name: VARCHAR(150) NOT NULL
- phone: VARCHAR(30)
- email: VARCHAR(120)
- current_shift_id: UUID NULL (FK → shifts.id)
- availability_status: VARCHAR(20) NOT NULL DEFAULT 'OFF_DUTY'
- created_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- updated_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

Indexes:

- user_id UNIQUE
- idx_staff_profiles_shift_status (current_shift_id, availability_status)

---

### room

- id: UUID (PK)
- room_number: INT NOT NULL UNIQUE
- room_status: VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
- created_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- updated_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

Indexes:

- room_number UNIQUE

---

### room_stay

- id: UUID (PK)
- room_id: UUID NOT NULL (FK → room.id)
- check_in_date: DATE NOT NULL
- check_out_date: DATE NOT NULL
- is_cleaned: BOOLEAN NOT NULL DEFAULT FALSE
- created_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- updated_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

Indexes:

- idx_room_stay_room_id (room_id)
- idx_room_stay_checkout_cleaned (check_out_date, is_cleaned)

---

### attendance

- id: UUID (PK)
- staff_id: UUID NOT NULL (FK → staff_profiles.id)
- shift_id: UUID NOT NULL (FK → shifts.id)
- work_date: DATE NOT NULL
- clock_in_time: TIMESTAMP NULL
- clock_out_time: TIMESTAMP NULL
- worked_minutes: INT NOT NULL DEFAULT 0
- created_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- updated_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

Indexes:

- idx_attendance_staff_date (staff_id, work_date)
- idx_attendance_date_shift (work_date, shift_id)

---

### leave_requests

- id: UUID (PK)
- staff_id: UUID NOT NULL (FK → staff_profiles.id)
- leave_start_date: DATE NOT NULL
- leave_end_date: DATE NOT NULL
- leave_type: VARCHAR(20) NOT NULL
- reason: VARCHAR(500)
- status: VARCHAR(20) NOT NULL DEFAULT 'APPROVED'
- requested_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- created_by_user_id: UUID NULL (FK → users.id)
- updated_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

Indexes:

- idx_leave_requests_dates (leave_start_date, leave_end_date)
- idx_leave_requests_staff_status (staff_id, status)

---

### cleaning_tasks

- id: UUID (PK)
- room_id: UUID NOT NULL (FK → room.id)
- task_date: DATE NOT NULL
- shift_id: UUID NULL (FK → shifts.id)
- task_type: VARCHAR(20) NOT NULL
- priority_order: INT NOT NULL
- estimated_minutes: INT NOT NULL
- task_status: VARCHAR(20) NOT NULL DEFAULT 'PENDING'
- source_stay_id: UUID NULL (FK → room_stay.id)
- created_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- updated_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

Indexes:

- idx_cleaning_tasks_date_shift (task_date, shift_id)
- idx_cleaning_tasks_priority (task_date, shift_id, priority_order)
- idx_cleaning_tasks_status_date (task_status, task_date)

---

### task_assignments

- id: UUID (PK)
- cleaning_task_id: UUID NOT NULL (FK → cleaning_tasks.id)
- staff_id: UUID NULL (FK → staff_profiles.id)
- created_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- updated_at: TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

Indexes:

- idx_task_assignments_task_id (cleaning_task_id)
- idx_task_assignments_staff_id (staff_id)

---

### activity_logs

- id: UUID (PK)
- event_code: VARCHAR(50) NOT NULL
- event_title: VARCHAR(150) NOT NULL
- event_message: TEXT NOT NULL
- event_category: VARCHAR(30) NOT NULL
- severity: VARCHAR(20) NOT NULL
- actor_type: VARCHAR(20) NOT NULL
- actor_user_id: UUID NULL
- actor_name: VARCHAR(150) NULL
- target_entity_type: VARCHAR(50) NULL
- target_entity_id: UUID NULL
- related_staff_id: UUID NULL
- related_room_id: UUID NULL
- metadata: JSONB NULL
- created_at: TIMESTAMPTZ NOT NULL DEFAULT NOW()

Indexes:

- idx_activity_logs_created_at (created_at DESC)
- idx_activity_logs_entity (target_entity_type, target_entity_id)

---

## ENUMS

### role

- ADMIN
- STAFF

### availability_status

- ON_DUTY
- OFF_DUTY

### room_status

- ACTIVE
- INACTIVE

### task_status

- PENDING
- ASSIGNED
- IN_PROGRESS
- COMPLETED
- CANCELLED

### task_type

- DEEP_CLEAN
- DAILY_CLEAN
- VACANT_CLEAN

### event_category

- TASK
- ATTENDANCE
- LEAVE
- SHIFT
- SYSTEM
- ROOM

### severity

- SUCCESS
- INFO
- WARNING
- ERROR

### actor_type

- USER
- SYSTEM
