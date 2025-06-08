package master.master.domain;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class WorkdayId implements Serializable {
    private Long employeeId;
    private Integer weekday;

    public WorkdayId() {
    }

    public WorkdayId(Long employeeId, Integer weekday) {
        this.employeeId = employeeId;
        this.weekday = weekday;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkdayId that)) return false;
        return Objects.equals(employeeId, that.employeeId) && Objects.equals(weekday, that.weekday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, weekday);
    }
}