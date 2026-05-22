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

  @Enumerated(EnumType.STRING)
  @Column(name = "account_status", nullable = false, length = 30)
  private AccountStatus accountStatus;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "users_roles",
      joinColumns = @JoinColumn(name = "id_user"),
      inverseJoinColumns = @JoinColumn(name = "id_role"))
  private Set<Role> roles = new HashSet<>();

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Client clientProfile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Employee employeeProfile;

  @PrePersist
  protected void onCreate() {
    if (accountCreationDate == null) {
      accountCreationDate = LocalDateTime.now();
    }
    if (accountStatus == null) {
      accountStatus = AccountStatus.ACTIVE;
    }
  }

  public String getFullName() {
    return firstName + " " + lastName;
  }

  public String getPassword() {
    return passwordHash;
  }

  public void setPassword(String password) {
    this.passwordHash = password;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User user)) return false;
    return id != null && Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
