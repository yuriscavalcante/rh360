package com.rh360.rh360;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de integração para verificar se o contexto Spring Boot carrega corretamente.
 * 
 * Este teste verifica se todas as configurações e beans estão sendo carregados
 * corretamente pelo Spring Boot.
 * 
 * @author Sistema RH360
 */
@SpringBootTest
@ActiveProfiles("test")
class Rh360ApplicationTests {

	@Test
	void contextLoads() {
		// Este teste verifica se o contexto Spring Boot carrega sem erros
		// Se o contexto carregar, o teste passa automaticamente
	}

}
