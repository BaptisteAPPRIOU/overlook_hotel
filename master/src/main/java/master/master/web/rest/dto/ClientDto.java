package master.master.web.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTOs for client operations exposed by REST controllers.
 * Used to transfer lightweight client information between the API and callers
 * without leaking internal entity details.
 */
public class ClientDto {

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  // Public view of a client's basic profile returned by endpoints
  public static class Info {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private int fidelityPoint;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  // Fields accepted when updating client data through the API
  public static class Update {
    @NotNull private Long userId;

    @Min(0)
    private Integer fidelityPoint;
  }
}
