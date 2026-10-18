package com.amira.ms.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

/** A one-time password reset link. Only a hash of the token is stored, never the token itself. */
@Getter
@Setter
@ToString(exclude = "tokenHash")
@Entity
@Table(name = "PasswordReset_data", uniqueConstraints = @UniqueConstraint(columnNames = "tokenHash"))
public class PasswordResetData extends AuditData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	int customerId;
	String tokenHash;
	LocalDateTime expiresAt;
}
