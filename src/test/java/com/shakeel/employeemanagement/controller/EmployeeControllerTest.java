package com.shakeel.employeemanagement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.shakeel.employeemanagement.config.WebConfig;
import com.shakeel.employeemanagement.dto.EmployeeRequest;
import com.shakeel.employeemanagement.dto.EmployeeResponse;
import com.shakeel.employeemanagement.entity.EmploymentStatus;
import com.shakeel.employeemanagement.exception.GlobalExceptionHandler;
import com.shakeel.employeemanagement.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
@Import({ GlobalExceptionHandler.class, WebConfig.class })
class EmployeeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmployeeService employeeService;

	@Test
	void getEmployeesReturnsJsonList() throws Exception {
		when(employeeService.getAllEmployees()).thenReturn(List.of(employeeResponse()));

		mockMvc.perform(get("/api/employees"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].firstName").value("Ada"))
				.andExpect(jsonPath("$[0].employmentStatus").value("ACTIVE"));
	}

	@Test
	void createEmployeeReturnsCreatedLocation() throws Exception {
		when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(employeeResponse());

		mockMvc.perform(post("/api/employees")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "firstName": "Ada",
						  "lastName": "Lovelace",
						  "email": "ada@example.com",
						  "department": "Engineering",
						  "jobTitle": "Developer",
						  "salary": 95000.00,
						  "phoneNumber": "555-0101",
						  "joiningDate": "2022-01-10",
						  "employmentStatus": "ACTIVE"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/employees/7"))
				.andExpect(jsonPath("$.id").value(7));
	}

	@Test
	void createEmployeeRejectsInvalidRequest() throws Exception {
		mockMvc.perform(post("/api/employees")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "firstName": "",
						  "lastName": "Lovelace",
						  "email": "not-an-email",
						  "department": "Engineering",
						  "jobTitle": "Developer",
						  "salary": -1,
						  "joiningDate": "2022-01-10",
						  "employmentStatus": "ACTIVE"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.firstName").exists())
				.andExpect(jsonPath("$.validationErrors.email").exists())
				.andExpect(jsonPath("$.validationErrors.salary").exists());
	}

	private EmployeeResponse employeeResponse() {
		return new EmployeeResponse(
				7L, "Ada", "Lovelace", "ada@example.com", "Engineering", "Developer",
				new BigDecimal("95000.00"), "555-0101", LocalDate.of(2022, 1, 10), EmploymentStatus.ACTIVE);
	}
}
