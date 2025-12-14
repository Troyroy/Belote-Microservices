package belote.ex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class GameApp {
    public static void main(String[] args) {

        System.out.println(System.getenv("MYSQLDB_URL"));
        System.out.println("Test");
        SpringApplication.run(GameApp.class, args);



    }
}