package master.master.domain;

import jakarta.persistence.*;

@Entity
public class EmployeeWorkday {
    @EmbeddedId
    private WorkdayId id;

    @ManyToOne
    @MapsId("employeeId")
    @JoinColumn(name = "employee_id")
    private Employee employee;
}