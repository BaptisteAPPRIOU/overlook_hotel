package master.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

@Getter
@Setter
@Entity
@Table(name = "user_reservation")
public class UserReservation {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "userId", column = @Column(name = "user_id")),
            @AttributeOverride(name = "roomId", column = @Column(name = "room_id"))
    })
    private ReservationId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("roomId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "reservation_date_start", nullable = false)
    private LocalDate reservationDateStart;

    @Column(name = "reservation_date_end", nullable = false)
    private LocalDate reservationDateEnd;

    @Column(name = "payed", nullable = false)
    private boolean payed;
}
