package com.imperialpedia.api.dto.termdto;

import com.imperialpedia.api.entity.term.TermStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload used to create or fully update a glossary term")
public class AddTerm {

    @Schema(description = "Human readable term title", example = "Algorithm", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Schema(description = "URL friendly unique identifier (lowercase and hyphen-separated)", example = "algorithm", maxLength = 255, pattern = "^[a-z0-9]+(?:-[a-z0-9]+)*$", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must be at most 255 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase and hyphen-separated")
    private String slug;

    @Schema(description = "Main glossary definition or explanation", example = "A finite sequence of instructions used to solve a class of problems.", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Content is required")
    @Size(max = 255, message = "Content must be at most 255 characters")
    private String content;

    @Schema(description = "SEO title for search engines", example = "Algorithm meaning and examples", maxLength = 255)
    @Size(max = 255, message = "SEO title must be at most 255 characters")
    private String seoTitle;

    @Schema(description = "SEO meta description", example = "Learn what an algorithm is, where it is used, and practical examples.", maxLength = 500)
    @Size(max = 500, message = "SEO description must be at most 500 characters")
    private String seoDescription;

    @Schema(description = "Featured image URL used by clients in cards/pages", example = "https://cdn.example.com/glossary/algorithm.png", maxLength = 500)
    @Size(max = 500, message = "Featured image URL must be at most 500 characters")
    private String featuredImageUrl;

    @Schema(description = "Current workflow status of the term", allowableValues = {"DRAFT", "PUBLISHED", "ARCHIVED"}, defaultValue = "DRAFT", example = "DRAFT")
    private TermStatus status = TermStatus.DRAFT;

    @Schema(description = "Category names assigned to this term (max 20 items, each up to 100 chars)", example = "[\"Computer Science\",\"Programming\"]")
    @Size(max = 20, message = "At most 20 categories are allowed")
    private List<@NotBlank(message = "Category name cannot be blank") @Size(max = 100, message = "Category name must be at most 100 characters") String> categoryNames = new ArrayList<>();

    @Schema(description = "Related term titles for lightweight linking (max 20 items)", example = "[\"Data Structure\",\"Complexity\"]")
    @Size(max = 20, message = "At most 20 related terms are allowed")
    private List<@NotBlank(message = "Related term name cannot be blank") @Size(max = 255, message = "Related term name must be at most 255 characters") String> relatedTerms = new ArrayList<>();
}
