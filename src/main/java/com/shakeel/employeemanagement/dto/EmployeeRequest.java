package com.shakeel.employeemanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.shakeel.employeemanagement.entity.EmploymentStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record EmployeeRequest(
		@NotBlank @Size(max = 80) String firstName,
		@NotBlank @Size(max = 80) String lastName,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(max = 100) String department,
		@NotBlank @Size(max = 120) String jobTitle,
		@NotNull @DecimalMin(value = "0.01") BigDecimal salary,
		@Size(max = 30) String phoneNumber,
		@NotNull @PastOrPresent LocalDate joiningDate,
		@NotNull EmploymentStatus employmentStatus) {
}
