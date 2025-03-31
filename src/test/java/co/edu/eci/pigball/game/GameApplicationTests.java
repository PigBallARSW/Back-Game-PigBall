package co.edu.eci.pigball.game;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
    "ALLOWED_ORIGINS_HTTP=http://localhost:3000,http://frontendeci.duckdns.org",
    "ALLOWED_ORIGINS_HTTPS=https://localhost:3000,https://192.168.0.191:3000,https://frontendeci.duckdns.org",
    "server.ssl.enabled=false",
    "server.port=8080"
})
class GameApplicationTests {
	
	@Test
	void contextLoads() {
	}

	@Test
	void mainMethodTest() {
		// Set environment variables for testing
		System.setProperty("ALLOWED_ORIGINS_HTTP", "http://localhost:3000,http://example.com");
		System.setProperty("ALLOWED_ORIGINS_HTTPS", "https://localhost:3000,https://test.example.com,https://example.com");
		System.setProperty("server.ssl.enabled", "false");
		System.setProperty("server.port", "8081"); // Use a different port for this test
		
		GameApplication.main(new String[]{});
	}

}
