package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rooms_photos")
public class RoomPhoto implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_room_photo")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_room", nullable = false)
  private Room room;

  @Column(name = "image_url", nullable = false, length = 255)
  private String imageUrl;

  @Column(name = "alt_text", length = 255)
  private String altText;

  @Column(name = "display_order", nullable = false)
  private Short displayOrder = 0;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RoomPhoto roomPhoto)) return false;
    return id != null && Objects.equals(id, roomPhoto.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
