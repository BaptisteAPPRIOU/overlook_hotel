package master.master.web.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import master.master.domain.Room;
import master.master.domain.RoomReview;
import master.master.domain.User;
import master.master.repository.ReviewRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/room-reviews")
public class EmployeeRoomReviewController {

  private final ReviewRepository reviewRepository;

  public EmployeeRoomReviewController(ReviewRepository reviewRepository) {
    this.reviewRepository = reviewRepository;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public Map<String, Object> listRoomReviews() {
    List<RoomReview> reviews = reviewRepository.findAllByOrderByCreatedAtDesc();
    Double averageRating = reviewRepository.getAverageRating();
    long verifiedCount = reviews.stream().filter(review -> Boolean.TRUE.equals(review.getVerified())).count();

    Map<String, Object> response = new HashMap<>();
    response.put("averageRating", averageRating == null ? 0.0 : Math.round(averageRating * 10.0) / 10.0);
    response.put("totalReviews", reviews.size());
    response.put("verifiedReviews", verifiedCount);
    response.put("reviews", reviews.stream().map(this::toReviewMap).toList());
    return response;
  }

  private Map<String, Object> toReviewMap(RoomReview review) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", review.getId());
    map.put("rating", review.getRating());
    map.put("comment", review.getComment());
    map.put("createdAt", review.getCreatedAt());
    map.put("reviewDate", review.getCreatedAt() == null ? null : review.getCreatedAt().toLocalDate());
    map.put("verified", Boolean.TRUE.equals(review.getVerified()));
    map.put("anonymous", Boolean.TRUE.equals(review.getAnonymous()));
    map.put("response", review.getResponseContent());

    if (review.getReservation() != null && review.getReservation().getRoom() != null) {
      Room room = review.getReservation().getRoom();
      map.put("roomId", room.getId());
      map.put("roomNumber", room.getNumber());
      map.put("roomType", room.getType() == null ? null : room.getType().name());
    }

    if (Boolean.TRUE.equals(review.getAnonymous())) {
      map.put("author", "Anonymous client");
    } else if (review.getReservation() != null && review.getReservation().getClient() != null) {
      User user = review.getReservation().getClient().getUser();
      map.put("author", user == null ? "Client" : user.getFullName());
      map.put("authorId", user == null ? null : user.getId());
    } else {
      map.put("author", "Client");
    }

    return map;
  }
}
