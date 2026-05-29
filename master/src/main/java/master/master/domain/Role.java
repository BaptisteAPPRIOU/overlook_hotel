package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_role")
  private Long id;

  // Enum values are stored as strings so role names remain readable in the database.
  @Enumerated(EnumType.STRING)
  @Column(name = "role_code", nullable = false, unique = true, length = 50)
  private RoleCode roleCode;

  @Column(name = "label", nullable = false, length = 100)
  private String label;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  // User owns the users_roles join table, so Role only exposes the inverse side.
  @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
  private Set<User> users = new HashSet<>();

  /**
   * Compares roles by their persisted identifier to keep entity equality stable.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Role role)) return false;
    return id != null && Objects.equals(id, role.id);
  }

  /**
   * Uses the entity class hash code to stay consistent before and after persistence.
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
