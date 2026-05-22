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

  @Enumerated(EnumType.STRING)
  @Column(name = "room_type", nullable = false, length = 50)
  private RoomType roomType;

  @Column(name = "capacity", nullable = false)
  private Short capacity;

  @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal basePrice;

  @Enumerated(EnumType.STRING)
  @Column(name = "room_status", nullable = false, length = 30)
  private RoomStatus roomStatus;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "rooms_amenities",
      joinColumns = @JoinColumn(name = "id_room"),
      inverseJoinColumns = @JoinColumn(name = "id_amenity"))
  private Set<Amenity> amenities = new HashSet<>();

  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RoomPhoto> photos = new ArrayList<>();

  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RoomUnavailability> unavailabilities = new ArrayList<>();

  @OneToMany(mappedBy = "room")
  private List<Reservation> reservations = new ArrayList<>();

  public boolean canBeBooked() {
    return RoomStatus.AVAILABLE.equals(roomStatus);
  }

  public String getNumber() {
    return roomNumber;
  }

  public void setNumber(String number) {
    this.roomNumber = number;
  }

  public RoomType getType() {
    return roomType;
  }

  public void setType(RoomType type) {
    this.roomType = type;
  }

  public RoomStatus getStatus() {
    return roomStatus;
  }

  public void setStatus(RoomStatus status) {
    this.roomStatus = status;
  }

  public Double getPrice() {
    return basePrice == null ? null : basePrice.doubleValue();
  }

  public void setPrice(Double price) {
    this.basePrice = price == null ? null : BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
  }

  public void setCapacity(Integer capacity) {
    this.capacity = capacity == null ? null : capacity.shortValue();
  }

  public String getName() {
    return "Room " + roomNumber;
  }

  public void setName(String ignoredName) {}

  public String getImageUrl() {
    return photos.isEmpty() ? null : photos.get(0).getImageUrl();
  }

  public void setImageUrl(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) {
      return;
    }
    RoomPhoto photo = photos.isEmpty() ? new RoomPhoto() : photos.get(0);
    photo.setRoom(this);
    photo.setImageUrl(imageUrl);
    photo.setDisplayOrder((short) 0);
    if (photos.isEmpty()) {
      photos.add(photo);
    }
  }

  public Integer getFloorNumber() {
    return null;
  }

  public void setFloorNumber(Integer ignoredFloorNumber) {}

  public Boolean getHasProjector() {
    return false;
  }

  public void setHasProjector(Boolean ignored) {}

  public Boolean getHasWhiteboard() {
    return false;
  }

  public void setHasWhiteboard(Boolean ignored) {}

  public Boolean getHasVideoConference() {
    return false;
  }

  public void setHasVideoConference(Boolean ignored) {}

  public Boolean getHasAirConditioning() {
    return true;
  }

  public void setHasAirConditioning(Boolean ignored) {}

  public void setAmenities(java.util.List<String> ignoredAmenities) {}

  public void setLastMaintenanceDate(java.time.LocalDateTime ignored) {}

  public void setNextMaintenanceDate(java.time.LocalDateTime ignored) {}

  public void setCreatedAt(java.time.LocalDateTime ignored) {}

  public void setCreatedBy(String ignored) {}

  public void setUpdatedAt(java.time.LocalDateTime ignored) {}

  public static RoomBuilder builder() {
    return new RoomBuilder();
  }

  public static class RoomBuilder {
    private final Room room = new Room();

    public RoomBuilder number(String number) {
      room.setRoomNumber(number);
      return this;
    }

    public RoomBuilder roomNumber(String number) {
      return number(number);
    }

    public RoomBuilder name(String ignoredName) {
      return this;
    }

    public RoomBuilder type(RoomType type) {
      room.setRoomType(type);
      return this;
    }

    public RoomBuilder roomType(RoomType type) {
      return type(type);
    }

    public RoomBuilder capacity(Integer capacity) {
      room.capacity = capacity == null ? null : capacity.shortValue();
      return this;
    }

    public RoomBuilder capacity(Short capacity) {
      room.capacity = capacity;
      return this;
    }

    public RoomBuilder description(String description) {
      room.setDescription(description);
      return this;
    }

    public RoomBuilder price(Double price) {
      room.setPrice(price);
      return this;
    }

    public RoomBuilder basePrice(BigDecimal price) {
      room.setBasePrice(price);
      return this;
    }

    public RoomBuilder status(RoomStatus status) {
      room.setRoomStatus(status);
      return this;
    }

    public RoomBuilder roomStatus(RoomStatus status) {
      return status(status);
    }

    public RoomBuilder floorNumber(Integer ignoredFloorNumber) {
      return this;
    }

    public RoomBuilder imageUrl(String imageUrl) {
      room.setImageUrl(imageUrl);
      return this;
    }

    public RoomBuilder amenities(java.util.List<String> ignoredAmenities) {
      return this;
    }

    public RoomBuilder hasProjector(Boolean ignored) {
      return this;
    }

    public RoomBuilder hasVideoConference(Boolean ignored) {
      return this;
    }

    public RoomBuilder hasWhiteboard(Boolean ignored) {
      return this;
    }

    public RoomBuilder hasAirConditioning(Boolean ignored) {
      return this;
    }

    public RoomBuilder createdAt(java.time.LocalDateTime ignored) {
      return this;
    }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Room room)) return false;
    return id != null && Objects.equals(id, room.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
