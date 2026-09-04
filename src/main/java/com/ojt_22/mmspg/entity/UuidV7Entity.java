package com.ojt_22.mmspg.entity;

import java.util.UUID;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

@MappedSuperclass
public abstract class UuidV7Entity {
    protected abstract UUID getId();
    protected abstract void setId(UUID id);

    @PrePersist
    private void assignUuidV7() {
        if (getId() == null) {
            setId(UuidV7.generate());
        }
    }
}
