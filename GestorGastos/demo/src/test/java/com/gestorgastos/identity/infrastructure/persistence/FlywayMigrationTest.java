package com.gestorgastos.identity.infrastructure.persistence;

import com.gestorgastos.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void shouldCreateCoreIdentityTables() {
		Integer usersTable = jdbcTemplate.queryForObject(
			"select count(*) from information_schema.tables where table_name = 'users'",
			Integer.class
		);
		Integer refreshTokensTable = jdbcTemplate.queryForObject(
			"select count(*) from information_schema.tables where table_name = 'refresh_tokens'",
			Integer.class
		);

		assertThat(usersTable).isEqualTo(1);
		assertThat(refreshTokensTable).isEqualTo(1);
	}
}
