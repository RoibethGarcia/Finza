package com.gestorgastos.ledger.application.service;

import com.gestorgastos.ledger.domain.model.CategoryType;

public record UpdateCategoryCommand(String name, CategoryType type) {
}
