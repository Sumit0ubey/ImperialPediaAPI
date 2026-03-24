package com.imperialpedia.api.controller;


import com.imperialpedia.api.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(
        name = "Categories",
        description = "Endpoints for managing categories of terms."
)
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final ModelMapper modelMapper;
    private final CategoryService categoryService;
}
