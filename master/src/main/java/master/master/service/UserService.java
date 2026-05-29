package master.master.service;

import master.master.domain.User;
import master.master.web.rest.dto.LoginRequestDto;
import master.master.web.rest.dto.RegisterRequestDto;

/**
 * Defines user registration, authentication, and lookup operations.
 */
public interface UserService {

  /**
   * Registers a new user account.
   */
  User register(RegisterRequestDto dto);

  /**
   * Authenticates credentials and returns a JWT token.
   */
  String authenticateAndGetToken(LoginRequestDto dto);

  /**
   * Finds a user by email address.
   */
  User findByEmail(String email);
}
