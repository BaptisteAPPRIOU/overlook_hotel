package master.master.repository;

import java.util.List;
import master.master.domain.ReservationId;
import master.master.domain.UserReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<UserReservation, ReservationId> {
  List<UserReservation> findByIdUserId(Long userId);
}
