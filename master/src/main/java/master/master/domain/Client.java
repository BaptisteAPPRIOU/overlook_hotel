package master.master.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "client")
public class Client {
    @Id
    private Integer userId;

    private Integer reservationId;

    @Column(name = "fidelity_point")
    private final Integer fidelityPoint = 0;
}