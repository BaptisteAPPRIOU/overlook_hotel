// ReservationDto.java
package master.master.web.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReservationDto {

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Info implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long roomId;
    private LocalDate reservationDateStart;
    private LocalDate reservationDateEnd;
    private boolean isPayed;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Create implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull private Long roomId;
    @NotNull private LocalDate reservationDateStart;
    @NotNull private LocalDate reservationDateEnd;
  }
}
