package master.master.domain;

/**
 * Enum representing the different types of roles available in the system.
 * <ul>
 *   <li>CLIENT - Represents a client or customer role.</li>
 *   <li>EMPLOYEE - Represents an employee role.</li>
 *   <li>ADMIN - Represents an administrator role with elevated privileges.</li>
 * </ul>
 */
public enum RoleType {
    CLIENT,
    EMPLOYEE,
    ADMIN
}