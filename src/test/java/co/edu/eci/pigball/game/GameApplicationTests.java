package co.edu.eci.pigball.game;

import static org.junit.Assert.assertEquals;

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
		"SSL_ENABLED=false",
		"REDIS_PORT=6379"
})
class GameApplicationTests {

//	@Test
//	void contextLoads() {
//		// This test will pass if the application context loads successfully
//		assert true; // If no exception is thrown, the test passes
//	}
//
//	@Test
//	void mainMethodTest() {
//		// Set environment variables for testing
//		System.setProperty("ALLOWED_ORIGINS_HTTP", "http://localhost:3000,http://example.com");
//		System.setProperty("ALLOWED_ORIGINS_HTTPS",
//				"https://localhost:3000,https://test.example.com,https://example.com");
//		System.setProperty("SSL_ENABLED", "false");
//		System.setProperty("APP_STORE_TYPE", "memory");
//		System.setProperty("REDIS_HOST", "localhost");
//		System.setProperty("REDIS_PORT", "6379");
//
//		GameApplication.main(new String[] {});
//
//		// Test if the system properties are set correctly
//		assertEquals("http://localhost:3000,http://example.com",
//				System.getProperty("ALLOWED_ORIGINS_HTTP"));
//		assertEquals("https://localhost:3000,https://test.example.com,https://example.com",
//				System.getProperty("ALLOWED_ORIGINS_HTTPS"));
//		assertEquals("false", System.getProperty("SSL_ENABLED"));
//	}

}


