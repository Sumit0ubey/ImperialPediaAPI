package com.imperialpedia.api.interfaces;

import com.imperialpedia.api.dto.termdto.AddTerm;
import com.imperialpedia.api.dto.termdto.TermDetailResponse;
import com.imperialpedia.api.dto.termdto.TermListResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TermServiceInterface {

    List<TermListResponse> getTermsByLetter(String letter);
    List<TermListResponse> getArchivedTerms(String letter);
    List<TermListResponse> getDraftTerms(String letter);
    TermDetailResponse getTermDetailBySlug(String slug);
    TermDetailResponse getTermDetailById(UUID id);
    TermDetailResponse addTerm(AddTerm request);
    TermDetailResponse updateTerm(UUID id, AddTerm request);
    TermDetailResponse patchTerm(UUID id, Map<String, Object> request);
    TermDetailResponse publishTerm(UUID id);
    TermDetailResponse makeArchivedTerm(UUID id);
    TermDetailResponse makeDraftTerm(UUID id);
    void deleteTerm(UUID id);

}
