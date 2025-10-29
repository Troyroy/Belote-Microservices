package belote.ex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class UserApp {
    public static void main(String[] args) {

        System.out.println(System.getenv("MYSQLDB_URL"));
        System.out.println("Test");
        SpringApplication.run(UserApp.class, args);



    }
}