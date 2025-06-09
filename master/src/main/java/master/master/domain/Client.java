package master.master.domain;

import jakarta.persistence.*;

@Entity
public class Client {
    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private int fidelityPoint;
}