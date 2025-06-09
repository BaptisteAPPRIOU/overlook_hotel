package master.master.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Composite primary key for the Workday entity.
 * This embeddable class represents a unique identifier consisting of an employee ID
 * and a weekday number to identify specific workdays for employees.
 * 
 * <p>This class is used as a composite key to uniquely identify workday records
 * by combining an employee identifier with a specific day of the week.</p>
 * 
 * @author Generated
 * @version 1.0
 * @since 1.0
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkdayId implements Serializable {

    private Long employeeId;
    private Integer weekday;
}
