package com.xuwei.service;


import com.xuwei.dto.TaskRequest;
import com.xuwei.dto.TaskResponse;
import com.xuwei.util.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    TaskResponse create(TaskRequest request);
    Optional<TaskResponse> update(Long id, TaskRequest request);
    Optional<TaskResponse> findById(Long id);
    void deleteById(Long id);
    PagedResult<TaskResponse> findAll(Pageable pageable);
    List<TaskResponse> findAllActive();
}
