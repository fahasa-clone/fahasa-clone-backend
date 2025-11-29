package vn.clone.fahasa_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import vn.clone.fahasa_backend.config.FahasaProperties;

@SpringBootApplication
@EnableConfigurationProperties(FahasaProperties.class)
@EnableAsync
public class FahasaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FahasaBackendApplication.class, args);
    }

}