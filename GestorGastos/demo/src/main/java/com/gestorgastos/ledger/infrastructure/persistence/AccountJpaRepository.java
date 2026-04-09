package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.ledger.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

	Optional<AccountEntity> findByIdAndUserId(UUID accountId, UUID userId);

	List<AccountEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
