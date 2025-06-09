// src/main/java/master/master/domain/Room.java
package master.master.domain;

import jakarta.persistence.*;
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
@Data                      // generates getters, setters, toString, equals & hashCode
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
