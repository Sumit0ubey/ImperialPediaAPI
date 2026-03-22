package com.imperialpedia.api.dto.termdto;

import com.imperialpedia.api.entity.term.TermStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTerm {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must be at most 255 characters")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase and hyphen-separated")
    private String slug;

    @NotBlank(message = "Content is required")
    @Size(max = 255, message = "Content must be at most 255 characters")
    private String content;

    @Size(max = 255, message = "SEO title must be at most 255 characters")
    private String seoTitle;

    @Size(max = 500, message = "SEO description must be at most 500 characters")
    private String seoDescription;

    @Size(max = 500, message = "Featured image URL must be at most 500 characters")
    private String featuredImageUrl;

    private TermStatus status = TermStatus.DRAFT;

    @Size(max = 20, message = "At most 20 categories are allowed")
    private List<@NotBlank(message = "Category name cannot be blank") @Size(max = 100, message = "Category name must be at most 100 characters") String> categoryNames = new ArrayList<>();

    @Size(max = 20, message = "At most 20 related terms are allowed")
    private List<@NotBlank(message = "Related term name cannot be blank") @Size(max = 255, message = "Related term name must be at most 255 characters") String> relatedTerms = new ArrayList<>();

    private List<UUID> relatedTermIds = new ArrayList<>();
}
