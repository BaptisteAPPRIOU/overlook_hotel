package master.master.repository;

import java.util.List;
import master.master.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
  List<Reservation> findByClientId(Long userId);
}
