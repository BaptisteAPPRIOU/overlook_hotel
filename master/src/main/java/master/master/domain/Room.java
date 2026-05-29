package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_room")
  private Long id;

  @Column(name = "room_number", nullable = false, unique = true, length = 20)
  private String roomNumber;

  // Enum values are stored as strings to avoid ordinal changes breaking existing data.
  @Enumerated(EnumType.STRING)
  @Column(name = "room_type", nullable = false, length = 50)
  private RoomType roomType;

  @Column(name = "capacity", nullable = false)
  private Short capacity;

  // BigDecimal keeps prices precise and avoids floating-point rounding issues.
  @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal basePrice;

  @Enumerated(EnumType.STRING)
  @Column(name = "room_status", nullable = false, length = 30)
  private RoomStatus roomStatus;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  // Room owns this many-to-many relation through the rooms_amenities join table.
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "rooms_amenities",
      joinColumns = @JoinColumn(name = "id_room"),
      inverseJoinColumns = @JoinColumn(name = "id_amenity"))
  private Set<Amenity> amenities = new HashSet<>();

  // Photos are removed automatically when they are detached from the room.
  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RoomPhoto> photos = new ArrayList<>();

  // Unavailability periods are owned by the room lifecycle.
  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RoomUnavailability> unavailabilities = new ArrayList<>();

  // Reservations are kept without cascading deletes to preserve booking history.
  @OneToMany(mappedBy = "room")
  private List<Reservation> reservations = new ArrayList<>();

  /**
   * Checks whether the room can currently be offered for booking.
   */
  public boolean canBeBooked() {
    return RoomStatus.AVAILABLE.equals(roomStatus);
  }

  /**
   * Compatibility accessor that returns the room number as a generic number field.
   */
  public String getNumber() {
    return roomNumber;
  }

  /**
   * Compatibility mutator that stores a generic number field as the room number.
   */
  public void setNumber(String number) {
    this.roomNumber = number;
  }

  /**
   * Compatibility accessor that exposes the room type as a generic type field.
   */
  public RoomType getType() {
    return roomType;
  }

  /**
   * Compatibility mutator that stores a generic type field as the room type.
   */
  public void setType(RoomType type) {
    this.roomType = type;
  }

  /**
   * Compatibility accessor that exposes the room status as a generic status field.
   */
  public RoomStatus getStatus() {
    return roomStatus;
  }

  /**
   * Compatibility mutator that stores a generic status field as the room status.
   */
  public void setStatus(RoomStatus status) {
    this.roomStatus = status;
  }

  /**
   * Returns the base price as a Double for legacy callers.
   */
  public Double getPrice() {
    return basePrice == null ? null : basePrice.doubleValue();
  }

  /**
   * Converts a Double price to BigDecimal with two decimal places.
   */
  public void setPrice(Double price) {
    this.basePrice = price == null ? null : BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Converts an Integer capacity to the Short value stored by the entity.
   */
  public void setCapacity(Integer capacity) {
    this.capacity = capacity == null ? null : capacity.shortValue();
  }

  /**
   * Builds a simple display name from the room number.
   */
  public String getName() {
    return "Room " + roomNumber;
  }

  /**
   * Keeps compatibility with callers that still submit a room name.
   */
  public void setName(String ignoredName) {}

  /**
   * Returns the first room photo URL as the primary image.
   */
  public String getImageUrl() {
    return photos.isEmpty() ? null : photos.get(0).getImageUrl();
  }

  /**
   * Creates or updates the primary room photo from a single image URL.
   */
  public void setImageUrl(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) {
      return;
    }
    // The first photo is treated as the primary image for legacy form bindings.
    RoomPhoto photo = photos.isEmpty() ? new RoomPhoto() : photos.get(0);
    photo.setRoom(this);
    photo.setImageUrl(imageUrl);
    photo.setDisplayOrder((short) 0);
    if (photos.isEmpty()) {
      photos.add(photo);
    }
  }

  /**
   * Compatibility accessor for older room forms that expected a floor number.
   */
  public Integer getFloorNumber() {
    return null;
  }

  /**
   * Keeps compatibility with older room forms that submitted a floor number.
   */
  public void setFloorNumber(Integer ignoredFloorNumber) {}

  /**
   * Compatibility accessor for older room forms that expected a projector flag.
   */
  public Boolean getHasProjector() {
    return false;
  }

  /**
   * Keeps compatibility with older room forms that submitted a projector flag.
   */
  public void setHasProjector(Boolean ignored) {}

  /**
   * Compatibility accessor for older room forms that expected a whiteboard flag.
   */
  public Boolean getHasWhiteboard() {
    return false;
  }

  /**
   * Keeps compatibility with older room forms that submitted a whiteboard flag.
   */
  public void setHasWhiteboard(Boolean ignored) {}

  /**
   * Compatibility accessor for older room forms that expected a video conference flag.
   */
  public Boolean getHasVideoConference() {
    return false;
  }

  /**
   * Keeps compatibility with older room forms that submitted a video conference flag.
   */
  public void setHasVideoConference(Boolean ignored) {}

  /**
   * Compatibility accessor for older room forms that expected an air conditioning flag.
   */
  public Boolean getHasAirConditioning() {
    return true;
  }

  /**
   * Keeps compatibility with older room forms that submitted an air conditioning flag.
   */
  public void setHasAirConditioning(Boolean ignored) {}

  /**
   * Keeps compatibility with older room forms that submitted amenities as strings.
   */
  public void setAmenities(java.util.List<String> ignoredAmenities) {}

  /**
   * Keeps compatibility with older room forms that submitted maintenance metadata.
   */
  public void setLastMaintenanceDate(java.time.LocalDateTime ignored) {}

  /**
   * Keeps compatibility with older room forms that submitted maintenance metadata.
   */
  public void setNextMaintenanceDate(java.time.LocalDateTime ignored) {}

  /**
   * Keeps compatibility with older room forms that submitted audit metadata.
   */
  public void setCreatedAt(java.time.LocalDateTime ignored) {}

  /**
   * Keeps compatibility with older room forms that submitted audit metadata.
   */
  public void setCreatedBy(String ignored) {}

  /**
   * Keeps compatibility with older room forms that submitted audit metadata.
   */
  public void setUpdatedAt(java.time.LocalDateTime ignored) {}

  /**
   * Creates a lightweight builder for tests and legacy code paths.
   */
  public static RoomBuilder builder() {
    return new RoomBuilder();
  }

  /**
   * Builder kept in the entity to support existing code without adding Lombok @Builder.
   */
  public static class RoomBuilder {
    private final Room room = new Room();

    /**
     * Sets the room number through the builder.
     */
    public RoomBuilder number(String number) {
      room.setRoomNumber(number);
      return this;
    }

    /**
     * Alias for number to support callers using the entity field name.
     */
    public RoomBuilder roomNumber(String number) {
      return number(number);
    }

    /**
     * Ignores the legacy room name field because the entity derives it from the number.
     */
    public RoomBuilder name(String ignoredName) {
      return this;
    }

    /**
     * Sets the room type through the builder.
     */
    public RoomBuilder type(RoomType type) {
      room.setRoomType(type);
      return this;
    }

    /**
     * Alias for type to support callers using the entity field name.
     */
    public RoomBuilder roomType(RoomType type) {
      return type(type);
    }

    /**
     * Converts an Integer capacity to the Short value stored by the entity.
     */
    public RoomBuilder capacity(Integer capacity) {
      room.capacity = capacity == null ? null : capacity.shortValue();
      return this;
    }

    /**
     * Sets the capacity using the entity field type.
     */
    public RoomBuilder capacity(Short capacity) {
      room.capacity = capacity;
      return this;
    }

    /**
     * Sets the room description through the builder.
     */
    public RoomBuilder description(String description) {
      room.setDescription(description);
      return this;
    }

    /**
     * Sets the price through the legacy Double-based API.
     */
    public RoomBuilder price(Double price) {
      room.setPrice(price);
      return this;
    }

    /**
     * Sets the base price using the entity field type.
     */
    public RoomBuilder basePrice(BigDecimal price) {
      room.setBasePrice(price);
      return this;
    }

    /**
     * Sets the room status through the builder.
     */
    public RoomBuilder status(RoomStatus status) {
      room.setRoomStatus(status);
      return this;
    }

    /**
     * Alias for status to support callers using the entity field name.
     */
    public RoomBuilder roomStatus(RoomStatus status) {
      return status(status);
    }

    /**
     * Ignores the legacy floor number field because it is not stored by this entity.
     */
    public RoomBuilder floorNumber(Integer ignoredFloorNumber) {
      return this;
    }

    /**
     * Sets the primary room image through the builder.
     */
    public RoomBuilder imageUrl(String imageUrl) {
      room.setImageUrl(imageUrl);
      return this;
    }

    /**
     * Ignores legacy string amenities because amenities are managed as Amenity entities.
     */
    public RoomBuilder amenities(java.util.List<String> ignoredAmenities) {
      return this;
    }

    /**
     * Ignores the legacy projector flag because it is not stored by this entity.
     */
    public RoomBuilder hasProjector(Boolean ignored) {
      return this;
    }

    /**
     * Ignores the legacy video conference flag because it is not stored by this entity.
     */
    public RoomBuilder hasVideoConference(Boolean ignored) {
      return this;
    }

    /**
     * Ignores the legacy whiteboard flag because it is not stored by this entity.
     */
    public RoomBuilder hasWhiteboard(Boolean ignored) {
      return this;
    }

    /**
     * Ignores the legacy air conditioning flag because it is not stored by this entity.
     */
    public RoomBuilder hasAirConditioning(Boolean ignored) {
      return this;
    }

    /**
     * Ignores legacy creation metadata because the room entity does not store it.
     */
    public RoomBuilder createdAt(java.time.LocalDateTime ignored) {
      return this;
    }

    /**
     * Builds the room and applies safe defaults required by non-null columns.
     */
    public Room build() {
      if (room.getRoomStatus() == null) {
        room.setRoomStatus(RoomStatus.AVAILABLE);
      }
      if (room.getBasePrice() == null) {
        room.setBasePrice(BigDecimal.ZERO);
      }
      return room;
    }
  }

  /**
   * Compares rooms by their persisted identifier to keep entity equality stable.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Room room)) return false;
    return id != null && Objects.equals(id, room.id);
  }

  /**
   * Uses the entity class hash code to stay consistent before and after persistence.
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
