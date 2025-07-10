package master.master.web.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeWorkTimeDto {
    private String name;
    private String team;
    private String workTime;
    private String clockIn;
    private String clockOut;
    private String idleTime;

}
