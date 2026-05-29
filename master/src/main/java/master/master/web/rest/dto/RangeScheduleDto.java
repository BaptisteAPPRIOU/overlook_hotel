package master.master.web.rest.dto;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;

/** DTO representing an employee schedule keyed by concrete LocalDate values. */
@Setter
@Getter
public class RangeScheduleDto {
  private String employeeName;
  // TreeMap keeps dates sorted chronologically for predictable rendering.
  private Map<LocalDate, String> schedule = new TreeMap<>();
}
