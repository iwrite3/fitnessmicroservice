package com.fitness.aiservice.service;

import com.fitness.aiservice.controller.repository.RecommendationRepository;
import com.fitness.aiservice.model.Recommendation;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RecommendationService {
    private final RecommendationRepository recommendationRepository;


    public List<Recommendation> getUserRecommendation(String userId) {
     return   recommendationRepository.findByUserId(userId);
    }

    public Recommendation getActivityRecommendation(String activityId) {
        return   recommendationRepository.findByActivityId(activityId).orElseThrow(() -> new RuntimeException("NO RECOMMENDATION FOUND FOR THIS ACTIVITY" + activityId));
    }
}
