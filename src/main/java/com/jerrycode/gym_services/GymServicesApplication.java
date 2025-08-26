package com.jerrycode.gym_services;

//import com.jerrycode.gym_services.comms.system.DataInjector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
//@EnableBinding(DataInjector.class)
@EnableAsync
@EnableDiscoveryClient
@EnableJpaAuditing
@SpringBootApplication
public class GymServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymServicesApplication.class, args);
	}

}
