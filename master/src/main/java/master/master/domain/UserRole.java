package master.master.domain;

/**
 * Enumeration representing the different user roles in the hotel reservation system.
 * 
 * <p>This enum defines the hierarchy and permissions structure for users within
 * the application, determining access levels and available functionalities.</p>
 * 
 * <ul>
 * <li>{@code CLIENT} - Standard users who can make reservations and manage their bookings</li>
 * <li>{@code EMPLOYEE} - Hotel staff members with access to reservation management and guest services</li>
 * <li>{@code ADMIN} - Administrative users with full system access and user management capabilities</li>
 * </ul>
 * 
 * @author Hotel Reservation System
 * @version 1.0
 * @since 1.0
 */
public enum UserRole {
    CLIENT, EMPLOYEE, ADMIN
}