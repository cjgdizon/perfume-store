package com.amira.ms.transform;

import com.amira.ms.entity.StatusData;
import com.amira.ms.model.Status;

public interface TransformStatusService {
	StatusData transform(Status status);

	Status transform(StatusData statusData);
}
