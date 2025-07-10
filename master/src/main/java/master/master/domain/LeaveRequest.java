package master.master.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for Leave Request.
 * Represents employee leave requests in the database.
 */
@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LeaveType type;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_by", length = 100)
    private String rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // Calculated field for leave duration in days
    @Transient
    public int getLeaveDurationDays() {
        if (startDate != null && endDate != null) {
            return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        }
        return 0;
    }

    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = LeaveStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for status checks
    public boolean isPending() {
        return LeaveStatus.PENDING.equals(this.status);
    }

    public boolean isApproved() {
        return LeaveStatus.APPROVED.equals(this.status);
    }

    public boolean isRejected() {
        return LeaveStatus.REJECTED.equals(this.status);
    }

    public boolean canBeModified() {
        return isPending();
    }

    public boolean canBeCancelled() {
        return isPending() || isApproved();
    }

    /**
     * Enum for leave request types.
     */
    public enum LeaveType {
        VACATION("Vacation"),
        SICK("Sick Leave"),
        PERSONAL("Personal Leave"),
        MATERNITY("Maternity Leave"),
        PATERNITY("Paternity Leave"),
        BEREAVEMENT("Bereavement Leave"),
        EMERGENCY("Emergency Leave"),
        STUDY("Study Leave"),
        UNPAID("Unpaid Leave");

        private final String displayName;

        LeaveType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum for leave request status.
     */
    public enum LeaveStatus {
        PENDING("Pending"),
        APPROVED("Approved"),
        REJECTED("Rejected"),
        CANCELLED("Cancelled"),
        WITHDRAWN("Withdrawn");

        private final String displayName;

        LeaveStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
