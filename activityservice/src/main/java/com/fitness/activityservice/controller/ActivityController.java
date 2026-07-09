package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.service.ActivityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor
@Slf4j
// 1. ADD CROSS ORIGIN: Prevents the microservice from blocking preflight requests 
// that slip through the Gateway
@CrossOrigin(origins = "*") 
public class ActivityController {
    
    private final ActivityService activityService;

    // 2. ADD A PING ENDPOINT: This is our ultimate network test.
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("PING ENDPOINT HIT! Gateway routing is 100% working.");
        return ResponseEntity.ok("Activity Service is alive and reachable!");
    }

    @PostMapping
    public ResponseEntity<?> trackActivity(
            @RequestBody ActivityRequest request, 
            // 3. Make header optional so Spring doesn't auto-reject with a 400/404
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        log.info("===> ENTERED trackActivity POST METHOD! <===");
        log.info("Received UserID Header: {}", userId);
        log.info("Received Request Body: {}", request);

        if(userId == null || userId.isEmpty()) {
            log.error("FAILURE: userId is null. The Gateway dropped the header.");
            return ResponseEntity.badRequest().body("Missing X-User-ID header");
        }

        request.setUserId(userId);

        try {
            ActivityResponse response = activityService.trackActivity(request);
            log.info("Successfully saved activity!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 4. Catch all errors so they don't get swallowed into generic 404s
            log.error("CRASH while saving activity to MongoDB: ", e);
            return ResponseEntity.internalServerError().body("Database Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserActivities(
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        log.info("===> ENTERED getUserActivities GET METHOD! UserID: {}", userId);
        
        if(userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing X-User-ID header");
        }
        
        try {
            return ResponseEntity.ok(activityService.getUserActivities(userId));
        } catch (Exception e) {
            log.error("CRASH while fetching activities: ", e);
            return ResponseEntity.internalServerError().body("Database Error: " + e.getMessage());
        }
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<?> getActivity(@PathVariable String activityId) {
        log.info("Fetching activity ID: {}", activityId);
        try {
            return ResponseEntity.ok(activityService.getActivityByIdUserId(activityId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
