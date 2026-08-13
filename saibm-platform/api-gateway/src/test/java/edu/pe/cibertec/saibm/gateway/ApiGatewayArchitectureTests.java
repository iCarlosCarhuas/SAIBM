package edu.pe.cibertec.saibm.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(properties = { "server.port=0", "eureka.client.enabled=false", "spring.cloud.discovery.enabled=false" })
class ApiGatewayArchitectureTests {

	@Autowired
	private RouteDefinitionLocator routes;

	@Autowired
	private ConfigurableApplicationContext defaultContext;

	@Test
	void defaultContextHasOnlyExplicitCatalogRoutes() {
		assertThat(defaultContext).isInstanceOf(ReactiveWebApplicationContext.class);
		List<RouteDefinition> definitions = routes.getRouteDefinitions().collectList().block(Duration.ofSeconds(5));
		assertThat(definitions).isNotNull().extracting(RouteDefinition::getId)
				.containsExactlyInAnyOrder("libro-books", "inventario-inventory", "membresia-memberships", "usuario-users");
	}

	@Test
	void gatewayContractsKeepDefaultsSafeAndLegacyOptIn() throws IOException {
		String reactor = read("saibm-platform/pom.xml");
		String pom = read("saibm-platform/api-gateway/pom.xml");
		String defaults = read("saibm-platform/api-gateway/src/main/resources/application.yml");
		String legacy = read("saibm-platform/api-gateway/src/main/resources/application-legacy.yml");

		assertThat(reactor).contains("<version>3.5.3</version>", "<spring-cloud.version>2025.0.3</spring-cloud.version>");
		assertThat(pom).contains("spring-cloud-starter-gateway-server-webflux", "spring-cloud-starter-netflix-eureka-client",
				"spring-cloud-starter-loadbalancer");
			assertThat(defaults).contains("port: 8080", "enabled: false", "libro-books", "inventario-inventory", "membresia-memberships", "usuario-users",
					"lb://libro-service", "lb://inventario-service", "lb://membresia-service", "lb://usuario-service", "/api/v1/books/**", "/api/v1/inventory/**", "/api/v1/memberships/**", "/api/v1/users/**", "register-with-eureka: true",
				"fetch-registry: true", "include: health,info", "probes:", "enabled: true")
				.doesNotContain("SAIBM_LEGACY_BASE_URL", "Path=/**");
			assertThat(legacy).contains("on-profile: legacy", "uri: ${SAIBM_LEGACY_BASE_URL", "Path=/legacy/**", "StripPrefix=1",
					"libro-books", "inventario-inventory", "membresia-memberships", "usuario-users", "lb://libro-service", "lb://inventario-service", "lb://membresia-service", "lb://usuario-service").doesNotContain("localhost", "Path=/**", "lb://catalog-service");
	}

	@Test
	void legacyProfileRemainsExplicitlyConfigured() throws IOException {
		String legacy = read("saibm-platform/api-gateway/src/main/resources/application-legacy.yml");
		assertThat(legacy).contains("on-profile: legacy", "uri: ${SAIBM_LEGACY_BASE_URL", "Path=/legacy/**",
				"id: libro-books", "id: inventario-inventory", "id: membresia-memberships", "id: usuario-users", "lb://inventario-service", "lb://libro-service", "lb://membresia-service", "lb://usuario-service").doesNotContain("lb://catalog-service");
	}

	@Test
	void legacyProfileFailsClosedWithoutTarget() throws IOException {
		String legacy = read("saibm-platform/api-gateway/src/main/resources/application-legacy.yml");
		assertThat(legacy).contains("${SAIBM_LEGACY_BASE_URL}");
	}

	private String read(String relative) throws IOException {
		Path directory = Path.of(System.getProperty("user.dir"));
		while (directory != null && !Files.exists(directory.resolve("saibm-platform/pom.xml"))) {
			directory = directory.getParent();
		}
		return Files.readString(directory.resolve(relative));
	}
}
