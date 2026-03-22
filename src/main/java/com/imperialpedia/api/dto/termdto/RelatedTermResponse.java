package com.imperialpedia.api.dto.termdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatedTermResponse {

    private UUID id;
    private String slug;
    private String title;
    private String featuredImageUrl;
}

