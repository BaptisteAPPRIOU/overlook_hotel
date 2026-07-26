package master.master.integration.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class JsonHttpClient {

  private final HttpClient client =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(5))
          .followRedirects(HttpClient.Redirect.NEVER)
          .build();

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  private final String baseUrl;

  public JsonHttpClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public HttpResult get(String path) throws IOException, InterruptedException {
    return sendJson("GET", path, null, null);
  }

  public HttpResult get(String path, String bearerToken) throws IOException, InterruptedException {
    return sendJson("GET", path, null, bearerToken);
  }

  public HttpResult post(String path, Object body) throws IOException, InterruptedException {
    return sendJson("POST", path, body, null);
  }

  public HttpResult post(String path, Object body, String bearerToken)
      throws IOException, InterruptedException {
    return sendJson("POST", path, body, bearerToken);
  }

  public HttpResult put(String path, Object body, String bearerToken)
      throws IOException, InterruptedException {
    return sendJson("PUT", path, body, bearerToken);
  }

  public HttpResult putFormless(String path, String bearerToken)
      throws IOException, InterruptedException {
    return sendJson("PUT", path, null, bearerToken);
  }

  public HttpResult postFormless(String path, String bearerToken)
      throws IOException, InterruptedException {
    return sendJson("POST", path, Map.of(), bearerToken);
  }

  private HttpResult sendJson(String method, String path, Object body, String bearerToken)
      throws IOException, InterruptedException {
    HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .timeout(Duration.ofSeconds(10))
            .header("Accept", "application/json");

    if (bearerToken != null && !bearerToken.isBlank()) {
      requestBuilder.header("Authorization", "Bearer " + bearerToken);
    }

    if (body == null) {
      requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
    } else {
      requestBuilder
          .header("Content-Type", "application/json")
          .method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
    }

    HttpResponse<String> response =
        client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    return new HttpResult(response.statusCode(), response.body(), response.headers().firstValue("location").orElse(null));
  }

  public JsonNode readTree(String body) throws IOException {
    return objectMapper.readTree(body);
  }

  public record HttpResult(int statusCode, String body, String location) {}
}
