package master.master.integration.support;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Locale;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class JdbcFixtures implements AutoCloseable {

  private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
  private static final String DEFAULT_PASSWORD = "RecettePass123!";

  private final Connection connection;
  private final String prefix;

  public JdbcFixtures(RecetteConfig config, String prefix) throws SQLException {
    this.connection = DriverManager.getConnection(config.jdbcUrl(), config.dbUser(), config.dbPassword());
    this.connection.setAutoCommit(true);
    this.prefix = prefix.toLowerCase(Locale.ROOT);
  }

  public String prefix() {
    return prefix;
  }

  public String defaultPassword() {
    return DEFAULT_PASSWORD;
  }

  public UserFixture createClientUser(String email) throws SQLException {
    long userId = createUser(email, "CLIENT");
    createClientProfile(userId, 0);
    return new UserFixture(userId, email, DEFAULT_PASSWORD, "CLIENT");
  }

  public UserFixture createClientUser(String email, int fidelityPoints) throws SQLException {
    long userId = createUser(email, "CLIENT");
    createClientProfile(userId, fidelityPoints);
    return new UserFixture(userId, email, DEFAULT_PASSWORD, "CLIENT");
  }

  public UserFixture createEmployeeUser(String email, String roleCode) throws SQLException {
    long userId = createUser(email, roleCode);
    createEmployeeProfile(userId);
    return new UserFixture(userId, email, DEFAULT_PASSWORD, roleCode);
  }

  public RoomFixture createRoom(String roomNumber, String roomType, int capacity, double price)
      throws SQLException {
    return createRoom(roomNumber, roomType, capacity, price, "AVAILABLE");
  }

  public RoomFixture createRoom(
      String roomNumber, String roomType, int capacity, double price, String status)
      throws SQLException {
    String sql =
        """
        INSERT INTO rooms (room_number, room_type, capacity, base_price, room_status, description)
        VALUES (?, ?, ?, ?, ?, ?)
        RETURNING id_room
        """;
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, roomNumber);
      statement.setString(2, roomType);
      statement.setInt(3, capacity);
      statement.setBigDecimal(4, java.math.BigDecimal.valueOf(price).setScale(2));
      statement.setString(5, status);
      statement.setString(6, "Recette room " + prefix);
      try (ResultSet resultSet = statement.executeQuery()) {
        resultSet.next();
        return new RoomFixture(resultSet.getLong(1), roomNumber);
      }
    }
  }

  public void addAmenityToRoom(long roomId, String amenityCode, String label) throws SQLException {
    long amenityId = ensureAmenity(amenityCode, label);
    try (PreparedStatement statement =
        connection.prepareStatement(
            "INSERT INTO rooms_amenities (id_room, id_amenity) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
      statement.setLong(1, roomId);
      statement.setLong(2, amenityId);
      statement.executeUpdate();
    }
  }

  public ReservationFixture createReservation(
      long clientUserId,
      long roomId,
      LocalDate startDate,
      LocalDate endDate,
      boolean paid,
      String status)
      throws SQLException {
    String sql =
        """
        INSERT INTO reservations
          (id_user, id_room, start_datetime, end_datetime, reservation_status, total_amount, payment_method, is_paid, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING id_reservation
        """;
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setLong(1, clientUserId);
      statement.setLong(2, roomId);
      statement.setTimestamp(3, Timestamp.valueOf(startDate.atStartOfDay()));
      statement.setTimestamp(4, Timestamp.valueOf(endDate.atStartOfDay()));
      statement.setString(5, status);
      statement.setBigDecimal(6, java.math.BigDecimal.valueOf(199.99).setScale(2));
      statement.setString(7, "CARD");
      statement.setBoolean(8, paid);
      statement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
      try (ResultSet resultSet = statement.executeQuery()) {
        resultSet.next();
        return new ReservationFixture(resultSet.getLong(1), clientUserId, roomId);
      }
    }
  }

  public Long findUserIdByEmail(String email) throws SQLException {
    try (PreparedStatement statement =
            connection.prepareStatement("SELECT id_user FROM users WHERE email = ?")) {
      statement.setString(1, email);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? resultSet.getLong(1) : null;
      }
    }
  }

  public String roomNumber(String suffix) {
    String normalizedSuffix = suffix.toUpperCase(Locale.ROOT);
    return roomNumberPrefix()
        + "-"
        + normalizedSuffix.substring(0, Math.min(11, normalizedSuffix.length()));
  }

  public void cleanup() throws SQLException {
    deleteReservations();
    deleteEmployees();
    deleteClients();
    deleteUserRoles();
    deleteUsers();
    deleteRoomAmenities();
    deleteRooms();
    deleteAmenities();
    deleteOrphanMonthlySchedules();
  }

  @Override
  public void close() throws SQLException {
    connection.close();
  }

  private long createUser(String email, String roleCode) throws SQLException {
    long roleId = ensureRole(roleCode);
    String sql =
        """
        INSERT INTO users (last_name, first_name, email, password_hash, account_creation_date, account_status)
        VALUES (?, ?, ?, ?, ?, ?)
        RETURNING id_user
        """;
    long userId;
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, "Recette");
      statement.setString(2, "User");
      statement.setString(3, email);
      statement.setString(4, PASSWORD_ENCODER.encode(DEFAULT_PASSWORD));
      statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
      statement.setString(6, "ACTIVE");
      try (ResultSet resultSet = statement.executeQuery()) {
        resultSet.next();
        userId = resultSet.getLong(1);
      }
    }

    try (PreparedStatement statement =
            connection.prepareStatement("INSERT INTO users_roles (id_user, id_role) VALUES (?, ?)")) {
      statement.setLong(1, userId);
      statement.setLong(2, roleId);
      statement.executeUpdate();
    }
    return userId;
  }

  private void createClientProfile(long userId, int fidelityPoints) throws SQLException {
    try (PreparedStatement statement =
            connection.prepareStatement("INSERT INTO clients (id_user, fidelity_points) VALUES (?, ?)")) {
      statement.setLong(1, userId);
      statement.setInt(2, fidelityPoints);
      statement.executeUpdate();
    }
  }

  private void createEmployeeProfile(long userId) throws SQLException {
    try (PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO employees (id_user, matricule, team, employee_status, hire_date) VALUES (?, ?, ?, ?, ?)")) {
      statement.setLong(1, userId);
      statement.setString(2, "MAT-" + prefix + "-" + userId);
      statement.setString(3, "recette");
      statement.setString(4, "ACTIVE");
      statement.setDate(5, Date.valueOf(LocalDate.now().minusDays(10)));
      statement.executeUpdate();
    }
  }

  private long ensureRole(String roleCode) throws SQLException {
    try (PreparedStatement select =
            connection.prepareStatement("SELECT id_role FROM roles WHERE role_code = ?")) {
      select.setString(1, roleCode);
      try (ResultSet resultSet = select.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getLong(1);
        }
      }
    }

    try (PreparedStatement insert =
        connection.prepareStatement(
            "INSERT INTO roles (role_code, label, description) VALUES (?, ?, ?) RETURNING id_role")) {
      insert.setString(1, roleCode);
      insert.setString(2, roleCode);
      insert.setString(3, roleCode + " role");
      try (ResultSet resultSet = insert.executeQuery()) {
        resultSet.next();
        return resultSet.getLong(1);
      }
    }
  }

  private long ensureAmenity(String amenityCode, String label) throws SQLException {
    try (PreparedStatement select =
        connection.prepareStatement("SELECT id_amenity FROM amenities WHERE amenity_code = ?")) {
      select.setString(1, amenityCode);
      try (ResultSet resultSet = select.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getLong(1);
        }
      }
    }

    try (PreparedStatement insert =
        connection.prepareStatement(
            "INSERT INTO amenities (amenity_code, label) VALUES (?, ?) RETURNING id_amenity")) {
      insert.setString(1, amenityCode);
      insert.setString(2, label);
      try (ResultSet resultSet = insert.executeQuery()) {
        resultSet.next();
        return resultSet.getLong(1);
      }
    }
  }

  private void deleteReservations() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            """
            DELETE FROM reservations
            WHERE id_user IN (SELECT id_user FROM users WHERE email LIKE ?)
               OR id_room IN (SELECT id_room FROM rooms WHERE room_number LIKE ?)
            """)) {
      statement.setString(1, prefix + "%");
      statement.setString(2, prefix + "%");
      statement.executeUpdate();
    }
  }

  private void deleteEmployees() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement("DELETE FROM employees WHERE matricule LIKE ?")) {
      statement.setString(1, "MAT-" + prefix + "%");
      statement.executeUpdate();
    }
  }

  private void deleteClients() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "DELETE FROM clients WHERE id_user IN (SELECT id_user FROM users WHERE email LIKE ?)")) {
      statement.setString(1, prefix + "%");
      statement.executeUpdate();
    }
  }

  private void deleteUserRoles() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "DELETE FROM users_roles WHERE id_user IN (SELECT id_user FROM users WHERE email LIKE ?)")) {
      statement.setString(1, prefix + "%");
      statement.executeUpdate();
    }
  }

  private void deleteUsers() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement("DELETE FROM users WHERE email LIKE ?")) {
      statement.setString(1, prefix + "%");
      statement.executeUpdate();
    }
  }

  private void deleteRooms() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement("DELETE FROM rooms WHERE room_number LIKE ?")) {
      statement.setString(1, roomNumberPrefix() + "%");
      statement.executeUpdate();
    }
  }

  private void deleteRoomAmenities() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            """
            DELETE FROM rooms_amenities
            WHERE id_room IN (SELECT id_room FROM rooms WHERE room_number LIKE ?)
               OR id_amenity IN (SELECT id_amenity FROM amenities WHERE amenity_code LIKE ?)
            """)) {
      statement.setString(1, roomNumberPrefix() + "%");
      statement.setString(2, prefix + "%");
      statement.executeUpdate();
    }
  }

  private void deleteAmenities() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement("DELETE FROM amenities WHERE amenity_code LIKE ?")) {
      statement.setString(1, prefix + "%");
      statement.executeUpdate();
    }
  }

  private void deleteOrphanMonthlySchedules() throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            """
            DELETE FROM monthly_schedules ms
            WHERE NOT EXISTS (
              SELECT 1
              FROM work_shifts ws
              WHERE ws.id_monthly_schedule = ms.id_monthly_schedule
            )
            """)) {
      statement.executeUpdate();
    }
  }

  private String roomNumberPrefix() {
    return prefix.substring(0, Math.min(8, prefix.length())).toUpperCase(Locale.ROOT);
  }

  public record UserFixture(long userId, String email, String password, String roleCode) {}

  public record RoomFixture(long roomId, String roomNumber) {}

  public record ReservationFixture(long reservationId, long userId, long roomId) {}
}
