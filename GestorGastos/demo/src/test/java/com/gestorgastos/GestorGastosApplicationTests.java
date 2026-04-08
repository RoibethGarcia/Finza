package com.gestorgastos;

import com.gestorgastos.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GestorGastosApplicationTests extends AbstractPostgresIntegrationTest {

	@Test
	void contextLoads() {
	}
}
