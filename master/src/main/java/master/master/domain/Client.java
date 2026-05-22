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

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "id_user")
  private User user;

  @Column(name = "fidelity_points", nullable = false)
  private Integer fidelityPoints = 0;

  @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Reservation> reservations = new ArrayList<>();

  public String getFullName() {
    return user != null ? user.getFullName() : "";
  }

  public Long getUserId() {
    return id;
  }

  public Integer getFidelityPoint() {
    return fidelityPoints;
  }

  public void setFidelityPoint(Integer fidelityPoint) {
    this.fidelityPoints = fidelityPoint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Client client)) return false;
    return id != null && Objects.equals(id, client.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
