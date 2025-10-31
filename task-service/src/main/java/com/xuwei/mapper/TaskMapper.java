package com.xuwei.mapper;


import com.xuwei.dto.TaskRequest;
import com.xuwei.dto.TaskResponse;
import com.xuwei.model.Task;

public class TaskMapper {

    public static Task toEntity(TaskRequest req) {
        if (req == null) return null;
        Task t = new Task();
        t.setName(req.getName());
        t.setDescription(req.getDescription());
        t.setBaseFee(req.getBaseFee());
        t.setPerKmRate(req.getPerKmRate());
        t.setActive(req.getActive() != null ? req.getActive() : true);
        return t;
    }

    public static TaskResponse toResponse(Task entity) {
        if (entity == null) return null;
        TaskResponse r = new TaskResponse();
        r.setId(entity.getId());
        r.setName(entity.getName());
        r.setDescription(entity.getDescription());
        r.setBaseFee(entity.getBaseFee());
        r.setPerKmRate(entity.getPerKmRate());
        r.setActive(entity.isActive());
        r.setCreatedAt(entity.getCreatedAt());
        r.setUpdatedAt(entity.getUpdatedAt());
        return r;
    }

    public static void updateEntityFromRequest(TaskRequest req, Task entity) {
        if (req == null || entity == null) return;
        entity.setName(req.getName());
        entity.setDescription(req.getDescription());
        entity.setBaseFee(req.getBaseFee());
        entity.setPerKmRate(req.getPerKmRate());
        entity.setActive(req.getActive() != null ? req.getActive() : entity.isActive());
    }
}
