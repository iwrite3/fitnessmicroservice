package com.fitness.activityservice.controller;


import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.service.ActivityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
//any host name will be added to this controller
@RequestMapping("/api/activities")
@AllArgsConstructor
@Slf4j
public class ActivityController {
    private ActivityService activityService;


    @PostMapping
    public ResponseEntity<ActivityResponse> saveActivity(@RequestBody ActivityRequest request) {
        // Logic to save the activity using the userId already inside the JSON request

        log.info("request came till here  path - /api/activities ");
        return ResponseEntity.ok(activityService.trackActivity(request));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@RequestHeader("X-User-ID") String userId) {

        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable String activityId) {

        return ResponseEntity.ok(activityService.getActivityByIduserId(activityId));
    }
}
