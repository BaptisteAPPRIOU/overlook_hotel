package master.master.web.rest.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for login requests.
 * <p>
 * This class encapsulates the user's email and password
 * required for authentication.
 * </p>
 */

@Getter
@Setter

public class LoginRequestDto {
    private String email;
    private String password;

}
