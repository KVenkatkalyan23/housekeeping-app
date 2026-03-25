package com.ibe.housekeeping.entity;

import com.ibe.housekeeping.entity.enums.ActorType;
import com.ibe.housekeeping.entity.enums.EventCategory;
import com.ibe.housekeeping.entity.enums.Severity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "activity_logs",
        indexes = {
                @Index(name = "idx_activity_logs_created_at", columnList = "created_at DESC"),
                @Index(name = "idx_activity_logs_entity", columnList = "target_entity_type, target_entity_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "event_code", nullable = false, length = 50)
    private String eventCode;

    @NotBlank
    @Size(max = 150)
    @Column(name = "event_title", nullable = false, length = 150)
    private String eventTitle;

    @NotBlank
    @Column(name = "event_message", nullable = false, columnDefinition = "TEXT")
    private String eventMessage;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_category", nullable = false, length = 30)
    private EventCategory eventCategory;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private Severity severity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20)
    private ActorType actorType;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Size(max = 150)
    @Column(name = "actor_name", length = 150)
    private String actorName;

    @Size(max = 50)
    @Column(name = "target_entity_type", length = 50)
    private String targetEntityType;

    @Column(name = "target_entity_id")
    private UUID targetEntityId;

    @Column(name = "related_staff_id")
    private UUID relatedStaffId;

    @Column(name = "related_room_id")
    private UUID relatedRoomId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
