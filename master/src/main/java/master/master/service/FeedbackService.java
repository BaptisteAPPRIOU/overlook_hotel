package master.master.service;

import master.master.domain.Feedback;
import master.master.domain.User;
import master.master.mapper.FeedbackMapper;
import master.master.repository.FeedbackRepository;
import master.master.repository.UserRepository;
import master.master.web.rest.dto.FeedbackDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FeedbackService {
    private final FeedbackRepository repo;
    private final UserRepository userRepo;
    private final FeedbackMapper mapper;

    public FeedbackService(
            FeedbackRepository repo,
            UserRepository userRepo,
            FeedbackMapper mapper
    ) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.mapper = mapper;
    }

    @Transactional
    public FeedbackDto.Info create(Long userId, FeedbackDto.Create dto) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        Feedback fb = mapper.toEntity(dto);
        fb.setUser(user);
        return mapper.toDto(repo.save(fb));
    }

    public List<FeedbackDto.Info> findByUser(Long userId) {
        return repo.findByUser_Id(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
