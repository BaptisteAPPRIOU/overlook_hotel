package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO utilisé pour la création d’un employé via l’API.
 */
@Data
@Builder
public class CreateEmployeeRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String department;
    private String position;
}
