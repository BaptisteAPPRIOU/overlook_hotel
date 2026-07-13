package master.master.web.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import master.master.domain.RoleCode;

public class UserSettingsDto {

  @Data
  public static class Profile {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private RoleCode role;
    private String token;

    public Profile(Long id, String firstName, String lastName, String email, RoleCode role) {
      this.id = id;
      this.firstName = firstName;
      this.lastName = lastName;
      this.email = email;
      this.role = role;
    }

    public Profile(
        Long id, String firstName, String lastName, String email, RoleCode role, String token) {
      this(id, firstName, lastName, email, role);
      this.token = token;
    }
  }

  @Data
  public static class Update {
    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;
  }

  @Data
  public static class PasswordUpdate {
    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 100)
    private String confirmPassword;
  }
}
