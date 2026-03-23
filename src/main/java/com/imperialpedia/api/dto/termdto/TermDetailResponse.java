package com.imperialpedia.api.dto.termdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermDetailResponse {

    private UUID id;
    private String title;
    private String slug;
    private String content;
    private String seoTitle;
    private String seoDescription;
    private String featuredImageUrl;
    private List<String> categoryNames;
    private List<RelatedTermResponse> relatedTerms;

}