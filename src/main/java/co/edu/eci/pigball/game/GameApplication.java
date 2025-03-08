package co.edu.eci.pigball.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // This enables the execution of @Scheduled methods
public class GameApplication {

	public static void main(String[] args) {
		//Comment
		SpringApplication.run(GameApplication.class, args);
	}

}
