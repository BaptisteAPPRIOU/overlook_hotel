package master.master.web.rest.dto;

import lombok.Getter;
import master.master.domain.RoleType;

/**
 * Data Transfer Object (DTO) representing a user response.
 * <p>
 * This class encapsulates user information to be sent in API responses.
 * </p>
 *
 * @author 
 */

@Getter

public class UserResponseDto {
    private final Integer id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final RoleType role;

    public UserResponseDto(Integer id, String firstName, String lastName, String email, RoleType role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }
}
