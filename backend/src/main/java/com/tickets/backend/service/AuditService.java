package com.tickets.backend.service;

import com.tickets.backend.model.AuditLog;
import com.tickets.backend.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(String actorEmail, String action, String entityType, UUID entityId, String details) {
        AuditLog log = AuditLog.builder()
            .id(UUID.randomUUID())
            .actorEmail(actorEmail)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .details(details)
            .build();
        repository.save(log);
    }
}
