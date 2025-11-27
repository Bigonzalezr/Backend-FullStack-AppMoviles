package com.appmovil.msvc.pedidos;

import com.appmovil.msvc.pedidos.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestSecurityConfig.class)
class MsvcPedidosApplicationTests {

	@Test
	void contextLoads() {
	}

}
