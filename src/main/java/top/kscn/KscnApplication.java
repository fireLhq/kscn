package top.kscn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KscnApplication {
    public static void main(String[] args) {
        SpringApplication.run(KscnApplication.class, args);
    }
}
