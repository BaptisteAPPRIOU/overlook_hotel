package master.master.integration.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecetteConfig {

  private static final List<Path> ENV_FALLBACK_PATHS =
      List.of(Path.of("../.env.recette"), Path.of(".env.recette"));

  private final String baseUrl;
  private final String dbHost;
  private final int dbPort;
  private final String dbName;
  private final String dbUser;
  private final String dbPassword;

  private RecetteConfig(
      String baseUrl, String dbHost, int dbPort, String dbName, String dbUser, String dbPassword) {
    this.baseUrl = stripTrailingSlash(baseUrl);
    this.dbHost = dbHost;
    this.dbPort = dbPort;
    this.dbName = dbName;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;
  }

  public static RecetteConfig load() {
    Map<String, String> envFileValues = loadEnvFallback();
    String baseUrl =
        readValue("recette.base-url", "RECETTE_BASE_URL", envFileValues, "http://localhost:8080");
    String dbHost = readValue(null, "RECETTE_POSTGRES_HOST", envFileValues, "localhost");
    String dbPortValue = readValue(null, "RECETTE_POSTGRES_PORT", envFileValues, "5433");
    String dbName = requireValue("RECETTE_POSTGRES_DB", envFileValues);
    String dbUser = requireValue("RECETTE_POSTGRES_USER", envFileValues);
    String dbPassword = requireValue("RECETTE_POSTGRES_PASSWORD", envFileValues);

    return new RecetteConfig(baseUrl, dbHost, Integer.parseInt(dbPortValue), dbName, dbUser, dbPassword);
  }

  public String baseUrl() {
    return baseUrl;
  }

  public String jdbcUrl() {
    return "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
  }

  public String dbUser() {
    return dbUser;
  }

  public String dbPassword() {
    return dbPassword;
  }

  private static String requireValue(String envKey, Map<String, String> envFileValues) {
    String value = readValue(null, envKey, envFileValues, null);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(
          "Missing required recette database setting: " + envKey + ". Define it in the environment or .env.recette.");
    }
    return value;
  }

  private static String readValue(
      String propertyKey,
      String envKey,
      Map<String, String> envFileValues,
      String defaultValue) {
    if (propertyKey != null) {
      String propertyValue = System.getProperty(propertyKey);
      if (propertyValue != null && !propertyValue.isBlank()) {
        return propertyValue.trim();
      }
    }

    String envValue = System.getenv(envKey);
    if (envValue != null && !envValue.isBlank()) {
      return envValue.trim();
    }

    String fileValue = envFileValues.get(envKey);
    if (fileValue != null && !fileValue.isBlank()) {
      return fileValue.trim();
    }

    return defaultValue;
  }

  private static Map<String, String> loadEnvFallback() {
    for (Path candidate : ENV_FALLBACK_PATHS) {
      if (!Files.isRegularFile(candidate)) {
        continue;
      }
      try {
        return parseEnvFile(candidate);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to read recette env file: " + candidate, e);
      }
    }
    return Map.of();
  }

  private static Map<String, String> parseEnvFile(Path path) throws IOException {
    Map<String, String> values = new HashMap<>();
    for (String rawLine : Files.readAllLines(path)) {
      String line = rawLine.trim();
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }

      int separatorIndex = line.indexOf('=');
      if (separatorIndex <= 0) {
        continue;
      }

      String key = line.substring(0, separatorIndex).trim();
      String value = line.substring(separatorIndex + 1).trim();
      values.put(key, unquote(value));
    }
    return values;
  }

  private static String unquote(String value) {
    if ((value.startsWith("\"") && value.endsWith("\""))
        || (value.startsWith("'") && value.endsWith("'"))) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  private static String stripTrailingSlash(String value) {
    return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
  }
}
