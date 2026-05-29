package master.master.repository;

import java.util.List;
import master.master.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access methods for reservations.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  /**
   * Finds all reservations owned by a client user id.
   */
  List<Reservation> findByClientId(Long userId);
}
