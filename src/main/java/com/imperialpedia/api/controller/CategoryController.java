package com.imperialpedia.api.controller;

import com.imperialpedia.api.dto.termdto.Categories;
import com.imperialpedia.api.response.ApiResponse;
import com.imperialpedia.api.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(
        name = "Categories",
        description = "Endpoints for managing categories of terms."
)
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(
            tags = {"Categories - Retrieval"},
            summary = "Get categories",
            description = "Returns all categories, or filters by starting letter when `letter` query param is provided.",
            parameters = {
                    @Parameter(name = "letter", in = ParameterIn.QUERY, required = false, description = "Optional single alphabetic character", example = "a")
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid letter filter"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No categories found")
    })
    public ResponseEntity<?> getCategories(@RequestParam(required = false) String letter) {
        if (letter == null || letter.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categoryService.getAllCategories()));
        }

        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categoryService.getCategoriesByLetter(letter)));
    }

    @PostMapping("/create")
    @Operation(
            tags = {"Categories - Creation"},
            summary = "Create a category",
            description = "Creates a new category with normalized name and generated unique slug.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Category payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Categories.class),
                            examples = {
                                    @ExampleObject(name = "Example", value = """
                                            {
                                              "name": "Programming"
                                            }
                                            """)
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category already exists")
    })
    public ResponseEntity<?> createCategory(@Valid @RequestBody Categories request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Category created successfully", categoryService.createCategories(request)));
    }

    @PutMapping("/update/{id}")
    @Operation(
            tags = {"Categories - Update"},
            summary = "Update a category",
            description = "Performs full update for a category by id."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<?> updateCategory(@PathVariable int id, @Valid @RequestBody Categories request) {
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", categoryService.updateCategory(id, request)));
    }

    @PatchMapping("/update/{id}")
    @Operation(
            tags = {"Categories - Update"},
            summary = "Patch a category",
            description = "Partially updates category fields. Currently supported: `name`."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category patched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid patch payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<?> patchCategory(@PathVariable int id, @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(ApiResponse.success("Category patched successfully", categoryService.patchCategory(id, request)));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(
            tags = {"Categories - Deletion"},
            summary = "Delete a category",
            description = "Deletes a category by id if it is not referenced by existing terms."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category is in use")
    })
    public ResponseEntity<?> deleteCategory(@PathVariable int id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(204, "Category deleted successfully", null));
    }
}
