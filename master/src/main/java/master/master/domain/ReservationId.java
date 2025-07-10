package master.master.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for UserReservation, **sans** la date.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ReservationId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "room_id")
    private Long roomId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationId that)) return false;
        return Objects.equals(userId, that.userId)
                && Objects.equals(roomId, that.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roomId);
    }
}
