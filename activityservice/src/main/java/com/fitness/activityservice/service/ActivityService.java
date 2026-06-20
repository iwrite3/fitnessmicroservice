package com.fitness.activityservice.service;
import org.springframework.beans.factory.annotation.Value;
import com.fitness.activityservice.ActivityRepository;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
//for automatically generate const without
//all args for all the fields req args for the final fields with constraints such nonull
@Slf4j
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.name}")
    private String routingKey;


    public ActivityResponse trackActivity(ActivityRequest request){

        boolean isValidUser = userValidationService.validateUser(request.getUserId());
        if(!isValidUser)
        {
            throw new RuntimeException("Invalid User : " + request.getUserId());
        }

        Activity activity = Activity.builder().userId(request.getUserId()).type(request.getType()).duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build(); log.info(activity.toString());
    Activity savedActivity = activityRepository.save(activity);

    //publish to rabbitmq for AI procewssing
        try{
           rabbitTemplate.convertAndSend(exchange , routingKey , savedActivity);
        }
        catch (Exception e )
        {
            log.error("FAILED TO PUBLISH ACTIVITY TO RABBIT MQ " , e);
        }
    return mapToResponse(savedActivity );
    }
    private ActivityResponse mapToResponse(Activity activity ){
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
    response.setType(activity.getType());
    response.setCaloriesBurned(activity.getCaloriesBurned());
    response.setDuration(activity.getDuration());
    response.setStartTime(activity.getStartTime());
    response.setAdditionalMetrics(activity.getAdditionalMetrics());
    response.setCreatedAt(activity.getCreatedAt());
    response.setUpdatedAt(activity.getUpdatedAt());
    return response;

    }

    public List<ActivityResponse> getUserActivities(String userId) {
   List<Activity> activities =   activityRepository.findByUserId(userId);
             //filter by userid
        //convert to stream map every obj in list to activity response
        return activities.stream()
                .map(this::mapToResponse)
    .collect(Collectors.toList());}

    public ActivityResponse getActivityByIduserId(String activityId) {
        return activityRepository.findById(activityId)
                .map(this::mapToResponse).orElseThrow(() -> new RuntimeException("Activity not found with id:" + activityId));    }
}

