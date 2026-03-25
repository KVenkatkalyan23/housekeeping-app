# Feature: Entities and Schema Mapping

## Goal

Implement all backend JPA entities based on `docs/database-schema.md`.

---

## Scope

- JPA entities
- Java enums
- table + column mappings
- relationships
- validations
- indexes (optimized only)
- unique constraints
- Lombok annotations

---

## Out of Scope

- repositories
- services
- controllers
- DTOs
- business logic

---

## General Rules

- Use UUID for all primary keys
- Use `@Entity` and `@Table`
- Use exact table and column names
- Use `@Column` where needed
- Keep entities inside feature-based structure (`feature/entity/`)
- Keep code clean and minimal

---

## Lombok (MANDATORY)

Use:

- `@Getter`
- `@Setter`
- `@NoArgsConstructor`
- `@AllArgsConstructor`
- `@Builder`

Rules:

- Do NOT use `@Data`
- Avoid circular references in `@ToString`

---

## Enums

Use:

```java
@Enumerated(EnumType.STRING)

Enums:

Role
AvailabilityStatus
RoomStatus
TaskStatus
TaskType
EventCategory
Severity
ActorType
Relationships
Use @ManyToOne
Use @JoinColumn
Use FetchType.LAZY

Rules:

Avoid unnecessary @OneToMany
Avoid bidirectional relationships unless required
Validation

Use:

@NotNull
@NotBlank
@Size
@Email
@Positive
Indexes

Use:

@Table(indexes = {...})

Rules:

Implement only indexes defined in database-schema.md
Maintain correct column order
Support composite indexes properly
Constraints

Use:

@Column(unique = true)
OR @Table(uniqueConstraints = {...})
Audit Fields

Each entity must include:
createdAt
updatedAt

## Entities

User
Shift
StaffProfile
Room
RoomStay
Attendance
LeaveRequest
CleaningTask
TaskAssignment
ActivityLog


## Done Criteria

All entities compile
Lombok works correctly
No manual getters/setters
Relationships are correct
Indexes are implemented
Validations are added
No unnecessary bidirectional mappings
```

Implement all the entities in
backend/src/main/java/com/ibe/housekeeping/entity
create a file for each entity.
