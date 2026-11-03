package com.shakeel.employeemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shakeel.employeemanagement.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

	List<Employee> findAllByOrderByLastNameAscFirstNameAsc();

	List<Employee> findByDepartmentIgnoreCaseOrderByLastNameAscFirstNameAsc(String department);

	@Query("""
			select employee from Employee employee
			where lower(employee.firstName) like lower(concat('%', concat(:name, '%')))
				or lower(employee.lastName) like lower(concat('%', concat(:name, '%')))
				or lower(concat(employee.firstName, concat(' ', employee.lastName)))
					like lower(concat('%', concat(:name, '%')))
			order by employee.lastName, employee.firstName
			""")
	List<Employee> searchByName(@Param("name") String name);
}
