package org.example.socialmedia_services.controller.search;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.socialmedia_services.dto.search.SearchResponse;
import org.example.socialmedia_services.services.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SearchResponse>> search(
            @RequestParam @NotBlank @Size(min = 1, max = 100) String query,
            @RequestParam(defaultValue = "20") int limit) {

        List<SearchResponse> results = searchService.search(query.trim(), limit);
        return ResponseEntity.ok(results);
    }
}
