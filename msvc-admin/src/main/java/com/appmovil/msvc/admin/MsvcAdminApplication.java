package com.appmovil.msvc.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcAdminApplication.class, args);
	}

}
