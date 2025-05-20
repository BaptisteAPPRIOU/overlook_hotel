package master.master.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "employee_workday")
@IdClass(EmployeeWorkdayId.class)
public class EmployeeWorkday {
    @Id
    private Integer employeeId;

    @Id
    private Short weekday;
}

class EmployeeWorkdayId implements Serializable {
    private Integer employeeId;
    private Short weekday;

}
