package master.master.web.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
  // Minimal login payload: email and password used for authentication.
  @NotBlank @Email private String email;

  @NotBlank private String password;
}
