// ReservationDto.java
package master.master.web.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class ReservationDto {

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create {
        @NotNull
        private Long roomId;
        @NotNull
        private LocalDate reservationDateStart;
        @NotNull
        private LocalDate reservationDateEnd;
    }
}
