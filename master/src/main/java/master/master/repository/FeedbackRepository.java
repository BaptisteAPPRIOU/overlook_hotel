package master.master.repository;

import java.util.List;
import master.master.domain.HotelFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access methods for hotel feedback records.
 */
public interface FeedbackRepository extends JpaRepository<HotelFeedback, Long> {

  /**
   * Finds all feedback entries written by a specific user.
   */
  List<HotelFeedback> findByUser_Id(Long userId);
}
