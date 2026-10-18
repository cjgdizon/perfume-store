package com.amira.ms.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@Entity
@Table(name = "Product_data")
public class ProductData extends AuditData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	String name;
	String brand;
	@Column(length = 2000)
	String description;
	String category;
	String imageUrl;
	LocalDate dateAdded;
}
