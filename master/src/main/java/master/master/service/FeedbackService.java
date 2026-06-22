package master.master.service;

import java.util.List;
import master.master.domain.HotelFeedback;
import master.master.domain.User;
import master.master.mapper.FeedbackMapper;
import master.master.repository.FeedbackRepository;
import master.master.repository.UserRepository;
import master.master.web.rest.dto.FeedbackDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class FeedbackService {
  private final FeedbackRepository repo;
  private final UserRepository userRepo;
  private final FeedbackMapper mapper;

  // Wire the repositories and mapper used to persist and list feedback.
  public FeedbackService(FeedbackRepository repo, UserRepository userRepo, FeedbackMapper mapper) {
    this.repo = repo;
    this.userRepo = userRepo;
    this.mapper = mapper;
  }

  // Create a feedback entry for one user.
  @Transactional
  public FeedbackDto.Info create(Long userId, FeedbackDto.Create dto) {
    User user =
        userRepo
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
    HotelFeedback fb = mapper.toEntity(dto);
    fb.setUser(user);
    return mapper.toDto(repo.save(fb));
  }

  // Return all feedback entries for one user.
  public List<FeedbackDto.Info> findByUser(Long userId) {
    return repo.findByUser_Id(userId).stream().map(mapper::toDto).toList();
  }
}
