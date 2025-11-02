package com.xuwei.service;

import com.xuwei.dto.TaskInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class TaskServiceClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TaskServiceClient(RestTemplateBuilder restTemplateBuilder,
                             @Value("${task.service.url:http://task-service:8081}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUrl = baseUrl;
    }

    public TaskInfoResponse getTaskById(Long id) {
        try {
            return restTemplate.getForObject(baseUrl + "/api/tasks/" + id, TaskInfoResponse.class);
        } catch (RestClientException ex) {
            log.warn("task-service call failed", ex);
            return null;
        }
    }
}
