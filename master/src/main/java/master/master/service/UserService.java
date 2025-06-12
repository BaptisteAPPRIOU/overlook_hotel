package master.master.service;

import master.master.domain.User;
import master.master.web.rest.dto.LoginRequestDto;
import master.master.web.rest.dto.RegisterRequestDto;


public interface UserService {
    User register(RegisterRequestDto dto);

    String authenticateAndGetToken(LoginRequestDto dto);
}
