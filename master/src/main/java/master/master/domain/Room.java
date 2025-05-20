package master.master.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "room_id_seq")
    @SequenceGenerator(name = "room_id_seq", sequenceName = "room_id_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "room_number", nullable = false, length = 10, unique = true)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    @Column(name = "is_occupied")
    private final Boolean isOccupied = false;
}