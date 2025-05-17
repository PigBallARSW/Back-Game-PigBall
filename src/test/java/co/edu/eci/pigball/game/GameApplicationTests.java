package co.edu.eci.pigball.game;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
@ImportAutoConfiguration(exclude = {
		RedisAutoConfiguration.class,
		RedissonAutoConfigurationV2.class
})
@SpringBootTest(
		properties = {
				"APP_STORE_TYPE=memory",
				"ALLOWED_ORIGINS_HTTP=http://test:3000,http://test.example.com",
				"ALLOWED_ORIGINS_HTTPS=https://test:3000,https://test.example.com",
				"SSL_ENABLED=false"
		}
)
@EnableAutoConfiguration(
		exclude = {
				RedisAutoConfiguration.class,
				RedissonAutoConfigurationV2.class
		}
)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
		"ALLOWED_ORIGINS_HTTP=http://test:3000,http://test.example.com",
		"ALLOWED_ORIGINS_HTTPS=https://test:3000,https://test.example.com",
		"SSL_ENABLED=false"
})
class GameApplicationTests {
//	@TestConfiguration
//	static class NoRedisConfig {
//		@Bean
//		public org.redisson.api.RedissonClient redissonClient() {
//			return mock(org.redisson.api.RedissonClient.class);
//		}
//	}
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
//		// Aunque pongas REDIS_HOST/PORT, estas auto-configs nunca se levantarán:
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
////		assertEquals("false", System.getProperty("SSL_ENABLED"));
//	}

}


