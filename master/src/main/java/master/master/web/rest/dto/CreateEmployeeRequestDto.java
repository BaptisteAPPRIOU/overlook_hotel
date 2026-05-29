package master.master.web.rest.dto;

import lombok.Builder;
import lombok.Data;

/** DTO used to create an employee account and employee profile through the API. */
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
