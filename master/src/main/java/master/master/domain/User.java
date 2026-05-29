package master.master.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_user")
  private Long id;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "email", nullable = false, unique = true, length = 255)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "account_creation_date", nullable = false, updatable = false)
  private LocalDateTime accountCreationDate;

  // Enum values are stored as strings to avoid ordinal changes breaking existing data.
  @Enumerated(EnumType.STRING)
  @Column(name = "account_status", nullable = false, length = 30)
  private AccountStatus accountStatus;

  // User owns the many-to-many relation through the users_roles join table.
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "users_roles",
      joinColumns = @JoinColumn(name = "id_user"),
      inverseJoinColumns = @JoinColumn(name = "id_role"))
  private Set<Role> roles = new HashSet<>();

  // Client and Employee profiles share this user's lifecycle when linked.
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Client clientProfile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Employee employeeProfile;

  /**
   * Initializes account metadata before the user is inserted in the database.
   */
  @PrePersist
  protected void onCreate() {
    if (accountCreationDate == null) {
      accountCreationDate = LocalDateTime.now();
    }
    if (accountStatus == null) {
      accountStatus = AccountStatus.ACTIVE;
    }
  }

  /**
   * Builds the display name from the first name and last name fields.
   */
  public String getFullName() {
    return firstName + " " + lastName;
  }

  /**
   * Compatibility accessor that exposes passwordHash through Spring Security naming.
   */
  public String getPassword() {
    return passwordHash;
  }

  /**
   * Compatibility mutator that stores the encoded password in passwordHash.
   */
  public void setPassword(String password) {
    this.passwordHash = password;
  }

  /**
   * Returns the first assigned role code, or null when the user has no role.
   */
  public RoleCode getRole() {
    return roles.stream().findFirst().map(Role::getRoleCode).orElse(null);
  }

  /*
   * Do not use this implementation for persisted users.
   * It creates a new transient Role that is not loaded from roles table, which can break
   * the users_roles join table. Use UserRoleService.assignRole(...) instead.
   */
  // public void setRole(RoleCode roleCode) {
  //   roles.clear();
  //   if (roleCode != null) {
  //     Role role = new Role();
  //     role.setRoleCode(roleCode);
  //     role.setLabel(roleCode.name());
  //     roles.add(role);
  //   }
  // }

  /**
   * Compares users by their persisted identifier to keep entity equality stable.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User user)) return false;
    return id != null && Objects.equals(id, user.id);
  }

  /**
   * Uses the entity class hash code to stay consistent before and after persistence.
   */
  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
