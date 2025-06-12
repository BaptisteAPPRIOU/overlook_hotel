package master.master.web.rest;

import jakarta.validation.Valid;
import master.master.domain.User;
import master.master.security.JwtUtil;
import master.master.service.ClientService;
import master.master.service.UserService;
import master.master.web.rest.dto.AuthResponseDto;
import master.master.web.rest.dto.LoginRequestDto;
import master.master.web.rest.dto.RegisterRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final ClientService clientService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService,
                          ClientService clientService,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.clientService = clientService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody RegisterRequestDto dto
    ) {
        User user = userService.register(dto);
        log.info("Nouvel utilisateur créé, id={} rôle={}", user.getId(), user.getRole());

        clientService.createFromUser(user);
        log.info("Enregistrement en table client effectué pour user={}", user.getId());

        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponseDto(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto dto
    ) {
        String token = userService.authenticateAndGetToken(dto);
        return ResponseEntity.ok(new AuthResponseDto(token));
    }
}
