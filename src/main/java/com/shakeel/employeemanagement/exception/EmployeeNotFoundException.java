package com.shakeel.employeemanagement.exception;

public class EmployeeNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmployeeNotFoundException(Long id) {
		super("Employee with ID " + id + " was not found");
	}
}
