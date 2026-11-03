package com.shakeel.employeemanagement.exception;

public class DuplicateEmployeeEmailException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicateEmployeeEmailException(String email) {
		super("An employee with email " + email + " already exists");
	}
}
