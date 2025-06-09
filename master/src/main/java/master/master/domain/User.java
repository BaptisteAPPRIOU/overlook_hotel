package master.master.domain;

import jakarta.persistence.*;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a user entity in the system.
 * <p>
 * This class is mapped to the "user" table in the database.
 * It contains user-related information such as first name, last name, email, password, and role.
 * </p>
 *
 * <p>
 * <b>Note:</b> The table name is quoted because "user" is a reserved keyword in SQL.
 * </p>
 *
 * <ul>
 *   <li><b>id</b>: Unique identifier for the user (auto-generated).</li>
 *   <li><b>firstName</b>: User's first name (required, max 100 characters).</li>
 *   <li><b>lastName</b>: User's last name (required, max 100 characters).</li>
 *   <li><b>email</b>: User's email address (required, unique, max 150 characters).</li>
 *   <li><b>password</b>: User's password (required).</li>
 *   <li><b>role</b>: User's role in the system, represented by the {@link RoleType} enum (required).</li>
 * </ul>
 *
 * <p>
 * Equality and hash code are based on the {@code id} field.
 * </p>
 *
 * @author 
 */

@Getter
@Setter
@Entity
@Table(name = "\"user\"") // Attention : nom réservé en SQL, donc les guillemets
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private Integer id;

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

    // equals/hashCode basé sur id

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
