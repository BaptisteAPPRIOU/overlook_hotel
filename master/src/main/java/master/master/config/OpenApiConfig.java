package master.master.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  // This name must match the security requirement and the declared OpenAPI security scheme.
  private static final String SECURITY_SCHEME_NAME = "bearerAuth";

  /**
   * Builds the main OpenAPI definition and declares JWT bearer authentication for Swagger UI.
   */
  @Bean
  public OpenAPI overlookHotelOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Overlook Hotel API")
                .description("Documentation des endpoints REST de l'application Overlook Hotel.")
                .version("v1"))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        .components(
            new Components()
                .addSecuritySchemes(
                    SECURITY_SCHEME_NAME,
                    // Swagger UI uses this HTTP bearer scheme to send JWT tokens in the Authorization header.
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }

  /**
   * Groups all REST API endpoints under a dedicated Swagger documentation section.
   */
  @Bean
  public GroupedOpenApi overlookHotelApiGroup() {
    return GroupedOpenApi.builder()
        .group("overlook-hotel-api")
        .pathsToMatch("/api/**")
        .build();
  }
}
