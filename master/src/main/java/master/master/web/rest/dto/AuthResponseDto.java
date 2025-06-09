package master.master.web.rest.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) representing the authentication response.
 * <p>
 * This class encapsulates the JWT token returned to the client after successful authentication.
 * </p>
 *
 * @author 
 */

@Getter
@Setter

public class AuthResponseDto {
    private final String token;

    public AuthResponseDto(String token) {
        this.token = token;
    }

}
