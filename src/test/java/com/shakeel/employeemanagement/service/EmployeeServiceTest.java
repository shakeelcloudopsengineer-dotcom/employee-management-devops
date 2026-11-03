package com.shakeel.employeemanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shakeel.employeemanagement.dto.EmployeeRequest;
import com.shakeel.employeemanagement.dto.EmployeeResponse;
import com.shakeel.employeemanagement.entity.Employee;
import com.shakeel.employeemanagement.entity.EmploymentStatus;
import com.shakeel.employeemanagement.exception.DuplicateEmployeeEmailException;
import com.shakeel.employeemanagement.exception.EmployeeNotFoundException;
import com.shakeel.employeemanagement.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

	@Mock
	private EmployeeRepository employeeRepository;

	@InjectMocks
	private EmployeeService employeeService;

	private EmployeeRequest request;

	@BeforeEach
	void setUp() {
		request = new EmployeeRequest(
				"Ada", "Lovelace", "ada@example.com", "Engineering", "Developer",
				new BigDecimal("95000.00"), "555-0101", LocalDate.of(2022, 1, 10), EmploymentStatus.ACTIVE);
	}

	@Test
	void createEmployeeSavesAndReturnsResponse() {
		when(employeeRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
			Employee employee = invocation.getArgument(0);
			return employee;
		});

		EmployeeResponse result = employeeService.createEmployee(request);

		assertEquals("ada@example.com", result.email());
		assertEquals("Ada", result.firstName());
		verify(employeeRepository).save(any(Employee.class));
	}

	@Test
	void createEmployeeRejectsDuplicateEmail() {
		when(employeeRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

		assertThrows(DuplicateEmployeeEmailException.class, () -> employeeService.createEmployee(request));

		verify(employeeRepository, never()).save(any(Employee.class));
	}

	@Test
	void getEmployeeByIdRejectsMissingEmployee() {
		when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

		assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(42L));
	}

	@Test
	void updateEmployeeAllowsTheExistingEmployeeEmail() {
		Employee employee = new Employee(
				"Ada", "Lovelace", "ada@example.com", "Engineering", "Developer",
				new BigDecimal("95000.00"), "555-0101", LocalDate.of(2022, 1, 10), EmploymentStatus.ACTIVE);
		when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
		when(employeeRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), 1L)).thenReturn(false);
		when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeResponse result = employeeService.updateEmployee(1L, request);

		assertEquals(request.email(), result.email());
		verify(employeeRepository).save(employee);
	}
}
