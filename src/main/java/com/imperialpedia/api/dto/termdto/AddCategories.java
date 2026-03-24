package com.imperialpedia.api.dto.termdto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCategories {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;

}
