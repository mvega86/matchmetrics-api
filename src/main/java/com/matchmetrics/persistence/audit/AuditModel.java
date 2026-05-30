package com.matchmetrics.persistence.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditModel {

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "modified_by")
    private String modifiedBy;

    @PrePersist
    public void prePersist() {
        this.modifiedBy = "SYSTEM"; // Aquí puedes cambiarlo dinámicamente
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedBy = "SYSTEM"; // Aquí podrías tomar el usuario actual
    }
}

