// src/main/java/master/master/domain/Room.java
package master.master.domain;

import jakarta.persistence.*;
import lombok.Data;       // or import lombok.Getter; import lombok.Setter;

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
