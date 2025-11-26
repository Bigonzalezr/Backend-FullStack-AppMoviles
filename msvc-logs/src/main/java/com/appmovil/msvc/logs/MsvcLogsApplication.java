package com.appmovil.msvc.logs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcLogsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcLogsApplication.class, args);
	}

}
