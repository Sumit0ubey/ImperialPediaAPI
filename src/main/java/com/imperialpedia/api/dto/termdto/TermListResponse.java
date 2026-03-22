package com.imperialpedia.api.dto.termdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermListResponse {

    private UUID id;
    private String slug;

}