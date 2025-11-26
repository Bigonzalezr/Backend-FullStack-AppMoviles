package com.appmovil.msvc.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsvcPedidosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcPedidosApplication.class, args);
	}

}
