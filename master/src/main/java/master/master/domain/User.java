package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a user entity in the system.
 *
 * <p>This class is mapped to the "user" table in the database. It contains user-related information
 * such as first name, last name, email, password, and role.
 *
 * <p><b>Note:</b> The table name is quoted because "user" is a reserved keyword in SQL.
 *
 * <ul>
 *   <li><b>id</b>: Unique identifier for the user (auto-generated).
 *   <li><b>firstName</b>: User's first name (required, max 100 characters).
 *   <li><b>lastName</b>: User's last name (required, max 100 characters).
 *   <li><b>email</b>: User's email address (required, unique, max 150 characters).
 *   <li><b>password</b>: User's password (required).
 *   <li><b>role</b>: User's role in the system, represented by the {@link RoleType} enum
 *       (required).
 * </ul>
 *
 * <p>Equality and hash code are based on the {@code id} field.
 *
 * @author
 */
@Getter
@Setter
@Entity
@Table(name = "\"user\"") // Reserved SQL name, so quotes are required
public class User implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
  @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
  private Long id;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RoleType role;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User user)) return false;
    return id != null && id.equals(user.id);
  }

  @Override
  public int hashCode() {
    return 31;
  }
}
