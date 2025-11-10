package com.tickets.backend.service;

import com.tickets.backend.model.AuditLog;
import com.tickets.backend.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void logPersistsAuditLog() {
        UUID entityId = UUID.randomUUID();

        auditService.log("user@example.com", "ACTION", "TYPE", entityId, "details");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getActorEmail()).isEqualTo("user@example.com");
        assertThat(saved.getAction()).isEqualTo("ACTION");
        assertThat(saved.getEntityType()).isEqualTo("TYPE");
        assertThat(saved.getEntityId()).isEqualTo(entityId);
        assertThat(saved.getDetails()).isEqualTo("details");
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}

