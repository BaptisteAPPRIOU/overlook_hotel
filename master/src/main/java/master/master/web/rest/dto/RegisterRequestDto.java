package master.master.web.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class RegisterRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;

}
