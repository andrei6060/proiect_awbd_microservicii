package com.clinic.clinic;

import com.clinic.clinic.role.Role;
import com.clinic.clinic.role.RoleJpaRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class ClinicApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinicApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(RoleJpaRepo ctx) {
		return args -> {
			if(ctx.findByName("USER").isEmpty()) {
				ctx.save(Role.builder().name("USER").build());
			}
			if(ctx.findByName("DOCTOR").isEmpty()) {
				ctx.save(Role.builder().name("DOCTOR").build());
			}

		};
	}

}
