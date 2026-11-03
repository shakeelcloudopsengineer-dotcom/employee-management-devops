package com.shakeel.employeemanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shakeel.employeemanagement.dto.EmployeeRequest;
import com.shakeel.employeemanagement.dto.EmployeeResponse;
import com.shakeel.employeemanagement.entity.Employee;
import com.shakeel.employeemanagement.exception.DuplicateEmployeeEmailException;
import com.shakeel.employeemanagement.exception.EmployeeNotFoundException;
import com.shakeel.employeemanagement.repository.EmployeeRepository;

@Service
@Transactional
public class EmployeeService {

	private final EmployeeRepository employeeRepository;

	public EmployeeService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	@Transactional(readOnly = true)
	public List<EmployeeResponse> getAllEmployees() {
		return employeeRepository.findAllByOrderByLastNameAscFirstNameAsc()
				.stream().map(EmployeeService::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public EmployeeResponse getEmployeeById(Long id) {
		return toResponse(findEmployee(id));
	}

	public EmployeeResponse createEmployee(EmployeeRequest request) {
		if (employeeRepository.existsByEmailIgnoreCase(request.email())) {
			throw new DuplicateEmployeeEmailException(request.email());
		}
		Employee employee = new Employee(
				request.firstName().trim(),
				request.lastName().trim(),
				request.email().trim(),
				request.department().trim(),
				request.jobTitle().trim(),
				request.salary(),
				normalizeOptional(request.phoneNumber()),
				request.joiningDate(),
				request.employmentStatus());
		return toResponse(employeeRepository.save(employee));
	}

	public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
		Employee employee = findEmployee(id);
		if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
			throw new DuplicateEmployeeEmailException(request.email());
		}
		employee.setFirstName(request.firstName().trim());
		employee.setLastName(request.lastName().trim());
		employee.setEmail(request.email().trim());
		employee.setDepartment(request.department().trim());
		employee.setJobTitle(request.jobTitle().trim());
		employee.setSalary(request.salary());
		employee.setPhoneNumber(normalizeOptional(request.phoneNumber()));
		employee.setJoiningDate(request.joiningDate());
		employee.setEmploymentStatus(request.employmentStatus());
		return toResponse(employeeRepository.save(employee));
	}

	public void deleteEmployee(Long id) {
		Employee employee = findEmployee(id);
		employeeRepository.delete(employee);
	}

	@Transactional(readOnly = true)
	public List<EmployeeResponse> searchEmployeesByName(String name) {
		if (name == null || name.isBlank()) {
			return getAllEmployees();
		}
		return employeeRepository.searchByName(name.trim())
				.stream().map(EmployeeService::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<EmployeeResponse> getEmployeesByDepartment(String department) {
		return employeeRepository.findByDepartmentIgnoreCaseOrderByLastNameAscFirstNameAsc(department.trim())
				.stream().map(EmployeeService::toResponse).toList();
	}

	private Employee findEmployee(Long id) {
		return employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
	}

	private static String normalizeOptional(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private static EmployeeResponse toResponse(Employee employee) {
		return new EmployeeResponse(
				employee.getId(),
				employee.getFirstName(),
				employee.getLastName(),
				employee.getEmail(),
				employee.getDepartment(),
				employee.getJobTitle(),
				employee.getSalary(),
				employee.getPhoneNumber(),
				employee.getJoiningDate(),
				employee.getEmploymentStatus());
	}
}
