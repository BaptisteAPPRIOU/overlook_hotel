package master.master.repository;

import java.util.List;
import master.master.domain.HotelFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<HotelFeedback, Long> {
  List<HotelFeedback> findByUser_Id(Long userId);
}
