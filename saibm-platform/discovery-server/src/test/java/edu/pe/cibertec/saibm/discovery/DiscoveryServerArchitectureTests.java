package edu.pe.cibertec.saibm.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootTest(properties = "server.port=0")
class DiscoveryServerArchitectureTests {

	@Autowired
	private ApplicationContext context;

	@Test
	void standaloneDiscoveryContextLoads() {
		assertThat(context).isNotNull();
		assertThat(DiscoveryServerApplication.class.isAnnotationPresent(EnableEurekaServer.class)).isTrue();
		assertThat(context.getEnvironment().getProperty("server.port")).isEqualTo("0");
	}

	@Test
	void discoveryContractsAreExplicitAndBounded() throws IOException {
		String reactor = read("saibm-platform/pom.xml");
		String pom = read("saibm-platform/discovery-server/pom.xml");
		String config = read("saibm-platform/discovery-server/src/main/resources/application.yml");

		assertThat(reactor).contains("<java.version>21</java.version>", "<spring-cloud.version>2025.0.3</spring-cloud.version>",
				"<module>discovery-server</module>", "<module>api-gateway</module>");
		assertThat(pom).contains("spring-cloud-starter-netflix-eureka-server");
		assertThat(config).contains("port: 8761", "register-with-eureka: false", "fetch-registry: false",
				"include: health,info", "probes:", "enabled: true");
		assertThat(config).doesNotContain("*", "password", "secret");
	}

	private String read(String relative) throws IOException {
		Path directory = Path.of(System.getProperty("user.dir"));
		while (directory != null && !Files.exists(directory.resolve("saibm-platform/pom.xml"))) {
			directory = directory.getParent();
		}
		return Files.readString(directory.resolve(relative));
	}
}
