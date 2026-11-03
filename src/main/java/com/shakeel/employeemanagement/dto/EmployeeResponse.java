package com.shakeel.employeemanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.shakeel.employeemanagement.entity.EmploymentStatus;

public record EmployeeResponse(
		Long id,
		String firstName,
		String lastName,
		String email,
		String department,
		String jobTitle,
		BigDecimal salary,
		String phoneNumber,
		LocalDate joiningDate,
		EmploymentStatus employmentStatus) {
}
