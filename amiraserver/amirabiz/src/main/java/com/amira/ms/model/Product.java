package com.amira.ms.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Product {
	int id;
	String name;
	String brand;
	String description;
	String category;
	String imageUrl;
	LocalDate dateAdded;
	List<ProductVariant> variants = new ArrayList<>();
}
