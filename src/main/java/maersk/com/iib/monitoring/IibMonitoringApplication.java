package maersk.com.iib.monitoring;

/*
 * Main Spring Boot application
 *            
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IibMonitoringApplication {

	public static void main(String[] args) {
		SpringApplication.run(IibMonitoringApplication.class, args);
	}

}
