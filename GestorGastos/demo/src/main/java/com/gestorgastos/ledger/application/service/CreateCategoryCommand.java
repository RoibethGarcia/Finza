package com.gestorgastos.ledger.application.service;

import com.gestorgastos.ledger.domain.model.CategoryType;

public record CreateCategoryCommand(String name, CategoryType type) {
}
