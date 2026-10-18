package com.amira.ms.controller;

import com.amira.ms.model.Status;
import com.amira.ms.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StatusController {
	@Autowired
	private StatusService statusService;

	@GetMapping("/api/status")
	public List<Status> list() {
		return statusService.getAll();
	}
}
