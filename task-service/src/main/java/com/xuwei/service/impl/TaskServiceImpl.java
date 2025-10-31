package com.xuwei.service.impl;

import com.xuwei.dto.TaskRequest;
import com.xuwei.dto.TaskResponse;
import com.xuwei.mapper.TaskMapper;
import com.xuwei.model.Task;
import com.xuwei.repository.TaskRepository;
import com.xuwei.service.TaskService;
import com.xuwei.util.PagedResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;

    public TaskServiceImpl(TaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public TaskResponse create(TaskRequest request) {
        Task entity = TaskMapper.toEntity(request);
        Task saved = repository.save(entity);
        return TaskMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TaskResponse> findById(Long id) {
        return repository.findById(id).map(TaskMapper::toResponse);
    }

    @Override
    public Optional<TaskResponse> update(Long id, TaskRequest request) {
        return repository.findById(id).map(entity -> {
            TaskMapper.updateEntityFromRequest(request, entity);
            Task saved = repository.save(entity);
            return TaskMapper.toResponse(saved);
        });
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<TaskResponse> findAll(Pageable pageable) {
        return PagedResult.of(
                repository.findAll(pageable)
                        .map(TaskMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findAllActive() {
        List<Task> list = repository.findByActiveTrue();
        return list.stream().map(TaskMapper::toResponse).collect(Collectors.toList());
    }
}
