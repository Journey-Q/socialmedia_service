package org.example.socialmedia_services.controller.stats;

import org.example.socialmedia_services.dto.post.GetStatResponse;
import org.example.socialmedia_services.services.stat.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/{id}")
    public ResponseEntity<GetStatResponse> getUserStats(@PathVariable String id) {
        GetStatResponse response = statsService.getUserStats(id);
        return ResponseEntity.ok(response);
    }
}