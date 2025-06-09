package master.master.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class UserReservation {
    @EmbeddedId
    private ReservationId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private Room room;

    private LocalDate reservationDateStart;
    private LocalDate reservationDateEnd;
    private boolean isPayed;
}