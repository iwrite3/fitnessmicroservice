package com.fitness.activityservice.model;

import org.springframework.data.mongodb.core.mapping.Document;
public enum ActivityType {
    //enum defines types
    RUNNING,
    WALKING,
    CYCLING,
    SWIMMING,
    WEIGHT_TRAINING,
    YOGA,
    HIIT,
    CARDIO,
    STRECHING,
    OTHER
}
