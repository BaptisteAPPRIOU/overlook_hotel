package master.master.web.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO namespace for reservation request and response payloads. */
public class ReservationDto {

  /** Reservation details returned to clients. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Info {
    private Long userId;
    private Long roomId;
    private LocalDate reservationDateStart;
    private LocalDate reservationDateEnd;
    private boolean isPayed;
  }

  /** Payload required to create a reservation for a client and room. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Create {
    @NotNull private Long roomId;
    @NotNull private LocalDate reservationDateStart;
    @NotNull private LocalDate reservationDateEnd;
  }
}
