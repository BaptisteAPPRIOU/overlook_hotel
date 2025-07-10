package master.master.repository;

import master.master.domain.ReservationId;
import master.master.domain.UserReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<UserReservation, ReservationId> {
    List<UserReservation> findByIdUserId(Long userId);
}
