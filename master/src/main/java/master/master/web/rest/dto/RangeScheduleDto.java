package master.master.web.rest.dto;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RangeScheduleDto {
  private String employeeName;
  private Map<LocalDate, String> schedule = new TreeMap<>();
}
