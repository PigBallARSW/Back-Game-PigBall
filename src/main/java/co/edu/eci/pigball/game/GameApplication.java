package co.edu.eci.pigball.game;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // This enables the execution of @Scheduled methods
public class GameApplication {

	public static void main(String[] args) {
		// Load environment variables from .env file if it exists
		try {
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			// Set system properties from .env file
			dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
		} catch (Exception e) {
			// Continue without .env file
		}
		
		// If SSL is disabled, ensure we're using HTTP
		if (Boolean.parseBoolean(System.getProperty("server.ssl.enabled", "false"))) {
			System.setProperty("server.port", "8443");
		} else {
			System.setProperty("server.port", "8080");
		}
		
		SpringApplication.run(GameApplication.class, args);
	}

}
