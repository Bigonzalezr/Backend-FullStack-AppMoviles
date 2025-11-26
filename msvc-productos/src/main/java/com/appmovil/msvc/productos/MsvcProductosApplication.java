package com.appmovil.msvc.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsvcProductosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcProductosApplication.class, args);
	}

}
