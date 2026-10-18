package com.amira.ms.transform;

import com.amira.ms.entity.CustomerData;
import com.amira.ms.model.Customer;
import org.springframework.stereotype.Service;

@Service
public class TransformCustomerServiceImpl implements TransformCustomerService {
	@Override
	public CustomerData transform(Customer customer) {
		CustomerData data = new CustomerData();
		data.setId(customer.getId());
		data.setName(customer.getName());
		data.setEmail(customer.getEmail());
		data.setPhone(customer.getPhone());
		data.setShippingAddress(customer.getShippingAddress());
		data.setRole(customer.getRole());
		return data;
	}

	@Override
	public Customer transform(CustomerData data) {
		Customer customer = new Customer();
		customer.setId(data.getId());
		customer.setName(data.getName());
		customer.setEmail(data.getEmail());
		customer.setPhone(data.getPhone());
		customer.setShippingAddress(data.getShippingAddress());
		customer.setRole(data.getRole());
		return customer;
	}
}
