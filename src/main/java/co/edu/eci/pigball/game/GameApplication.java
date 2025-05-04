package co.edu.eci.pigball.game;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // This enables the execution of @Scheduled methods
public class GameApplication {

	public static void main(String[] args) {
		// Load environment variables from .env file only if not already set
		try {
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			dotenv.entries().forEach(entry -> {
				String key = entry.getKey();
				if (System.getProperty(key) == null && System.getenv(key) == null) {
					System.setProperty(key, entry.getValue());
				}
			});
		} catch (Exception e) {
			// Continue without .env file
		}
	
		SpringApplication.run(GameApplication.class, args);
	}
}
