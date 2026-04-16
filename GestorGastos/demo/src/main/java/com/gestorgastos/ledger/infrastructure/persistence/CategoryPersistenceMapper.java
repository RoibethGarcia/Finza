package com.gestorgastos.ledger.infrastructure.persistence;

import com.gestorgastos.ledger.domain.model.Category;
import com.gestorgastos.ledger.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryPersistenceMapper {

	public CategoryEntity toEntity(Category category) {
		CategoryEntity entity = new CategoryEntity();
		entity.setId(category.id());
		entity.setUserId(category.userId());
		entity.setName(category.name());
		entity.setType(category.type());
		entity.setArchived(category.archived());
		entity.setCreatedAt(category.createdAt());
		entity.setUpdatedAt(category.updatedAt());
		return entity;
	}

	public Category toDomain(CategoryEntity entity) {
		return new Category(
			entity.getId(),
			entity.getUserId(),
			entity.getName(),
			entity.getType(),
			entity.isArchived(),
			entity.getCreatedAt(),
			entity.getUpdatedAt()
		);
	}
}
