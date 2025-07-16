package master.master.mapper;

import master.master.domain.Employee;
import master.master.domain.EmployeeWorkday;
import master.master.web.rest.dto.EmployeeWorkTimeDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    default EmployeeWorkTimeDto toWorkTimeDto(Employee employee) {
        EmployeeWorkTimeDto dto = new EmployeeWorkTimeDto();

        if (employee.getUser() != null) {
            dto.setName(employee.getUser().getFirstName() + " " + employee.getUser().getLastName());
        }

        dto.setTeam(employee.getTeam() != null ? employee.getTeam() : "Unknown");

        if (employee.getWorkdays() != null && !employee.getWorkdays().isEmpty()) {
            EmployeeWorkday w = employee.getWorkdays().get(0);

            dto.setClockIn(w.getClockIn() != null ? w.getClockIn().toString() : "-");
            dto.setClockOut(w.getClockOut() != null ? w.getClockOut().toString() : "-");
            dto.setIdleTime(w.getFormattedIdleTime());
            dto.setWorkTime(w.getFormattedWorkTime());
        } else {
            dto.setClockIn("-");
            dto.setClockOut("-");
            dto.setIdleTime("-");
            dto.setWorkTime("-");
        }

        return dto;
    }
}
