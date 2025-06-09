package master.master.domain;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key class for Reservation entity.
 * <p>
 * This class is used as an embedded ID for the Reservation entity,
 * combining userId and roomId to uniquely identify a reservation.
 * </p>
 *
 * <p>
 * Implements {@link Serializable} as required by JPA for primary key classes.
 * </p>
 *
 * <p>
 * Overrides {@code equals} and {@code hashCode} to ensure correct behavior
 * when used as a key in collections or by JPA.
 * </p>
 *
 * @see javax.persistence.Embeddable
 */
@Embeddable
public class ReservationId implements Serializable {
    private Long userId;
    private Long roomId;

    public ReservationId() {
    }

    public ReservationId(Long userId, Long roomId) {
        this.userId = userId;
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(roomId, that.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roomId);
    }
}