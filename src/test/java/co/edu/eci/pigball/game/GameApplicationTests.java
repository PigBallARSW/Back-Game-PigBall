package co.edu.eci.pigball.game;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
		"ALLOWED_ORIGINS_HTTP=http://test:3000,http://test.example.com",
		"ALLOWED_ORIGINS_HTTPS=https://test:3000,https://test.example.com",
		"SSL_ENABLED=false"
})
class GameApplicationTests {

	@Test
	void contextLoads() {
		// This test will pass if the application context loads successfully
		assert true; // If no exception is thrown, the test passes
	}

	@Test
	void mainMethodTest() {
		// Set environment variables for testing
		System.setProperty("ALLOWED_ORIGINS_HTTP", "http://localhost:3000,http://example.com");
		System.setProperty("ALLOWED_ORIGINS_HTTPS",
				"https://localhost:3000,https://test.example.com,https://example.com");
		System.setProperty("SSL_ENABLED", "false");
		GameApplication.main(new String[] {});
		assert true; // If no exception is thrown, the test passes
	}

}
