package master.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing the association between an Employee and a specific workday.
 * Utilizes a composite primary key defined by {@link WorkdayId}.
 *
 * <p>
 * Fields:
 * <ul>
 *   <li>{@code id} - Composite key containing employee and workday identifiers.</li>
 *   <li>{@code employee} - Reference to the associated {@link Employee} entity.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The {@code @MapsId("employeeId")} annotation ensures that the employee part of the composite key
 * is mapped to the {@code employee_id} column in the database.
 * </p>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class EmployeeWorkday {

    @EmbeddedId
    private WorkdayId id;

    @ManyToOne
    @MapsId("employeeId")
    @JoinColumn(name = "employee_id")
    private Employee employee;
}
