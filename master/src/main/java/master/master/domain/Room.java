// src/main/java/master/master/domain/Room.java
package master.master.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Entity for Room.
 * Represents rooms/meeting spaces in the database.
import lombok.Data;

/**
 * Represents a hotel room entity with a unique room number, type, and occupancy status.
 * <p>
 * Fields:
 * <ul>
 *   <li><b>id</b>: The unique identifier for the room (auto-generated).</li>
 *   <li><b>roomNumber</b>: The unique number assigned to the room.</li>
 *   <li><b>roomType</b>: The type/category of the room (e.g., SINGLE, DOUBLE, SUITE).</li>
 *   <li><b>isOccupied</b>: Indicates whether the room is currently occupied.</li>
 * </ul>
 * 
 * Annotations:
 * <ul>
 *   <li><b>@Entity</b>: Marks this class as a JPA entity.</li>
 *   <li><b>@Data</b>: Lombok annotation to generate boilerplate code (getters, setters, etc.).</li>
 *   <li><b>@Id</b>, <b>@GeneratedValue</b>: Specifies the primary key and its generation strategy.</li>
 *   <li><b>@Column(unique = true)</b>: Ensures the room number is unique in the database.</li>
 *   <li><b>@Enumerated(EnumType.STRING)</b>: Stores the enum as a string in the database.</li>
 * </ul>
 */

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number", nullable = false, unique = true, length = 20)
    private String number;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RoomType type;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @Column(name = "price", precision = 10)
    private Double price;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "description", length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "room_amenities", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "amenity")
    private List<String> amenities;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "building", length = 50)
    private String building;

    @Column(name = "has_projector")
    @Builder.Default
    private Boolean hasProjector = false;

    @Column(name = "has_whiteboard")
    @Builder.Default
    private Boolean hasWhiteboard = false;

    @Column(name = "has_video_conference")
    @Builder.Default
    private Boolean hasVideoConference = false;

    @Column(name = "has_air_conditioning")
    @Builder.Default
    private Boolean hasAirConditioning = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDateTime nextMaintenanceDate;

    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = RoomStatus.AVAILABLE;
        }
        if (hasProjector == null) hasProjector = false;
        if (hasWhiteboard == null) hasWhiteboard = false;
        if (hasVideoConference == null) hasVideoConference = false;
        if (hasAirConditioning == null) hasAirConditioning = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isAvailable() {
        return RoomStatus.AVAILABLE.equals(this.status);
    }

    public boolean isOccupied() {
        return RoomStatus.OCCUPIED.equals(this.status);
    }

    public boolean isUnderMaintenance() {
        return RoomStatus.MAINTENANCE.equals(this.status) || RoomStatus.OUT_OF_ORDER.equals(this.status);
    }

    public boolean canBeBooked() {
        return isAvailable();
    }

    public String getFullName() {
        if (name != null && !name.trim().isEmpty()) {
            return number + " - " + name;
        }
        return number;
    }

    public boolean hasAmenity(String amenity) {
        return amenities != null && amenities.contains(amenity);
    }

    public int getAmenityCount() {
        return amenities != null ? amenities.size() : 0;
    }

    public boolean needsMaintenance() {
        return nextMaintenanceDate != null && nextMaintenanceDate.isBefore(LocalDateTime.now());
    }

    public boolean isOverdue() {
        return needsMaintenance() && !isUnderMaintenance();
    }

    /**
     * Get a description including key features
     */
    public String getFeatureDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Capacity: ").append(capacity);

        if (Boolean.TRUE.equals(hasProjector)) desc.append(" | Projector");
        if (Boolean.TRUE.equals(hasWhiteboard)) desc.append(" | Whiteboard");
        if (Boolean.TRUE.equals(hasVideoConference)) desc.append(" | Video Conference");
        if (Boolean.TRUE.equals(hasAirConditioning)) desc.append(" | A/C");

        if (amenities != null && !amenities.isEmpty()) {
            desc.append(" | Amenities: ").append(String.join(", ", amenities));
        }

        return desc.toString();
    }

    /**
     * Enum for room types.
     */
    public enum RoomType {
        CONFERENCE("Conference Room"),
        MEETING("Meeting Room"),
        OFFICE("Office"),
        TRAINING("Training Room"),
        BOARDROOM("Board Room"),
        HUDDLE("Huddle Room"),
        PHONE_BOOTH("Phone Booth"),
        LOUNGE("Lounge"),
        COLLABORATION("Collaboration Space"),
        PRESENTATION("Presentation Room");

        private final String displayName;

        RoomType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum for room status.
     */
    public enum RoomStatus {
        AVAILABLE("Available"),
        OCCUPIED("Occupied"),
        MAINTENANCE("Under Maintenance"),
        OUT_OF_ORDER("Out of Order"),
        RESERVED("Reserved"),
        CLEANING("Being Cleaned");

        private final String displayName;

        RoomStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
