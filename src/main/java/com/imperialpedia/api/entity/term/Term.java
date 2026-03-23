package com.imperialpedia.api.entity.term;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "terms", indexes = {
        @Index(name = "idx_term_slug", columnList = "slug", unique = true),
        @Index(name = "idx_term_letter", columnList = "letter"),
        @Index(name = "idx_term_status", columnList = "status"),
        @Index(name = "idx_term_letter_status", columnList = "letter,status"),
        @Index(name = "idx_term_status_title", columnList = "status,title"),
        @Index(name = "idx_term_letter_status_title", columnList = "letter,status,title")
})
@NoArgsConstructor
@AllArgsConstructor
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "letter", nullable = false, length = 1)
    private String letter;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "featured_image_url", length = 500)
    private String featuredImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TermStatus status = TermStatus.DRAFT;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime creationAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updateAt;

    @ManyToMany
    @JoinTable(
            name = "term_categories",
            joinColumns = @JoinColumn(name = "term_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelatedTerm> relatedTerms = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.creationAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();

        if (this.title != null && !this.title.isBlank()) {
            this.letter = extractLetter(this.title);
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();

        if(this.title != null && !this.title.isBlank()) {
            this.letter = extractLetter(this.title);
        }
    }

    private String extractLetter(String title) {
        String cleaned = title.trim();

        String lower = cleaned.toLowerCase();
        if (lower.startsWith("a ")) {
            cleaned = cleaned.substring(2).trim();
        } else if (lower.startsWith("an ")) {
            cleaned = cleaned.substring(3).trim();
        } else if (lower.startsWith("the ")) {
            cleaned = cleaned.substring(4).trim();
        }

        if (cleaned.isEmpty()) {
            return "#";
        }

        char first = Character.toUpperCase(cleaned.charAt(0));
        return Character.isLetter(first) ? String.valueOf(first): "#";
    }

}
