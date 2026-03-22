package com.imperialpedia.api.controller;

import com.imperialpedia.api.dto.termdto.AddTerm;
import com.imperialpedia.api.response.ApiResponse;
import com.imperialpedia.api.service.TermService;
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
public class TermController {

    private final TermService service;

    @GetMapping("/{letter}")
    public ResponseEntity<?> getTermsByLetter(@PathVariable String letter) {
        return ResponseEntity.ok(ApiResponse.success("Terms retrieved successfully", service.getTermsByLetter(letter)));
    }

    @GetMapping("/archived")
    public ResponseEntity<?> getArchivedTerms(@RequestParam(required = false) String letter) {
        return ResponseEntity.ok(ApiResponse.success(
                "Archived terms retrieved successfully",
                service.getArchivedTerms(letter)
        ));
    }

    @GetMapping("/draft")
    public ResponseEntity<?> getDraftTerms(@RequestParam(required = false) String letter) {
        return ResponseEntity.ok(ApiResponse.success(
                "Draft terms retrieved successfully",
                service.getDraftTerms(letter)
        ));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getTermDetailsBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success("Term retrieved successfully", service.getTermDetailBySlug(slug)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTermById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Term retrieved successfully", service.getTermDetailById(id)));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTerm(@Valid @RequestBody AddTerm request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Term created successfully", service.addTerm(request)));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTerm(@PathVariable UUID id, @Valid @RequestBody AddTerm request) {
        return ResponseEntity.ok(ApiResponse.success("Term updated successfully", service.updateTerm(id, request)));
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> patchTerm(@PathVariable UUID id, @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(ApiResponse.success("Term patched successfully", service.patchTerm(id, request)));
    }

    @PutMapping("/publish/{id}")
    public ResponseEntity<?> publishTerm(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Term published successfully", service.publishTerm(id)));
    }

    @PutMapping("/draft/{id}")
    public ResponseEntity<?> draftTerm(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Term moved to draft successfully", service.makeDraftTerm(id)));
    }

    @PutMapping("/archive/{id}")
    public ResponseEntity<?> archiveTerm(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Term archived successfully", service.makeArchivedTerm(id)));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTerm(@PathVariable UUID id) {
        service.deleteTerm(id);
        return ResponseEntity.ok(ApiResponse.success(204, "Term deleted successfully"));
    }
}
