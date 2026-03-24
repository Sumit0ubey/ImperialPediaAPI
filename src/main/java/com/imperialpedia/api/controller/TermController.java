package com.imperialpedia.api.controller;

import com.imperialpedia.api.dto.termdto.AddTerm;
import com.imperialpedia.api.response.ApiResponse;
import com.imperialpedia.api.service.TermService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.UUID;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
@Tag(
        name = "Glossary",
        description = """
                Includes retrieval, CRUD workflows, and publishing lifecycle operations.
                All responses follow a consistent `ApiResponse` envelope.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class TermController {

    private final TermService service;

    @GetMapping("/letter/{letter}")
    @Operation(
            summary = "Get glossary by letter",
            description = """
                    Returns published terms filtered by initial letter.

                    **Path Parameter**
                    - `letter`: single alphabetic character used as the filter key.

                    **Behavior**
                    - Case-insensitive matching is recommended for clients.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Terms retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid letter filter")
    })
    public ResponseEntity<?> getTermsByLetter(
            @Parameter(description = "Single starting letter", example = "a", required = true)
            @PathVariable String letter
    ) {
        return ResponseEntity.ok(ApiResponse.success("Terms retrieved successfully", service.getTermsByLetter(letter)));
    }

    @GetMapping("/archived")
    @Operation(
            summary = "Get archived glossary",
            description = """
                    Returns archived terms.

                    **Optional Query Parameter**
                    - `letter`: filter archived terms by first character.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Archived terms retrieved successfully")
    })
    public ResponseEntity<?> getArchivedTerms(
            @Parameter(description = "Optional first-letter filter", example = "b")
            @RequestParam(required = false) String letter
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Archived terms retrieved successfully",
                service.getArchivedTerms(letter)
        ));
    }

    @GetMapping("/draft")
    @Operation(
            summary = "Get draft glossary",
            description = """
                    Returns draft terms.

                    **Optional Query Parameter**
                    - `letter`: filter draft terms by first character.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Draft terms retrieved successfully")
    })
    public ResponseEntity<?> getDraftTerms(
            @Parameter(description = "Optional first-letter filter", example = "c")
            @RequestParam(required = false) String letter
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Draft terms retrieved successfully",
                service.getDraftTerms(letter)
        ));
    }

    @GetMapping("/slug/{slug}")
    @Operation(
            summary = "Get term by slug",
            description = "Retrieve a single term using its URL-safe slug identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Term retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Term not found")
    })
    public ResponseEntity<?> getTermDetailsBySlug(
            @Parameter(description = "Unique slug value", example = "algorithm", required = true)
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(ApiResponse.success("Term retrieved successfully", service.getTermDetailBySlug(slug)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get term by ID",
            description = "Retrieve a single term using its UUID identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Term retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Term not found")
    })
    public ResponseEntity<?> getTermById(
            @Parameter(description = "Term UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success("Term retrieved successfully", service.getTermDetailById(id)));
    }

    @PostMapping("/create")
    @Operation(
            summary = "Create a new term",
            description = """
                    Adds a new glossary term.

                    **Required Headers**
                    - `Authorization`: Bearer token (`Bearer <token>`)

                    **Optional Headers**
                    - `X-Request-Id`: request tracing identifier

                    **Required Body Fields**
                    - `title`, `slug`, `content`

                    **Optional Body Fields**
                    - `seoTitle`, `seoDescription`, `featuredImageUrl`, `status`, `categoryNames`, `relatedTerms`

                    **Limits**
                    - `title`, `slug`, `content`: max 255 chars
                    - `seoDescription`, `featuredImageUrl`: max 500 chars
                    - `categoryNames`, `relatedTerms`: max 20 items

                    **Tip**
                    - Prefer unique `slug` values to avoid conflicts and improve URL stability.
                    """,
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer token in format: Bearer <token>", example = "Bearer eyJhbGciOi..."),
                    @Parameter(name = "X-Request-Id", in = ParameterIn.HEADER, description = "Optional trace identifier", example = "fb4d6d77-8f36-4d71-a97d-3fb52f1e2b1a")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Term payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddTerm.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Minimal",
                                            value = """
                                                    {
                                                      "title": "Algorithm",
                                                      "slug": "algorithm",
                                                      "content": "A finite sequence of instructions.",
                                                      "status": "DRAFT"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Complete",
                                            value = """
                                                    {
                                                      "title": "Algorithm",
                                                      "slug": "algorithm",
                                                      "content": "A finite sequence of instructions used to solve problems.",
                                                      "seoTitle": "Algorithm definition",
                                                      "seoDescription": "What algorithm means and where it is used.",
                                                      "featuredImageUrl": "https://cdn.example.com/terms/algorithm.png",
                                                      "status": "PUBLISHED",
                                                      "categoryNames": ["Computer Science", "Programming"],
                                                      "relatedTerms": ["Data Structure", "Complexity"]
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Term created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Term already exists")
    })
    public ResponseEntity<?> createTerm(@Valid @RequestBody AddTerm request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Term created successfully", service.addTerm(request)));
    }

    @PutMapping("/update/{id}")
    @Operation(
            summary = "Update an existing term",
            description = """
                    Performs a full replacement update of an existing term.

                    All required fields from `AddTerm` must be provided.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Term updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Term not found")
    })
    public ResponseEntity<?> updateTerm(
            @Parameter(description = "Term UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody AddTerm request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Term updated successfully", service.updateTerm(id, request)));
    }

    @PatchMapping("/update/{id}")
    @Operation(
            summary = "Partially update a term",
            description = """
                    Partially updates selected fields for an existing term.

                    **Patch Rules**
                    - Supported keys match properties in `AddTerm`.
                    - Send only fields that should change.
                    - Invalid fields or invalid value formats return validation errors.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Term patched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid patch payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Term not found")
    })
    public ResponseEntity<?> patchTerm(
            @Parameter(description = "Term UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Term patched successfully", service.patchTerm(id, request)));
    }

    @PutMapping("/publish/{id}")
    @Operation(
            summary = "Publish a term",
            description = "Promotes a term to `PUBLISHED` so it is visible in public glossary views."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Term published successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Term not found")
    })
    public ResponseEntity<?> publishTerm(
            @Parameter(description = "Term UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success("Term published successfully", service.publishTerm(id)));
    }

    @PutMapping("/draft/{id}")
    @Operation(
            summary = "Move term to draft",
            description = "Moves a term back to `DRAFT` so it is hidden from public glossary views."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Term moved to draft successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Term not found")
    })
    public ResponseEntity<?> draftTerm(
            @Parameter(description = "Term UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success("Term moved to draft successfully", service.makeDraftTerm(id)));
    }

    @PutMapping("/archive/{id}")
    @Operation(
            summary = "Archive a term",
            description = "Moves a term to `ARCHIVED` for historical retention without active display."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Term archived successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Term not found")
    })
    public ResponseEntity<?> archiveTerm(
            @Parameter(description = "Term UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success("Term archived successfully", service.makeArchivedTerm(id)));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "Delete a term",
            description = "Permanently removes a term from the system by UUID."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Term deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Term not found")
    })
    public ResponseEntity<?> deleteTerm(
            @Parameter(description = "Term UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @PathVariable UUID id
    ) {
        service.deleteTerm(id);
        return ResponseEntity.ok(ApiResponse.success(204, "Term deleted successfully"));
    }
}
