package com.amira.ms.transform;

import com.amira.ms.entity.CustomerData;
import com.amira.ms.model.Customer;

public interface TransformCustomerService {
	// never copies the password: the service hashes it and sets passwordHash
	CustomerData transform(Customer customer);

	Customer transform(CustomerData customerData);
}
