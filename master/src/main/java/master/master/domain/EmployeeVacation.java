package master.master.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee_vacation")
public class EmployeeVacation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_vacation_id_seq")
    @SequenceGenerator(name = "employee_vacation_id_seq", sequenceName = "employee_vacation_id_seq", allocationSize = 1)
    private Integer id;

    private Integer employeeId;

    @Column(name = "vacation_start", nullable = false)
    private LocalDate vacationStart;

    @Column(name = "vacation_end", nullable = false)
    private LocalDate vacationEnd;

    @Column(name = "is_accepted")
    private Boolean isAccepted;
}

