//package master.master.service;
//
//import master.master.web.rest.dto.*;
//import org.springframework.stereotype.Service;
//import java.util.List;
//import java.util.ArrayList;
//import java.time.LocalDateTime;
//import java.util.Optional;
//
/// **
// * Service for managing room reviews and ratings.
// * Handles creation, retrieval, and management of room reviews.
// */
//@Service
//public class ReviewService {
//
//    // TODO: Inject actual repository/DAO dependencies
//    // private final ReviewRepository reviewRepository;
//    // private final RoomRepository roomRepository;
//    // private final EmployeeRepository employeeRepository;
//
//    /**
//     * Create a new room review.
//     */
//    public ReviewDto createReview(CreateReviewDto request) {
//        // TODO: Implement actual business logic
//        // Validate room exists
//        // Validate employee exists
//        // Check if employee has already reviewed this room
//        // Create and save review entity
//
//        return ReviewDto.builder()
//                .id(System.currentTimeMillis()) // Temporary ID generation
//                .roomId(request.getRoomId())
//                .roomName(request.getRoomName())
//                .employeeId(request.getEmployeeId())
//                .employeeName(request.getEmployeeName())
//                .rating(request.getRating())
//                .comment(request.getComment())
//                .reviewDate(LocalDateTime.now())
//                .build();
//    }
//
//    /**
//     * Get all reviews for a specific room.
//     */
//    public List<ReviewDto> getReviewsByRoom(Long roomId) {
//        // TODO: Implement actual repository query
//        // return reviewRepository.findByRoomIdOrderByReviewDateDesc(roomId);
//
//        // Mock data for now
//        List<ReviewDto> reviews = new ArrayList<>();
//
//        if (roomId.equals(1L)) {
//            reviews.add(ReviewDto.builder()
//                    .id(1L)
//                    .roomId(1L)
//                    .roomName("Conference Room A")
//                    .employeeId(101L)
//                    .employeeName("John Doe")
//                    .rating(5)
//                    .comment("Excellent room with great AV equipment. Perfect for presentations.")
//                    .reviewDate(LocalDateTime.now().minusDays(1))
//                    .build());
//
//            reviews.add(ReviewDto.builder()
//                    .id(2L)
//                    .roomId(1L)
//                    .roomName("Conference Room A")
//                    .employeeId(102L)
//                    .employeeName("Jane Smith")
//                    .rating(4)
//                    .comment("Good room, but the air conditioning could be better.")
//                    .reviewDate(LocalDateTime.now().minusDays(3))
//                    .build());
//        } else if (roomId.equals(2L)) {
//            reviews.add(ReviewDto.builder()
//                    .id(3L)
//                    .roomId(2L)
//                    .roomName("Meeting Room B")
//                    .employeeId(103L)
//                    .employeeName("Mike Johnson")
//                    .rating(3)
//                    .comment("Average room, suitable for small meetings.")
//                    .reviewDate(LocalDateTime.now().minusDays(2))
//                    .build());
//        }
//
//        return reviews;
//    }
//
//    /**
//     * Get all reviews by a specific employee.
//     */
//    public List<ReviewDto> getReviewsByEmployee(Long employeeId) {
//        // TODO: Implement actual repository query
//        // return reviewRepository.findByEmployeeIdOrderByReviewDateDesc(employeeId);
//
//        // Mock data for now
//        List<ReviewDto> reviews = new ArrayList<>();
//
//        if (employeeId.equals(101L)) {
//            reviews.add(ReviewDto.builder()
//                    .id(1L)
//                    .roomId(1L)
//                    .roomName("Conference Room A")
//                    .employeeId(101L)
//                    .employeeName("John Doe")
//                    .rating(5)
//                    .comment("Excellent room with great AV equipment. Perfect for presentations.")
//                    .reviewDate(LocalDateTime.now().minusDays(1))
//                    .build());
//
//            reviews.add(ReviewDto.builder()
//                    .id(4L)
//                    .roomId(3L)
//                    .roomName("Training Room C")
//                    .employeeId(101L)
//                    .employeeName("John Doe")
//                    .rating(4)
//                    .comment("Spacious room, good for training sessions.")
//                    .reviewDate(LocalDateTime.now().minusDays(5))
//                    .build());
//        }
//
//        return reviews;
//    }
//
//    /**
//     * Get recent reviews across all rooms.
//     * Used for dashboard display.
//     */
//    public List<ReviewDto> getRecentReviews(int limit) {
//        // TODO: Implement actual repository query
//        // return reviewRepository.findTopByOrderByReviewDateDesc(PageRequest.of(0, limit));
//
//        // Mock data for now
//        List<ReviewDto> recentReviews = new ArrayList<>();
//
//        recentReviews.add(ReviewDto.builder()
//                .id(1L)
//                .roomId(1L)
//                .roomName("Conference Room A")
//                .employeeId(101L)
//                .employeeName("John Doe")
//                .rating(5)
//                .comment("Excellent room with great AV equipment. Perfect for presentations.")
//                .reviewDate(LocalDateTime.now().minusDays(1))
//                .build());
//
//        recentReviews.add(ReviewDto.builder()
//                .id(3L)
//                .roomId(2L)
//                .roomName("Meeting Room B")
//                .employeeId(103L)
//                .employeeName("Mike Johnson")
//                .rating(3)
//                .comment("Average room, suitable for small meetings.")
//                .reviewDate(LocalDateTime.now().minusDays(2))
//                .build());
//
//        recentReviews.add(ReviewDto.builder()
//                .id(2L)
//                .roomId(1L)
//                .roomName("Conference Room A")
//                .employeeId(102L)
//                .employeeName("Jane Smith")
//                .rating(4)
//                .comment("Good room, but the air conditioning could be better.")
//                .reviewDate(LocalDateTime.now().minusDays(3))
//                .build());
//
//        // Limit results
//        return recentReviews.stream()
//                .limit(limit)
//                .toList();
//    }
//
//    /**
//     * Calculate average rating for a specific room.
//     */
//    public Double getAverageRatingByRoom(Long roomId) {
//        // TODO: Implement actual repository query
//        // return reviewRepository.getAverageRatingByRoomId(roomId);
//
//        List<ReviewDto> reviews = getReviewsByRoom(roomId);
//        if (reviews.isEmpty()) {
//            return 0.0;
//        }
//
//        double sum = reviews.stream()
//                .mapToInt(ReviewDto::getRating)
//                .sum();
//
//        return sum / reviews.size();
//    }
//
//    /**
//     * Get review statistics for a room.
//     */
//    public RoomReviewStatsDto getRoomReviewStats(Long roomId) {
//        List<ReviewDto> reviews = getReviewsByRoom(roomId);
//
//        if (reviews.isEmpty()) {
//            return RoomReviewStatsDto.builder()
//                    .roomId(roomId)
//                    .totalReviews(0)
//                    .averageRating(0.0)
//                    .ratingDistribution(new int[]{0, 0, 0, 0, 0}) // 1-5 stars
//                    .build();
//        }
//
//        int[] distribution = new int[5]; // Index 0 = 1 star, Index 4 = 5 stars
//        for (ReviewDto review : reviews) {
//            distribution[review.getRating() - 1]++;
//        }
//
//        return RoomReviewStatsDto.builder()
//                .roomId(roomId)
//                .totalReviews(reviews.size())
//                .averageRating(getAverageRatingByRoom(roomId))
//                .ratingDistribution(distribution)
//                .build();
//    }
//
//    /**
//     * Update an existing review.
//     */
//    public ReviewDto updateReview(Long reviewId, UpdateReviewDto request) {
//        // TODO: Implement actual business logic
//        // Find review by ID
//        // Validate employee can update this review (ownership check)
//        // Update review fields
//        // Save updated review
//
//        return ReviewDto.builder()
//                .id(reviewId)
//                .roomId(request.getRoomId())
//                .roomName(request.getRoomName())
//                .employeeId(request.getEmployeeId())
//                .employeeName(request.getEmployeeName())
//                .rating(request.getRating())
//                .comment(request.getComment())
//                .reviewDate(LocalDateTime.now()) // Updated timestamp
//                .build();
//    }
//
//    /**
//     * Delete a review.
//     */
//    public void deleteReview(Long reviewId) {
//        // TODO: Implement actual business logic
//        // Find review by ID
//        // Validate employee can delete this review (ownership check or admin)
//        // Delete review
//
//        // reviewRepository.deleteById(reviewId);
//        System.out.println("Review " + reviewId + " deleted");
//    }
//
//    /**
//     * Get top-rated rooms based on average ratings.
//     */
//    public List<TopRatedRoomDto> getTopRatedRooms(int limit) {
//        // TODO: Implement actual repository query with aggregation
//        // SELECT room_id, room_name, AVG(rating) as avg_rating, COUNT(*) as review_count
//        // FROM reviews GROUP BY room_id, room_name
//        // ORDER BY avg_rating DESC, review_count DESC
//        // LIMIT ?
//
//        // Mock data for now
//        List<TopRatedRoomDto> topRooms = new ArrayList<>();
//
//        topRooms.add(TopRatedRoomDto.builder()
//                .roomId(1L)
//                .roomName("Conference Room A")
//                .averageRating(4.5)
//                .reviewCount(2)
//                .build());
//
//        topRooms.add(TopRatedRoomDto.builder()
//                .roomId(3L)
//                .roomName("Training Room C")
//                .averageRating(4.0)
//                .reviewCount(1)
//                .build());
//
//        topRooms.add(TopRatedRoomDto.builder()
//                .roomId(2L)
//                .roomName("Meeting Room B")
//                .averageRating(3.0)
//                .reviewCount(1)
//                .build());
//
//        return topRooms.stream()
//                .limit(limit)
//                .toList();
//    }
//
//    /**
//     * Check if an employee has already reviewed a specific room.
//     */
//    public boolean hasEmployeeReviewedRoom(Long employeeId, Long roomId) {
//        // TODO: Implement actual repository query
//        // return reviewRepository.existsByEmployeeIdAndRoomId(employeeId, roomId);
//
//        return getReviewsByEmployee(employeeId).stream()
//                .anyMatch(review -> review.getRoomId().equals(roomId));
//    }
//
//    /**
//     * Get review count for dashboard statistics.
//     */
//    public Long getTotalReviewCount() {
//        // TODO: Implement actual repository query
//        // return reviewRepository.count();
//
//        return 10L; // Mock count
//    }
//}
