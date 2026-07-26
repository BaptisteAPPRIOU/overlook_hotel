package master.master.web.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {
  @NotBlank @Email private String email;

  @NotBlank
  @Size(min = 8, max = 72)
  private String password;

  @NotBlank private String firstName;

  @NotBlank private String lastName;
}
