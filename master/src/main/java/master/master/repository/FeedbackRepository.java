package master.master.repository;

import java.util.List;
import master.master.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
  List<Feedback> findByUser_Id(Long userId);
}
