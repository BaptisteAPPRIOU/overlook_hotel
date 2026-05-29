package master.master.mapper;

import master.master.domain.Employee;
import master.master.web.rest.dto.EmployeeWorkTimeDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

  /**
   * Builds the work-time dashboard DTO for an employee.
   */
  default EmployeeWorkTimeDto toWorkTimeDto(Employee employee) {
    EmployeeWorkTimeDto dto = new EmployeeWorkTimeDto();

    // User information is optional because Employee is loaded lazily in some queries.
    if (employee.getUser() != null) {
      dto.setName(employee.getUser().getFirstName() + " " + employee.getUser().getLastName());
    }

    // The dashboard expects a visible fallback instead of an empty team value.
    dto.setTeam(employee.getTeam() != null ? employee.getTeam() : "Unknown");

    // Time values are initialized as placeholders until time entries are attached.
    dto.setClockIn("-");
    dto.setClockOut("-");
    dto.setIdleTime("-");
    dto.setWorkTime("-");

    return dto;
  }
}
