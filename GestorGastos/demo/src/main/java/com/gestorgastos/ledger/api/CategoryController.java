package com.gestorgastos.ledger.api;

import com.gestorgastos.ledger.application.service.CategoryManagementService;
import com.gestorgastos.ledger.application.service.CategoryView;
import com.gestorgastos.ledger.application.service.CreateCategoryCommand;
import com.gestorgastos.ledger.application.service.UpdateCategoryCommand;
import com.gestorgastos.ledger.domain.model.CategoryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

	private final CategoryManagementService categoryManagementService;

	public CategoryController(CategoryManagementService categoryManagementService) {
		this.categoryManagementService = categoryManagementService;
	}

	@PostMapping
	public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(categoryManagementService.create(
			new CreateCategoryCommand(request.name(), request.type())
		)));
	}

	@GetMapping
	public List<CategoryResponse> findAll(@RequestParam(defaultValue = "false") boolean includeArchived) {
		return categoryManagementService.findAll(includeArchived).stream()
			.map(this::toResponse)
			.toList();
	}

	@GetMapping("/{categoryId}")
	public CategoryResponse getById(@PathVariable UUID categoryId) {
		return toResponse(categoryManagementService.getById(categoryId));
	}

	@PutMapping("/{categoryId}")
	public CategoryResponse update(@PathVariable UUID categoryId, @Valid @RequestBody UpdateCategoryRequest request) {
		return toResponse(categoryManagementService.update(
			categoryId,
			new UpdateCategoryCommand(request.name(), request.type())
		));
	}

	@DeleteMapping("/{categoryId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void archive(@PathVariable UUID categoryId) {
		categoryManagementService.archive(categoryId);
	}

	private CategoryResponse toResponse(CategoryView categoryView) {
		return new CategoryResponse(
			categoryView.id(),
			categoryView.name(),
			categoryView.type(),
			categoryView.archived(),
			categoryView.createdAt(),
			categoryView.updatedAt()
		);
	}
}

record CreateCategoryRequest(
	@NotBlank @Size(max = 120) String name,
	@NotNull CategoryType type
) {
}

record UpdateCategoryRequest(
	@NotBlank @Size(max = 120) String name,
	@NotNull CategoryType type
) {
}

record CategoryResponse(
	UUID id,
	String name,
	CategoryType type,
	boolean archived,
	Instant createdAt,
	Instant updatedAt
) {
}
