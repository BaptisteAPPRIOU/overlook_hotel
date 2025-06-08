package master.master.domain;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Employee {
    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "employee")
    private List<EmployeeVacation> vacations;

    @OneToMany(mappedBy = "employee")
    private List<EmployeeWorkday> workdays;
}