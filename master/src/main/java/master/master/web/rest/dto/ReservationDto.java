package master.master.web.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReservationDto {

  /*
   * DTOs for reservation-related API interactions.
   * Contains simplified payloads for creating and returning reservation data.
   */

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  // Overview of reservation details exposed to clients
  public static class Info {
    private Long userId;
    private Long roomId;
    private LocalDate reservationDateStart;
    private LocalDate reservationDateEnd;
    private boolean isPayed;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  // Payload required to create a new reservation
  public static class Create {
    @NotNull private Long roomId;
    @NotNull private LocalDate reservationDateStart;
    @NotNull private LocalDate reservationDateEnd;
  }
}
