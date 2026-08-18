package com.ccms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors server/models/Complaint.js.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @Column(length = 36)
    private String id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "student_id", length = 36)
    private String studentId; // reference to User.id

    @Builder.Default
    private String department = "General";

    @Builder.Default
    private String urgency = "low"; // low | medium | high | critical

    @Builder.Default
    private String status = "Pending"; // Pending | In Progress | Resolved | Rejected

    @Column(columnDefinition = "TEXT")
    private String progress;

    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
