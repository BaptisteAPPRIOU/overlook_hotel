package master.master.web.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO namespace for client profile payloads used by the client REST endpoints. */
public class ClientDto {

  /** Read-only client profile returned by the API. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Info {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private int fidelityPoint;
  }

  /** Payload used to update editable client profile fields. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Update {
    @NotNull private Long userId;

    @Min(0)
    private Integer fidelityPoint;
  }
}
