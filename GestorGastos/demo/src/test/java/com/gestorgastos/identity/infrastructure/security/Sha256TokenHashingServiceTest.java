package com.gestorgastos.identity.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Sha256TokenHashingServiceTest {

	private final Sha256TokenHashingService hashingService = new Sha256TokenHashingService();

	@Test
	void shouldProduceDeterministicHashes() {
		String hashA = hashingService.hash("refresh-token");
		String hashB = hashingService.hash("refresh-token");

		assertThat(hashA).isEqualTo(hashB);
		assertThat(hashA).hasSize(64);
	}
}
