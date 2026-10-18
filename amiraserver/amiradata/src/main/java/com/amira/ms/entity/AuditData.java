package com.amira.ms.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditData {
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	@CreationTimestamp
	private LocalDateTime created;
}
