package master.master.domain;

import jakarta.persistence.*;

/**
 * Represents a hotel room entity.
 * <p>
 * Each room has a unique room number, a type (as defined by {@link RoomType}),
 * and an occupancy status.
 * </p>
 *
 * <ul>
 *   <li>{@code id} - The unique identifier for the room (auto-generated).</li>
 *   <li>{@code roomNumber} - The unique number assigned to the room.</li>
 *   <li>{@code roomType} - The type/category of the room.</li>
 *   <li>{@code isOccupied} - Indicates whether the room is currently occupied.</li>
 * </ul>
 */
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    private boolean isOccupied;
}