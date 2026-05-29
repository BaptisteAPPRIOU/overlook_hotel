package master.master.web.rest.dto;

import lombok.Getter;
import master.master.domain.RoleCode;

/**
 * Data Transfer Object (DTO) representing a user response.
 *
 * <p>This class encapsulates user information to be sent in API responses.
 *
 * @author
 */
@Getter
public class UserResponseDto {
  private final Long id;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final RoleCode role;

  /** Builds the response object sent after user lookup or creation flows. */
  public UserResponseDto(Long id, String firstName, String lastName, String email, RoleCode role) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.role = role;
  }
}
