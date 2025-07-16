package master.master.web.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class ClientDto {

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        @NotNull
        private Long userId;

        @Min(0)
        private Integer fidelityPoint;
    }
}
