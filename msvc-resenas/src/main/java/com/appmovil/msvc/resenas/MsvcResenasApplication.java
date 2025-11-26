package com.appmovil.msvc.resenas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsvcResenasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcResenasApplication.class, args);
	}

}
