package vn.clone.fahasa_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FahasaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FahasaBackendApplication.class, args);
    }

}