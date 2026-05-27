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
@Table(name = "amenities")
public class Amenity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_amenity")
  private Long id;

  @Column(name = "amenity_code", nullable = false, unique = true, length = 50)
  private String amenityCode;

  @Column(name = "label", nullable = false, length = 100)
  private String label;

  @ManyToMany(mappedBy = "amenities", fetch = FetchType.LAZY)
  private Set<Room> rooms = new HashSet<>();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Amenity amenity)) return false;
    return id != null && Objects.equals(id, amenity.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
