// src/main/java/master/master/domain/Room.java
package master.master.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Table(name = "room")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number", nullable = false, unique = true, length = 255)
    private String number;

    @Column(name = "type", nullable = false, length = 255)
    @Enumerated(EnumType.STRING)
    private RoomType type;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "floor_number")
    private Integer floorNumber;

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

    @Column(name = "last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDateTime nextMaintenanceDate;

    @Column(name = "name", nullable = true, length = 100)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "price", precision = 10)
    private Double price;

    @Column(name = "status", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "room_amenities", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "amenity")
    private List<String> amenities;


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
     * Enum for hotel room types.
     */
    public enum RoomType {
        STANDARD("Chambre Standard"),
        SUPERIOR("Chambre Supérieure"),
        DELUXE("Chambre Deluxe"),
        JUNIOR_SUITE("Junior Suite"),
        SUITE("Suite"),
        PRESIDENTIAL_SUITE("Suite Présidentielle"),
        FAMILY_ROOM("Chambre Familiale"),
        TWIN("Chambre Twin"),
        DOUBLE("Chambre Double"),
        SINGLE("Chambre Simple"),
        PENTHOUSE("Penthouse"),
        
        // Keep some meeting room types for backward compatibility during migration
        @Deprecated CONFERENCE("Conference Room"),
        @Deprecated MEETING("Meeting Room"),
        @Deprecated OFFICE("Office"),
        @Deprecated TRAINING("Training Room"),
        @Deprecated BOARDROOM("Board Room"),
        @Deprecated HUDDLE("Huddle Room"),
        @Deprecated PHONE_BOOTH("Phone Booth"),
        @Deprecated LOUNGE("Lounge"),
        @Deprecated COLLABORATION("Collaboration Space"),
        @Deprecated PRESENTATION("Presentation Room"),
        @Deprecated ROOM("Room"),
        @Deprecated EVENT("Event");

        private final String displayName;

        RoomType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * Check if this is a hotel room type (not a meeting room type).
         */
        public boolean isHotelRoom() {
            return !this.name().equals("CONFERENCE") && 
                   !this.name().equals("MEETING") && 
                   !this.name().equals("OFFICE") && 
                   !this.name().equals("TRAINING") && 
                   !this.name().equals("BOARDROOM") && 
                   !this.name().equals("HUDDLE") && 
                   !this.name().equals("PHONE_BOOTH") && 
                   !this.name().equals("LOUNGE") && 
                   !this.name().equals("COLLABORATION") && 
                   !this.name().equals("PRESENTATION") && 
                   !this.name().equals("ROOM") && 
                   !this.name().equals("EVENT");
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
