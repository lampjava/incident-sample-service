package org.chiwooplatform.incident;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

@EnableIntegration
@SpringBootApplication
public class IncidentApplication {

    public static void main( String[] args ) {
        SpringApplication.run( IncidentApplication.class, args );
    }
}
