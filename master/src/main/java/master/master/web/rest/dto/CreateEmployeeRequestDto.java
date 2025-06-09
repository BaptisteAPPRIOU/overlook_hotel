package master.master.web.rest.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO utilisé pour la création d’un employé via l’API.
 */
@Getter
@Setter
public class CreateEmployeeRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
