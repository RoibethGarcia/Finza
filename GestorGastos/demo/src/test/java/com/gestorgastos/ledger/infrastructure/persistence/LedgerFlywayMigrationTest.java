package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LedgerFlywayMigrationTest extends AbstractPostgresIntegrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void shouldCreateAccountsTable() {
		Integer accountsTable = jdbcTemplate.queryForObject(
			"select count(*) from information_schema.tables where table_name = 'accounts'",
			Integer.class
		);

		assertThat(accountsTable).isEqualTo(1);
	}
}
