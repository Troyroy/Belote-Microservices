package belote.ex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})

public class LobbyApp {
    public static void main(String[] args) {

        System.out.println(System.getenv("MYSQLDB_URL"));
        System.out.println("Test");
        SpringApplication.run(LobbyApp.class, args);



    }
}