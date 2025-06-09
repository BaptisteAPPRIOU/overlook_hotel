package master.master.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entity representing a reservation made by a user for a specific room.
 * Utilizes a composite primary key (ReservationId) consisting of user and room identifiers.
 *
 * <p>
 * Fields:
 * <ul>
 *   <li>{@code id} - Composite primary key for the reservation, embedding user and room IDs.</li>
 *   <li>{@code user} - The user who made the reservation.</li>
 *   <li>{@code room} - The room that is reserved.</li>
 *   <li>{@code reservationDateStart} - The start date of the reservation period.</li>
 *   <li>{@code reservationDateEnd} - The end date of the reservation period.</li>
 *   <li>{@code isPayed} - Indicates whether the reservation has been paid for.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Relationships:
 * <ul>
 *   <li>{@code @ManyToOne} with {@code User} and {@code Room} entities.</li>
 *   <li>{@code @EmbeddedId} for composite key management.</li>
 * </ul>
 * </p>
 */
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