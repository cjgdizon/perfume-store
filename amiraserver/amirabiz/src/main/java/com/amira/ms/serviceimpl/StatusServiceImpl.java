package com.amira.ms.serviceimpl;

import com.amira.ms.model.Status;
import com.amira.ms.repository.StatusDataRepository;
import com.amira.ms.service.StatusService;
import com.amira.ms.transform.TransformStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional(readOnly = true)
public class StatusServiceImpl implements StatusService {
	@Autowired
	StatusDataRepository statusDataRepository;

	@Autowired
	TransformStatusService transformStatusService;

	@Override
	public List<Status> getAll() {
		return StreamSupport.stream(statusDataRepository.findAll().spliterator(), false)
				.map(transformStatusService::transform).collect(Collectors.toList());
	}
}
