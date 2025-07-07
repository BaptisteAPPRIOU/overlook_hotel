package master.master.web.rest.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

@Setter
@Getter
public class RangeScheduleDto {
    private String employeeName;
    private Map<LocalDate, String> schedule = new TreeMap<>();
}
