package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "clients")
public class Client implements Serializable {

  @Id
  @Column(name = "id_user")
  private Long id;

  // @MapsId shares the primary key with User, so Client.id is also User.id.
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "id_user")
  private User user;

  @Column(name = "fidelity_points", nullable = false)
  private Integer fidelityPoints = 0;

  // Removing a client also removes its reservations from the persistence context.
  @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Reservation> reservations = new ArrayList<>();

  /**
   * Returns the display name from the linked user profile.
   */
  public String getFullName() {
    return user != null ? user.getFullName() : "";
  }

  /**
   * Exposes the shared user identifier for code that expects a user id.
   */
  public Long getUserId() {
    return id;
  }

  /**
   * Returns the client loyalty point balance.
   */
  public Integer getFidelityPoint() {
    return fidelityPoints;
  }

  /**
   * Updates the client loyalty point balance.
   */
  public void setFidelityPoint(Integer fidelityPoint) {
    this.fidelityPoints = fidelityPoint;
  }

  /**
   * Compares clients by their persisted identifier to keep entity equality stable.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Client client)) return false;
    return id != null && Objects.equals(id, client.id);
  }

  /**
   * Uses the entity class hash code to stay consistent before and after persistence.
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
