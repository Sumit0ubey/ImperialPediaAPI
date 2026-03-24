package com.imperialpedia.api.dto.termdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDetailResponse {
    
    private int id;
    private String name;
    private String slug;
    private List<TermSlug> terms;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TermSlug {
        private String slug;
    }
    
}
