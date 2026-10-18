package com.amira.ms.transform;

import com.amira.ms.entity.StatusData;
import com.amira.ms.model.Status;
import org.springframework.stereotype.Service;

@Service
public class TransformStatusServiceImpl implements TransformStatusService {
	@Override
	public StatusData transform(Status status) {
		StatusData data = new StatusData();
		data.setId(status.getId());
		data.setName(status.getName());
		return data;
	}

	@Override
	public Status transform(StatusData data) {
		Status status = new Status();
		status.setId(data.getId());
		status.setName(data.getName());
		return status;
	}
}
