package co.edu.eci.pigball.game;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GameApplicationTests {
	
	@Test
	void contextLoads() {
	}

	@Test
    void mainMethodTest() {
        GameApplication.main(new String[]{});
    }

}
