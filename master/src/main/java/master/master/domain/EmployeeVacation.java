package master.master.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class EmployeeVacation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate vacationStart;
    private LocalDate vacationEnd;
    private Boolean isAccepted;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}