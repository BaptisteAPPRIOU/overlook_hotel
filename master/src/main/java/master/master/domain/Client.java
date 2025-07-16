package master.master.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a client entity in the hotel reservation system.
 * <p>
 * Each client is associated with a {@link User} entity via a one-to-one relationship,
 * sharing the same primary key. The client also has a fidelity point balance.
 * </p>
 *
 * <ul>
 *   <li><b>userId</b>: The unique identifier for the client, mapped from the associated user.</li>
 *   <li><b>user</b>: The associated {@link User} entity.</li>
 *   <li><b>fidelityPoint</b>: The number of fidelity points the client has accumulated.</li>
 * </ul>
 *
 * @author YourName
 */

@Getter
@Setter
@Entity
public class Client {
    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "fidelity_point", nullable = false, columnDefinition = "integer default 0")
    private Integer fidelityPoint;

}